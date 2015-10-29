package com.monk.lottery.service.dal.manager;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.monk.lottery.service.dal.Mapper;
import com.monk.lottery.service.dal.entity.AbstractEntity;

public abstract class AbstractManager<T extends AbstractEntity,V>{
	
	private static final Logger log = LoggerFactory.getLogger(AbstractManager.class);

	private Mapper<T,V> mapper;
	
	public void setMapper(Mapper<T, V> mapper) {
		this.mapper = mapper;
	}
	
	public T get(Integer id) {
		log.debug("Enter (id[{}])", id);
		T entity = mapper.get(id);
		log.debug("Exit ({})", entity);
		return entity;
	}
	
	@Transactional
	public int delete(Integer id) {
		log.debug("Enter (id[{}])", id);
		int num = mapper.delete(id);
		log.debug("Exit ({})", num);
		return num;
	}
	
	@Transactional
	public int save(T entity) {
		log.debug("Enter ([{}])", entity.toString());
		int num = mapper.insert(entity);
		log.debug("Exit({})",  num);
		return num;
	}
	
	@Transactional
	public int update(T entity) {
		log.debug("Enter ([{}])", entity.toString());
		int num = mapper.update(entity);
		log.debug("Exit ({})",  num);
		return num;
	}
	
	public List<T> findByExample(V v){
		log.debug("Enter ([{}])", v.toString());
		List<T> list = mapper.findByExample(v);
		list = (list == null ? new ArrayList<T>(0) : list);
		log.debug("Exit(list.size {},{})",list.size(),v.toString());
		return list;
	}
	
	public Integer countByExample(V v){
		log.debug("Enter ([{}])", v.toString());
		Integer cnt = mapper.countByExample(v);
		cnt = (cnt == null ? 0 : cnt);
		log.debug("Exit(cnt {},v{})", cnt,v.toString());
		return cnt;
	}
}
