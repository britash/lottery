package com.monk.lottery.service.dal.query;

import java.util.ArrayList;
import java.util.List;


public class CriteriaAnd {

		protected List<Criterion> criteria;

		protected CriteriaAnd() {
			criteria = new ArrayList<Criterion>();
		}

		public boolean isValid() {
			return criteria.size() > 0;
		}

		public List<Criterion> getAllCriteria() {
			return criteria;
		}

		public List<Criterion> getCriteria() {
			return criteria;
		}

		public CriteriaAnd addCriterion(String condition) {
			if (condition != null) {
				criteria.add(new Criterion(condition));
			}
			return this;
		}
		
		public CriteriaAnd addCriterion(Criterion condition) {
			if (condition != null) {
				criteria.add(condition);
			}
			return this;
		}

		protected CriteriaAnd addCriterion(String condition, Object value, String property) {
			if (value != null) {
				criteria.add(new Criterion(condition, value));
			}
			return this;
		}

		protected CriteriaAnd addCriterion(String condition, Object value1, Object value2, String property) {
			if (value1 != null && value2 != null) {
				criteria.add(new Criterion(condition, value1, value2));
			}
			return this;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if(criteria != null){
				for(Criterion criterion : criteria){
					sb.append(" and (").append(criterion.toString()).append(")");
				}
			}
			return sb.length() > 0 ? sb.substring(4) : "";
		}
		
		
}