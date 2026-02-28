package xyz.anythings.gw.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import xyz.anythings.gw.entity.IndConfig;
import xyz.anythings.gw.entity.IndConfigSet;
import xyz.anythings.gw.service.api.IIndConfigProfileService;
import xyz.anythings.sys.service.AbstractExecutionService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.util.ValueUtil;

/**
 * IConfigSetService 구현
 * 
 * @author shortstop
 */
@Component
public class IndConfigProfileService extends AbstractExecutionService implements IIndConfigProfileService {
	
	/**
	 * 표시기 프로파일 셋 Copy Fields
	 */
	private static final String[] IND_CONFIG_SET_COPY_FIELDS = new String[] { "stageCd", "indType", "jobType", "equipType", "equipCd", "comCd", "confSetCd", "confSetNm", "remark" };
	/**
	 * ConfigSet 
	 */
	private static final String[] CONFIG_COPY_FIELDS = new String[] { "category", "name", "description", "value", "remark", "config" };
	
	/**
	 * 배치 ID - 표시기 설정 셋
	 */
	private Map<String, IndConfigSet> configProfiles = new HashMap<String, IndConfigSet>();
	
	/**
	 * 스테이지 키를 생성
	 * 
	 * @param domainId
	 * @param stageCd
	 * @return
	 */
	public String makeStageKey(Long domainId, String stageCd) {
		return domainId + SysConstants.DASH + stageCd;
	}
	
	@Override
	public int buildStageConfigSet(Long domainId) {
		String sql = "select id,domain_id,conf_set_cd,conf_set_nm,stage_cd,ind_type from ind_config_set where domain_id = :domainId and default_flag = :defaultFlag and stage_cd is not null and (equip_type is null or equip_type = '') and (equip_cd is null or equip_cd = '') and (job_type is null or job_type = '') and (com_cd is null or com_cd = '')";
		List<IndConfigSet> confSetList = AnyEntityUtil.searchItems(domainId, false, IndConfigSet.class, sql, "domainId,defaultFlag", domainId, true);
		
		if(ValueUtil.isNotEmpty(confSetList)) {
			for(IndConfigSet confSet : confSetList) {
				this.addStageConfigSet(confSet);
			}
		}
		
		return confSetList.size();
	}

	@Override
	public IndConfigSet addStageConfigSet(IndConfigSet configSet) {
		List<IndConfig> items = AnyEntityUtil.searchDetails(configSet.getDomainId(), IndConfig.class, "indConfigSetId", configSet.getId());
		configSet.setItems(items);
		String stageKey = this.makeStageKey(configSet.getDomainId(), configSet.getStageCd());
		this.configProfiles.put(stageKey, configSet);
		return configSet;
	}
	
	@Override
	public IndConfigSet getStageConfigSet(Long domainId, String stageCd) {
		String stageKey = this.makeStageKey(domainId, stageCd);
		return this.configProfiles.get(stageKey);
	}
	
	@Override
	public String getStageConfigValue(Long domainId, String stageCd, String key) {
		String stageKey = this.makeStageKey(domainId, stageCd);
		IndConfigSet configSet = this.configProfiles.get(stageKey);
		return configSet == null ? null : configSet.findValue(key);
	}

	@Override
	public String getStageConfigValue(Long domainId, String stageCd, String key, String defaultValue) {
		String value = this.getStageConfigValue(domainId, stageCd, key);
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}
	
	@Override
	public void clearStageConfigSet(Long domainId, String stageCd) {
		String stageKey = this.makeStageKey(domainId, stageCd);
		this.configProfiles.remove(stageKey);
	}
	
	@Override
	public IndConfigSet getConfigSet(String batchId) {
		return this.configProfiles.get(batchId);
	}
	
	@Override
	public IndConfigSet addConfigSet(String batchId, IndConfigSet configSet) {
		if(ValueUtil.isNotEmpty(configSet.getId())) {
			List<IndConfig> sourceItems = AnyEntityUtil.searchDetails(configSet.getDomainId(), IndConfig.class, "indConfigSetId", configSet.getId());
			configSet.setItems(sourceItems);
			this.configProfiles.put(batchId, configSet);
			return configSet;
		} else {
			throw new ElidomRuntimeException(MessageUtil.getMessage("BATCH_NOT_SET_INDICATOR_PROFILE","작업 배치 [{0}]에 표시기 설정 프로파일이 설정되지 않았습니다.",ValueUtil.newStringList(batchId)));
		}
	}

	@Override
	public String getConfigValue(String batchId, String key) {
		IndConfigSet configSet = this.configProfiles.get(batchId);
		return configSet == null ? null : configSet.findValue(key);
	}

	@Override
	public String getConfigValue(String batchId, String key, String defaultValue) {
		String value = this.getConfigValue(batchId, key);
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}

	@Override
	public void clearConfigSet(String batchId) {
		this.configProfiles.remove(batchId);
	}
	
	@Override
	public IndConfigSet copyIndConfigSet(Long domainId, String templateConfigSetId, String targetSetCd, String targetSetNm) {
		// 1. templateConfigSetId로 템플릿 설정을 조회 
		IndConfigSet sourceSet = AnyEntityUtil.findEntityById(true, IndConfigSet.class, templateConfigSetId);
		IndConfigSet targetSet = AnyValueUtil.populate(sourceSet, new IndConfigSet(), IND_CONFIG_SET_COPY_FIELDS);
		targetSet.setConfSetCd(targetSetCd);
		targetSet.setConfSetNm(targetSetNm);
		this.queryManager.insert(targetSet);
		// 2. 템플릿 설정 생성
		this.cloneIndConfigItems(sourceSet, targetSet);
		// 3. 복사한 JobConfigSet 리턴
		return targetSet;
	}

	/**
	 * sourceSet의 설정 항목을 targetSet의 설정 항목으로 복사
	 * 
	 * @param sourceSet
	 * @param targetSet
	 */
	protected void cloneIndConfigItems(IndConfigSet sourceSet, IndConfigSet targetSet) {
		List<IndConfig> sourceItems = AnyEntityUtil.searchDetails(sourceSet.getDomainId(), IndConfig.class, "indConfigSetId", sourceSet.getId());
		
		if(ValueUtil.isNotEmpty(sourceItems)) {
			List<IndConfig> targetItems = new ArrayList<IndConfig>(sourceItems.size());
						
			for(IndConfig sourceItem : sourceItems) {
				IndConfig targetItem = AnyValueUtil.populate(sourceItem, new IndConfig(), CONFIG_COPY_FIELDS);
				targetItem.setIndConfigSetId(targetSet.getId());
				targetItems.add(targetItem);
			}
			
			this.queryManager.insertBatch(targetItems);
			targetSet.setItems(targetItems);
		}
	}

}
