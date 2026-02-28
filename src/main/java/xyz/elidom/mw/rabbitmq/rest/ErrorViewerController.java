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
import xyz.elidom.mw.rabbitmq.entity.TraceError;
import xyz.elidom.mw.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectQueryMaster;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTraceErrorResult;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTraceErrorResultSource;
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
 * 에러 로그 뷰어 컨트롤러
 * 
 * @author yang
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/rabbitmq/error")
@ServiceDesc(description = "Mq Error View Service API")
public class ErrorViewerController extends AbstractRestService {

	@Autowired
	private RabbitmqProperties properties;

	@Autowired
	private ElasticRestHandler elastic;

	@Override
	protected Class<?> entityClass() {
		return TraceError.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {

		page = page == null ? 1 : page.intValue();
		limit = (limit == null) ? ValueUtil.toInteger(SettingUtil.getValue(SysConfigConstants.SCREEN_PAGE_LIMIT, "50"))
				: limit.intValue();

		Filter[] querys = new Filter[0];
		if (ValueUtil.isNotEmpty(query)) {
			querys = FormatUtil.jsonToObject(query, Filter[].class);
		}

		String traceType = this.properties.getTraceType();

		if (traceType.equalsIgnoreCase("file")) {
			// file 타입에서는 지원 하지 않음
			throw new ElidomBadRequestException("MQ_TRACE_FILE_NOT_SUPPORT_VIEW");

		} else if (traceType.equalsIgnoreCase("db")) {
			// err_date 에 대한 범위 검색을 위해 조건 변경
			for (Filter filter : querys) {
				if (filter.getLeftOperand().equalsIgnoreCase("err_date")) {
					filter.setOperator("between");
					Date fromDate = DateUtil.parse(filter.getValue().toString(), DateUtil.getDateFormat());
					filter.addRightOperand(DateUtil.addDateToStr(fromDate, 1));
				}
			}

			String[] splitSelect = select.split(SysConstants.COMMA);
			// 조회 조건 set
			Query input = new Query();
			input.addFilter(querys);
			input.setPageIndex(page);
			input.setPageSize(limit);
			input.addOrder(new Order("err_date", false));
			input.addSelect(splitSelect);
			return this.queryManager.selectPage(this.entityClass(), input);

		} else if (traceType.equalsIgnoreCase("elastic")) {
			String elasticIndex = SysConstants.EMPTY_STRING;
			// 날짜 조회 조건 column 변경
			String[] splitSelect = select.replaceAll("err_date", "err_date_long").split(SysConstants.COMMA);
			List<Filter> queryFilter = new ArrayList<Filter>();

			// 조회 조건 중 elastic index get
			for (Filter f : querys) {
				if (f.getLeftOperand().equalsIgnoreCase("err_date") == false) {
					queryFilter.add(f);
				} else
					elasticIndex = f.getRightOperand().get(0).toString().replaceAll(SysConstants.DASH,
							SysConstants.EMPTY_STRING);
			}

			// 조회 조건 set
			SelectQueryMaster elasticQuery = new SelectQueryMaster();
			elasticQuery.setSourceParseUnderscore(splitSelect);
			elasticQuery.setFrom(limit * (page - 1));
			elasticQuery.setSize(limit);
			elasticQuery.addSort("err_date_long", long.class, false);
			elasticQuery.addFilter(new Filter("type", "eq", "trace_err"));
			elasticQuery.addFilters(queryFilter.toArray(new Filter[queryFilter.size()]));

			try {
				SelectTraceErrorResult elasticResult = this.elastic.searchTraceErrorList(elasticIndex, elasticQuery);
				boolean useErrDate = select.contains("err_date");
				List<TraceError> res = new ArrayList<TraceError>();

				// 에러 데이트에 대한 return 값 set
				for (SelectTraceErrorResultSource pub : elasticResult.getHits().getHits()) {
					if (useErrDate) {
						pub.get_source().setErrDate(new Date(pub.get_source().getErrDateLong()));
					}

					res.add(pub.get_source());
				}

				// 페이지 리턴값 생성
				Page<TraceError> result = new Page<TraceError>();
				result.setTotalSize(elasticResult.getHits().getTotal());
				result.setList(res);
				return result;
			} catch (Exception e) {
				throw new ElidomRuntimeException(e);
			}
		}

		return new Page<TraceError>();
	}

	/**
	 * 에러 메시지 상세 보기
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Error Message Detail View")
	public TraceError getMessageDetails(@PathVariable("id") String id) {
		String traceType = this.properties.getTraceType();

		if (traceType.equalsIgnoreCase("db")) {
			TraceError searchDetail = new TraceError();
			searchDetail.setId(id);
			return this.queryManager.select(searchDetail);

		} else if (traceType.equalsIgnoreCase("elastic")) {
			// 조회 조건 set
			SelectQueryMaster elasticQuery = new SelectQueryMaster();
			elasticQuery.setSize(10000);
			elasticQuery.addFilter(new Filter("id", "eq", id));
			elasticQuery.addFilter(new Filter("type", "eq", "trace_err"));

			try {
				// 조회
				SelectTraceErrorResult elasticResult = this.elastic.searchTraceErrorList(SysConstants.EMPTY_STRING,
						elasticQuery);

				for (SelectTraceErrorResultSource pub : elasticResult.getHits().getHits()) {
					pub.get_source().setErrDate(new Date(pub.get_source().getErrDateLong()));
					return pub.get_source();
				}
			} catch (Exception e) {
				throw new ElidomRuntimeException(e);
			}
		}

		return null;
	}
}