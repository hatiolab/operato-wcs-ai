package xyz.elidom.mw.rabbitmq.rest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
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
import xyz.elidom.exception.client.ElidomBadRequestException;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.mw.rabbitmq.service.model.elastic.RangeStatisticsQueryMaster;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTracePubChartResult;
import xyz.elidom.mw.rabbitmq.service.model.elastic.SelectTracePubChartResultBucket;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConfigConstants;
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
@RequestMapping("/rest/rabbitmq/chart")
@ServiceDesc(description = "Mq Message Chart View Service API")
public class TraceChartViewController extends AbstractRestService {

	@Autowired
	RabbitmqProperties properties;

	@Autowired
	ElasticRestHandler elastic;

	@Override
	protected Class<?> entityClass() {
		return Long.class;
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
		if (ValueUtil.isNotEmpty(query))
			querys = FormatUtil.jsonToObject(query, Filter[].class);

		// 파일 일때는 기능 지원 안함.
		if (properties.getTraceType().equalsIgnoreCase("file")) {
			throw new ElidomBadRequestException("MQ_TRACE_FILE_NOT_SUPPORT_VIEW", "");
		} else if (properties.getTraceType().equalsIgnoreCase("db")) {
			throw new ElidomBadRequestException("MQ_TRACE_NOT_SUPPORT_VIEW", "DB 는 차트 기능을 제공하지 않습니다.");
		} else if (properties.getTraceType().equalsIgnoreCase("elastic")) {

			String elasticIndex = "";

			List<Filter> queryFilter = new ArrayList<Filter>();
			// 조회 조건중 elastic index get
			for (Filter f : querys) {
				if (f.getLeftOperand().equalsIgnoreCase("pub_time") == false) {
					queryFilter.add(f);
				} else
					elasticIndex = f.getRightOperand().get(0).toString().replaceAll("-", "");
			}

			// 조회 조건 set
			RangeStatisticsQueryMaster elasticQuery = new RangeStatisticsQueryMaster();

			elasticQuery.addFilter(new Filter("type", "eq", "trace_pub"));
			elasticQuery.addFilters(queryFilter.toArray(new Filter[queryFilter.size()]));

			Date rangeStDate = DateUtil.parse(elasticIndex, "yyyyMMdd");
			List<Long> ranges = new ArrayList<Long>();
			ranges.add(rangeStDate.getTime());

			for (int i = 1; i <= 24; i++) {
				ranges.add(DateUtils.addHours(rangeStDate, i).getTime());
			}

			elasticQuery.setRanges("logTime", "logTimeLong", ranges);

			try {
				SelectTracePubChartResult elasticResult = this.elastic.searchTracePublishChart(elasticIndex,
						elasticQuery);

				Map<String, Map<String, List<SelectTracePubChartResultBucket>>> result = elasticResult
						.getAggregations();
				List<SelectTracePubChartResultBucket> buckets = new ArrayList<SelectTracePubChartResultBucket>();

				if (ValueUtil.isNotEmpty(result)) {
					Map<String, List<SelectTracePubChartResultBucket>> logTimeResult = result.get("logTime");
					buckets = logTimeResult.get("buckets");
				}

				// 페이지 리턴값 생성
				Page<Long> resultPage = new Page<Long>();
				List<Long> chartList = new ArrayList<Long>();
				for (SelectTracePubChartResultBucket bucket : buckets) {
					chartList.add(bucket.getDoc_count());
				}

				resultPage.setList(chartList);
				return resultPage;

			} catch (Exception e) {
				throw new ElidomRuntimeException(e);
			}
		}

		return new Page<Long>();
	}
}