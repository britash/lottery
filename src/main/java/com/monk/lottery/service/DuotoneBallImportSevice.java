package com.monk.lottery.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.monk.lottery.service.dal.entity.DuotoneBallEntity;
import com.monk.lottery.service.dal.entity.DuotoneBallEntityExample;
import com.monk.lottery.service.dal.manager.DuotoneBallManager;
import com.monk.lottery.util.DateUtils;
import com.monk.lottery.util.HttpPooledInvoker;
import com.monk.lottery.util.HttpRequestUtil;


@Service("duotoneBallImportSevice")
public class DuotoneBallImportSevice {
	
	private static final Logger log = LoggerFactory.getLogger(DuotoneBallImportSevice.class);
	
	@Resource
	private DuotoneBallManager<DuotoneBallEntity,DuotoneBallEntityExample> manager;

	private static final String start = "001";
	private static final String end = "500";
	
	private int minYear = 2003;
    //private host = "http://baidu.lecai.com/lottery/draw/list/50?type=range&start=2003001&end=2003800
    private String host = "http://baidu.lecai.com/lottery/draw/list/50?type=range&";
    
    private String netHost  = "http://caipiao.163.com/award/ssq/%d.html";
    HttpPooledInvoker invoker = new HttpPooledInvoker();
    
	public String construstStartIssue() {
		// 从历史中查询最新导入记录的时间
		Integer date = findLatestTransDateFromTargetSource();
		// 出错下，需要重新查询date时间的记录
		if(date != null){
			return (date + 1)+"";
		}
		return minYear + start;
	}

	public Integer findLatestTransDateFromTargetSource() {
		DuotoneBallEntityExample example = new DuotoneBallEntityExample();

		example.setOrderWithIssue("desc");
		example.setLimit(1);

		List<DuotoneBallEntity> list = manager.findByExample(example);
		if (list != null && list.size() > 0) {
			DuotoneBallEntity entity = list.get(0);
			return entity.getIssue();
		}
		return null;
	}

	public  int constructEndDate() {
		return DateUtils.getYear(new Date());
	}

	public void batchImport() {
		Integer latestIssue = findLatestTransDateFromTargetSource();
		int startYear  = minYear;
		int latestYear = DateUtils.getYear(new Date());
		if(latestIssue != null){
			startYear = latestIssue / 1000;
		}
		
		for( ;startYear <= latestYear;startYear++ ){
			String startIssue = "";
			if(latestIssue != null){
				startIssue = String.valueOf(latestIssue);
			}else{
				startIssue = String.valueOf(startYear) + start;
			}
			latestIssue = null;
			String endIssue = String.valueOf(startYear) + end;
			List<DuotoneBallEntity>  list = downloadBaidu(startIssue, endIssue);
			for(int i = list.size() - 1; i>=0;--i){
				saveIfNotExist(list.get(i));
			}
			
		}
	}
	
	public List<DuotoneBallEntity>  downloadBaidu(String startI, String endI){
		StringBuilder param = new StringBuilder();
		param.append(host);
		param.append("start=").append(startI).append("&").append("end=").append(endI);
		
		String ret = HttpRequestUtil.get(param.toString(), null);
		return resolveBaiduHTML(ret);
		
	}
	
	public List<DuotoneBallEntity> resolveBaiduHTML(String html){
		List<DuotoneBallEntity>  list = new ArrayList<DuotoneBallEntity>();
		Document doc = Jsoup.parse(html);
		Elements resultLinks = doc.select("table.historylist > tbody > tr");
		for(int i = 0;i< resultLinks.size();++i){ // tr list
			DuotoneBallEntity dbe = new DuotoneBallEntity();
			Element e  = resultLinks.get(i); //tr
			String issue =e.select("td").eq(0).select("a").html();
			dbe.setIssue(Integer.parseInt(issue));
			
			
			DuotoneBallEntity netdbe = downloadDetailFromNet(dbe.getIssue());
			if(netdbe != null && dbe.getIssue().intValue() == netdbe.getIssue()){
				dbe = netdbe;
			}else{
				String red =e.select("td.balls tr td").eq(0).html();
				String redBall = red.replace("<em>", "").replace("</em>", "");
				if(redBall != null){
					redBall = redBall.trim();
				}
				dbe.setRedBall(redBall);
				
				String blue =e.select("td.balls tr td").eq(1).html();
				String blueBall = blue.replace("<em>", "").replace("</em>", "");
				if(blueBall != null){
					blueBall = blueBall.trim();
				}
				dbe.setBlueBall(blueBall);
			}
			
			list.add(dbe);
		}
		return list;
	}
	
	public List<DuotoneBallEntity> resolveBaiduHTMLAndDetail(String html){
		List<DuotoneBallEntity>  list = new ArrayList<DuotoneBallEntity>();
		Document doc = Jsoup.parse(html);
		Elements resultLinks = doc.select("table.historylist > tbody > tr");
		for(int i = 0;i< resultLinks.size();++i){ // tr list
			Element e  = resultLinks.get(i); //tr
			String issue =e.select("td").eq(0).select("a").html();
			
			DuotoneBallEntity dbe = downloadDetailFromNet(Integer.parseInt(issue));
			dbe.setIssue(Integer.parseInt(issue));
			list.add(dbe);
		}
		return list;
	}
	
	public DuotoneBallEntity  downloadDetailFromNet(Integer issue){
		String net = String.format(netHost, issue);
		//String ret = HttpRequestUtil.get(net, null);
		String ret = invoker.get(net);
		try{
			DuotoneBallEntity bean =  resolveNetDetailHTML(ret);
			return bean;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public DuotoneBallEntity resolveNetDetailHTML(String html){
		DuotoneBallEntity dbe = new DuotoneBallEntity();
		Document doc = Jsoup.parse(html,"utf-8");
		
		String issue = doc.select("span[id=date_no]").html();
		dbe.setIssue(Integer.parseInt(issue));
		
		String totalSell = doc.select("span[id=sale]").html();
		dbe.setTotalSell(Integer.parseInt(totalSell));
		String totalMoney = doc.select("#pool").html();
		dbe.setTotalMoney(Integer.parseInt(totalMoney));
		String redBall = doc.select("#zj_area > .red_ball").html();
		dbe.setRedBall(redBall.replace("\n", ""));
		String blueBall = doc.select("#zj_area > .blue_ball").html();
		dbe.setBlueBall(blueBall.replace("\n", ""));
		String fisrt = doc.select("table[id=bonus] tbody > tr:eq(1) > td:eq(1)").html();
		dbe.setFirst(Integer.parseInt(fisrt));
		String fisrtMoney = doc.select("table[id=bonus] tbody > tr:eq(1) > td:eq(2)").html();
		dbe.setFirstMoney(Integer.parseInt(fisrtMoney));
		
		String second = doc.select("table[id=bonus] tbody > tr:eq(2) > td:eq(1)").html();
		dbe.setSecond(Integer.parseInt(second));
		String secondMoney = doc.select("table[id=bonus] tbody > tr:eq(2) > td:eq(2)").html();
		dbe.setSecondMoney(Integer.parseInt(secondMoney));
		
		return dbe;
	}


	public boolean saveIfNotExist(DuotoneBallEntity transEntity) {
		DuotoneBallEntityExample example = new DuotoneBallEntityExample();
		example.addCriteriaOr().addCriterion(example.createIssueEquals(transEntity.getIssue()));
		example.setOrderWithIssue("desc");
		example.setLimit(1);

		List<DuotoneBallEntity> list = manager.findByExample(example);

		if (list != null && list.size() > 0) {
			return true;
		}
		this.manager.save(transEntity);
		return false;
	}
	


}
