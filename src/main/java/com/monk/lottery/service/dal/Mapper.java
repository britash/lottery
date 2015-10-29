package com.monk.lottery.service.dal;

import java.util.List;

import org.apache.ibatis.annotations.Param;

public interface Mapper<T, V> {

	int insert(T entity);

	int update(T entity);

	int delete(Integer id);

	public T get(@Param("id") Integer id);

	public Integer countByExample(@Param("example") V entityExample);

	public List<T> findByExample(@Param("example") V entityExample);
}