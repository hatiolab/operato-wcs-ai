package operato.logis.light.entity;

import java.util.List;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Ignore;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.util.ValueUtil;

/**
 * 표시기 설정 셋
 * 
 * @author shortstop
 */
@Table(name = "ind_config_set", idStrategy = GenerationRule.UUID, indexes = {
	@Index(name = "ix_ind_config_set_0", columnList = "domain_id,config_set_cd", unique = true)
})
public class IndConfigSet extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 854265517216781150L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "config_set_cd", nullable = false, length = 30)
	private String configSetCd;
	
	@Column (name = "config_set_nm", nullable = false, length = 100)
	private String configSetNm;
	
	@Column (name = "ind_type", length = 20)
	private String indType;

	@Column (name = "default_flag", length = 1)
	private Boolean defaultFlag;

	@Ignore
	private List<IndConfig> items;
  
	/**
	 * key로 값을 찾아 리턴
	 * 
	 * @param key
	 * @return
	 */
	public String findValue(String key) {
		if(ValueUtil.isNotEmpty(this.items)) {
			for(IndConfig item : this.items) {
				if(ValueUtil.isEqualIgnoreCase(key, item.getName())) {
					return item.getValue();
				}
			}
		}
		
		return null;
	}
	
	/**
	 * key로 값을 찾아 리턴
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public String findValue(String key, String defaultValue) {
		String value = this.findValue(key);
		return ValueUtil.isEmpty(value) ? defaultValue : value;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getConfigSetCd() {
		return configSetCd;
	}

	public void setConfigSetCd(String configSetCd) {
		this.configSetCd = configSetCd;
	}

	public String getConfigSetNm() {
		return configSetNm;
	}

	public void setConfigSetNm(String configSetNm) {
		this.configSetNm = configSetNm;
	}

	public String getIndType() {
		return indType;
	}

	public void setIndType(String indType) {
		this.indType = indType;
	}

	public Boolean getDefaultFlag() {
		return defaultFlag;
	}

	public void setDefaultFlag(Boolean defaultFlag) {
		this.defaultFlag = defaultFlag;
	}

	public List<IndConfig> getItems() {
		return items;
	}

	public void setItems(List<IndConfig> items) {
		this.items = items;
	}

}
