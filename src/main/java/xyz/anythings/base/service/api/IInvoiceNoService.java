package xyz.anythings.base.service.api;

/**
 * 송장 번호 관리 서비스
 * 	1. 송장 번호 생성
 * 	2. 송장 번호 부여
 *  
 * @author shortstop
 */
public interface IInvoiceNoService {

	/**
	 * 송장 번호 파라미터를 받아 송장 번호를 생성
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param comCd
	 * @param customerCd
	 * @param params
	 * @return 생성한 송장 번호 개수 리턴
	 */
	public int generateInvoiceNo(Long domainId, String stageCd, String comCd, String customerCd, Object ... params);
	
	/**
	 * 스테이지 범위 내에서 사용 가능한 다음 송장 번호를 추출하여 리턴.
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param comCd
	 * @param customerCd
	 * @return
	 */
	public String nextInvoiceId(Long domainId, String stageCd, String comCd, String customerCd);

}
