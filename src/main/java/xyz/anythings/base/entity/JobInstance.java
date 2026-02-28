package xyz.anythings.base.entity;

import xyz.anythings.base.LogisConstants;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 작업 인스턴스
 * 
 * @author shortstop
 */
@Table(name = "job_instances", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_job_instances_1", columnList = "batch_id,domain_id"),
	@Index(name = "ix_job_instances_2", columnList = "order_no,batch_id"),
	@Index(name = "ix_job_instances_3", columnList = "box_id,invoice_id,batch_id"),
	@Index(name = "ix_job_instances_4", columnList = "status,equip_type,equip_cd,sub_equip_cd,batch_id"),
	@Index(name = "ix_job_instances_5", columnList = "status,job_date,job_seq,batch_id"),
	@Index(name = "ix_job_instances_6", columnList = "input_seq,sub_equip_cd,sku_cd,status,batch_id"),
	@Index(name = "ix_job_instances_7", columnList = "status,box_type_cd,batch_id"),
	@Index(name = "ix_job_instances_8", columnList = "class_cd,box_class_cd,batch_id"),
	@Index(name = "ix_job_instances_9", columnList = "report_status,batch_id"),
	@Index(name = "ix_job_instances_10", columnList = "job_type,biz_type,domain_id")
})
public class JobInstance extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 886823091901247100L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "batch_id", length = 40, nullable = false)
	private String batchId;
	
	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	@Column (name = "job_seq", nullable = false, length = 10)
	private String jobSeq;

	@Column (name = "job_type", nullable = false, length = 20)
	private String jobType;
	
	/**
	 * 비지니스 유형 - B2B-IN, B2B-OUT, B2C-IN, B2C-OUT
	 */
	@Column (name = "biz_type", length = 10)
	private String bizType;

	@Column (name = "com_cd", nullable = false, length = 30)
	private String comCd;

	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	
	@Column (name = "equip_type", length = 20)
	private String equipType;

	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;

	@Column (name = "ind_cd", length = 30)
	private String indCd;
	
	@Column (name = "color_cd", length = 10)
	private String colorCd;

	/**
	 * 주문 유형
	 */
	@Column (name = "order_type", length = 20)
	private String orderType;
	/**
	 * B2B : 매장 코드, B2C : 판매처 코드
	 */
	@Column (name = "shop_cd", length = 30)
	private String shopCd;
	/**
	 * B2B : 매장 명, B2C : 판매처 명
	 */
	@Column (name = "shop_nm", length = 40)
	private String shopNm;
	/**
	 * 주문자 ID
	 */
	@Column (name = "orderer_id", length = 30)
	private String ordererId;
	
	/**
	 * 주문자 명
	 */
	@Column (name = "orderer_nm", length = 30)
	private String ordererNm;
	
	/**
	 * 원주문 번호
	 */
	@Column (name = "cust_order_no", length = 40)
	private String custOrderNo;
	
	/**
	 * 주문 번호
	 */
	@Column (name = "order_no", nullable = false, length = 40)
	private String orderNo;
	
	/**
	 * 송장 번호
	 */
	@Column (name = "invoice_id", length = 40)
	private String invoiceId;
	
	/**
	 * 박스 유형
	 */
	@Column (name = "box_type_cd", length = 30)
	private String boxTypeCd;
	
	/**
	 * 투입 순번
	 */
	@Column (name = "input_seq", length = 10)
	private Integer inputSeq;
	
	/**
	 * 박스 ID
	 */
	@Column (name = "box_id", length = 30)
	private String boxId;
	
	/**
	 * 순수 박스 중량 값
	 */
	@Column (name = "box_net_wt", length = 15)
	private Float boxNetWt;
	
	/**
	 * 박스 계산 중량 값
	 */
	@Column (name = "box_expect_wt", length = 15)
	private Float boxExpectWt;
	
	/**
	 * 박스 측정 중량 값
	 */
	@Column (name = "box_real_wt", length = 15)
	private Float boxRealWt;
	
	/**
	 * 상품 코드
	 */
	@Column (name = "sku_cd", nullable = false, length = 30)
	private String skuCd;
	
	/**
	 * 상품 바코드
	 */
	@Column (name = "sku_barcd", length = 30)
	private String skuBarcd;

	/**
	 * 상품 명
	 */
	@Column (name = "sku_nm", length = 200)
	private String skuNm;
	
	/**
	 * 상품 표준 중량
	 */
	@Column (name = "sku_wt", length = 15)
	private Float skuWt;
	
	/**
	 * 상품 포장 유형
	 */
	@Column (name = "pack_type", length = 20)
	private String packType;

	/**
	 * 상품 박스 입수
	 */
	@Column (name = "box_in_qty", length = 10)
	private Integer boxInQty;
	
	/**
	 * 피킹 예정 수량
	 */
	@Column (name = "pick_qty", length = 10)
	private Integer pickQty;

	/**
	 * 파킹 중 수량
	 */
	@Column (name = "picking_qty", length = 10)
	private Integer pickingQty;

	/**
	 * 피킹 완료 수량
	 */
	@Column (name = "picked_qty", length = 10)
	private Integer pickedQty;
	
	/**
	 * 검수 수량
	 */
	@Column (name = "inspected_qty", length = 10)
	private Integer inspectedQty;

	/**
	 * 소 분류 용
	 */
	@Column (name = "class_cd", length = 40)
	private String classCd;
	
	/**
	 * 방면 분류 용
	 */
	@Column (name = "box_class_cd", length = 40)
	private String boxClassCd;

	/**
	 * 피킹 작업 상태 - 작업 대기 > 투입 > 피킹 시작 > 피킹 완료 > 주문 완료 > 검수 완료 > 출고 완료
	 */
	@Column (name = "status", length = 10)
	private String status;
	
	/**
	 * 중량 검수 상태
	 */
	@Column (name = "auto_insp_status", length = 1)
	private String autoInspStatus;
	
	/**
	 * 수기 검수 상태
	 */
	@Column (name = "manual_insp_status", length = 1)
	private String manualInspStatus;
	
	/**
	 * 실적 전송 상태
	 */
	@Column (name = "report_status", length = 1)
	private String reportStatus;
	
	/**
	 * 부분 취소 여부
	 */
	@Column (name = "cancel_flag", length = 1)
	private Boolean cancelFlag;

	/**
	 * 박스 투입 시각
	 */
	@Column (name = "input_at", length = 22)
	private String inputAt;

	/**
	 * 피킹 시작 시각
	 */
	@Column (name = "pick_started_at", length = 22)
	private String pickStartedAt;

	/**
	 * 피킹 완료 시각
	 */
	@Column (name = "pick_ended_at", length = 22)
	private String pickEndedAt;

	/**
	 * 박싱 완료 시각
	 */
	@Column (name = "boxed_at", length = 22)
	private String boxedAt;
	
	/**
	 * 자동 검수 (예: 중량 검수) 시각
	 */
	@Column (name = "auto_inspected_at", length = 22)
	private String autoInspectedAt;

	/**
	 * 수기 검수 시각
	 */
	@Column (name = "manual_inspected_at", length = 22)
	private String manualInspectedAt;
	
	/**
	 * 최종 출고 시각
	 */
	@Column (name = "final_out_at", length = 22)
	private String finalOutAt;
	
	/**
	 * 실적 전송 시각
	 */
	@Column (name = "reported_at", length = 22)
	private String reportedAt;
	
	/**
	 * 작업 스테이션
	 */
	@Ignore
	private String stationCd;
	
	/**
	 * 게이트웨이 패스
	 */
	@Ignore
	private String gwPath;
	
	/**
	 * 작업 사이드
	 */
	@Ignore
	private String sideCd;
	
	/**
	 * 시리얼
	 */
	@Ignore
	private String serialNo;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getStageCd() {
		return stageCd;
	}

	public void setStageCd(String stageCd) {
		this.stageCd = stageCd;
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

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
	}

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
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

	public String getSubEquipCd() {
		return subEquipCd;
	}

	public void setSubEquipCd(String subEquipCd) {
		this.subEquipCd = subEquipCd;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}

	public String getColorCd() {
		return colorCd;
	}

	public void setColorCd(String colorCd) {
		this.colorCd = colorCd;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}
	
	public String getShopCd() {
		return shopCd;
	}

	public void setShopCd(String shopCd) {
		this.shopCd = shopCd;
	}

	public String getShopNm() {
		return shopNm;
	}

	public void setShopNm(String shopNm) {
		this.shopNm = shopNm;
	}

	public String getOrdererId() {
		return ordererId;
	}

	public void setOrdererId(String ordererId) {
		this.ordererId = ordererId;
	}

	public String getOrdererNm() {
		return ordererNm;
	}

	public void setOrdererNm(String ordererNm) {
		this.ordererNm = ordererNm;
	}

	public String getCustOrderNo() {
		return custOrderNo;
	}

	public void setCustOrderNo(String custOrderNo) {
		this.custOrderNo = custOrderNo;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getInvoiceId() {
		return invoiceId;
	}

	public void setInvoiceId(String invoiceId) {
		this.invoiceId = invoiceId;
	}

	public String getBoxTypeCd() {
		return boxTypeCd;
	}

	public void setBoxTypeCd(String boxTypeCd) {
		this.boxTypeCd = boxTypeCd;
	}

	public Integer getInputSeq() {
		return inputSeq;
	}

	public void setInputSeq(Integer inputSeq) {
		this.inputSeq = inputSeq;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
	}

	public Float getBoxNetWt() {
		return boxNetWt;
	}

	public void setBoxNetWt(Float boxNetWt) {
		this.boxNetWt = boxNetWt;
	}

	public Float getBoxExpectWt() {
		return boxExpectWt;
	}

	public void setBoxExpectWt(Float boxExpectWt) {
		this.boxExpectWt = boxExpectWt;
	}

	public Float getBoxRealWt() {
		return boxRealWt;
	}

	public void setBoxRealWt(Float boxRealWt) {
		this.boxRealWt = boxRealWt;
	}

	public String getSkuCd() {
		return skuCd;
	}

	public void setSkuCd(String skuCd) {
		this.skuCd = skuCd;
	}

	public String getSkuBarcd() {
		return skuBarcd;
	}

	public void setSkuBarcd(String skuBarcd) {
		this.skuBarcd = skuBarcd;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public Float getSkuWt() {
		return skuWt;
	}

	public void setSkuWt(Float skuWt) {
		this.skuWt = skuWt;
	}

	public String getPackType() {
		return packType;
	}

	public void setPackType(String packType) {
		this.packType = packType;
	}

	public Integer getBoxInQty() {
		return boxInQty;
	}

	public void setBoxInQty(Integer boxInQty) {
		this.boxInQty = boxInQty;
	}

	public Integer getPickQty() {
		return pickQty;
	}

	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
	}

	public Integer getPickingQty() {
		return pickingQty;
	}

	public void setPickingQty(Integer pickingQty) {
		this.pickingQty = pickingQty;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public Integer getInspectedQty() {
		return inspectedQty;
	}

	public void setInspectedQty(Integer inspectedQty) {
		this.inspectedQty = inspectedQty;
	}

	public String getClassCd() {
		return classCd;
	}

	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public String getBoxClassCd() {
		return boxClassCd;
	}

	public void setBoxClassCd(String boxClassCd) {
		this.boxClassCd = boxClassCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAutoInspStatus() {
		return autoInspStatus;
	}

	public void setAutoInspStatus(String autoInspStatus) {
		this.autoInspStatus = autoInspStatus;
	}

	public String getManualInspStatus() {
		return manualInspStatus;
	}

	public void setManualInspStatus(String manualInspStatus) {
		this.manualInspStatus = manualInspStatus;
	}

	public String getReportStatus() {
		return reportStatus;
	}

	public void setReportStatus(String reportStatus) {
		this.reportStatus = reportStatus;
	}

	public Boolean getCancelFlag() {
		return cancelFlag;
	}

	public void setCancelFlag(Boolean cancelFlag) {
		this.cancelFlag = cancelFlag;
	}

	public String getInputAt() {
		return inputAt;
	}

	public void setInputAt(String inputAt) {
		this.inputAt = inputAt;
	}

	public String getPickStartedAt() {
		return pickStartedAt;
	}

	public void setPickStartedAt(String pickStartedAt) {
		this.pickStartedAt = pickStartedAt;
	}

	public String getPickEndedAt() {
		return pickEndedAt;
	}

	public void setPickEndedAt(String pickEndedAt) {
		this.pickEndedAt = pickEndedAt;
	}

	public String getBoxedAt() {
		return boxedAt;
	}

	public void setBoxedAt(String boxedAt) {
		this.boxedAt = boxedAt;
	}

	public String getAutoInspectedAt() {
		return autoInspectedAt;
	}

	public void setAutoInspectedAt(String autoInspectedAt) {
		this.autoInspectedAt = autoInspectedAt;
	}

	public String getManualInspectedAt() {
		return manualInspectedAt;
	}

	public void setManualInspectedAt(String manualInspectedAt) {
		this.manualInspectedAt = manualInspectedAt;
	}

	public String getFinalOutAt() {
		return finalOutAt;
	}

	public void setFinalOutAt(String finalOutAt) {
		this.finalOutAt = finalOutAt;
	}

	public String getReportedAt() {
		return reportedAt;
	}

	public void setReportedAt(String reportedAt) {
		this.reportedAt = reportedAt;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

	public String getGwPath() {
		return gwPath;
	}

	public void setGwPath(String gwPath) {
		this.gwPath = gwPath;
	}

	public String getSideCd() {
		return sideCd;
	}

	public void setSideCd(String sideCd) {
		this.sideCd = sideCd;
	}
	
	public String getSerialNo() {
		return serialNo;
	}

	public void setSerialNo(String serialNo) {
		this.serialNo = serialNo;
	}

	/**
	 * 아직 완료되지 않은 작업인지 체크
	 * 
	 * @return
	 */
	public boolean isTodoJob() {
		return (ValueUtil.isEmpty(this.status) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_CANCEL) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_PICKING) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_WAIT) || ValueUtil.isEqual(this.status, LogisConstants.JOB_STATUS_INPUT));
	}
	
	/**
	 * 이미 완료된 작업인지 체크
	 * 
	 * @return
	 */
	public boolean isDoneJob() {
		return !this.isTodoJob();
	}

}
