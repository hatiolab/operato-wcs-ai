package xyz.anythings.gw.service.api;

import java.util.List;
import java.util.Map;

import xyz.anythings.gw.entity.Deployment;
import xyz.anythings.gw.service.model.IGwIndInit;
import xyz.anythings.gw.service.model.IIndOnInfo;
import xyz.anythings.gw.service.model.IndCommonReq;

/**
 * 표시기 각종 점/소등 요청 서비스 인터페이스 
 * 
 * @author shortstop
 */
public interface IIndRequestService {
	
	/**
	 * 표시기 정보 모델 생성 
	 * 
	 * @return
	 */
	public IIndOnInfo newIndicatorInfomration();
	
	/**
	 * 게이트웨이 정보 모델 생성
	 * 
	 * @return
	 */
	public IGwIndInit newGwIndicatorInit(); 
	
	/**
	 * 표시기가 소속된 게이트웨이 경로를 찾는다.
	 * 
	 * @param domainId
	 * @param indCd
	 * @return
	 */
	public String findGwPath(Long domainId, String indCd);
	
	/**********************************************************************
	 * 							1. 표시기 On 요청
	 **********************************************************************/
	
	/**
	 * 여러 표시기에 한꺼번에 분류 처리를 위한 점등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param actionType
	 * @param indOnForPickList - key : gwPath, value : IIndOnInfo
	 * @param params
	 */
	public void requestIndListOn(Long domainId, String stageCd, String jobType, String actionType, Map<String, List<IIndOnInfo>> indOnForPickList, Map<?, ?>... params);
	
	/**
	 * 여러 표시기에 한꺼번에 재고 실사용 점등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param stockIndOnList - key : gwPath, value : IIndOnInfo 
	 */
	public void requestIndListOnForStocktake(Long domainId, String stageCd, Map<String, List<IIndOnInfo>> stockIndOnList);
	
	/**
	 * 여러 표시기에 한꺼번에 분류 처리를 위한 점등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param indOnForPickList - key : gwPath, value : IIndOnInfo 
	 */
	public void requestIndListOnForInspect(Long domainId, String stageCd, String jobType, Map<String, List<IIndOnInfo>> indOnForPickList);
		
	/**
	 * 하나의 표시기에 액션 타입별 점등 요청 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param actionType
	 * @param color
	 * @param boxQty
	 * @param eaQty
	 * @param params
	 */
	public void requestIndOn(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String actionType, String color, Integer boxQty, Integer eaQty, Map<?, ?>... params);
	
	/**
	 * 하나의 표시기에 분류 처리를 위한 점등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param color
	 * @param boxQty
	 * @param eaQty
	 */
	public void requestIndOnForPick(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String color, Integer boxQty, Integer eaQty, Map<?, ?>... params);
	
	/**
	 * 하나의 표시기에 검수를 위한 점등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param color
	 * @param boxQty
	 * @param eaQty
	 */
	public void requestIndOnForInspect(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String color, Integer boxQty, Integer eaQty);
	
	/**********************************************************************
	 * 							2. 표시기 Off 요청
	 **********************************************************************/
	/**
	 * 표시기 하나에 대한 소등 요청 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param indCd
	 * @param forceOff
	 */
	public void requestIndOff(Long domainId, String stageCd, String gwPath, String indCd, boolean forceOff);
	
	/**
	 * 표시기 하나에 대한 소등 요청 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param indCd
	 */
	public void requestIndOff(Long domainId, String stageCd, String gwPath, String indCd);
	
	/**
	 * 게이트웨이 - 표시기 리스트 값으로 표시기 소등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param indOffMap - key : gwPath, value : indicator code list
	 * @param forceOff
	 */
	public void requestIndListOff(Long domainId, String stageCd, Map<String, List<String>> indOffMap, boolean forceOff);
		
	/**
	 * 게이트웨이에 게이트웨이 소속 모든 표시기 소등 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param indCdList indicator code list
	 * @param forceOff 강제 소등 여부
	 */
	public void requestIndListOff(Long domainId, String stageCd, String gwPath, List<String> indCdList, boolean forceOff);
	
	/**
	 * 호기별 표시기 Off 요청 전송 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param indOffList 소등할 표시기 리스트 
	 * @param forceOff 강제 소등 여부
	 */
	public void requestIndListOff(Long domainId, String stageCd, List<IndCommonReq> indList, boolean forceOff);
	
	/**********************************************************************
	 * 						3. 표시기 숫자, 문자 표시 요청
	 **********************************************************************/
	
	/**
	 * 작업 완료 표시기 표시 요청 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param finalEnd 최종 완료 (End End 표시 후 Fullbox까지 마쳤는지) 여부
	 */
	public void requestIndEndDisplay(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, boolean finalEnd);
	
	/**
	 * 로케이션 별 공박스 매핑 필요 표시 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 */
	public void requestIndNoBoxDisplay(Long domainId, String stageCd, String jobType, String gwPath, String indCd);
	
	/**
	 * Fullbox시에 로케이션-공박스 매핑이 안 된 에러를 표시기에 표시하기 위한 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 */
	public void requestIndErrBoxDisplay(Long domainId, String stageCd, String jobType, String gwPath, String indCd);
	
	/**
	 * 표시기에 버튼 점등은 되지 않고 eaQty 정보로 표시 - 사용자 터치 반응 안함 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param pickEaQty
	 */
	public void requestIndDisplayOnly(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, Integer pickEaQty);
	
	/**
	 * 세그먼트 정보를 커스터마이징 한 표시기 표시 - 이 때 Fullbox가 되어야 하므로 readOnly는 false로
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param firstSegQty
	 * @param secondSegQty
	 * @param thirdSegQty
	 */
	public void requestIndSegmentDisplay(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, Integer firstSegQty, Integer secondSegQty, Integer thirdSegQty);
	
	/**
	 * 각종 옵션으로 표시기에 표시 요청 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param displayActionType
	 * @param readOnly
	 * @param firstSegQty
	 * @param secondSegQty
	 * @param thirdSegQty
	 */
	public void requestIndDisplay(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String displayActionType, boolean readOnly, Integer firstSegQty, Integer secondSegQty, Integer thirdSegQty);
	
	/**
	 * 각종 옵션으로 표시기에 표시 요청 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param displayActionType
	 * @param segRole
	 * @param readOnly
	 * @param firstSegQty
	 * @param secondSegQty
	 * @param thirdSegQty
	 */
	public void requestIndDisplay(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String displayActionType, String[] segRole, boolean readOnly, Integer firstSegQty, Integer secondSegQty, Integer thirdSegQty);
	
	/**
	 * 총 처리한 수량 / 방금 처리한 수량을 표시
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param accumQty
	 * @param pickedQty
	 */
	public void requestIndDisplayAccumQty(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, Integer accumQty, Integer pickedQty);
		
	/**
	 * FullBox 표시기 표시 요청 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param color
	 */
	public void requestFullbox(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String color);
	
	/**
	 * 표시기에 문자열 표시 요청
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param displayStr
	 */
	public void requestShowString(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String displayStr);
	
	/**
	 * 표시기 표시 방향과 숫자를 동시에 표시 - 왼쪽은 'L' or 'R' 표시 오른쪽은 숫자 표시
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param leftSideFlag 왼쪽 로케이션 표시용인지 여부
	 * @param rightQty
	 */
	public void requestDisplayDirectionAndQty(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, boolean leftSideFlag, Integer rightQty);
	
	/**
	 * 왼쪽은 문자 오른쪽은 숫자 표시
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param leftStr
	 * @param rightQty
	 */
	public void requestDisplayLeftStringRightQty(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, String leftStr, Integer rightQty);
	
	/**
	 * 표시기 표시 방향과 표시 수량을 좌, 우측에 동시에 표시 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param jobType
	 * @param gwPath
	 * @param indCd
	 * @param bizId
	 * @param leftQty
	 * @param rightQty
	 */
	public void requestDisplayBothDirectionQty(Long domainId, String stageCd, String jobType, String gwPath, String indCd, String bizId, Integer leftQty, Integer rightQty);
	
	/**********************************************************************
	 * 							4. LED 바 점등 / 소등
	 **********************************************************************/
	
	/**
	 * 표시기 LED 점등 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param indCd
	 * @param ledBarBrightness
	 */
	public void requestLedOn(Long domainId, String stageCd, String gwPath, String indCd, Integer ledBarBrightness);
	
	/**
	 * 표시기 LED 소등 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param indCd
	 */
	public void requestLedOff(Long domainId, String stageCd, String gwPath, String indCd);
	
	/**
	 * 표시기 LED 리스트 점등 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param indList
	 * @param ledBrightness
	 */
	public void requestLedListOn(Long domainId, String stageCd, List<IndCommonReq> indList, Integer ledBrightness);
	
	/**
	 * 표시기 LED 리스트 소등 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param indList
	 */
	public void requestLedListOff(Long domainId, String stageCd, List<IndCommonReq> indList);
	
	/**********************************************************************
	 * 					5. 표시기 교체 / 게이트웨이, 표시기 펌웨어 배포
	 **********************************************************************/
	
	/**
	 * 펌웨어 배포 요청
	 * 
	 * @param deployment
	 */
	public void deployFirmware(Deployment deployment);
	
	/**
	 * 게이트웨이에 게이트웨이 펌웨어 배포 정보 전송 
	 * 
	 * @parma domainId
	 * @param stageCd
	 * @param gwChannel 게이트웨이 구분 채널 
	 * @param gwVersion 게이트웨이 펌웨어 버전 
	 * @param gwFwDownloadUrl 게이트웨이 펌웨어 다운로드 URL
	 * @param filename 파일명
	 * @param forceFlag 강제 업데이트 여부
	 */
	public void deployGwFirmware(Long domainId, String stageCd, String gwChannel, String gwVersion, String gwFwDownloadUrl, String filename, Boolean forceFlag);
	
	/**
	 * 게이트웨이에 표시기 펌웨어 배포 정보 전송 
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwChannel 게이트웨이 구분 채널
	 * @param indVersion 표시기 펌웨어 버전 
	 * @param indFwDownloadUrl 표시기 펌웨어 다운로드 URL
	 * @param filename 파일명
	 * @param forceFlag 강제 업데이트 여부
	 */
	public void deployIndFirmware(Long domainId, String stageCd, String gwChannel, String indVersion, String indFwDownloadUrl, String filename, Boolean forceFlag);
	
	/**
	 * 표시기 교체
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param gwPath
	 * @param fromIndCd
	 * @param toIndCd
	 */
	public void changeIndicator(Long domainId, String stageCd, String gwPath, String fromIndCd, String toIndCd);

}
