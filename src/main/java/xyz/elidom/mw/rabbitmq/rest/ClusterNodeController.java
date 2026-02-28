package xyz.elidom.mw.rabbitmq.rest;


import java.util.ArrayList;
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

import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.mw.rabbitmq.service.BrokerAdminService;
import xyz.elidom.mw.rabbitmq.service.ServiceUtil;
import xyz.elidom.mw.rabbitmq.service.model.Node;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * RabbitMQ 클러스터 조회 컨트롤러
 * 
 * @author yang
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/cluster")
@ServiceDesc(description = "Mq Cluster Node Service API")
public class ClusterNodeController extends AbstractRestService {
	
	@Autowired
	private BrokerAdminService adminServie;
	
	@Override
	protected Class<?> entityClass() {
		return Node.class;
	}
	
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {

		page =  page == null ? 1 : page.intValue();
		limit = (limit == null) ? ValueUtil.toInteger(SettingUtil.getValue(SysConfigConstants.SCREEN_PAGE_LIMIT, "50")) : limit.intValue();
		
		Filter[] querys = new Filter[0];
		String name = SysConstants.EMPTY_STRING;

		if (ValueUtil.isNotEmpty(query)) querys = FormatUtil.jsonToObject(query, Filter[].class);
		for(Filter filter : querys) {
			if(filter.getName().equalsIgnoreCase("name")) {
				name = filter.getValue().toString().toLowerCase();
			}
		}
		
		// node 전체 리스트 조회 
		Node[] nodes = this.adminServie.getClusterNodeList();
		List<Node> resultList = new ArrayList<Node>();
		
		// node 명 조회 조건이 있으면 loop filter 
		for(Node node : nodes) {
			String nodeName = node.getName();
			String[] nodeNameSplits = nodeName.split("@")[0].split("-");
			node.setName(nodeNameSplits[0]);
			
			if(node.getName().toLowerCase().contains(name)) {
				node.setMemUsedStr(ServiceUtil.valueToByteString(node.getMemUsed()));
				resultList.add(node);
			}
		}
		
		Page<Node> result = new Page<Node>();
		result.setTotalSize(nodes.length);
		result.setList(resultList);
		return result;
	}
}