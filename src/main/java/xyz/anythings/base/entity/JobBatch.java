package xyz.anythings.base.entity;

import java.util.Date;
import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 작업 배치
 * 
 * @author shortstop
 */
@Table(name = "job_batches", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_job_batches_0", columnList = "domain_id,job_type,job_date,job_seq,status"),
	@Index(name = "ix_job_batches_1", columnList = "domain_id,wms_batch_no"),
	@Index(name = "ix_job_batches_2", columnList = "domain_id,batch_group_id"),
	@Index(name = "ix_job_batches_3", columnList = "domain_id,equip_group_cd,equip_type,equip_cd"),
	@Index(name = "ix_job_batches_4", columnList = "domain_id,area_cd,stage_cd"),
	@Index(name = "ix_job_batches_5", columnList = "domain_id,biz_type")
})
public class JobBatch extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 594615629904242255L;

	/**
	 * I/F 데이터 수신 중 상태 : RECEIVING
	 */
	public static final String STATUS_RECEIVE = "RECEIVING";
	/**
	 * 대기 상태 : WAIT
	 */
	public static final String STATUS_WAIT = "WAIT";
	/**
	 * 작업 지시 가능 상태 : READY
	 */
	public static final String STATUS_READY = "READY";
	/**
	 * 메인 배치에 병합된 상태 : MERGED
	 */
	public static final String STATUS_MERGED = "MERGED";
	/**
	 * 진행 상태 : RUNNING
	 */
	public static final String STATUS_RUNNING = "RUN";
	/**
	 * 완료 상태 : END
	 */
	public static final String STATUS_END = "END";
	/**
	 * 주문 취소 상태 : CANCEL
	 */
	public static final String STATUS_CANCEL = "CANCEL";
	/**
	 * 작업 정지 상태 : PAUSED
	 */
	public static final String STATUS_PAUSED = "PAUSED";
	
	/**
	 * 작업 배치 ID
	 */
	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;
	/**
	 * Wave No.
	 */
	@Column (name = "wms_batch_no", length = 40)
	private String wmsBatchNo;
	/**
	 * Wave로 부터  분리된 경우 WCS의 배치 번호
	 */
	@Column (name = "wcs_batch_no", length = 40)
	private String wcsBatchNo;
	/**
	 * 배치 그룹 ID
	 */
	@Column (name = "batch_group_id", length = 40)
	private String batchGroupId;
	/**
	 * 화주사 코드
	 */
	@Column (name = "com_cd", length = 30)
	private String comCd;
	/**
	 * 작업 유형
	 */
	@Column (name = "job_type", nullable = false, length = 20)
	private String jobType;
	/**
	 * 비지니스 유형 - B2B-IN, B2B-OUT, B2C-IN, B2C-OUT
	 */
	@Column (name = "biz_type", length = 10)
	private String bizType;
	/**
	 * 분류 작업 일자
	 */
	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;
	/**
	 * 작업 차수
	 */
	@Column (name = "job_seq", length = 12)
	private String jobSeq;
	/**
	 * 창고 구역 코드
	 */
	@Column (name = "area_cd", length = 30)
	private String areaCd;
	/**
	 * 창고 스테이지 코드
	 */
	@Column (name = "stage_cd", length = 30)
	private String stageCd;
	/**
	 * 설비 유형
	 */
	@Column (name = "equip_type", length = 20)
	private String equipType;
	/**
	 * 설비 그룹 코드
	 */
	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	/**
	 * 설비 코드
	 */
	@Column (name = "equip_cd", length = 30)
	private String equipCd;
	/**
	 * 설비 명
	 */
	@Column (name = "equip_nm", length = 40)
	private String equipNm;

	/**
	 * 원 Wave의 주문 수
	 */
	@Column (name = "parent_order_qty", length = 12)
	private Integer parentOrderQty;
	/**
	 * 작업 배치 주문 수
	 */
	@Column (name = "batch_order_qty", length = 12)
	private Integer batchOrderQty;
	/**
	 * 작업 배치 주문 처리 수
	 */
	@Column (name = "result_order_qty", length = 12)
	private Integer resultOrderQty;
	/**
	 * 작업 배치 박스 처리 수
	 */
	@Column (name = "result_box_qty", length = 12)
	private Integer resultBoxQty;
	/**
	 * 오 피킹 주문 건수
	 */
	@Column (name = "wrong_picking_qty", length = 12)
	private Integer wrongPickingQty;
	
	/**
	 * 원 Wave의 상품 수
	 */
	@Column (name = "parent_sku_qty", length = 12)
	private Integer parentSkuQty;
	/**
	 * 작업 배치 상품 수
	 */
	@Column (name = "batch_sku_qty", length = 12)
	private Integer batchSkuQty;
	/**
	 * 작업 배치 상품 
	 */
	@Column (name = "result_sku_qty", length = 12)
	private Integer resultSkuQty;
	/**
	 * 원 Wave의 pcs
	 */
	@Column (name = "parent_pcs", length = 12)
	private Integer parentPcs;
	/**
	 * 작업 배치 pcs
	 */
	@Column (name = "batch_pcs", length = 12)
	private Integer batchPcs;
	/**
	 * 작업 배치 처리 pcs
	 */
	@Column (name = "result_pcs", length = 12)
	private Integer resultPcs;
	
	/**
	 * 작업 진행율
	 */
	@Column (name = "progress_rate", length = 19)
	private Float progressRate;
	/**
	 * 투입 작업자 수
	 */
	@Column (name = "input_workers", length = 12)
	private Float inputWorkers;
	/**
	 * 시간당 분류 PCS (Unit Per Hour)
	 */
	@Column (name = "uph", length = 19)
	private Float uph;
	/**
	 * 설비 가동시간
	 */
	@Column (name = "equip_runtime", length = 12)
	private Float equipRuntime;
	/**
	 * 작업지시시간
	 */
	@Column (name = "instructed_at", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date instructedAt;
	/**
	 * 배치완료시간
	 */
	@Column (name = "finished_at", type = xyz.elidom.dbist.annotation.ColumnType.DATETIME)
	private Date finishedAt;

	/**
	 * 배치의 마지막 투입 시퀀스
	 */
	@Column (name = "last_input_seq", length = 12)
	private Integer lastInputSeq;
	/**
	 * 배치 상태
	 */
	@Column (name = "status", length = 10)
	private String status;
	/**
	 * 작업 설정 셋 ID
	 */
	@Column (name = "job_config_set_id", length = 40)
	private String jobConfigSetId;
	/**
	 * 표시기 설정 셋 ID
	 */
	@Column (name = "ind_config_set_id", length = 40)
	private String indConfigSetId;

	@Relation(field = "jobConfigSetId")
	private JobConfigSet jobConfigSet;

	@Relation(field = "indConfigSetId")
	private IndConfigSet indConfigSet;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getWmsBatchNo() {
		return wmsBatchNo;
	}

	public void setWmsBatchNo(String wmsBatchNo) {
		this.wmsBatchNo = wmsBatchNo;
	}

	public String getWcsBatchNo() {
		return wcsBatchNo;
	}

	public void setWcsBatchNo(String wcsBatchNo) {
		this.wcsBatchNo = wcsBatchNo;
	}

	public String getBatchGroupId() {
		return batchGroupId;
	}

	public void setBatchGroupId(String batchGroupId) {
		this.batchGroupId = batchGroupId;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getBizType() {
		return bizType;
	}

	public void setBizType(String bizType) {
		this.bizType = bizType;
	}

	public String getJobDate() {
		return jobDate;
	}

	public void setJobDate(String jobDate) {
		this.jobDate = jobDate;
	}

	public String getJobSeq() {
		return jobSeq;
	}

	public void setJobSeq(String jobSeq) {
		this.jobSeq = jobSeq;
	}

	public String getAreaCd() {
		return areaCd;
	}

	public void setAreaCd(String areaCd) {
		this.areaCd = areaCd;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
	}
	
	public String getEquipType() {
		return equipType;
	}

	public void setEquipType(String equipType) {
		this.equipType = equipType;
	}
	
	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}

	public String getEquipNm() {
		return equipNm;
	}

	public void setEquipNm(String equipNm) {
		this.equipNm = equipNm;
	}

	public Integer getParentOrderQty() {
		return parentOrderQty;
	}

	public void setParentOrderQty(Integer parentOrderQty) {
		this.parentOrderQty = parentOrderQty;
	}

	public Integer getBatchOrderQty() {
		return batchOrderQty;
	}

	public void setBatchOrderQty(Integer batchOrderQty) {
		this.batchOrderQty = batchOrderQty;
	}

	public Integer getResultOrderQty() {
		return resultOrderQty;
	}

	public void setResultOrderQty(Integer resultOrderQty) {
		this.resultOrderQty = resultOrderQty;
	}
	
	public Integer getResultBoxQty() {
		return resultBoxQty;
	}

	public void setResultBoxQty(Integer resultBoxQty) {
		this.resultBoxQty = resultBoxQty;
	}

	public Integer getWrongPickingQty() {
		return wrongPickingQty;
	}

	public void setWrongPickingQty(Integer wrongPickingQty) {
		this.wrongPickingQty = wrongPickingQty;
	}

	public Integer getParentSkuQty() {
		return parentSkuQty;
	}

	public void setParentSkuQty(Integer parentSkuQty) {
		this.parentSkuQty = parentSkuQty;
	}

	public Integer getBatchSkuQty() {
		return batchSkuQty;
	}

	public void setBatchSkuQty(Integer batchSkuQty) {
		this.batchSkuQty = batchSkuQty;
	}

	public Integer getResultSkuQty() {
		return resultSkuQty;
	}

	public void setResultSkuQty(Integer resultSkuQty) {
		this.resultSkuQty = resultSkuQty;
	}

	public Integer getParentPcs() {
		return parentPcs;
	}

	public void setParentPcs(Integer parentPcs) {
		this.parentPcs = parentPcs;
	}

	public Integer getBatchPcs() {
		return batchPcs;
	}

	public void setBatchPcs(Integer batchPcs) {
		this.batchPcs = batchPcs;
	}

	public Integer getResultPcs() {
		return resultPcs;
	}

	public void setResultPcs(Integer resultPcs) {
		this.resultPcs = resultPcs;
	}

	public Float getProgressRate() {
		return progressRate;
	}

	public void setProgressRate(Float progressRate) {
		this.progressRate = progressRate;
	}

	public Float getInputWorkers() {
		return inputWorkers;
	}

	public void setInputWorkers(Float inputWorkers) {
		this.inputWorkers = inputWorkers;
	}

	public Float getUph() {
		return uph;
	}

	public void setUph(Float uph) {
		this.uph = uph;
	}

	public Float getEquipRuntime() {
		return equipRuntime;
	}

	public void setEquipRuntime(Float equipRuntime) {
		this.equipRuntime = equipRuntime;
	}

	public Date getInstructedAt() {
		return instructedAt;
	}

	public void setInstructedAt(Date instructedAt) {
		this.instructedAt = instructedAt;
	}

	public Date getFinishedAt() {
		return finishedAt;
	}

	public void setFinishedAt(Date finishedAt) {
		this.finishedAt = finishedAt;
	}

	public Integer getLastInputSeq() {
		return lastInputSeq;
	}

	public void setLastInputSeq(Integer lastInputSeq) {
		this.lastInputSeq = lastInputSeq;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public String getJobConfigSetId() {
		return this.jobConfigSetId;
	}
	
	public void setJobConfigSetId(String jobConfigSetId) {
		this.jobConfigSetId = jobConfigSetId;
	}
	
	public String getIndConfigSetId() {
		return this.indConfigSetId;
	}
	
	public void setIndConfigSetId(String indConfigSetId) {
		this.indConfigSetId = indConfigSetId;
	}
	
	public JobConfigSet getJobConfigSet() {
		return jobConfigSet;
	}

	public void setJobConfigSet(JobConfigSet jobConfigSet) {
		this.jobConfigSet = jobConfigSet;

		if(this.jobConfigSet != null) {
			String refId = this.jobConfigSet.getId();
			if (refId != null )
				this.jobConfigSetId = refId;
		}
	}

	public IndConfigSet getIndConfigSet() {
		return indConfigSet;
	}

	public void setIndConfigSet(IndConfigSet indConfigSet) {
		this.indConfigSet = indConfigSet;

		if(this.indConfigSet != null) {
			String refId = this.indConfigSet.getId();
			if (refId != null)
				this.indConfigSetId = refId;
		}
	}
		
	/**
	 * 상태 업데이트, 즉시 반영
	 *  
	 * @param status
	 */
	@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public void updateStatusImmediately(String status) {
		this.updateStatus(status);
	}
	
	/**
	 * 상태 업데이트
	 * 
	 * @param status
	 */
	public void updateStatus(String status) {
		this.setStatus(status);
		
		if(ValueUtil.isEqual(JobBatch.STATUS_CANCEL, status)) {
			this.setJobSeq(LogisConstants.ZERO_STRING);
			BeanUtil.get(IQueryManager.class).update(this, "jobSeq", "status");
		} else {
			BeanUtil.get(IQueryManager.class).update(this, "status");
		}
	}

	/**
	 * 현재 잡 시퀀스의 최대 값을 가져온다.
	 * 
	 * @param domainId
	 * @param comCd
	 * @param areaCd
	 * @param stageCd
	 * @param jobDate
	 * @return
	 */
	public static int getMaxJobSeq(Long domainId, String comCd, String areaCd, String stageCd, String jobDate) {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addSelect("jobSeq");
		condition.addFilter("comCd", comCd);
		condition.addFilter("areaCd", areaCd);
		condition.addFilter("stageCd", stageCd);
		condition.addFilter("jobDate", jobDate);
		condition.addOrder("jobSeq", false);
		List<JobBatch> jobSeqList = queryManager.selectList(JobBatch.class, condition);
		String maxJobSeq = (ValueUtil.isEmpty(jobSeqList) ? LogisConstants.ZERO_STRING : jobSeqList.get(0).getJobSeq());
		return ValueUtil.toInteger(maxJobSeq);
	}
	
	/**
	 * JobBatch 데이터 생성
	 * 
	 * @param batchId
	 * @param jobSeq
	 * @param batchReceipt
	 * @param receiptItem
	 * @return
	 */
	//@Transactional(propagation=Propagation.REQUIRES_NEW) 
	public static JobBatch createJobBatch(String batchId, String jobSeq, BatchReceipt batchReceipt, BatchReceiptItem receiptItem) {
		JobBatch batch = new JobBatch();
		batch.setId(batchId);
		batch.setBatchGroupId(batchId);
		batch.setWmsBatchNo(receiptItem.getWmsBatchNo());
		batch.setWcsBatchNo(receiptItem.getWcsBatchNo());
		batch.setComCd(receiptItem.getComCd());
		batch.setJobType(receiptItem.getJobType());
		batch.setJobDate(batchReceipt.getJobDate());
		batch.setJobSeq(jobSeq);
		batch.setAreaCd(receiptItem.getAreaCd());
		batch.setStageCd(receiptItem.getStageCd());
		batch.setEquipType(receiptItem.getEquipType());
		batch.setEquipCd(receiptItem.getEquipCd());
		batch.setEquipNm(LogisConstants.EMPTY_STRING);
		batch.setParentOrderQty(receiptItem.getTotalOrders());
		batch.setParentSkuQty(receiptItem.getTotalSku());
		batch.setParentPcs(receiptItem.getTotalPcs());
		batch.setBatchOrderQty(receiptItem.getTotalOrders());
		batch.setBatchSkuQty(receiptItem.getTotalSku());
		batch.setBatchPcs(receiptItem.getTotalPcs());
		batch.setResultOrderQty(0);
		batch.setResultBoxQty(0);
		batch.setResultSkuQty(0);
		batch.setResultPcs(0);
		batch.setWrongPickingQty(0);
		batch.setLastInputSeq(0);
		batch.setProgressRate(0.0f);
		batch.setUph(0.0f);
		batch.setEquipRuntime(0.0f);
		batch.setStatus(JobBatch.STATUS_RECEIVE);
		BeanUtil.get(IQueryManager.class).insert(batch);
		return batch;
	}

}
