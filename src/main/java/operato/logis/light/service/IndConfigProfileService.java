package operato.logis.light.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.logis.light.entity.IndConfig;
import operato.logis.light.entity.IndConfigSet;
import xyz.anythings.sys.service.AbstractExecutionService;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.util.ValueUtil;

/**
 * IConfigSetService 구현
 * 
 * @author shortstop
 */
@Component
public class IndConfigProfileService extends AbstractExecutionService {
	
	/**
	 * 표시기 프로파일 셋 Copy Fields
	 */
	private static final String[] IND_CONFIG_SET_COPY_FIELDS = new String[] { "configSetCd", "configSetNm", "indType" };
	/**
	 * ConfigSet 
	 */
	private static final String[] CONFIG_COPY_FIELDS = new String[] { "category", "name", "description", "value", "remark"};
	
	/**
	 * 설정셋ID - 표시기 설정 셋
	 * or
	 * 게이트웨이 CD - 표시기 설정 셋 
	 */
	private Map<String, IndConfigSet> configProfiles = new HashMap<String, IndConfigSet>();
	
	public int buildConfigSet(Long domainId) {
		String sql = "select id,domain_id,conf_set_cd,conf_set_nm,ind_type from ind_config_set where domain_id = :domainId and default_flag = :defaultFlag ";
		List<IndConfigSet> confSetList = AnyEntityUtil.searchItems(domainId, false, IndConfigSet.class, sql, "domainId,defaultFlag", domainId, true);
		
		if(ValueUtil.isNotEmpty(confSetList)) {
			for(IndConfigSet confSet : confSetList) {
				this.addConfigSet(confSet);
			}
		}
		
		return confSetList.size();
	}

	public IndConfigSet addConfigSet(IndConfigSet configSet) {
		List<IndConfig> items = AnyEntityUtil.searchDetails(configSet.getDomainId(), IndConfig.class, "indConfigSetId", configSet.getId());
		configSet.setItems(items);
		this.configProfiles.put(configSet.getId(), configSet);
		return configSet;
	}
	
	public IndConfigSet getConfigSet(Long domainId, String configSetId) {
		return this.configProfiles.get(configSetId);
	}
	
	public String getConfigValue(Long domainId, String configSetId, String key) {
		IndConfigSet configSet = this.configProfiles.get(configSetId);
		return configSet == null ? null : configSet.findValue(key);
	}

	public String getConfigValue(Long domainId, String configSetId, String key, String defaultValue) {
		String value = this.getConfigValue(domainId, configSetId, key);
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}
	
	public void clearConfigSet(Long domainId, String configSetId) {
		this.configProfiles.remove(configSetId);
	}

	public IndConfigSet copyIndConfigSet(Long domainId, String templateConfigSetId, String targetSetCd, String targetSetNm) {
		// 1. templateConfigSetId로 템플릿 설정을 조회 
		IndConfigSet sourceSet = AnyEntityUtil.findEntityById(true, IndConfigSet.class, templateConfigSetId);
		IndConfigSet targetSet = AnyValueUtil.populate(sourceSet, new IndConfigSet(), IND_CONFIG_SET_COPY_FIELDS);
		targetSet.setConfigSetCd(targetSetCd);
		targetSet.setConfigSetNm(targetSetNm);
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
