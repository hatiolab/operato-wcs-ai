package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Table;

/**
 * 주문 라벨 정보
 * 
 * @author shortstop
 */
@Table(name = "order_labels", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_order_labels_1", columnList = "domain_id,batch_id"),
	@Index(name = "ix_order_labels_2", columnList = "domain_id,job_date,job_seq"),
	@Index(name = "ix_order_labels_3", columnList = "domain_id,order_id"),
	@Index(name = "ix_order_labels_4", columnList = "domain_id,b2c_cust_id")
})
public class OrderLabel extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 789276685704369381L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	/**
	 * 배치 ID
	 */
	@Column (name = "batch_id", length = 40)
	private String batchId;
	
	/**
	 * 작업 일자
	 */
	@Column (name = "job_date", length = 10)
	private String jobDate;
	
	/**
	 * 작업 차수
	 */
	@Column (name = "job_seq", length = 10)
	private String jobSeq;
	
	/**
	 * 웨이브 번호
	 */
	@Column (name = "wave_no", length = 30)
	private String waveNo;
	
	/**
	 * Order의 특정 필드와 매핑되는 번호, 즉 주문 테이블에서 찾아가기 위한 정보
	 */
	@Column (name = "order_id", nullable = false, length = 40)
	private String orderId;
	
	/**
	 * 고객 코드 - 주소정제 파라미터
	 */
	@Column (name = "b2c_cust_id", length = 40)
	private String b2cCustId;

	/**
	 * 고객 관리 거래처 코드 - 주소정제 파라미터
	 */
	@Column (name = "b2c_cust_mgr_id", length = 40)
	private String b2cCustMgrId;
	
	/**
	 * 계약 구분 (01 : 일반) - 주소정제 파라미터
	 */
	@Column (name = "contract_type", length = 20)
	private String contractType;
	
	/**
	 * 예약 구분 (01 : 일반, 02 : 반품) - 주소정제 파라미터
	 */
	@Column (name = "resv_type", length = 20)
	private String resvType;
	
	/**
	 * 운임, 정산 구분 (01 : 선불, 02 : 착불, 03 : 신용) - 주소정제 파라미터
	 */
	@Column (name = "pay_type", length = 20)
	private String payType;

	/**
	 * 주문자 명
	 */
	@Column (name = "orderer_nm", length = 100)
	private String ordererNm;

	/**
	 * 주문 대리자 명 (혹은 수령인 명)
	 */
	@Column (name = "orderer2_nm", length = 100)
	private String orderer2Nm;
	
	/**
	 * 받는 분 이름
	 */
	@Column (name = "rcv_nm", length = 100)
	private String rcvNm;

	/**
	 * 받는 분 전화번호 1
	 */
	@Column (name = "rcv_tel_no1", length = 100)
	private String rcvTelNo1;

	/**
	 * 받는 분 전화번호 2
	 */
	@Column (name = "rcv_tel_no2", length = 100)
	private String rcvTelNo2;

	/**
	 * 받는 분 주소 - 주소정제 파라미터
	 */
	@Column (name = "rcv_addr", length = 500)
	private String rcvAddr;

	/**
	 * 받는 분 기타 주소 - 주소정제 파라미터
	 */
	@Column (name = "rcv_etc_addr", length = 255)
	private String rcvEtcAddr;
	
	/**
	 * 받는 분 실제 주소 - 주소정제 결과
	 */
	@Column (name = "rcv_real_addr", length = 500)
	private String rcvRealAddr;
	
	/**
	 * 받는 분 주소 약칭 - 주소정제 결과
	 */
	@Column (name = "rcv_abbr_addr", length = 100)
	private String rcvAbbrAddr;

	/**
	 * 받는 분 주소 우편번호 - 주소정제 결과
	 */
	@Column (name = "rcv_zip_cd", length = 30)
	private String rcvZipCd;

	/**
	 * 보내는 분 이름
	 */
	@Column (name = "snd_nm", length = 100)
	private String sndNm;

	/**
	 * 보내는 분 전화번호 1
	 */
	@Column (name = "snd_tel_no1", length = 40)
	private String sndTelNo1;
	
	/**
	 * 보내는 분 전화번호 2
	 */
	@Column (name = "snd_tel_no2", length = 40)
	private String sndTelNo2;
	
	/**
	 * 보내는 분 우편번호
	 */
	@Column (name = "snd_zip_cd", length = 30)
	private String sndZipCd;

	/**
	 * 보내는 분 주소 - 주소정제 파라미터
	 */
	@Column (name = "snd_addr", length = 500)
	private String sndAddr;

	/**
	 * 보내는 분 기타 주소 - 주소정제 파라미터
	 */
	@Column (name = "snd_etc_addr", length = 255)
	private String sndEtcAddr;
	
	/**
	 * 보내는 분 실제 주소 - 주소정제 결과
	 */
	@Column (name = "snd_real_addr", length = 500)
	private String sndRealAddr;

	/**
	 * 도착지 권역 코드 (ex: 2T05-1의 2T05) - 주소정제 결과
	 */
	@Column (name = "dlv_region_cd", length = 30)
	private String dlvRegionCd;

	/**
	 * 도착지 권역 서브 코드 (ex: 2T05-1의 1) - 주소정제 결과
	 */
	@Column (name = "dlv_region_sub_cd", length = 10)
	private String dlvRegionSubCd;
	
	/**
	 * 배송 집배점 코드 - 주소정제 결과
	 */
	@Column (name = "dlv_store_cd", length = 10)
	private String dlvStoreCd;
	
	/**
	 * 배송 집배점 명 - 주소정제 결과
	 */
	@Column (name = "dlv_store_nm", length = 20)
	private String dlvStoreNm;
	
	/**
	 * 배송 집배 사원 코드 - 주소정제 결과
	 */
	@Column (name = "dlv_emp_cd", length = 20)
	private String dlvEmpCd;
	
	/**
	 * 배송 집배 사원 명 - 주소정제 결과
	 */
	@Column (name = "dlv_emp_nm", length = 20)
	private String dlvEmpNm;

	/**
	 * 메모
	 */
	@Column (name = "memo")
	private String memo;
	
	/**
	 * 라벨 추가 속성 1
	 */
	@Column (name = "attr01", length = 100)
	private String attr01;

	/**
	 * 라벨 추가 속성 2
	 */
	@Column (name = "attr02", length = 100)
	private String attr02;

	/**
	 * 라벨 추가 속성 3
	 */
	@Column (name = "attr03", length = 100)
	private String attr03;

	/**
	 * 라벨 추가 속성 4
	 */
	@Column (name = "attr04", length = 100)
	private String attr04;

	/**
	 * 라벨 추가 속성 5
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

	public String getWaveNo() {
		return waveNo;
	}

	public void setWaveNo(String waveNo) {
		this.waveNo = waveNo;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getB2cCustId() {
		return b2cCustId;
	}

	public void setB2cCustId(String b2cCustId) {
		this.b2cCustId = b2cCustId;
	}

	public String getB2cCustMgrId() {
		return b2cCustMgrId;
	}

	public void setB2cCustMgrId(String b2cCustMgrId) {
		this.b2cCustMgrId = b2cCustMgrId;
	}

	public String getContractType() {
		return contractType;
	}

	public void setContractType(String contractType) {
		this.contractType = contractType;
	}

	public String getResvType() {
		return resvType;
	}

	public void setResvType(String resvType) {
		this.resvType = resvType;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}
	
	public String getOrdererNm() {
		return ordererNm;
	}

	public void setOrdererNm(String ordererNm) {
		this.ordererNm = ordererNm;
	}

	public String getOrderer2Nm() {
		return orderer2Nm;
	}

	public void setOrderer2Nm(String orderer2Nm) {
		this.orderer2Nm = orderer2Nm;
	}

	public String getRcvNm() {
		return rcvNm;
	}

	public void setRcvNm(String rcvNm) {
		this.rcvNm = rcvNm;
	}

	public String getRcvTelNo1() {
		return rcvTelNo1;
	}

	public void setRcvTelNo1(String rcvTelNo1) {
		this.rcvTelNo1 = rcvTelNo1;
	}

	public String getRcvTelNo2() {
		return rcvTelNo2;
	}

	public void setRcvTelNo2(String rcvTelNo2) {
		this.rcvTelNo2 = rcvTelNo2;
	}

	public String getRcvAddr() {
		return rcvAddr;
	}

	public void setRcvAddr(String rcvAddr) {
		this.rcvAddr = rcvAddr;
	}

	public String getRcvEtcAddr() {
		return rcvEtcAddr;
	}

	public void setRcvEtcAddr(String rcvEtcAddr) {
		this.rcvEtcAddr = rcvEtcAddr;
	}

	public String getRcvRealAddr() {
		return rcvRealAddr;
	}

	public void setRcvRealAddr(String rcvRealAddr) {
		this.rcvRealAddr = rcvRealAddr;
	}

	public String getRcvAbbrAddr() {
		return rcvAbbrAddr;
	}

	public void setRcvAbbrAddr(String rcvAbbrAddr) {
		this.rcvAbbrAddr = rcvAbbrAddr;
	}

	public String getRcvZipCd() {
		return rcvZipCd;
	}

	public void setRcvZipCd(String rcvZipCd) {
		this.rcvZipCd = rcvZipCd;
	}

	public String getSndNm() {
		return sndNm;
	}

	public void setSndNm(String sndNm) {
		this.sndNm = sndNm;
	}

	public String getSndTelNo1() {
		return sndTelNo1;
	}

	public void setSndTelNo1(String sndTelNo1) {
		this.sndTelNo1 = sndTelNo1;
	}

	public String getSndTelNo2() {
		return sndTelNo2;
	}

	public void setSndTelNo2(String sndTelNo2) {
		this.sndTelNo2 = sndTelNo2;
	}

	public String getSndZipCd() {
		return sndZipCd;
	}

	public void setSndZipCd(String sndZipCd) {
		this.sndZipCd = sndZipCd;
	}

	public String getSndAddr() {
		return sndAddr;
	}

	public void setSndAddr(String sndAddr) {
		this.sndAddr = sndAddr;
	}

	public String getSndEtcAddr() {
		return sndEtcAddr;
	}

	public void setSndEtcAddr(String sndEtcAddr) {
		this.sndEtcAddr = sndEtcAddr;
	}

	public String getSndRealAddr() {
		return sndRealAddr;
	}

	public void setSndRealAddr(String sndRealAddr) {
		this.sndRealAddr = sndRealAddr;
	}

	public String getDlvRegionCd() {
		return dlvRegionCd;
	}

	public void setDlvRegionCd(String dlvRegionCd) {
		this.dlvRegionCd = dlvRegionCd;
	}

	public String getDlvRegionSubCd() {
		return dlvRegionSubCd;
	}

	public void setDlvRegionSubCd(String dlvRegionSubCd) {
		this.dlvRegionSubCd = dlvRegionSubCd;
	}

	public String getDlvStoreCd() {
		return dlvStoreCd;
	}

	public void setDlvStoreCd(String dlvStoreCd) {
		this.dlvStoreCd = dlvStoreCd;
	}

	public String getDlvStoreNm() {
		return dlvStoreNm;
	}

	public void setDlvStoreNm(String dlvStoreNm) {
		this.dlvStoreNm = dlvStoreNm;
	}

	public String getDlvEmpCd() {
		return dlvEmpCd;
	}

	public void setDlvEmpCd(String dlvEmpCd) {
		this.dlvEmpCd = dlvEmpCd;
	}

	public String getDlvEmpNm() {
		return dlvEmpNm;
	}

	public void setDlvEmpNm(String dlvEmpNm) {
		this.dlvEmpNm = dlvEmpNm;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
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

}