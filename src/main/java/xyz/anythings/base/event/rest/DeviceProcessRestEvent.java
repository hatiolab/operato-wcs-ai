package xyz.anythings.base.event.rest;

import java.util.Map;

import org.springframework.web.bind.annotation.RequestMethod;

import xyz.anythings.sys.event.model.SysRestEvent;
import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.util.ValueUtil;

/**
 * 디바이스 컨트롤러에 정의 되지 않은 
 * 각각 비즈니스 모델에서 처리되어야 하는 레스트 호출 이벤트 
 * @author yang
 *
 */
public class DeviceProcessRestEvent extends SysRestEvent{
	
	/**
	 * 작업 타입 
	 */
	public String jobType;

	/**
	 * 생성자 1
	 * 
	 * @param domainId
	 * @param jobType
	 * @param restPath
	 * @param requestMethod
	 */
	public DeviceProcessRestEvent(long domainId, String jobType, String restPath, RequestMethod requestMethod) {
		this(domainId, jobType, restPath, requestMethod, null);
	}
	
	/**
	 * 생성자 2
	 * 
	 * @param domainId
	 * @param jobType
	 * @param restPath
	 * @param requestMethod
	 * @param requestParams
	 */
	public DeviceProcessRestEvent(long domainId, String jobType, String restPath, RequestMethod requestMethod, Map<String, Object> requestParams) {
		super(domainId, restPath, requestMethod, requestParams);
		this.setJobType(jobType);
		this.setReturnResult(new BaseResponse(false,"NOT_EXECUTION"));
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}
	
	public boolean checkCondition(String restPath, String jobType) {
		if(!super.checkCondition(restPath)) {
			return false;
		} else {
			return ValueUtil.isEqualIgnoreCase(jobType, this.getJobType());
		}
	}

}