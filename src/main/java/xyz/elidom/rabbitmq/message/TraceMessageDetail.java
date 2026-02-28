package xyz.elidom.rabbitmq.message;

import java.util.List;

import xyz.elidom.util.ValueUtil;

public class TraceMessageDetail {
	private String action;
	
	/**
	 * GW / MPI / 
	 */
	private String id;
	
	/**
	 * mpi 부팅 리스트 
	 */
	private List<DetailArray> indList;
	/**
	 * mpi 점등시 id list 
	 */
	private List<DetailArray> indOn;
	
	/**
	 * mpi 소등 id list
	 */
	private List<String> indOff;
	
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public List<DetailArray> getIndOn() {
		return indOn;
	}
	public void setIndOn(List<DetailArray> indOn) {
		this.indOn = indOn;
	}


	public List<DetailArray> getIndList() {
		return indList;
	}
	public void setIndList(List<DetailArray> indList) {
		this.indList = indList;
	}
	public void setIndOff(List<String> indOff) {
		this.indOff = indOff;
	}
	public List<String> getIndOff() {
		return indOff;
	}
	
	
	public String getEquipId() {
		String equipId = "";
		
		if(ValueUtil.isNotEmpty(this.indList)) {
			// GW Boot 
//			equipId = this.detailArrayToString(this.indList);
			if(this.indList.size() == 1) {
				equipId = this.indList.get(0).getId();
			} else {
				equipId = "MULTI";
			}
		} else if (ValueUtil.isNotEmpty(this.indOn)) {
			// MPI on 
//			equipId = this.detailArrayToString(this.indOn);
			if(this.indOn.size() == 1) {
				equipId = this.indOn.get(0).getId();
			} else {
				equipId = "MULTI";
			}
		} else if (ValueUtil.isNotEmpty(this.indOff)) {
			// mpi off
			if(this.indOff.size() == 1) {
				equipId = this.indOff.get(0);
			} else {
				equipId = "MULTI";
			}
			
		} else {
			// id 
			equipId = this.id;
		}
		
		return equipId;
	}
	
	/*
	private String detailArrayToString(List<DetailArray> details) {
		StringJoiner retStr = new StringJoiner("/");
		for(DetailArray obj : details) retStr.add(obj.getId());
		return retStr.toString();
	}
	*/
	
	class DetailArray {
		/**
		 * MPI / 
		 */
		private String id;
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}
}
