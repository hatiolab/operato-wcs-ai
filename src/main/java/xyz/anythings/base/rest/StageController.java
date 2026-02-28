package xyz.anythings.base.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.anythings.base.entity.Stage;
import xyz.anythings.comm.rabbitmq.event.MwLogisQueueListEvent;
import xyz.anythings.comm.rabbitmq.event.MwQueueManageEvent;
import xyz.anythings.comm.rabbitmq.event.model.IQueueNameModel;
import xyz.anythings.comm.rabbitmq.event.model.LogisQueueNameModel;
import xyz.anythings.sys.event.EventPublisher;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/stages")
@ServiceDesc(description = "Stage Service API")
public class StageController extends AbstractRestService {

	@Autowired
	private EventPublisher eventPublisher;

	@Override
	protected Class<?> entityClass() {
		return Stage.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public Stage findOne(@PathVariable("id") String id) {
		return this.getOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public Stage create(@RequestBody Stage input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Stage update(@PathVariable("id") String id, @RequestBody Stage input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Stage> list) {
		//스테이지 정보 변경 분에 대하여 mw에 반영 
		this.setMwQueueList(list);
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	/**
	 * 스테이지 정보 변경 분에 대해 mw 반영 
	 * @param list
	 */
	private void setMwQueueList(List<Stage> list) {
		// 현재 도메인 검색 
		Domain domain = Domain.currentDomain();
		
		// 도메인에 siteCd 가 있을 경우에만 MW 이벤트 발생 
		if(ValueUtil.isNotEmpty(domain.getMwSiteCd())) {
			// 스테이지 내용 변경 분에 대한 rabbitmq 에서의 이벤트 처리 리스트 
			List<IQueueNameModel> queueModels = new ArrayList<IQueueNameModel>();
			
			for(Stage stage : list) {
				// Update 는 기존 Queue 이름에 대한 조회가 필요 
				if(stage.getCudFlag_().equalsIgnoreCase("u")) {
					Stage befStage = this.findOne(stage.getId());
					
					// StageCd 가 변경 되면 이벤트 발생  
					if(ValueUtil.isNotEqual(befStage.getStageCd(), stage.getStageCd())) {
						queueModels.add(new LogisQueueNameModel(domain.getId(), stage.getCudFlag_(), domain.getMwSiteCd(),befStage.getStageCd() , stage.getStageCd()));
					}
					
				} else {
					queueModels.add(new LogisQueueNameModel(domain.getId(), stage.getCudFlag_(), domain.getMwSiteCd(), null, stage.getStageCd()));
				}
			}
			
			if(queueModels.size() > 0 ) {
				MwQueueManageEvent event = new MwQueueManageEvent(domain.getId(), queueModels);
				eventPublisher.publishEvent(event);
			}
		}		
	}
	
	
	@EventListener(condition = "#event.isExecuted() == false")
	@Order(Ordered.LOWEST_PRECEDENCE)
	public void getRabbitMqVhostQueueList(MwLogisQueueListEvent event) {
		
		String qry = "select stage_cd, domain_id from stages where 1 = 1 #if($domainId) and domain_id = :domainId #end";
		
		Map<String,Object> params = ValueUtil.newMap("1","1");
		
		// domainId 가 0 이 아니면 조회 조건 설정 
		if(event.getDomainId() != 0) {
			params.put("domainId", event.getDomainId());
		}
		
		// 조회
		List<LogisQueueNameModel> result = this.queryManager.selectListBySql(qry, params, LogisQueueNameModel.class, 0, 0);
		event.setInitQueueNames(result);
		event.setExecuted(true);
	}
}