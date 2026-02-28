package xyz.anythings.base.service.api;

/**
 * 송장 번호 생성기 API
 * 
 * @author shortstop
 */
public interface IInvoiceNoGenerator {

	/**
	 * 송장 번호 대역을 받아 개별 송장 번호를 생성하는 서비스
	 * 
	 * @param domainId
	 * @param comCd
	 * @param customerCd
	 * @param fromNo
	 * @param toNo
	 * @return 생성한 송장 번호 개수 리턴
	 */
	public int generateInvoiceNumbers(Long domainId, String comCd, String customerCd, String fromNo, String toNo);
	
	/**
	 * 사용 가능한 다음 송장 번호를 추출하여 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param comCd
	 * @param customerCd
	 * @return
	 */
	public String nextInvoiceId(Long domainId, String stageCd, String comCd, String customerCd);
	
}
