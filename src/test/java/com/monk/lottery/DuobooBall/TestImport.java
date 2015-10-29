package com.monk.lottery.DuobooBall;


import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.monk.lottery.service.DuotoneBallImportSevice;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring-service.xml" })
public class TestImport {
	private static final Logger log = LoggerFactory.getLogger(TestImport.class);

	@Resource
	private DuotoneBallImportSevice service;

	
	@Test
	public void testImport() {
		
		service.batchImport();
	}
	
	//@Test
	public void testDetail() {
		
		service.downloadDetailFromNet(2014044);
	}

}
