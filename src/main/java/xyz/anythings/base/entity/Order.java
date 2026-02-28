package xyz.anythings.base.entity;

import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;

/**
 * 입/출고 주문 정보
 * 
 * @author shortstop
 */
@Table(name = "orders", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_orders_0", columnList = "domain_id,batch_id,order_no"),
	@Index(name = "ix_orders_1", columnList = "domain_id,batch_id,wms_batch_no,wcs_batch_no"),
	@Index(name = "ix_orders_2", columnList = "domain_id,job_date,job_seq,area_cd,stage_cd,equip_type,equip_cd,sub_equip_cd,status"),
	@Index(name = "ix_orders_3", columnList = "domain_id,batch_id,box_id,invoice_id,box_type_cd"),
	@Index(name = "ix_orders_4", columnList = "domain_id,batch_id,class_cd,box_class_cd,sku_cd,shop_cd"),
	@Index(name = "ix_orders_5", columnList = "domain_id,biz_type,job_type,job_date"),
	@Index(name = "ix_orders_6", columnList = "domain_id,batch_id"),
	@Index(name = "ix_orders_7", columnList = "domain_id,wms_batch_no")
})
public class Order extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 166602676775758234L;
	
	/**
	 * 상태 E : 주문을 처리할 수 없는 에러 상태
	 */
	public static final String STATUS_ERROR = "E";
	
	/**
	 * 상태 W : 대기
	 */
	public static final String STATUS_WAIT = "W";
	
	/**
	 * 상태 C : 수신 취소
	 */
	public static final String STATUS_CANCEL = "C";
	
	/**
	 * 상태 T : 대상 분류
	 */
	public static final String STATUS_TYPE = "T";
	
	/**
	 * 상태 I : 작업 (박스) 투입
	 */
	public static final String STATUS_INPUT = "I";
	
	/**
	 * 상태 A : 작업 할당
	 */
	public static final String STATUS_ASSIGN = "A";
	
	/**
	 * 상태 R : 진행 중
	 */
	public static final String STATUS_RUNNING = "R";
	
	/**
	 * 상태 F : 완료
	 */
	public static final String STATUS_FINISHED = "F";

	/**
	 * ID
	 */
	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	/**
	 * 배치 ID
	 */
	@Column (name = "batch_id", nullable = false, length = 40)
	private String batchId;

	/**
	 * WMS 배치 ID
	 */
	@Column (name = "wms_batch_no", length = 40)
	private String wmsBatchNo;

	/**
	 * WCS 배치 ID
	 */
	@Column (name = "wcs_batch_no", length = 40)
	private String wcsBatchNo;

	/**
	 * 작업 일자
	 */
	@Column (name = "job_date", nullable = false, length = 10)
	private String jobDate;

	/**
	 * 작업 차수
	 */
	@Column (name = "job_seq", length = 10)
	private String jobSeq;

	/**
	 * 작업 유형 - DAS / DPS / P-DAS ...
	 */
	@Column (name = "job_type", length = 20)
	private String jobType;
	
	/**
	 * 비지니스 유형 - B2B-IN, B2B-OUT, B2C-IN, B2C-OUT
	 */
	@Column (name = "biz_type", length = 10)
	private String bizType;
	
	/**
	 * 입/출고 유형 - IN / OUT
	 */
	@Column (name = "inout_mode", length = 5)
	private String inoutMode;
	
	/**
	 * 출고 - 주문 일자, 입고 - 입고 예정일
	 */
	@Column (name = "order_date", length = 10)
	private String orderDate;
	
	/**
	 * 주문 유형 - 작업 유형에 따라 다름
	 * ex) DPS : 단포 / 합포 ...
	 * ex) DAS : 출고 / 반품 ...
	 * ex) 기타 ...
	 */
	@Column (name = "order_type", length = 20)
	private String orderType;

	/**
	 * 주문 번호
	 */
	@Column (name = "order_no", nullable = false, length = 40)
	private String orderNo;

	/**
	 * 주문 라인 번호
	 */
	@Column (name = "order_line_no", length = 40)
	private String orderLineNo;

	/**
	 * 주문 상세 ID - 주문 번호 + 주문 라인 번호
	 */
	@Column (name = "order_detail_id", length = 40)
	private String orderDetailId;

	/**
	 * 원주문 번호
	 */
	@Column (name = "cust_order_no", length = 40)
	private String custOrderNo;

	/**
	 * 원주문 라인 번호
	 */
	@Column (name = "cust_order_line_no", length = 40)
	private String custOrderLineNo;
	
	/**
	 * 박스 ID
	 */
	@Column (name = "box_id", length = 40)
	private String boxId;

	/**
	 * 송장 번호
	 */
	@Column (name = "invoice_id", length = 40)
	private String invoiceId;
	
	/**
	 * 주문 처리에 사용할 박스 유형
	 */
	@Column (name = "box_type_cd", length = 30)
	private String boxTypeCd;

	/**
	 * 화주사
	 */
	@Column (name = "com_cd", length = 30)
	private String comCd;

	/**
	 * 구역 코드
	 */
	@Column (name = "area_cd", length = 30)
	private String areaCd;

	/**
	 * 주문을 작업할 스테이지
	 */
	@Column (name = "stage_cd", length = 30)
	private String stageCd;

	/**
	 * 주문을 처리할 설비 유형
	 */
	@Column (name = "equip_type", length = 30)
	private String equipType;

	/**
	 * 주문을 처리할 설비 그룹
	 */
	@Column (name = "equip_group_cd", length = 30)
	private String equipGroupCd;
	
	/**
	 * 주문을 처리할 설비 코드
	 */
	@Column (name = "equip_cd", length = 30)
	private String equipCd;

	/**
	 * 주문을 처리할 설비 이름
	 */
	@Column (name = "equip_nm", length = 40)
	private String equipNm;

	/**
	 * 주문을 처리할 설비 서브 코드
	 */
	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;

	/**
	 * 주문자 코드
	 * B2B : 매장 코드 / B2C : 수령인
	 */
	@Column (name = "orderer_id", length = 30)
	private String ordererId;

	/**
	 * 주문자 명
	 * B2B : 매장 명 / B2C : 주문자
	 */
	@Column (name = "orderer_nm", length = 40)
	private String ordererNm;
	
	/**
	 * 판매처 코드
	 */
	@Column (name = "shop_cd", length = 30)
	private String shopCd;

	/**
	 * 판매처 명
	 */
	@Column (name = "shop_nm", length = 40)
	private String shopNm;
	
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
	 * 상품 바코드 2
	 */
	@Column (name = "sku_barcd2", length = 30)
	private String skuBarcd2;

	/**
	 * 상품명
	 */
	@Column (name = "sku_nm", length = 200)
	private String skuNm;

	/**
	 * 박스 입수
	 */
	@Column (name = "box_in_qty", length = 12)
	private Integer boxInQty;
	
	/**
	 * 상품별 (혹은 박스별) 포장 유형
	 */
	@Column (name = "pack_type", length = 20)
	private String packType;

	/**
	 * 주문 수량 (출고 - 출고 수량 / 입고 - 입고 수량)
	 */
	@Column (name = "order_qty", nullable = false, length = 12)
	private Integer orderQty;
	
	/**
	 * 작업 할당 수량
	 */
	@Column (name = "assign_qty", length = 12)
	private Integer assignQty;

	/**
	 * 주문 처리 실적 수량
	 */
	@Column (name = "picked_qty", length = 12)
	private Integer pickedQty;

	/**
	 * 박스 처리 수량
	 */
	@Column (name = "boxed_qty", length = 12)
	private Integer boxedQty;

	/**
	 * 취소 수량
	 */
	@Column (name = "cancel_qty", length = 12)
	private Integer cancelQty;

	/**
	 * 소분류 용 구분 코드
	 */
	@Column (name = "class_cd", length = 40)
	private String classCd;
	
	/**
	 * 방면 분류 용
	 */
	@Column (name = "box_class_cd", length = 40)
	private String boxClassCd;

	/**
	 * 제품 LOT 번호
	 */
	@Column (name = "lot_no", length = 40)
	private String lotNo;

	/**
	 * From Zone Cd
	 */
	@Column (name = "from_zone_cd", length = 30)
	private String fromZoneCd;

	/**
	 * From Cell Cd
	 */
	@Column (name = "from_cell_cd", length = 30)
	private String fromCellCd;

	/**
	 * To Zone Cd
	 */
	@Column (name = "to_zone_cd", length = 30)
	private String toZoneCd;

	/**
	 * To Cell Cd
	 */
	@Column (name = "to_cell_cd", length = 30)
	private String toCellCd;

	/**
	 * 상태
	 */
	@Column (name = "status", length = 10)
	private String status;
	
	/**
	 * 예비 필드, 속성01
	 */
	@Column (name = "attr01", length = 100)
	private String attr01;
	/**
	 * 예비 필드, 속성02
	 */
	@Column (name = "attr02", length = 100)
	private String attr02;
	/**
	 * 예비 필드, 속성03
	 */
	@Column (name = "attr03", length = 100)
	private String attr03;
	/**
	 * 예비 필드, 속성04
	 */
	@Column (name = "attr04", length = 100)
	private String attr04;
	/**
	 * 예비 필드, 속성05
	 */
	@Column (name = "attr05", length = 100)
	private String attr05;

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

	public String getInoutMode() {
		return inoutMode;
	}

	public void setInoutMode(String inoutMode) {
		this.inoutMode = inoutMode;
	}

	public String getOrderDate() {
		return orderDate;
	}

	public void setOrderDate(String orderDate) {
		this.orderDate = orderDate;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getOrderLineNo() {
		return orderLineNo;
	}

	public void setOrderLineNo(String orderLineNo) {
		this.orderLineNo = orderLineNo;
	}

	public String getOrderDetailId() {
		return orderDetailId;
	}

	public void setOrderDetailId(String orderDetailId) {
		this.orderDetailId = orderDetailId;
	}

	public String getCustOrderNo() {
		return custOrderNo;
	}

	public void setCustOrderNo(String custOrderNo) {
		this.custOrderNo = custOrderNo;
	}

	public String getCustOrderLineNo() {
		return custOrderLineNo;
	}

	public void setCustOrderLineNo(String custOrderLineNo) {
		this.custOrderLineNo = custOrderLineNo;
	}

	public String getBoxId() {
		return boxId;
	}

	public void setBoxId(String boxId) {
		this.boxId = boxId;
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

	public String getComCd() {
		return comCd;
	}

	public void setComCd(String comCd) {
		this.comCd = comCd;
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

	public String getEquipGroupCd() {
		return equipGroupCd;
	}

	public void setEquipGroupCd(String equipGroupCd) {
		this.equipGroupCd = equipGroupCd;
	}

	public String getEquipCd() {
		return equipCd;
	}

	public void setEquipCd(String equipCd) {
		this.equipCd = equipCd;
	}

	public String getEquipNm() {
		return equipNm;
	}

	public void setEquipNm(String equipNm) {
		this.equipNm = equipNm;
	}

	public String getSubEquipCd() {
		return subEquipCd;
	}

	public void setSubEquipCd(String subEquipCd) {
		this.subEquipCd = subEquipCd;
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

	public String getSkuBarcd2() {
		return skuBarcd2;
	}

	public void setSkuBarcd2(String skuBarcd2) {
		this.skuBarcd2 = skuBarcd2;
	}

	public String getSkuNm() {
		return skuNm;
	}

	public void setSkuNm(String skuNm) {
		this.skuNm = skuNm;
	}

	public Integer getBoxInQty() {
		return boxInQty;
	}

	public void setBoxInQty(Integer boxInQty) {
		this.boxInQty = boxInQty;
	}

	public String getPackType() {
		return packType;
	}

	public void setPackType(String packType) {
		this.packType = packType;
	}

	public Integer getOrderQty() {
		return orderQty;
	}

	public void setOrderQty(Integer orderQty) {
		this.orderQty = orderQty;
	}

	public Integer getAssignQty() {
		return assignQty;
	}

	public void setAssignQty(Integer assignQty) {
		this.assignQty = assignQty;
	}

	public Integer getPickedQty() {
		return pickedQty;
	}

	public void setPickedQty(Integer pickedQty) {
		this.pickedQty = pickedQty;
	}

	public Integer getBoxedQty() {
		return boxedQty;
	}

	public void setBoxedQty(Integer boxedQty) {
		this.boxedQty = boxedQty;
	}

	public Integer getCancelQty() {
		return cancelQty;
	}

	public void setCancelQty(Integer cancelQty) {
		this.cancelQty = cancelQty;
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

	public String getLotNo() {
		return lotNo;
	}

	public void setLotNo(String lotNo) {
		this.lotNo = lotNo;
	}

	public String getFromZoneCd() {
		return fromZoneCd;
	}

	public void setFromZoneCd(String fromZoneCd) {
		this.fromZoneCd = fromZoneCd;
	}

	public String getFromCellCd() {
		return fromCellCd;
	}

	public void setFromCellCd(String fromCellCd) {
		this.fromCellCd = fromCellCd;
	}

	public String getToZoneCd() {
		return toZoneCd;
	}

	public void setToZoneCd(String toZoneCd) {
		this.toZoneCd = toZoneCd;
	}

	public String getToCellCd() {
		return toCellCd;
	}

	public void setToCellCd(String toCellCd) {
		this.toCellCd = toCellCd;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAttr01() {
		return attr01;
	}

	public void setAttr01(String attr01) {
		this.attr01 = attr01;
	}

	public String getAttr02() {
		return attr02;
	}

	public void setAttr02(String attr02) {
		this.attr02 = attr02;
	}

	public String getAttr03() {
		return attr03;
	}

	public void setAttr03(String attr03) {
		this.attr03 = attr03;
	}

	public String getAttr04() {
		return attr04;
	}

	public void setAttr04(String attr04) {
		this.attr04 = attr04;
	}

	public String getAttr05() {
		return attr05;
	}

	public void setAttr05(String attr05) {
		this.attr05 = attr05;
	}

	/**
	 * ID로 작업 배치 조회
	 *
	 * @param stageCd Stage ID
	 * @param batchId 배치 ID
	 * @param withLock 테이블 락을 걸지 여부
	 * @param exceptionWhenEmpty 조회 결과가 null이면 예외 발생 여부
	 * @return
	 */
	public static Order find(Long domainId, String batchId, String skuCd, boolean withLock, boolean exceptionWhenEmpty) {
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter("batch_id", batchId);
		if(!skuCd.isEmpty()) {
			condition.addFilter("sku_cd", skuCd);
		}
		
		Order order = withLock ?
				BeanUtil.get(IQueryManager.class).selectByConditionWithLock(Order.class, condition) :
				BeanUtil.get(IQueryManager.class).selectByCondition(Order.class, condition);

		if(order == null && exceptionWhenEmpty) {
			throw ThrowUtil.newNotFoundRecord("terms.menu.Order", batchId);
		}

		return order;
	}

}
