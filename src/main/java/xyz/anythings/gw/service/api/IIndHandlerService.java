package xyz.anythings.gw.service.api;

import java.util.List;

import xyz.anythings.gw.entity.Gateway;
import xyz.anythings.gw.entity.Indicator;
import xyz.elidom.sys.entity.Domain;

/**
 * 게이트웨이로 받은 메시지를 처리하는 서비스 인터페이스
 * 
 * @author shortstop
 */
public interface IIndHandlerService {
	
	/**
	 * 게이트웨이에서의 부팅 요청에 대한 애플리케이션 측 응답
	 * 
	 * @param gateway 게이트웨이 
	 * @return
	 */
	public boolean handleGatewayBootReq(Gateway gateway);
		
	/**
	 * 게이트웨이 측 타임 싱크 요청에 대한 애플리케이션 측 처리
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param msgDestId
	 */
	public void handleTimesyncReq(Long domainId, String stageCd, String msgDestId);
	
	/**
	 * 게이트웨이 초기화 완료 보고에 대한 애플리케이션 측 처리
	 * 
	 * @param gateway
	 * @param params
	 */
	public void handleGatewayInitReport(Gateway gateway, Object ... params);

	/**
	 * 표시기 초기화 완료 보고에 대한 애플리케이션 측 처리
	 * 
	 * @param indicator
	 * @param stageCd
	 * @param params
	 */
	public void handleIndicatorInitReport(Indicator indicator, String stageCd, Object ... params);
	
	/**
	 * 게이트웨이 상태 보고에 대한 애플리케이션 측 처리
	 * 
	 * @param gateway
	 * @param status
	 * @param version
	 * @param params
	 */
	public void handleGatewayStatusReport(Gateway gateway, String status, String version, Object ... params);

	/**
	 * 표시기 상태 보고에 대한 애플리케이션 측 처리
	 * 
	 * @param indicator
	 * @param stageCd
	 * @param status
	 * @param version
	 * @param params
	 */
	public void handleIndicatorStatusReport(Indicator indicator, String stageCd, String status, String version, Object ... params);
	
	/**
	 * 펌웨어 배포 완료 응답에 대한 애플리케이션 측 처리
	 * 
	 * @param gateway
	 * @param deploySuccess
	 * @param params
	 */
	public void handleDeploymentResponse(Gateway gateway, boolean deploySuccess, Object ... params);
	
	/**
	 * 표시기의 셀 위치를 교체 
	 * 
	 * @param domainId
	 * @param indCd
	 * @param cellCd
	 */
	public void handleChangeCellOfIndReq(Long domainId, String indCd, String cellCd);
	
	/**
	 * 셀의 표시기를 교체 
	 * 
	 * @param domainId
	 * @param fromIndCd
	 * @param toIndCd
	 */
	public void handleChangeIndOfCellReq(Long domainId, String fromIndCd, String toIndCd);
	
	/**
	 * 접속 정보 변경 보고에 대한 애플리케이션 측 처리 
	 * 
	 * @param domain
	 * @param gwList
	 * @param changeSuccess
	 */
	public void handleChangeAccessInfo(Domain domain, List<Gateway> gwList, boolean changeSuccess);

}
