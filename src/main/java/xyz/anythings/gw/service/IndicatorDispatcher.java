package xyz.anythings.gw.service;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.gw.service.api.IIndHandlerService;
import xyz.anythings.gw.service.api.IIndRequestService;

/**
 * 인디케이터 벤더별 서비스 디스패처
 * 
 * @author shortstop
 */
@Component
public class IndicatorDispatcher implements BeanFactoryAware {

	/**
	 * BeanFactory
	 */
	protected BeanFactory beanFactory;
	/**
	 * 표시기 설정 프로파일 서비스
	 */
	@Autowired
	private IndConfigProfileService indConfigSetService;
	/**
	 * 표시기 요청 혹은 응답에 대한 핸들러 서비스 컴포넌트 기본 명 
	 */
	private String indicatorHandlerServiceName = "IndHandlerService";
	/**
	 * 표시기 점, 소등 요청 서비스 컴포넌트 기본 명 
	 */
	private String indicatorRequestServiceName = "IndRequestService";

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	
	/**
	 * 표시기 타입으로 요청 혹은 응답에 대한 핸들러 서비스 찾아 리턴
	 * 
	 * @param indType
	 * @return
	 */
	public IIndHandlerService getIndicatorHandlerService(String indType) {
		String indReqSvcName = indType.toLowerCase() + this.indicatorHandlerServiceName;
		return (IIndHandlerService)this.beanFactory.getBean(indReqSvcName);
	}
	
	/**
	 * 작업 배치 ID로 요청 혹은 응답에 대한 핸들러 서비스 찾아 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public IIndHandlerService getIndicatorHandlerServiceByBatch(String batchId) {
		IndConfigSet configSet = this.indConfigSetService.getConfigSet(batchId);
		return this.getIndicatorHandlerService(configSet.getIndType());
	}
	
	/**
	 * 스테이지 코드로 요청 혹은 응답에 대한 핸들러 서비스 찾아 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public IIndHandlerService getIndicatorHandlerServiceByStage(Long domainId, String stageCd) {
		IndConfigSet configSet = this.indConfigSetService.getStageConfigSet(domainId, stageCd);
		return this.getIndicatorHandlerService(configSet.getIndType());
	}

	/**
	 * 표시기 타입으로 표시기 점,소등 요청 서비스 찾아 리턴
	 * 
	 * @param indType
	 * @return
	 */
	public IIndRequestService getIndicatorRequestService(String indType) {
		String indReqSvcName = indType.toLowerCase() + this.indicatorRequestServiceName;
		return (IIndRequestService)this.beanFactory.getBean(indReqSvcName);
	}
	
	/**
	 * 작업 배치 ID로 표시기 점,소등 요청 서비스 찾아 리턴
	 * 
	 * @param batchId
	 * @return
	 */
	public IIndRequestService getIndicatorRequestServiceByBatch(String batchId) {
		IndConfigSet configSet = this.indConfigSetService.getConfigSet(batchId);
		return (configSet == null) ? null : this.getIndicatorRequestService(configSet.getIndType());
	}
	
	/**
	 * 배치 ID와 표시기 점등 설정 프로파일을 등록
	 *  
	 * @param batchId
	 * @param configSet
	 */
	public void addIndicatorConfigSet(String batchId, IndConfigSet configSet) {
		this.indConfigSetService.addConfigSet(batchId, configSet);
	}
	
	/**
	 * 스테이지 코드로 표시기 점,소등 요청 서비스 찾아 리턴
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public IIndRequestService getIndicatorRequestServiceByStage(Long domainId, String stageCd) {
		IndConfigSet configSet = this.indConfigSetService.getStageConfigSet(domainId, stageCd);
		return this.getIndicatorRequestService(configSet.getIndType());
	}

}
