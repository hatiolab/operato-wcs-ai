package xyz.anythings.base.event;

/**
 * 작업 관련 Root 이벤트
 * 
 * @author yang
 */
public class EventConstants {
	
	/******************************************************************************
	 *								ID 생성 : IdGenerationEvent
	 ******************************************************************************/
	
	/**
	 * 배치 ID 생성 타입
	 */
	public static final short EVENT_ID_GENERATION_BATCH_ID = 10;
	
	/******************************************************************************
	 *								배치 수신 : BatchReceiveEvent
	 ******************************************************************************/
	
	/**
	 * 배치 수신 서머리 정보 수집 타입
	 */
	public static final short EVENT_RECEIVE_TYPE_RECEIPT = 10;
	
	/**
	 * 배치 수신 타입
	 */
	public static final short EVENT_RECEIVE_TYPE_RECEIVE = 20;
	
	/**
	 * 배치 수신 취소 타입
	 */
	public static final short EVENT_RECEIVE_TYPE_CANCEL = 30;
	
	/******************************************************************************
	 *								배치 가공 : BatchPreprocerssEvent
	 ******************************************************************************/
	
	/**
	 * 주문 가공 액션 - 대상 분류
	 */
	public static final short EVENT_PREPROCESS_TARGET_CLASSING = 5;
	/**
	 * 주문 가공 액션 - 주문 가공 요약 정보 조회
	 */
	public static final short EVENT_PREPROCESS_SUMMARY = 10;
	/**
	 * 주문 가공 액션 - 설비 수동 할당
	 */
	public static final short EVENT_PREPROCESS_EQUIP_MANUAL_ASSIGN = 20;
	/**
	 * 주문 가공 액션 - 설비 자동 할당
	 */
	public static final short EVENT_PREPROCESS_EQUIP_AUTO_ASSIGN = 30;
	/**
	 * 주문 가공 액션 - 셀/슈트 할당
	 */
	public static final short EVENT_PREPROCESS_SUB_EQUIP_ASSIGN = 40;
	/**
	 * 주문 가공 액션 - 주문 가공 완료
	 */
	public static final short EVENT_PREPROCESS_COMPLETE = 50;
	
	/******************************************************************************
	 *								작업 지시 : BatchInstructEvent
	 ******************************************************************************/
	
	/**
	 * 배치 작업 지시
	 */
	public static final short EVENT_INSTRUCT_TYPE_INSTRUCT = 10;
	
	/**
	 * 배치 작업 지시 취소
	 */
	public static final short EVENT_INSTRUCT_TYPE_INSTRUCT_CANCEL = 20;
	
	/**
	 * 배치 작업 병합
	 */
	public static final short EVENT_INSTRUCT_TYPE_MERGE = 30;
	
	/**
	 * 배치 대상 분류
	 */
	public static final short EVENT_INSTRUCT_TYPE_CLASSIFICATION = 40;
	
	/**
	 * 배치 작업 지시 후 박스 요청
	 */
	public static final short EVENT_INSTRUCT_TYPE_BOX_REQ = 50;
	
	/**
	 * 토털 피킹
	 */
	public static final short EVENT_INSTRUCT_TYPE_TOTAL_PICKING = 60;
	
	/**
	 * 추천 로케이션
	 */
	public static final short EVENT_INSTRUCT_TYPE_RECOMMEND_CELLS = 70;
}
