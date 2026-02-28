package xyz.anythings.base.entity;

import java.util.List;

import xyz.elidom.core.entity.Code;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.core.rest.CodeController;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.AbstractStamp;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * box_packs 뷰 용 (읽기 전용)
 * CREATE OR REPLACE VIEW box_packs AS
 * select 
 * 	batch_id || '_' || class_cd || '_' || COALESCE(invoice_id, box_id) as id,
 * 	domain_id, 
 * 	stage_cd, 
 * 	batch_id, 
 * 	job_date, 
 * 	job_seq,
 * 	max(job_type) as job_type,
 * 	com_cd, 
 * 	equip_cd,
 * 	max(sub_equip_cd) as sub_equip_cd,
 * 	order_type, 
 * 	max(cust_order_no) as cust_order_no,
 * 	order_no,
 * 	max(shop_cd) as shop_cd,
 * 	max(shop_nm) as shop_nm,
 * 	invoice_id, 
 * 	max(box_type_cd) as box_type_cd, 
 * 	box_id, 
 * 	class_cd, 
 * 	box_class_cd, 
 * 	max(status) as status,
 * 	max(input_seq) as input_seq,
 * 	max(box_net_wt) as box_net_wt,
 * 	max(box_expect_wt) as box_expect_wt,
 * 	max(box_real_wt) as box_real_wt,
 * 	max(auto_insp_status) as auto_insp_status,
 * 	max(manual_insp_status) as manual_insp_status,
 * 	max(report_status) as report_status,
 * 	max(input_at) as input_at,
 * 	min(pick_started_at) as pick_started_at,
 * 	max(boxed_at) as boxed_at,
 * 	max(auto_inspected_at) as auto_inspected_at,
 * 	max(manual_inspected_at) as manual_inspected_at,
 * 	max(final_out_at) as final_out_at,
 * 	max(reported_at) as reported_at,
 * 	sum(pick_qty) as pick_qty, 
 * 	sum(picked_qty) as picked_qty,
 * 	sum(inspected_qty) as inspected_qty
 * from
 * 	job_instances
 * where
 * 	domain_id = :domainId
 * 	and (status is not null and status in ('I', 'P', 'F', 'B', 'E', 'O'))
 *  and (invoice_id is not null or box_id is not null)
 * group by
 * 	domain_id, stage_cd, batch_id, job_date, job_seq,
 * 	com_cd, equip_cd, order_type, invoice_id, 
 * 	box_id, class_cd, box_class_cd
 * order by
 * 	job_date desc, job_seq desc, input_seq desc
 * 
 * @author shortstop
 */
@Table(name = "box_packs", ignoreDdl = true, idStrategy = GenerationRule.NONE)
public class BoxPack extends AbstractStamp {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 886823091901247512L;

	@PrimaryKey
	@Column (name = "id", length = 100)
	private String id;
	
	@Column(name = OrmConstants.TABLE_FIELD_DOMAIN_ID)
	private Long domainId;

	@Column (name = "batch_id", length = 40)
	private String batchId;
	
	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	@Column (name = "job_date", length = 10)
	private String jobDate;

	@Column (name = "job_seq", length = 10)
	private String jobSeq;
	
//	@Column (name = "biz_type", length = 10)
//	private String bizType;
	
	@Column (name = "job_type", length = 20)
	private String jobType;

	@Column (name = "com_cd", length = 30)
	private String comCd;
	/**
	 * 랙 코드
	 */
	@Column (name = "equip_cd", length = 30)
	private String equipCd;
	/**
	 * 셀 코드
	 */
	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;
	/**
	 * 주문 유형
	 */
	@Column (name = "order_type", length = 20)
	private String orderType;
	
	/**
	 * 원주문 번호
	 */
	@Column (name = "cust_order_no", length = 40)
	private String custOrderNo;
	
	/**
	 * 주문 번호
	 */
	@Column (name = "order_no", length = 40)
	private String orderNo;
	
	/**
	 * 송장 번호
	 */
	@Column (name = "invoice_id", length = 40)
	private String invoiceId;
	
	/**
	 * 거래처 코드
	 */
	@Column (name = "shop_cd", length = 30)
	private String shopCd;

	/**
	 * 거래처 명
	 */
	@Column (name = "shop_nm", length = 40)
	private String shopNm;
	
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
	 * 피킹 예정 수량
	 */
	@Column (name = "pick_qty", length = 10)
	private Integer pickQty;

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
	
	@Ignore
	private String statusStr;
	
	@Ignore
	private List<BoxItem> items;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getDomainId() {
		return domainId;
	}

	public void setDomainId(Long domainId) {
		this.domainId = domainId;
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

//	public String getBizType() {
//		return bizType;
//	}
//
//	public void setBizType(String bizType) {
//		this.bizType = bizType;
//	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
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

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
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

	public Integer getPickQty() {
		return pickQty;
	}

	public void setPickQty(Integer pickQty) {
		this.pickQty = pickQty;
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

	public String getStatusStr() {
		if(this.statusStr == null && this.status != null) {
			Code code = BeanUtil.get(CodeController.class).findOne(SysConstants.SHOW_BY_NAME_METHOD, "JOB_STATUS");
			if(code != null) {
				List<CodeDetail> codeItems = code.getItems();
				for(CodeDetail item : codeItems) {
					if(ValueUtil.isEqualIgnoreCase(item.getName(), this.status)) {
						this.statusStr = item.getDescription();
						break;
					}
				}
			}
		}
		
		return statusStr;
	}

	public void setStatusStr(String statusStr) {
		this.statusStr = statusStr;
	}

	public List<BoxItem> getItems() {
		return items;
	}

	public void setItems(List<BoxItem> items) {
		this.items = items;
	}

	public void searchBoxItems() {
		this.items = BeanUtil.get(IQueryManager.class).selectList(BoxItem.class, ValueUtil.newMap("boxPackId", this.getId()));
	}
}
