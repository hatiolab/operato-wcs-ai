package xyz.anythings.sys.service;

import java.util.Map;

/**
 * 커스텀 서비스 호출 서비스
 * 
 * @author shortstop
 */
public interface ICustomService {

	/**
	 * 커스텀 서비스를 실행
	 * 
	 * @param domainId
	 * @param diyServiceName
	 * @param parameters
	 * @return
	 */
	public Object doCustomService(Long domainId, String diyServiceName, Map<String, Object> parameters);
	
	/**
	 * 별도 트랜잭션으로 커스텀 서비스를 동기 모드로 실행
	 * 
	 * @param domainId
	 * @param diyServiceName
	 * @param parameters
	 * @return
	 */
	public Object doCustomServiceByNewTransaction(Long domainId, String diyServiceName, Map<String, Object> parameters);
	
	/**
	 * 비동기 모드(별도 트랜잭션)로 커스텀 서비스를 실행
	 * 
	 * @param domainId
	 * @param diyServiceName
	 * @param parameters
	 */
	public void doCustomServiceByAsync(Long domainId, String diyServiceName, Map<String, Object> parameters);
	
}
