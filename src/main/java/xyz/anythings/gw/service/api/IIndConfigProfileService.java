package xyz.anythings.gw.service.api;

import xyz.anythings.gw.entity.IndConfigSet;

/**
 * 설정 프로파일 서비스 API 정의
 * 
 * @author shortstop
 */
public interface IIndConfigProfileService {

	/**
	 * 스테이지 범위 내 표시기 설정 프로파일 초기화
	 * 
	 * @param domainId
	 * @return
	 */
	public int buildStageConfigSet(Long domainId);
	
	/**
	 * 스테이지 기본 표시기 설정 프로파일 초기화
	 * 
	 * @param configSet
	 * @return
	 */
	public IndConfigSet addStageConfigSet(IndConfigSet configSet);
	
	/**
	 * 스테이지 기본 표시기 설정 프로파일 찾아 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public IndConfigSet getStageConfigSet(Long domainId, String stageCd);
	
	/**
	 * 스테이지 기본 설정 프로파일에서 키로 설정 값 조회
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param key
	 * @return
	 */
	public String getStageConfigValue(Long domainId, String stageCd, String key);
	
	/**
	 * 스테이지 기본 설정 프로파일에서 키로 설정 값 조회
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getStageConfigValue(Long domainId, String stageCd, String key, String defaultValue);
	
	/**
	 * 스테이지 기본 표시기 설정 프로파일 리셋 (캐쉬 리셋)
	 * 
	 * @param domainId
	 * @param stageCd
	 */
	public void clearStageConfigSet(Long domainId, String stageCd);
	
	/**
	 * 작업 배치 ID 혹은 스테이지 코드로 표시기 설정 프로파일을 찾아 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public IndConfigSet getConfigSet(String batchId);
	
	/**
	 * 작업 배치 정보로 표시기 설정 프로파일 생성
	 * 
	 * @param batchId
	 * @param configSet
	 * @return
	 */
	public IndConfigSet addConfigSet(String batchId, IndConfigSet configSet);
	
	/**
	 * 작업 배치와 설정 키로 표시기 설정 값 조회
	 * 
	 * @param batchId
	 * @param key
	 * @return
	 */
	public String getConfigValue(String batchId, String key);
	
	/**
	 * 작업 배치와 설정 키로 표시기 설정 값 조회
	 * 
	 * @param batchId
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getConfigValue(String batchId, String key, String defaultValue);
	
	/**
	 * 작업 배치 정보로 표시기 설정 프로파일 리셋 (캐쉬 리셋)
	 * 
	 * @param batchId
	 */
	public void clearConfigSet(String batchId);

	/**
	 * templateConfigSetId로 표시기 설정 프로파일 복사
	 * 
	 * @param domainId
	 * @param templateConfigSetId
	 * @param targetSetCd
	 * @param targetSetNm
	 * @return
	 */
	public IndConfigSet copyIndConfigSet(Long domainId, String templateConfigSetId, String targetSetCd, String targetSetNm);
}
