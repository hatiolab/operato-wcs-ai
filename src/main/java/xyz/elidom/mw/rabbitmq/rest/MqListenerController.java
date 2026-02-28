package xyz.elidom.mw.rabbitmq.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.model.BrokerListenInfo;
import xyz.elidom.mw.rabbitmq.service.model.VirtualHost;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;

/**
 * MQ Listener 조회 컨트롤러
 * 
 * @author yang
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/listener")
@ServiceDesc(description = "Company Service API")
public class MqListenerController extends AbstractRestService {

	@Autowired
	private RabbitmqProperties mqProperties;

	@Override
	protected Class<?> entityClass() {
		return null;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public List<VirtualHost> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return null;
	}

	@RequestMapping(value = "/getConnInfo", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public BrokerListenInfo getBrokerConnInfo() {
		BrokerListenInfo info = new BrokerListenInfo();
		info.setIp(new String[] { this.mqProperties.getBrokerAddress() });
		info.setPort(new int[] { this.mqProperties.getBrokerPort() });
		return info;
	}
}