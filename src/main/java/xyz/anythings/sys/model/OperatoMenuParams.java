package xyz.anythings.sys.model;

import java.util.List;
import java.util.Map;

import xyz.elidom.base.entity.MenuParam;
import xyz.elidom.util.ValueUtil;

/**
 * menu params 를 key (name) value 형식으로 변경 
 * @author yang
 *
 */
public class OperatoMenuParams{

	Map<String,Object> params = ValueUtil.newMap("");

	public OperatoMenuParams(List<MenuParam> params) {
		for(MenuParam param : params) {
			if(param.getName().endsWith("-btn-tap")) {
				this.params.put(param.getName(), ValueUtil.newMap("value,option", param.getValue(), param.getDescription()));
			} else {
				this.params.put(param.getName(), param.getValue());
			}
		}
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}
}
