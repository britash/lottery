package com.monk.lottery.service.dal.entity;

import org.springframework.util.StringUtils;

import com.monk.lottery.service.dal.query.CriteriaOr;
import com.monk.lottery.service.dal.query.Criterion;

public class DuotoneBallEntityExample extends CriteriaOr {
	
	public Criterion createIssueEquals(Integer issue) {
		if (issue != null) {
			return new Criterion("a.issue =", issue);
		}
		return null;
	}
	
	public Criterion createRedBallLike(String rd) {
		if (rd != null) {
			return new Criterion("a.red_ball like ", "%" + rd + "%");
		}
		return null;
	}
	public Criterion createBlueBallLike(String bd) {
		if (bd != null) {
			return new Criterion("a.blue_ball like ", "%" + bd + "%");
		}
		return null;
	}
	
	public Criterion createRedAndBlueBallLike(String rd,String bd) {
		if (bd != null) {
			return new Criterion("a.red_ball like ", "%" + rd + "%" + " and a.blue_ball like ", "%" + bd + "%");
		}
		return null;
	}

	public void setOrderWithIssue(String order) {
		if (!StringUtils.isEmpty(order)) {
			this.orderByClause = "issue " + order;
		} else {
			this.orderByClause = "issue asc";
		}
	}

}
