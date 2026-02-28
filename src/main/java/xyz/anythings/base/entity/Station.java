package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.sys.entity.relation.UserRef;

/**
 * 작업 스테이션
 * 
 * @author shortstop
 */
@Table(name = "stations", idStrategy = GenerationRule.UUID, uniqueFields="domainId,stationCd", indexes = {
	@Index(name = "ix_stations_0", columnList = "domain_id,station_cd", unique = true)
})
public class Station extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 230927217325229224L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column(name = "equip_type", nullable = false, length = 20)
	private String equipType;

	@Column(name = "equip_cd", nullable = false, length = 30)
	private String equipCd;

	@Column (name = "station_cd", nullable = false, length = 30)
	private String stationCd;

	@Column (name = "station_nm", length = 40)
	private String stationNm;
	
	@Column (name = "station_ip", length = 32)
	private String stationIp;

	@Column (name = "station_type", length = 20)
	private String stationType;

	@Column (name = "station_seq", length = 8)
	private Integer stationSeq;
	
	@Column (name = "printer_cd", length = 30)
	private String printerCd;
	
	@Column (name = "class_cd", length = 30)
	private String classCd;
	
	@Column (name = "worker_id", length = 32)
	private String workerId;
	
	@Relation(field = "workerId")
	protected UserRef worker;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

	public String getStationCd() {
		return stationCd;
	}

	public void setStationCd(String stationCd) {
		this.stationCd = stationCd;
	}

	public String getStationNm() {
		return stationNm;
	}

	public void setStationNm(String stationNm) {
		this.stationNm = stationNm;
	}

	public String getStationIp() {
		return stationIp;
	}

	public void setStationIp(String stationIp) {
		this.stationIp = stationIp;
	}

	public String getStationType() {
		return stationType;
	}

	public void setStationType(String stationType) {
		this.stationType = stationType;
	}

	public Integer getStationSeq() {
		return stationSeq;
	}

	public void setStationSeq(Integer stationSeq) {
		this.stationSeq = stationSeq;
	}

	public String getPrinterCd() {
		return printerCd;
	}

	public void setPrinterCd(String printerCd) {
		this.printerCd = printerCd;
	}

	public String getClassCd() {
		return classCd;
	}

	public void setClassCd(String classCd) {
		this.classCd = classCd;
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}
	
	public UserRef getWorker() {
		return this.worker;
	}

	public void setWorker(UserRef worker) {
		this.worker = worker;
	}

}
