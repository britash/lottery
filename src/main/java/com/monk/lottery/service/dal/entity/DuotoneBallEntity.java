package com.monk.lottery.service.dal.entity;


public class DuotoneBallEntity extends AbstractEntity{
	
	private static final long serialVersionUID = 1L;
	private Integer id;// ID
	private Integer issue;
	private String redBall;
	private String blueBall;
	private Integer totalSell;
	private Integer totalMoney;
	private Integer first;
	private Integer firstMoney;
	private Integer second;
	private Integer secondMoney;
	
	public Integer getTotalSell() {
		return totalSell;
	}

	public void setTotalSell(Integer totalSell) {
		this.totalSell = totalSell;
	}

	public Integer getTotalMoney() {
		return totalMoney;
	}

	public void setTotalMoney(Integer totalMoney) {
		this.totalMoney = totalMoney;
	}

	public Integer getFirst() {
		return first;
	}

	public void setFirst(Integer first) {
		this.first = first;
	}

	public Integer getFirstMoney() {
		return firstMoney;
	}

	public void setFirstMoney(Integer firstMoney) {
		this.firstMoney = firstMoney;
	}

	public Integer getSecond() {
		return second;
	}

	public void setSecond(Integer second) {
		this.second = second;
	}

	public Integer getSecondMoney() {
		return secondMoney;
	}

	public void setSecondMoney(Integer secondMoney) {
		this.secondMoney = secondMoney;
	}

	@Override
	public Integer getId() {
		return id;
	}

	public Integer getIssue() {
		return issue;
	}

	public void setIssue(Integer issue) {
		this.issue = issue;
	}

	public String getRedBall() {
		return redBall;
	}

	public void setRedBall(String redBall) {
		this.redBall = redBall;
	}

	public String getBlueBall() {
		return blueBall;
	}

	public void setBlueBall(String blueBall) {
		this.blueBall = blueBall;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	
}
