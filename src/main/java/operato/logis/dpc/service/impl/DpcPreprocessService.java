package operato.logis.dpc.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.OrderPreprocess;
import xyz.anythings.base.service.api.IPreprocessService;
import xyz.anythings.sys.service.AbstractExecutionService;
import xyz.anythings.sys.service.ICustomService;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

/**
 * DPC 주문 가공 서비스
 * 
 * @author shortstop
 */
@Component("dpcPreprocessService")
public class DpcPreprocessService extends AbstractExecutionService implements IPreprocessService {

	/**
	 * 주문 가공 처리 커스텀 서비스
	 */
	public static final String CUSTOM_DPC_BATCH_PREPROCESS = "diy-dpc-batch-preprocess";
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	protected ICustomService customService;

	@Override
	public List<JobBatch> completePreprocess(JobBatch batch, Object... params) {
		// 1. 작업 배치 상태 체크
		if(ValueUtil.isNotEqual(batch.getStatus(), JobBatch.STATUS_WAIT)) {
			throw ThrowUtil.newValidationErrorWithNoLog(true, "INVALID_STATUS");
		}
		
		// 2. 커스텀 서비스로 주문 가공 처리
		this.customService.doCustomService(batch.getDomainId(), CUSTOM_DPC_BATCH_PREPROCESS, ValueUtil.newMap("batch", batch));
		
		// 3. 작업 배치 상태 업데이트
		batch.setStatus(JobBatch.STATUS_READY);
		this.queryManager.update(batch, "status");
		return ValueUtil.toList(batch);
	}
	
	@Override
	public List<OrderPreprocess> searchPreprocessList(JobBatch batch) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public Map<String, ?> buildPreprocessSet(JobBatch batch, Query query) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public int generatePreprocess(JobBatch batch, Object... params) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public int deletePreprocess(JobBatch batch) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public int assignEquipLevel(JobBatch batch, String equipCds, List<OrderPreprocess> items, boolean automatically) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public int assignSubEquipLevel(JobBatch batch, String equipType, String equipCd, List<OrderPreprocess> items) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public void resetPreprocess(JobBatch batch, boolean isRackReset, List<String> equipCdList) {
		throw ThrowUtil.newNotSupportedMethod();
	}

	@Override
	public void splitBatch(JobBatch batch) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void cancelSplitBatch(JobBatch splittedbatch) {
		// TODO Auto-generated method stub
		
	}
}
