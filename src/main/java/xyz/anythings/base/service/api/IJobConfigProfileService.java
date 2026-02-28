package xyz.anythings.base.service.api;

import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.JobConfigSet;

/**
 * 작업 관련 설정 프로파일 서비스 API 정의
 * 
 * @author shortstop
 */
public interface IJobConfigProfileService {
		
	/**
	 * 스테이지 범위 내 작업 설정 프로파일 초기화
	 * 
	 * @param domainId
	 * @return
	 */
	public int buildStageConfigSet(Long domainId);
	
	/**
	 * 스테이지 기본 작업 설정 프로파일 초기화
	 * 
	 * @param configSet
	 * @return
	 */
	public JobConfigSet addStageConfigSet(JobConfigSet configSet);
	
	/**
	 * 스테이지 기본 작업 설정 프로파일 찾아 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public JobConfigSet getStageConfigSet(Long domainId, String stageCd);
	
	/**
	 * 스테이지 기본 작업 설정 프로파일에서 키로 설정 값 조회
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
	 * 작업 배치 정보로 작업 설정 프로파일 생성
	 * 
	 * @param batch
	 * @return
	 */
	public JobConfigSet addConfigSet(JobBatch batch);
	
	/**
	 * 작업 배치 기본 작업 설정 프로파일 찾아 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public JobConfigSet getConfigSet(String batchId);
	
	/**
	 * 작업 배치와 설정 키로 작업 설정 값 조회
	 * 
	 * @param batch
	 * @param key
	 * @return
	 */
	public String getConfigValue(JobBatch batch, String key);
	
	/**
	 * 작업 배치와 설정 키로 작업 설정 값 조회
	 * 
	 * @param batchId
	 * @param key
	 * @return
	 */
	public String getConfigValue(String batchId, String key);
	
	/**
	 * 작업 배치와 설정 키로 작업 설정 값 조회, 값이 없으면 기본 값 리턴
	 * 
	 * @param batch
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getConfigValue(JobBatch batch, String key, String defaultValue);
	
	/**
	 * 작업 배치와 설정 키로 작업 설정 값 조회, 값이 없으면 기본 값 리턴
	 * 
	 * @param batchId
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String getConfigValue(String batchId, String key, String defaultValue);
	
	/**
	 * 작업 배치 정보로 작업 설정 프로파일 리셋 (캐쉬 리셋)
	 * 
	 * @param batchId
	 */
	public void clearConfigSet(String batchId);
	
	/**
	 * templateConfigSetId로 작업 설정 프로파일 복사
	 * 
	 * @param domainId
	 * @param templateConfigSetId
	 * @param targetSetCd
	 * @param targetSetNm
	 * @return
	 */
	public JobConfigSet copyConfigSet(Long domainId, String templateConfigSetId, String targetSetCd, String targetSetNm);

}
