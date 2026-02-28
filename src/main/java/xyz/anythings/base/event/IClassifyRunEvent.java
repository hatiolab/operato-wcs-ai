package xyz.anythings.base.event;

import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.entity.WorkCell;

/**
 * 소분류 처리 이벤트
 * 
 * @author shortstop
 */
public interface IClassifyRunEvent extends IClassifyEvent {
	
	/**
	 * 분류 처리 장비 리턴
	 * LogisCodeConstants.CLASSIFICATION_DEVICE_INDICATOR : 표시기 
	 * LogisCodeConstants.CLASSIFICATION_DEVICE_KIOSK : KIOSK
	 * LogisCodeConstants.CLASSIFICATION_DEVICE_PDA : PDA
	 * LogisCodeConstants.CLASSIFICATION_DEVICE_TABLET : Tablet
	 * LogisCodeConstants.CLASSIFICATION_DEVICE_MACHINE : 기타 설비 I/F 
	 * 
	 * @return
	 */
	public String getClassifyDevice();
	
	/**
	 * 분류 처리 장비 설정
	 * 
	 * @param classifyDevice
	 */
	public void setClassifyDevice(String classifyDevice);
	
	/**
	 * 분류 처리 셀 코드 리턴 
	 * 
	 * @return
	 */
	public String getCellCd();
	
	/**
	 * 분류 처리 셀 코드 설정 
	 * 
	 * @param cellCd
	 */
	public void setCellCd(String cellCd);
	
	/**
	 * 작업 셀 리턴
	 * 
	 * @return
	 */
	public WorkCell getWorkCell();
	
	/**
	 * 작업 셀 설정
	 * 
	 * @param workCell
	 */
	public void setWorkCell(WorkCell workCell);
	
	/**
	 * 분류 액션을 리턴 - 확정 처리, 취소 처리, 수량 조절, Fullbox ....
	 * LogisCodeConstants.CLASSIFICATION_ACTION_CONFIRM
	 * LogisCodeConstants.CLASSIFICATION_ACTION_MODIFY
	 * LogisCodeConstants.CLASSIFICATION_ACTION_CANCEL
	 * LogisCodeConstants.CLASSIFICATION_ACTION_FULL
	 * LogisCodeConstants.CLASSIFICATION_ACTION_FULL_MODIFY
	 * 
	 * @return
	 */
	public String getClassifyAction();
	
	/**
	 * 분류 액션을 설정 
	 * 
	 * @param classifyAction
	 */
	public void setClassifyAction(String classifyAction);
	
	/**
	 * 작업 인스턴스
	 * 
	 * @return
	 */
	public JobInstance getJobInstance();

	/**
	 * 작업 인스턴스 설정
	 * 
	 * @param jobInstance
	 */
	public void setJobInstance(JobInstance jobInstance);
		
	/**
	 * 분류 요청 수량 리턴 
	 * 
	 * @return
	 */
	public int getReqQty();
	
	/**
	 * 분류 요청 수량 설정
	 * 
	 * @param reqQty
	 */
	public void setReqQty(int reqQty);
	
	/**
	 * 분류 처리 수량 리턴
	 * 
	 * @return
	 */
	public int getResQty();
	
	/**
	 * 분류 처리 수량 설정
	 * 
	 * @param resQty
	 */
	public void setResQty(int resQty);
	
	/**
	 * 피킹 & 검수 모드 여부
	 * 
	 * @return
	 */
	public boolean isPickWithInspection();

	/**
	 * 피킹 & 검수 모드 여부
	 * 
	 * @param pickWithInspection
	 */
	public void setPickWithInspection(boolean pickWithInspection);

}
