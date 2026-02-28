package xyz.anythings.base.event.main.model;

import java.util.List;

/**
 * scope setting 의 config 변환 모델 
 * @author yang
 *
 */
public class EventSettingModel {

	/**
	 * 타입 : proc / dsrc
	 */
	private String type;
	
	/**
	 * proc 타입의 경우 호출 프로시저 명 
	 */
	private String procedureName;
	
	/**
	 * if 대상 데이터 소스 명 
	 */
	private String sourceDsrc;
	
	/**
	 * if 대상 테이블 명 
	 */
	private String sourceTable;
	
	/**
	 * if 대상 테이블 컬림 리스트 
	 */
	private List<String> sourceCols;
	
	/**
	 * 수신 테이블 
	 */
	private String rcvTable;
	
	/**
	 * 수신테이블 컬림 리스트 
	 */
	private List<String> rcvCols;
	
	/**
	 * 수신 완료시 대상 테이블에 업데이트 쿼리 ? 
	 */
	private String complateQry;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProcedureName() {
		return procedureName;
	}

	public void setProcedureName(String procedureName) {
		this.procedureName = procedureName;
	}

	public String getSourceDsrc() {
		return sourceDsrc;
	}

	public void setSourceDsrc(String sourceDsrc) {
		this.sourceDsrc = sourceDsrc;
	}

	public String getSourceTable() {
		return sourceTable;
	}

	public void setSourceTable(String sourceTable) {
		this.sourceTable = sourceTable;
	}

	public List<String> getSourceCols() {
		return sourceCols;
	}

	public void setSourceCols(List<String> sourceCols) {
		this.sourceCols = sourceCols;
	}

	public String getRcvTable() {
		return rcvTable;
	}

	public void setRcvTable(String rcvTable) {
		this.rcvTable = rcvTable;
	}

	public List<String> getRcvCols() {
		return rcvCols;
	}

	public void setRcvCols(List<String> rcvCols) {
		this.rcvCols = rcvCols;
	}

	public String getComplateQry() {
		return complateQry;
	}

	public void setComplateQry(String complateQry) {
		this.complateQry = complateQry;
	}
	
}
