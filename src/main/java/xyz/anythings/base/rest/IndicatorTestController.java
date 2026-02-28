package xyz.anythings.base.rest;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.Cell;
import xyz.anythings.base.entity.JobInstance;
import xyz.anythings.base.service.util.RuntimeIndServiceUtil;
import xyz.anythings.gw.GwConstants;
import xyz.anythings.gw.service.IndicatorDispatcher;
import xyz.anythings.gw.service.api.IIndRequestService;
import xyz.anythings.gw.service.model.IIndOnInfo;
import xyz.anythings.gw.service.model.IndCommonReq;
import xyz.anythings.gw.service.model.IndTest;
import xyz.anythings.gw.service.model.IndTest.IndAction;
import xyz.anythings.gw.service.model.IndTest.IndTarget;
import xyz.anythings.gw.service.mq.model.Action;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/indicator_test")
@ServiceDesc(description = "Indicator Test Service API")
public class IndicatorTestController {
	
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	private IQueryManager queryManager;

	/**
	 * 표시기 서비스를 요청
	 * 
	 * @return
	 */
	public IIndRequestService getIndicatorRequestService(IndTest indTest) {
		return BeanUtil.get(IndicatorDispatcher.class).getIndicatorRequestService(indTest.getIndConfigSet().getIndType());
	}
	
	@RequestMapping(value = "/unit_test", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Indicator Unit Test")
	public Map<String, Object> unitTest(@RequestBody IndTest indTest) {
		indTest.setIndConfigSetId(indTest.getIndConfigSetId());
		String action = indTest.getAction().getAction();
		String sendMsg = null;
		boolean success = true;
		
		if(ValueUtil.isEqualIgnoreCase(action, Action.Values.IndicatorOnRequest)) {
			sendMsg = this.testOn(indTest);
			
		} else if (ValueUtil.isEqualIgnoreCase(action, Action.Values.IndicatorOffRequest)) {
			sendMsg = this.testOff(indTest);
			
		} else if(ValueUtil.isEqualIgnoreCase(action, Action.Values.LedOnRequest)) {
			sendMsg = this.testLedOn(indTest);
			
		} else if(ValueUtil.isEqualIgnoreCase(action, Action.Values.LedOffRequest)) {
			sendMsg = this.testLedOff(indTest);
			
		} else {
			success = false;
			sendMsg = "Invalid action!";
		}
		
		return ValueUtil.newMap("success,send_msg", success, sendMsg);
	}

	/**
	 * 표시기 점등 테스트
	 * 
	 * @param indTest
	 * @return
	 */
	private String testOn(IndTest indTest) {
		Map<String, List<IIndOnInfo>> indOnList = this.createIndOnInfoList(indTest);
		
		if(ValueUtil.isNotEmpty(indOnList)) {
			return this.indicatorOnByInfo(Domain.currentDomainId(), indTest, indOnList);
		} else {
			// 점등할 정보가 없습니다.
			return ThrowUtil.notFoundRecordMsg("terms.button.on");
		}
	}
	
	/**
	 * 표시기 소등 테스트
	 * 
	 * @param indTest
	 * @return
	 */
	private String testOff(IndTest indTest) {
		List<IndCommonReq> indOffList = this.createIndOffInfoList(indTest);
		String msg = null;
		
		if(ValueUtil.isNotEmpty(indOffList)) {
			msg = FormatUtil.toUnderScoreJsonString(indOffList);
			boolean forceFlag = (indTest.getAction().getForceFlag() == null) ? false : indTest.getAction().getForceFlag().booleanValue();
			this.getIndicatorRequestService(indTest).requestIndListOff(Domain.currentDomainId(), indTest.getIndConfigSet().getStageCd(), indOffList, forceFlag);
		} else {
			// 소등할 정보가 없습니다
			msg = ThrowUtil.notFoundRecordMsg("terms.button.off");
		}
		
		return msg;
	}
	
	/**
	 * LED 바 점등 테스트
	 * 
	 * @param indTest
	 * @return
	 */
	private String testLedOn(IndTest indTest) {
		List<IndCommonReq> ledOnList = this.createIndOffInfoList(indTest);
		
		String msg = null;
		
		if(ValueUtil.isNotEmpty(ledOnList)) {
			msg = FormatUtil.toUnderScoreJsonString(ledOnList);
			this.getIndicatorRequestService(indTest).requestLedListOn(Domain.currentDomainId(), indTest.getIndConfigSet().getStageCd(), ledOnList, 10);
		} else {
			// 점등할 정보가 없습니다
			msg = ThrowUtil.notFoundRecordMsg("terms.button.on");
		}
		
		return msg;
	}
	
	/**
	 * LED 바 소등 테스트
	 * 
	 * @param indTest
	 * @return
	 */
	private String testLedOff(IndTest indTest) {
		List<IndCommonReq> ledOffList = this.createIndOffInfoList(indTest);
		
		String msg = null;
		
		if(ValueUtil.isNotEmpty(ledOffList)) {
			msg = FormatUtil.toUnderScoreJsonString(ledOffList);
			this.getIndicatorRequestService(indTest).requestLedListOff(Domain.currentDomainId(), indTest.getIndConfigSet().getStageCd(), ledOffList);
		} else {
			// 소등할 정보가 없습니다
			msg = ThrowUtil.notFoundRecordMsg("terms.button.off");
		}
		
		return msg;
	}
	
	/**
	 * 표시기 점등 ...
	 * 
	 * @param domainId
	 * @param indTest
	 * @param indOnList
	 * @return
	 */
	private String indicatorOnByInfo(Long domainId, IndTest indTest, Map<String, List<IIndOnInfo>> indOnInfo) {
		String actionType = indTest.getAction().getActionType();
		String jobType = indTest.getJobType();
		String stageCd = indTest.getIndConfigSet().getStageCd();
		
		if(ValueUtil.isEqualIgnoreCase(actionType, GwConstants.IND_ACTION_TYPE_PICK)) {
			this.getIndicatorRequestService(indTest).requestIndListOn(domainId, stageCd, jobType, actionType, indOnInfo);
			return FormatUtil.toUnderScoreJsonString(indOnInfo);
			
		} else {
			if(this.indicatorOn(domainId, indTest, indOnInfo)) {
				return FormatUtil.toUnderScoreJsonString(indOnInfo);
			} else {
				return null;
			}
		}
	}
	
	/**
	 * 표시기 점등
	 * 
	 * @param domainId
	 * @param stageCd
	 * @param actionType
	 * @param jobType
	 * @param indOnInfo
	 */
	private boolean indicatorOn(Long domainId, IndTest indTest, Map<String, List<IIndOnInfo>> indOnInfo) {
		IIndRequestService indReqSvc = this.getIndicatorRequestService(indTest);
		
		String actionType = indTest.getAction().getActionType();
		String jobType = indTest.getJobType();
		String stageCd = indTest.getIndConfigSet().getStageCd();
		
		Iterator<String> indOnIter = indOnInfo.keySet().iterator();
		int count = 0;
		
		while(indOnIter.hasNext()) {
			String gwPath = indOnIter.next();
			List<IIndOnInfo> infoList = indOnInfo.get(gwPath);
			
			for(IIndOnInfo info : infoList) {
				String indCd = info.getId();
				
				switch(actionType) {
					case "ind_cd" : {
					}
					
					case "cell_cd" : {
					}
					
					case GwConstants.IND_ACTION_TYPE_STR_SHOW : {
						indReqSvc.requestShowString(domainId, stageCd, jobType, gwPath, indCd, indCd, info.getViewStr());
						count++;
						break;
					}
					
					case GwConstants.IND_BIZ_FLAG_FULL : {
						indReqSvc.requestFullbox(domainId, stageCd, jobType, gwPath, indCd, indCd, info.getColor());
						count++;
						break;
					}
					
					case GwConstants.IND_BIZ_FLAG_END : {
						indReqSvc.requestIndEndDisplay(domainId, stageCd, jobType, gwPath, indCd, indCd, false);
						count++;
						break;
					}
					
					case GwConstants.IND_ACTION_TYPE_NOBOX : {
						indReqSvc.requestIndNoBoxDisplay(domainId, stageCd, jobType, gwPath, indCd);
						count++;
						break;
					}
					
					case GwConstants.IND_ACTION_TYPE_ERRBOX : {
						indReqSvc.requestIndErrBoxDisplay(domainId, stageCd, jobType, gwPath, indCd);
						count++;
						break;
					}
					
					case GwConstants.IND_ACTION_TYPE_DISPLAY : {
						indReqSvc.requestDisplayBothDirectionQty(domainId, stageCd, jobType, gwPath, indCd, indCd, info.getOrgRelay(), info.getOrgEaQty());
						count++;
						break;
					}
				}
			}
		}
		
		return count > 0;
	}
	
	/**
	 * 표시기 점등을 위한 모델을 생성한다.
	 * 
	 * @param indTest
	 * @return
	 */
	private Map<String, List<IIndOnInfo>> createIndOnInfoList(IndTest indTest) {
		IndAction action = indTest.getAction();
		
		switch(action.getActionType()) {
			case GwConstants.IND_ACTION_TYPE_PICK : {
				return this.createIndOnIndInfoList(indTest);
			}
			
			case GwConstants.IND_BIZ_FLAG_FULL : {
				return this.createIndOnIndInfoList(indTest);
			}
			
			case GwConstants.IND_BIZ_FLAG_END : {
				return this.createIndOnIndInfoList(indTest);
			}
			
			case GwConstants.IND_ACTION_TYPE_NOBOX : {
				return this.createIndOnIndInfoList(indTest);
			}
			
			case GwConstants.IND_ACTION_TYPE_ERRBOX : {
				return this.createIndOnIndInfoList(indTest);
			}
			
			case GwConstants.IND_ACTION_TYPE_DISPLAY : {
				return this.createIndOnDisplayInfoList(indTest);
			}
			
			case GwConstants.IND_ACTION_TYPE_STR_SHOW : {
				return this.createIndOnShowStrInfoList(indTest);
			}
			
			case "ind_cd" : {
				return this.createIndOnShowStrInfoList(indTest);
			}
			
			case "cell_cd" : {
				return this.createIndOnShowStrInfoList(indTest);
			}
		}
		
		return null;
	}
	
	/**
	 * 표시기 점등 (피킹)을 위한 모델을 생성
	 * 
	 * @param indTest
	 * @return
	 */
	private Map<String, List<IIndOnInfo>> createIndOnIndInfoList(IndTest indTest) {
		IndAction action = indTest.getAction();
		IndTarget target = indTest.getTarget();
		
		String btnColor = action.getBtnColor();
		Integer firstQty = ValueUtil.toInteger(action.getFirstQty());
		Integer secondQty = ValueUtil.toInteger(action.getSecondQty());
		
		Map<String, Object> params = ValueUtil.newMap(target.getTargetType(), target.getTargetIdList());
		params.put("domainId", Domain.currentDomainId());
		params.put("activeFlag", true);
		List<JobInstance> jobList = this.queryManager.selectListBySql(this.getIndOnQuery(), params, JobInstance.class, 0, 0);
		
		for(JobInstance job : jobList) {
			job.setId(UUID.randomUUID().toString());
			job.setColorCd(btnColor);
			job.setBoxInQty(1);
			job.setInputSeq(firstQty);
			job.setPickQty(secondQty);
			job.setPickedQty(0);
		}
		
		return RuntimeIndServiceUtil.buildTestIndOnList(indTest.getIndConfigSet().getDomainId(), indTest.getIndConfigSet().getStageCd(), LogisConstants.JOB_TYPE_DAS, jobList);
	}
	
	/**
	 * 표시기 점등 (문자열 표시)을 위한 모델을 생성
	 * 
	 * @param indTest
	 * @return
	 */
	private Map<String, List<IIndOnInfo>> createIndOnShowStrInfoList(IndTest indTest) {
		Long domainId = Domain.currentDomainId();
		Map<String, List<IIndOnInfo>> indOnInfoMap = this.createIndOnIndInfoList(indTest);
		Iterator<List<IIndOnInfo>> valueIter = indOnInfoMap.values().iterator();
		IndAction action = indTest.getAction();
		
		while(valueIter.hasNext()) {
			List<IIndOnInfo> indOnInfoList = valueIter.next();
			
			for(IIndOnInfo indOnInfo : indOnInfoList) {
				String viewStr = "";
				
				if(ValueUtil.isEqualIgnoreCase(action.getActionType(), "ind_cd")) {
					viewStr = indOnInfo.getId();
							
				} else if(ValueUtil.isEqualIgnoreCase(action.getActionType(), "cell_cd")) {
					Cell cell = AnyEntityUtil.findEntityByCode(domainId, true, Cell.class, "indCd", indOnInfo.getId());
					
					if(cell != null) {
						// 영문을 제외한 최대 6개 문자로 변환 ...
						String cellCd = cell.getCellCd();
						for(int i = 0 ; i < cellCd.length() ; i++) {
							Character ch = cellCd.charAt(i);
							if(Character.isDigit(ch)) {
								viewStr += ch;
							}
						}
						
						if(viewStr.length() < 6) {
							viewStr = StringUtils.leftPad(viewStr, 6);
						}
						
					} else {
						viewStr = "======";
					}
					
				} else {
					String firstData = ValueUtil.toNotNull(action.getFirstQty());
					String secondData = ValueUtil.toNotNull(action.getSecondQty());	
					viewStr = firstData + secondData;
				}
				
				indOnInfo.setViewStr(viewStr);
			}
		}
		
		return indOnInfoMap;
	}
	
	/**
	 * 표시기 점등 (좌측 문자열, 우측 숫자 표시)을 위한 모델을 생성
	 * 
	 * @param indTest
	 * @return
	 */
	private Map<String, List<IIndOnInfo>> createIndOnDisplayInfoList(IndTest indTest) {
		Map<String, List<IIndOnInfo>> indOnInfoMap = this.createIndOnIndInfoList(indTest);
		Iterator<List<IIndOnInfo>> valueIter = indOnInfoMap.values().iterator();
		
		IndAction action = indTest.getAction();
		Integer firstQty = ValueUtil.toInteger(action.getFirstQty());
		Integer secondQty = ValueUtil.toInteger(action.getSecondQty());
		
		while(valueIter.hasNext()) {
			List<IIndOnInfo> indOnInfoList = valueIter.next();
			for(IIndOnInfo indOnInfo : indOnInfoList) {
				indOnInfo.setOrgRelay(firstQty);
				indOnInfo.setOrgEaQty(secondQty);
			}
		}
		
		return indOnInfoMap;
	}
	
	/**
	 * 표시기 소등을 위한 모델을 생성한다.
	 * 
	 * @param indTest
	 * @return
	 */
	private List<IndCommonReq> createIndOffInfoList(IndTest indTest) {
		IndTarget target = indTest.getTarget();
		Map<String, Object> params = ValueUtil.newMap(target.getTargetType(), target.getTargetIdList());
		params.put("domainId", Domain.currentDomainId());
		params.put("activeFlag", true);
		return this.queryManager.selectListBySql(this.getIndOffQuery(), params, IndCommonReq.class, 0, 0);
	}
	
	/**
	 * 표시기 리스트 조회를 위한 쿼리
	 * 
	 * @return
	 */
	private String getIndOnQuery() {
		StringJoiner sql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		return 
		sql.add("SELECT")
		   .add("	GATE.domain_id, GATE.gw_nm as gw_path, CELL.ind_cd, CELL.cell_cd")
		   .add("FROM")
		   .add("	CELLS CELL")
		   .add("	INNER JOIN INDICATORS IND ON CELL.DOMAIN_ID = IND.DOMAIN_ID AND CELL.IND_CD = IND.IND_CD")
		   .add("	INNER JOIN GATEWAYS GATE ON CELL.DOMAIN_ID = GATE.DOMAIN_ID AND IND.GW_CD = GATE.GW_CD")
		   .add("WHERE")
		   .add("	CELL.DOMAIN_ID = :domainId")
		   .add("	AND CELL.ACTIVE_FLAG = :activeFlag")
		   .add("	#if($rack)")
		   .add("	AND CELL.EQUIP_CD in (:rack)")
		   .add("	#end")
		   .add("	#if($gateway)")
		   .add("	AND GATE.GW_CD in (:gateway)")
		   .add("	#end")
		   .add("	#if($equip_zone)")
		   .add("	AND CELL.EQUIP_ZONE in (:equip_zone)")
		   .add("	#end")
		   .add("	#if($work_zone)")
		   .add("	AND CELL.STATION_CD in (:work_zone)")
		   .add("	#end")
		   .add("	#if($cell)")
		   .add("	AND CELL.CELL_CD in (:cell)")
		   .add("	#end")
		   .add("	#if($indicator)")
		   .add("	AND CELL.IND_CD in (:indicator)")
		   .add("	#end")
		   .add("GROUP BY")
		   .add("	GATE.DOMAIN_ID, GATE.GW_NM, CELL.IND_CD, CELL.CELL_CD")
		   .add("ORDER BY")
		   .add("	GATE.GW_NM, CELL.CELL_CD").toString();
	}
	
	/**
	 * 표시기 소등을 위한 리스트 조회를 위한 쿼리
	 * 
	 * @return
	 */
	private String getIndOffQuery() {
		StringJoiner sql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		return 
		sql.add("SELECT")
		   .add("	GATE.gw_nm as gw_path, IND.ind_cd, CELL.cell_cd")
		   .add("FROM")
		   .add("	CELLS CELL")
		   .add("	INNER JOIN INDICATORS IND ON CELL.DOMAIN_ID = IND.DOMAIN_ID AND CELL.IND_CD = IND.IND_CD")
		   .add("	INNER JOIN GATEWAYS GATE ON IND.DOMAIN_ID = GATE.DOMAIN_ID AND IND.GW_CD = GATE.GW_CD")
		   .add("WHERE")
		   .add("	CELL.DOMAIN_ID = :domainId")
		   .add("	AND CELL.ACTIVE_FLAG = :activeFlag")
		   .add("	#if($rack)")
		   .add("	AND CELL.EQUIP_CD in (:rack)")
		   .add("	#end")
		   .add("	#if($gateway)")
		   .add("	AND GATE.GW_CD in (:gateway)")
		   .add("	#end")
		   .add("	#if($equip_zone)")
		   .add("	AND CELL.EQUIP_ZONE in (:equip_zone)")
		   .add("	#end")
		   .add("	#if($work_zone)")
		   .add("	AND CELL.STATION_CD in (:work_zone)")
		   .add("	#end")
		   .add("	#if($cell)")
		   .add("	AND CELL.CELL_CD in (:cell)")
		   .add("	#end")
		   .add("	#if($indicator)")
		   .add("	AND IND.IND_CD in (:indicator)")
		   .add("	#end")
		   .add("GROUP BY")
		   .add("	GATE.GW_NM, IND.IND_CD, CELL.CELL_CD")
		   .add("ORDER BY")
		   .add("	GATE.GW_NM, CELL.CELL_CD").toString();
	}

}
