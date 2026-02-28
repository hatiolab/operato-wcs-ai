package xyz.elidom.mw.rabbitmq.rest;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Order;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.client.ElidomBadRequestException;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.entity.TraceDead;
import xyz.elidom.mw.rabbitmq.entity.TraceDeliver;
import xyz.elidom.mw.rabbitmq.entity.TracePublish;
import xyz.elidom.mw.rabbitmq.model.TraceDeliverResult;
import xyz.elidom.mw.rabbitmq.model.TraceDetailView;
import xyz.elidom.mw.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectQueryMaster;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTraceDeadResult;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTraceDeadResultSource;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTracePubResult;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTracePubResultSource;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTraceSubResult;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTraceSubResultSource;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 메시지 트레이스 뷰어 컨트롤러
 * 
 * @author yang
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/trace")
@ServiceDesc(description = "Mq Message Trace View Service API")
public class TraceViewerController extends AbstractRestService {
	/**
	 * RabbitMQ 프로퍼티
	 */
	@Autowired
	private RabbitmqProperties properties;
	/**
	 * ElasticSearch Handler
	 */
	@Autowired
	private ElasticRestHandler elastic;
	
	@Override
	protected Class<?> entityClass() {
		return TracePublish.class;
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
		if (ValueUtil.isNotEmpty(query)) {
			querys = FormatUtil.jsonToObject(query, Filter[].class);
		}
		
		Order[] orders = new Order[0];
		if (ValueUtil.isNotEmpty(sort)) {
			orders = FormatUtil.jsonToObject(sort, Order[].class);
		}
		
		String traceType = this.properties.getTraceType();
		if(traceType.equalsIgnoreCase("file")) {
			// 파일 일때는 기능 지원 안함.
			throw new ElidomBadRequestException("MQ_TRACE_FILE_NOT_SUPPORT_VIEW");
			
		} else if(traceType.equalsIgnoreCase("db")) {
			// 날짜 범위 검색을 위해 검색 조건 가공 
			for(Filter filter : querys) {
				if(filter.getLeftOperand().equalsIgnoreCase("pub_time")) {
					filter.setOperator("between");
					Date fromDate = DateUtil.parse(filter.getValue().toString(), DateUtil.getDateFormat());
					filter.addRightOperand(DateUtil.addDateToStr(fromDate, 1));
				}
			}
			
			String[] splitSelect = select.split(SysConstants.COMMA);
			Query input = new Query();
			input.addSelect(splitSelect);
			input.addFilter(querys);
			input.setPageIndex(page);
			input.setPageSize(limit);
			
			if(orders.length == 0) {
				input.addOrder(new Order("log_time", false));
			} else {
				input.addOrder(orders);
			}
			
			return this.queryManager.selectPage(this.entityClass(), input);
			
		} else if(traceType.equalsIgnoreCase("elastic")) {
			String elasticIndex = SysConstants.EMPTY_STRING;
			// 날짜 조회 조건 column 변경 
			String[] splitSelect = select.replaceAll("pub_time", "pub_time_long").replaceAll("log_time", "log_time_long").split(SysConstants.COMMA);
			List<Filter> queryFilter = new ArrayList<Filter>();
			
			// 조회 조건 중 elastic index get 
			for(Filter f : querys) {
				if(!f.getLeftOperand().equalsIgnoreCase("pub_time")) {
					queryFilter.add(f);
				} else {
					elasticIndex = f.getRightOperand().get(0).toString().replaceAll(SysConstants.DASH, SysConstants.EMPTY_STRING);
				}
			}
			
			// 조회 조건 set
			SelectQueryMaster elasticQuery = new SelectQueryMaster();
			elasticQuery.setSourceParseUnderscore(splitSelect);
			elasticQuery.setFrom(limit * (page -1) );
			elasticQuery.setSize(limit);
			elasticQuery.addFilter(new Filter("type", "eq", "trace_pub"));
			elasticQuery.addFilters(queryFilter.toArray(new Filter[queryFilter.size()]));
			
			if(orders.length == 0) {
				elasticQuery.addSort("log_time_long", long.class, false);
			} else {
				for(Order order : orders) {
					if(order.getField().endsWith("time")) {
						elasticQuery.addSort(order.getField() + "_long", long.class, order.isAscending());
					} else {
						elasticQuery.addSort(order.getField(), String.class, order.isAscending());
					}
				}
			}
			
			try {
				SelectTracePubResult elasticResult = this.elastic.searchTracePublishList(elasticIndex, elasticQuery);
				boolean usePubTime = select.contains("pub_time");
				boolean useLogTime = select.contains("log_time");
				List<TracePublish> res = new ArrayList<TracePublish>();
				
				// 퍼블리시 시간, 로그시간 에 대한 return 값 set
				for(SelectTracePubResultSource pub : elasticResult.getHits().getHits()) {
					if(usePubTime) {
						pub.get_source().setPubTime(new Date(pub.get_source().getPubTimeLong()));
					}
					
					if(useLogTime) {
						pub.get_source().setLogTime(new Date(pub.get_source().getLogTimeLong()));
					}
					
					res.add(pub.get_source());
				}
				
				// 페이지 리턴값 생성 
				Page<TracePublish> result = new Page<TracePublish>();
				result.setTotalSize(elasticResult.getHits().getTotal());
				result.setList(res);
				return result;
			} catch (Exception e) {
				throw new ElidomRuntimeException(e);
			}
		}
		
		return new Page<TracePublish>();
	}
	
	/**
	 * 퍼블리시 메시지 상세 조회
	 * 
	 * @param id 메시지 아이디 
	 * @param source_id 발신자 아이디 
	 * @param dest_id 목적지 라우링 키 
	 * @return
	 */
	@RequestMapping(value="/{id}/{source_id}/{dest_id}/details", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Publish Message Detail View")
	public TraceDetailView getMessageDetails(@PathVariable("id") String id,
			@PathVariable("source_id") String source_id,
			@PathVariable("dest_id") String dest_id) {
		
		TraceDetailView viewer = new TraceDetailView();
		source_id = source_id.replaceAll("\\.", "/");
		dest_id = dest_id.replaceAll("\\.", "/");
		String traceType = this.properties.getTraceType();
		
		if(traceType.equalsIgnoreCase("db")) {
			TracePublish searchDetail = new TracePublish();
			searchDetail.setId(id);
			searchDetail.setSourceId(source_id);
			searchDetail.setDestId(dest_id);
			viewer.setDetail(this.queryManager.select(searchDetail));
			
			Query query = new Query();
			query.addFilter(new Filter("id", id));
			query.addFilter(new Filter("source_id", source_id));
			query.addOrder("log_time", true);
			List<TraceDeliverResult> list = new ArrayList<TraceDeliverResult>();
			
			for(TraceDeliver deliver : this.queryManager.selectList(TraceDeliver.class, query)) {
				list.add(new TraceDeliverResult(deliver));
			}
			
			for(TraceDead deliver : this.queryManager.selectList(TraceDead.class, query)) {
				list.add(new TraceDeliverResult(deliver));
			}
			
			viewer.setList(list);
			
		} else if(traceType.equalsIgnoreCase("elastic")) {
			Filter[] defFilter = new Filter[2] ;
			defFilter[0] = new Filter("id", "eq", id);
			defFilter[1] = new Filter("source_id", "eq", source_id);
			
			SelectQueryMaster elasticPubQuery = new SelectQueryMaster();
			elasticPubQuery.setSize(10000);
			elasticPubQuery.addFilter(new Filter("dest_id", "eq",dest_id));
			elasticPubQuery.addFilter(new Filter("type", "eq","trace_pub"));
			elasticPubQuery.addFilters(defFilter);
			
			SelectQueryMaster elasticSubQuery = new SelectQueryMaster();
			elasticSubQuery.setSize(10000);
			elasticSubQuery.addFilter(new Filter("type", "eq","trace_sub"));
			elasticSubQuery.addFilters(defFilter);
			
			SelectQueryMaster elasticDeadQuery = new SelectQueryMaster();
			elasticDeadQuery.setSize(10000);
			elasticDeadQuery.addFilter(new Filter("type", "eq","trace_dead"));
			elasticDeadQuery.addFilters(defFilter);
			
			try {
				SelectTracePubResult elasticPubResult = this.elastic.searchTracePublishList(SysConstants.EMPTY_STRING, elasticPubQuery);
				SelectTraceSubResult elasticSubResult = this.elastic.searchTraceSubList(SysConstants.EMPTY_STRING, elasticSubQuery);
				SelectTraceDeadResult elasticDeadResult = this.elastic.searchTraceDeadList(SysConstants.EMPTY_STRING, elasticDeadQuery);
				
				List<TracePublish> resPub = new ArrayList<TracePublish>();
				List<TraceDeliverResult> resDel = new ArrayList<TraceDeliverResult>();
				
				for(SelectTracePubResultSource pub : elasticPubResult.getHits().getHits()) {
					pub.get_source().setPubTime(new Date(pub.get_source().getPubTimeLong()));
					pub.get_source().setLogTime(new Date(pub.get_source().getLogTimeLong()));
					resPub.add(pub.get_source());
				}
				
				for(SelectTraceSubResultSource sub : elasticSubResult.getHits().getHits()) {
					sub.get_source().setLogTime(new Date(sub.get_source().getLogTimeLong()));
					resDel.add(new TraceDeliverResult(sub.get_source()));
				}
				
				for(SelectTraceDeadResultSource dead : elasticDeadResult.getHits().getHits()) {
					dead.get_source().setDeadTime(new Date(dead.get_source().getDeadTimeLong()));
					dead.get_source().setLogTime(new Date(dead.get_source().getLogTimeLong()));
					resDel.add(new TraceDeliverResult(dead.get_source()));
				}
				
				viewer.setDetail(resPub.get(0));
				viewer.setList(resDel);
			} catch (Exception e) {
				throw new ElidomRuntimeException(e);
			}
		}
		
		return viewer;
	}
}