package com.monk.lottery.service.dal.manager;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.monk.lottery.service.dal.DuotoneBallMapper;
import com.monk.lottery.service.dal.entity.AbstractEntity;

@Component
public class DuotoneBallManager<T extends AbstractEntity,V> extends AbstractManager<T, V>{
	
	private static final Logger log = LoggerFactory.getLogger(DuotoneBallManager.class);

	@Autowired
	private DuotoneBallMapper<T,V> mapper;
	
	@PostConstruct
	public void init(){
		setMapper(mapper);
	}
	
}
