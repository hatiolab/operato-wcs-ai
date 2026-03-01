package operato.logis.dps.service.api;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.service.api.IPickingService;
import xyz.anythings.sys.model.BaseResponse;

/**
 * 피킹 서비스 트랜잭션 API 
 * 
 * 	1. 분류 모듈 정보
 * 		1) 
 * 	2. 투입
 * 		1) 
 * 	3. 소분류 처리
 * 		1) 단포 처리 API 등 추가
 */
public interface IDpsPickingService extends IPickingService {

	/**
	 * 피킹 확정
	 * 
	 * @param batch
	 * @param job
	 * @param resQty
	 * @param pickWithInspection
	 */
	public void confirmPick(JobBatch batch, JobInstance job, int resQty, boolean pickWithInspection);
	
	/**
	 * 박스 투입
	 * 
	 * @param batch
	 * @param isBox
	 * @param boxId
	 * @param boxTypeCd
	 * @param params
	 * @return
	 */
	public Object inputEmptyBucket(JobBatch batch, boolean isBox, String boxId, String boxTypeCd, Object... params);
	
	/**
	 * boxId로 부터 boxType을 추출
	 * 
	 * @param batch
	 * @param boxTypeCd
	 * @return
	 */
	public String getBoxTypeByBoxId(JobBatch batch, String boxTypeCd);
	
	/**
	 * 박스가 스테이션에 도착했을 때 박스의 처리할 주문이 스테이션에 존재하는지 체크
	 * 
	 * @param domainId
	 * @param barcodeIp
	 * @param boxId
	 * @return
	 */
	public boolean checkBoxArrived(Long domainId, String barcodeIp, String boxId);
	
	/**
	 * 박스 도착 보고
	 * 
	 * @param batch
	 * @param equipCd
	 * @param stationCd
	 * @param boxId
	 * @param singleBoxMode
	 * @return
	 */
	public BaseResponse boxArrived(JobBatch batch, String equipCd, String stationCd, String boxId, boolean singleBoxMode);
	
	/**
	 * 박스 출발 처리
	 * 
	 * @param batch
	 * @param stationCd
	 * @param boxId
	 * @return
	 */
	public Object boxLeave(JobBatch batch, String stationCd, String boxId);

}