package xyz.elidom.base.rest;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.base.BaseConfigConstants;
import xyz.elidom.base.entity.PageRouteHistory;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.IDataSourceManager;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/page_route_histories")
@ServiceDesc(description = "PageRouteHistory Service API")
public class PageRouteHistoryController extends AbstractRestService {
	
	/**
	 * Logger
	 */
	private static Logger logger = LoggerFactory.getLogger(PageRouteHistoryController.class);

	@Override
	protected Class<?> entityClass() {
		return PageRouteHistory.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		this.deleteOldHistories();
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public PageRouteHistory findOne(@PathVariable("id") String id) {
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
	public PageRouteHistory create(HttpServletRequest req, @RequestBody PageRouteHistory input) {
		// Type이 존재하지 않을 경우 이력을 생성하지 않음.
		String type = input.getType();
		if (ValueUtil.isEmpty(type))
			return input;

		// 이력 생성 여부.
		String isEnable = SettingUtil.getValue(BaseConfigConstants.PAGE_ROUTE_HISTORY_ENABLE);
		if (!ValueUtil.toBoolean(isEnable, false))
			return input;

		// Type이 설정된 항목 내에 존재 할 경우만, 이력 생성.
		String availableTypes = SettingUtil.getValue(BaseConfigConstants.PAGE_ROUTE_HISTORY_TYPES);
		if (ValueUtil.isNotEmpty(availableTypes)) {
			List<String> typeList = Arrays.asList(StringUtils.tokenizeToStringArray(availableTypes, ","));
			if (!typeList.contains(type)) {
				return input;
			}
		}

		// DataSource 이름에 해당하는 QueryManager 추출.
		IQueryManager queryManager = null;
		String dataSourceName = SettingUtil.getValue(BaseConfigConstants.PAGE_ROUTE_HISTORY_DATASOURCE);

		if (ValueUtil.isNotEmpty(dataSourceName)) {
			IDataSourceManager dsMan = BeanUtil.get(IDataSourceManager.class);
			queryManager = dsMan.getQueryManager(dataSourceName);
		} else {
			queryManager = BeanUtil.get(IQueryManager.class);
		}

		// 이력을 비동기 방식으로 저장.
		/*ThreadUtil.doAsynch(() -> {
			input.setIp(req.getRemoteAddr());
			input.setAgent(req.getHeader("user-agent"));
			input.setYear(ValueUtil.toInteger(DateUtil.getYear()));
			input.setMonth(ValueUtil.toInteger(DateUtil.getMonth()));
			input.setDay(ValueUtil.toInteger(DateUtil.getDay()));
			input.setCreatorId(User.currentUser().getId());
			queryManager.insert(input);
		});*/

		input.setIp(req.getRemoteAddr());
		input.setAgent(req.getHeader("user-agent"));
		input.setYear(ValueUtil.toInteger(DateUtil.getYear()));
		input.setMonth(ValueUtil.toInteger(DateUtil.getMonth()));
		input.setDay(ValueUtil.toInteger(DateUtil.getDay()));
		input.setCreatorId(User.currentUser().getId());
		queryManager.insert(input);
		
		return input;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public PageRouteHistory update(@PathVariable("id") String id, @RequestBody PageRouteHistory input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<PageRouteHistory> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	/**
	 * 설정한 기간이 지난 페이지 이력 삭제.
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	public void deleteOldHistories() {
		try {
			// 수정 1. 스케줄러로 실행될 때는 Domain.currentDomain()이 null이기 때문에 설정에 시스템 도메인 ID로 설정해줘야 한다.
			Long domainId = Domain.systemDomain().getId();
			int saveMonth = ValueUtil.toInteger(SettingUtil.getValue(domainId, BaseConfigConstants.PAGE_ROUTE_HISTORY_SAVE_PERIOD_MONTH), 3);
			if (saveMonth < 1) {
				saveMonth = 1;
			}

			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(Calendar.MONTH, -saveMonth);
			calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0);

			Query queryObj = new Query();
			queryObj.addFilter(new Filter("created_at", OrmConstants.LESS_THAN, calendar.getTime()));

			List<PageRouteHistory> list = queryManager.selectList(PageRouteHistory.class, queryObj);
			if (ValueUtil.isNotEmpty(list)) {
				queryManager.deleteBatch(list);
			}
	
		} catch (Exception e) {
			logger.warn("Failed to delete old page route histories!", e);
		}
	}
}