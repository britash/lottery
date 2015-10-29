package com.monk.lottery.service.dal.query;

import java.util.ArrayList;
import java.util.List;


public abstract class CriteriaOr {
	
	
	private Long id;

	protected String orderByClause;

	protected boolean distinct;

	protected List<CriteriaAnd> queryCriteria;

	protected Integer start;
	protected Integer limit;
	
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	
	public boolean isValid() {
		return queryCriteria.size() > 0;
	}


	public Integer getStart() {
		return start;
	}

	public void setStart(Integer start) {
		this.start = start;
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	public CriteriaOr() {
		queryCriteria = new ArrayList<CriteriaAnd>();
	}

	public void setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
	}

	public String getOrderByClause() {
		return orderByClause;
	}

	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public List<CriteriaAnd> getqueryCriteria() {
		return queryCriteria;
	}

	public void or(CriteriaAnd criteria) {
		queryCriteria.add(criteria);
	}

	public CriteriaAnd or() {
		CriteriaAnd criteria = createCriteriaAndInternal();
		queryCriteria.add(criteria);
		return criteria;
	}

	public CriteriaAnd addCriteriaOr() {
		CriteriaAnd criteria = createCriteriaAndInternal();
		queryCriteria.add(criteria);
		return criteria;
	}
	
	public CriteriaAnd addCriteriaOr(CriteriaAnd criteria) {
		queryCriteria.add(criteria);
		return criteria;
	}

	protected CriteriaAnd createCriteriaAndInternal() {
		CriteriaAnd criteria = new CriteriaAnd();
		return criteria;
	}

	public void clear() {
		queryCriteria.clear();
		orderByClause = null;
		distinct = false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(queryCriteria != null){
			for(CriteriaAnd criterion : queryCriteria){
				sb.append(" or (").append(criterion.toString()).append(")");
			}
		}
		return sb.length() > 0 ? sb.substring(3) : "";
	}
}