package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.entity.basic.AbstractStamp;

/**
 * box_items 뷰 용 (읽기 전용)
 * 
 * CREATE OR REPLACE VIEW box_items AS	
 * select
 * 	max(j.id) as id,
 * 	batch_id || '_' || j.class_cd || '_' || COALESCE(j.invoice_id, j.box_id) as box_pack_id, 
 * 	c.station_cd,
 * 	sub_equip_cd, 
 * 	sku_cd, 
 * 	max(sku_barcd) as sku_barcd, 
 * 	max(sku_nm) as sku_nm, 
 * 	max(pack_type) as pack_type,
 * 	max(sku_wt) as sku_wt,
 * 	sum(pick_qty) as pick_qty,
 * 	sum(picked_qty) as picked_qty,
 *  sum(inspected_qty) as inspected_qty
 * from
 * 	job_instances j left outer join cells c on j.domain_id = c.domain_id and j.sub_equip_cd = c.cell_cd
 * where
 * 	j.domain_id = :domainId
 * group by
 * 	j.domain_id, j.batch_id, j.class_cd, j.invoice_id, j.box_id, j.sku_cd, j.sub_equip_cd, c.station_cd
 * 
 * @author shortstop
 */
@Table(name = "box_items", ignoreDdl = true, idStrategy = GenerationRule.NONE)
public class BoxItem extends AbstractStamp {
	
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 886823091901249876L;

	@PrimaryKey
	@Column (name = "id", length = 40)
	private String id;
	
	/**
	 * BoxPack ID
	 */
	@Column (name = "box_pack_id", length = 100)
	private String boxPackId;
	
	/**
	 * 작업 스테이션
	 */
	@Column (name = "station_cd", length = 30)
	private String stationCd;
	
	/**
	 * 셀 코드
	 */
	@Column (name = "sub_equip_cd", length = 30)
	private String subEquipCd;

	/**
	 * 상품 코드
	 */
	@Column (name = "sku_cd", length = 30)
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

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getBoxPackId() {
		return boxPackId;
	}

	public void setBoxPackId(String boxPackId) {
		this.boxPackId = boxPackId;
	}

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

	public String getSubEquipCd() {
		return subEquipCd;
	}

	public void setSubEquipCd(String subEquipCd) {
		this.subEquipCd = subEquipCd;
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

}
