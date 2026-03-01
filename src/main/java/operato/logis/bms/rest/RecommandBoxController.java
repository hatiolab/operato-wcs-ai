package operato.logis.bms.rest;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import operato.logis.bms.LogisBmsConstants;
import operato.logis.bms.model.BmsOrder;
import operato.logis.bms.model.BmsRequest;
import operato.logis.bms.service.RecommandBoxService;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/bms")
@ServiceDesc(description = "BMS Service API")
public class RecommandBoxController extends AbstractRestService {
	
	@Autowired
	RecommandBoxService recommandBoxService;

	@Override
	protected Class<?> entityClass() {
		return null;
	}
	
	@RequestMapping(value = "/recommand/etc", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Recommand Box API for Etc.")
	public Map<String, BmsOrder> multipleUpdate(@RequestBody BmsRequest bmsRequest) {
		return new TreeMap<String, BmsOrder>(recommandBoxService.recommandBox(bmsRequest, LogisBmsConstants.SPLIT_BY_VOLUME));
	}

	
}
