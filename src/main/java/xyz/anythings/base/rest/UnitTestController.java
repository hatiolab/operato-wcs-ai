package xyz.anythings.base.rest;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.anythings.base.util.LogisBaseUtil;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.util.ValueUtil;

/**
 * 단위 테스트 용 
 * 
 * @author shortstop
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/unit_test")
@ServiceDesc(description="Unit Test")
public class UnitTestController {

	@RequestMapping(value = "/current_time", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "데이터베이스 기준 현재 시간 조회")
	public Map<String, Object> currentTime() {
		return ValueUtil.newMap("currentTime", LogisBaseUtil.currentDbTime());
	}
}
