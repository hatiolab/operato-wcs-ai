package xyz.anythings.gw.service.model;

import java.util.List;

import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.util.FormatUtil;

/**
 * 표시기 테스트 모델
 * 
 * @author shortstop
 */
public class IndTest {
	/**
	 * 표시기 타겟 유형 - 호기 : rack
	 */
	public static final String TARGET_TYPE_RACK = "rack";
	/**
	 * 표시기 타겟 유형 - 게이트웨이 : gateway
	 */
	public static final String TARGET_TYPE_GATEWAY = "gateway";
	/**
	 * 표시기 타겟 유형 - 장비 존 : equip_zone
	 */
	public static final String TARGET_TYPE_EQUIP_ZONE = "equip_zone";
	/**
	 * 표시기 타겟 유형 - 작업 존 : station
	 */
	public static final String TARGET_TYPE_STATION = "station";
	/**
	 * 표시기 타겟 유형 - 로케이션 : location
	 */
	public static final String TARGET_TYPE_CELL = "cell";
	/**
	 * 표시기 타겟 유형 - 표시기 : indicator
	 */
	public static final String TARGET_TYPE_INDICATOR = "indicator";
	
	/**
	 * 표시기 설정 셋 ID 
	 */
	private String indConfigSetId;
	
	/**
	 * 설정 프로파일
	 */
	private IndConfigSet indConfigSet;
		
	/**
	 * 작업 유형 
	 */
	private String jobType;
	
	/**
	 * 표시기 점등 타겟
	 */
	private IndTarget target;
	/**
	 * 표시기 액션
	 */
	private IndAction action;
	
	public String getIndConfigSetId() {
		return indConfigSetId;
	}

	public void setIndConfigSetId(String indConfigSetId) {
		this.indConfigSetId = indConfigSetId;
		this.indConfigSet = AnyEntityUtil.findEntityById(true, IndConfigSet.class, indConfigSetId);
	}

	public IndConfigSet getIndConfigSet() {
		return indConfigSet;
	}

	public void setIndConfigSet(IndConfigSet indConfigSet) {
		this.indConfigSet = indConfigSet;
	}

	public IndTarget getTarget() {
		return target;
	}

	public void setTarget(IndTarget target) {
		this.target = target;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public IndAction getAction() {
		return action;
	}

	public void setAction(IndAction action) {
		this.action = action;
	}

	/**
	 * 표시기 Target
	 * 
	 * @author shortstop
	 */
	public class IndTarget {
		/**
		 * 표시기 Target Type
		 */
		String targetType;
		/**
		 * 표시기 Target ID List
		 */
		List<String> targetIdList;
		
		public String getTargetType() {
			return targetType;
		}
		
		public void setTargetType(String targetType) {
			this.targetType = targetType;
		}
		
		public List<String> getTargetIdList() {
			return targetIdList;
		}
		
		public void setTargetIdList(List<String> targetIdList) {
			this.targetIdList = targetIdList;
		}
	}
	
	/**
	 * 표시기 점등/소등 액션
	 * 
	 * @author shortstop
	 */
	public class IndAction {
		/**
		 * 점등 / 소등
		 */
		String action;
		/**
		 * Action Type
		 */
		String actionType;
		/**
		 * 표시기 점등 색깔
		 */
		String btnColor;
		/**
		 * 릴레이 수량
		 */
		String firstQty;
		/**
		 * 박스 수량 
		 */
		String secondQty;
		/**
		 * 강제 소등 여부
		 */
		Boolean forceFlag;
		
		public String getAction() {
			return action;
		}
		
		public void setAction(String action) {
			this.action = action;
		}
		
		public String getActionType() {
			return actionType;
		}
		
		public void setActionType(String actionType) {
			this.actionType = actionType;
		}
		
		public String getBtnColor() {
			return btnColor;
		}
		
		public void setBtnColor(String btnColor) {
			this.btnColor = btnColor;
		}
		
		public String getFirstQty() {
			return firstQty;
		}
		
		public void setFirstQty(String firstQty) {
			this.firstQty = firstQty;
		}
		
		public String getSecondQty() {
			return secondQty;
		}
		
		public void setSecondQty(String secondQty) {
			this.secondQty = secondQty;
		}
		
		public Boolean getForceFlag() {
			return forceFlag;
		}
		
		public void setForceFlag(Boolean forceFlag) {
			this.forceFlag = forceFlag;
		}
	}
	
	/**
	 * @Override
	 */
	public String toString() {
		return FormatUtil.toUnderScoreJsonString(this);
	}
}
