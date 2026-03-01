package operato.logis.insp.service.api;

import java.util.List;

import operato.logis.insp.model.OutInspection;
import xyz.anythings.base.entity.BoxItem;
import xyz.anythings.base.entity.BoxPack;
import xyz.anythings.base.entity.JobBatch;

/**
 * 출고 검수용 서비스
 * 
 * @author shortstop
 */
public interface IOutInspectionService {
	
	/**
	 * 송장 번호, 주문 번호, 박스 ID, 분류 코드 등으로 박스 정보 조회
	 * 
	 * @param batch
	 * @param invoiceId
	 * @param orderNo
	 * @param boxId
	 * @param classCd
	 * @param boxType
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public OutInspection findInspection(JobBatch batch, String invoiceId, String orderNo, String boxId, String classCd, String boxType, boolean reprintMode, boolean exceptionWhenEmpty);

	/**
	 * 투입 박스 유형 (주문 번호, 송장 번호, 박스 ID, 버킷 ID)에 따라 검수 항목 조회
	 * 
	 * @param batch
	 * @param inputType - 주문 번호, 송장 번호, 박스 ID, 버킷 ID
	 * @param inputId - ID
	 * @param reprintMode
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public OutInspection findInspectionByInput(JobBatch batch, String inputType, String inputId, boolean reprintMode, boolean exceptionWhenEmpty);
	
	/**
	 * 트레이 코드로 검수 항목 조회
	 * 
	 * @param batch
	 * @param trayCd
	 * @param reprintMode
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public OutInspection findInspectionByTrayCd(JobBatch batch, String trayCd, boolean reprintMode, boolean exceptionWhenEmpty);
	
	/**
	 * 박스 ID로 검수 항목 조회
	 * 
	 * @param batch
	 * @param boxId
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public OutInspection findInspectionByBoxId(JobBatch batch, String boxId, boolean exceptionWhenEmpty);
	
	/**
	 * 송장 번호로 검수 항목 조회
	 * 
	 * @param batch
	 * @param invoiceId
	 * @param boxType
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public OutInspection findInspectionByInvoiceId(JobBatch batch, String invoiceId, String boxType, boolean exceptionWhenEmpty);
	
	/**
	 * 주문 번호로 검수 항목 조회
	 * 
	 * @param batch
	 * @param orderNo
	 * @param boxType
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public OutInspection findInspectionByOrderNo(JobBatch batch, String orderNo, String boxType, boolean exceptionWhenEmpty);
	
	/**
	 * 분류 코드로 검수 항목 조회
	 * 
	 * @param batch
	 * @param classCd
	 * @param boxType
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public OutInspection findInspectionByClassCd(JobBatch batch, String classCd, String boxType, boolean exceptionWhenEmpty);
	
	/**
	 * 분류 코드로 박스 리스트 조회
	 * 
	 * @param batch
	 * @param classCd
	 * @param boxType
	 * @param exceptionWhenEmpty
	 * @return
	 */
	public List<OutInspection> searchInspectionList(JobBatch batch, String classCd, String boxType, boolean exceptionWhenEmpty);

	/**
	 * 박스 실적 정보로 검수 완료
	 * 
	 * @param batch
	 * @param inspection
	 * @param printerId
	 * @param params 기타 파라미터 ...
	 */
	public void finishInspection(JobBatch batch, OutInspection inspection, String printerId, Object ... params);
	
	/**
	 * 박스 분할
	 * 
	 * @param batch
	 * @param inspection
	 * @param inspectionItems
	 * @param printerId
	 * @param params
	 * @return 분할된 박스
	 */
	public BoxPack splitBox(JobBatch batch, OutInspection inspection, List<BoxItem> inspectionItems, String printerId, Object ... params);
		
	/**
	 * 박스 송장 라벨 발행
	 * 
	 * @param batch
	 * @param inspection
	 * @param printerId
	 * @param params
	 * @return 출력 매수
	 */
	public int printInvoiceLabel(JobBatch batch, OutInspection inspection, String printerId, Object ... params);
	
	/**
	 * 거래명세서 출력
	 * 
	 * @param batch
	 * @param inspection
	 * @param printerId
	 * @param params
	 * @return 출력 매수
	 */
	public int printTradeStatement(JobBatch batch, OutInspection inspection, String printerId, Object ... params);

}
