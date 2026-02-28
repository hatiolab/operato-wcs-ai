package xyz.anythings.sys.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import xyz.anythings.sys.AnyConstants;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dev.entity.DiyService;
import xyz.elidom.exception.ElidomException;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.context.DomainContext;
import xyz.elidom.sys.system.engine.IScriptEngine;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 동적 서비스 호출 구현
 * 
 * @author shortstop
 */
@Component
public class AnyCustomService implements ICustomService {

	/**
	 * 도메인 컨트롤러
	 */
	@Autowired
	private DomainController domainCtrl;
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	private IQueryManager queryManager;
	/**
	 * 스크립트 엔진
	 */
	@Autowired
	private IScriptEngine scriptEngine;
	/**
	 * 비동기 처리시 예외 핸들러
	 */
	@Autowired
	private AsyncExceptionHandler asyncExceptionHandler;
	
	@Override
	public Object doCustomService(Long domainId, String diyServiceName, Map<String, Object> parameters) {
		// 커스텀 서비스 실행
		return this.doService(domainId, diyServiceName, parameters);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW)
	public Object doCustomServiceByNewTransaction(Long domainId, String diyServiceName, Map<String, Object> parameters) {
		
		// 1. 실행 결과
		Object retVal = null;
		try {
			// 2. 커스텀 서비스 실행
			retVal = this.executeService(domainId, diyServiceName, parameters);

		} catch (ElidomException ee) {
			// 3. Elidom 예외 처리
			this.asyncExceptionHandler.handleException(domainId, ee.getCode(), ee, ee.isWritable(), !ee.isWritable());
			
		} catch(Throwable th) {
			// 4. 예외 처리
			this.asyncExceptionHandler.handleException(domainId, th.getMessage(), (Exception)th, true, true);
		}
		
		// 5. 결과 리턴
		return retVal;
	}

	@Override
	@Async
	public void doCustomServiceByAsync(Long domainId, String diyServiceName, Map<String, Object> parameters) {
		// 1. 사이트 도메인 조회
		Domain siteDomain = this.domainCtrl.findOne(domainId, null);
		
		// 2. 스레드 로컬 변수에서 currentDomain 설정
		DomainContext.setCurrentDomain(siteDomain);
		try {
			// 3. 서비스 실행
			this.executeService(domainId, diyServiceName, parameters);
			
		} catch (ElidomException ee) {
			// 4. Elidom 예외 처리
			this.asyncExceptionHandler.handleException(domainId, ee.getCode(), ee, ee.isWritable(), !ee.isWritable());
			
		} catch(Throwable th) {
			// 5. 예외 처리
			this.asyncExceptionHandler.handleException(domainId, th.getMessage(), (Exception)th, true, true);
			
		} finally {
			// 6. 스레드 로컬 변수에서 currentDomain 리셋 
			DomainContext.unsetAll();
		}
	}
	
	/**
	 * 서비스 실행
	 * 
	 * @param domainId
	 * @param diyServiceName
	 * @param parameters
	 * @return
	 */
	private Object doService(Long domainId, String diyServiceName, Map<String, Object> parameters) {
		
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter(AnyConstants.ENTITY_FIELD_NAME, diyServiceName);
		DiyService diyService = this.queryManager.selectByCondition(DiyService.class, condition);
		Object retVal = null;
		
		if(diyService != null && ValueUtil.isNotEmpty(diyService.getServiceLogic())) {
			try {
				retVal = this.scriptEngine.runScript(diyService.getLangType(), diyService.getServiceLogic(), parameters);

			} catch(ElidomException ee) {
				throw ee;
			
			} catch(Exception e) {
				Throwable th = e.getCause();
				
				if(th != null) {
					if(th instanceof ElidomException) {
						throw (ElidomException)th;
					}
				}
				
				throw new ElidomRuntimeException(th == null ? e : th);
			}
		}
		
		return retVal;
	}
	
	/**
	 * 에외 처리 없이 서비스 실행
	 * 
	 * @param domainId
	 * @param diyServiceName
	 * @param parameters
	 * @return
	 */
	private Object executeService(Long domainId, String diyServiceName, Map<String, Object> parameters) {
		
		Query condition = AnyOrmUtil.newConditionForExecution(domainId);
		condition.addFilter(AnyConstants.ENTITY_FIELD_NAME, diyServiceName);
		DiyService diyService = this.queryManager.selectByCondition(DiyService.class, condition);
		Object retVal = null;
		
		if(diyService != null && ValueUtil.isNotEmpty(diyService.getServiceLogic())) {
			retVal = this.scriptEngine.runScript(diyService.getLangType(), diyService.getServiceLogic(), parameters);
		}
		
		return retVal;
	}

}
