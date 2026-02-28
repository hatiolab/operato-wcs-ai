package xyz.anythings.sys.rest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

import net.sf.common.util.ValueUtils;
import xyz.anythings.sys.service.ICustomService;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Order;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.dbist.metadata.Column;
import xyz.elidom.dbist.metadata.Table;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.util.ValueUtil;
import xyz.elidom.util.converter.msg.IJsonParser;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/custom_tables")
@ServiceDesc(description = "Custom Tables Service API")
public class CustomTableController {
	/**
	 * 엔티티 업데이트 시 삭제할 필드 리스트
	 */
	public static final List<String> ENTITY_FIELD_LIST_TO_REMOVE_WHEN_UPDATE = 
			ValueUtil.newStringList(SysConstants.ENTITY_FIELD_ID, SysConstants.ENTITY_FIELD_CREATED_AT, SysConstants.ENTITY_FIELD_UPDATED_AT);
	/**
	 * 쿼리 매니저
	 */
	@Autowired
	protected IQueryManager queryManager;
	/**
	 * JSON 파서
	 */
	@Autowired
	@Qualifier("under_to_camel")
	protected IJsonParser jsonParser;
	/**
	 * 커스텀 서비스
	 */
	@Autowired
	private ICustomService customService;
	
	/**
	 * Entity Class 리턴 - 구현 서비스에서 정의 필요
	 * 
	 * @return
	 */
	protected Class<?> entityClass() {
		return Map.class;
	}
	
	/**
	 * JSON Parser
	 * 
	 * @return
	 */
	public IJsonParser getJsonParser() {
		return this.jsonParser;
	}
	
	@RequestMapping(value = "/{table_name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search (Pagination) By Search Conditions")
		public Page<?> index(
			@PathVariable("table_name") String tableName,
			@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		return this.search(tableName, page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{table_name}/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public Map<?, ?> findOne(@PathVariable("table_name") String tableName, @PathVariable("id") String id) {
		
		Table table = this.getTable(tableName);
		return this.getOne(true, table, id);
	}

	@RequestMapping(value = "/{table_name}/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("table_name") String tableName, @PathVariable("id") String id) {
		
		return ValueUtil.isNotEmpty(this.queryManager.select(tableName, id, this.entityClass()));
	}

	@RequestMapping(value = "/{table_name}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public Map<?, ?> create(@PathVariable("table_name") String tableName, @RequestBody Map<?, ?> input) {
		
		Table table = this.getTable(tableName);
		return this.createOne(table, input);
	}

	@RequestMapping(value = "/{table_name}/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Map<?, ?> update(@PathVariable("table_name") String tableName, @PathVariable("id") String id, @RequestBody Map<?, ?> input) {
		
		Table table = this.getTable(tableName);
		return this.updateOne(table, input);
	}

	@RequestMapping(value = "/{table_name}/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("table_name") String tableName, @PathVariable("id") String id) {
		
		Table table = this.getTable(tableName);
		this.deleteOne(table, id);
	}

	@RequestMapping(value = "/{table_name}/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@PathVariable("table_name") String tableName, @RequestBody List<Map<?, ?>> list) {
		
		// 1. 가상 테이블 정보 추출 
		Table table = this.getTable(tableName);
		// 2. 전 처리 커스텀 서비스 호출
		Long domainId = Domain.currentDomainId();
		Map<String, Object> params = ValueUtil.newMap("table,list", table, list);
		this.customService.doCustomService(domainId, "diy-" + tableName + "-bum", params);
		// 3. upsert multiple
		boolean updated = this.cudMultipleData(table, list);
		// 4. 후 처리 커스텀 서비스 호출
		this.customService.doCustomService(domainId, "diy-" + tableName + "-aum", params);
		// 5. 결과 리턴
		return updated;
	}
	
	/**
	 * 테이블 존재 여부 리턴
	 * 
	 * @param tableName
	 * @return
	 */
	protected Table getTable(String tableName) {
		return this.queryManager.getDml().getTable(tableName);
	}
	
	/**
	 * 테이블 존재 여부 리턴
	 * 
	 * @param tableName
	 * @return
	 */
	protected boolean isTableExist(String tableName) {
		Table table = this.queryManager.getDml().getTable(tableName);
		return table != null;
	}
	
	/**
	 * table에 domain_id 필드가 있는지 체크
	 * 
	 * @param table
	 * @return
	 */
	protected boolean isDomainBased(Table table) {
		return this.isColumnExist(table, OrmConstants.TABLE_FIELD_DOMAIN_ID);
	}
	
	/**
	 * colName에 해당하는 필드가 테이블에 존재하는지 체크
	 * 
	 * @param table
	 * @param colName
	 * @return
	 */
	protected boolean isColumnExist(Table table, String colName) {
		Column col = table.getColumn(colName);
		return col != null;
	}
	
	/**
	 * pagination 검색
	 * 
	 * @param tableName
	 * @param page
	 * @param limit
	 * @param select
	 * @param sort
	 * @param query
	 * @return
	 */
	protected Page<?> search(String tableName, Integer page, Integer limit, String select, String sort, String query) {
		Table table = this.getTable(tableName);
		Query queryObj = this.parseQuery(table, page, limit, select, sort, query);
		return (Page<?>)this.queryManager.selectPage(tableName, queryObj, this.entityClass());
	}
	
	/**
	 * 검색을 위한 파라미터 파싱
	 * 
	 * @param table
	 * @param page
	 * @param limit
	 * @param select
	 * @param sort
	 * @param query
	 * @return
	 */	
	protected Query parseQuery(Table table, Integer page, Integer limit, String select, String sort, String query) {
		Query queryObj = new Query();
		queryObj.setPageIndex(page == null ? 1 : page.intValue());
		limit = (limit == null) ? ValueUtil.toInteger(SettingUtil.getValue(SysConfigConstants.SCREEN_PAGE_LIMIT, "50")) : limit.intValue();
		queryObj.setPageSize(limit);

		if (ValueUtil.isNotEmpty(select)) {
			List<String> selectList = new ArrayList<String>(Arrays.asList(select.split(SysConstants.COMMA)));
			// 테이블에 domain_id 필드가 있다면 무조건 추가되어야 함
			if(!selectList.contains(OrmConstants.TABLE_FIELD_DOMAIN_ID) && this.isDomainBased(table)) {
				selectList.add(OrmConstants.TABLE_FIELD_DOMAIN_ID);
			}
			
			List<String> columnList = new ArrayList<String>();

			for (String column : selectList) {
				if(this.isColumnExist(table, column)) {
					columnList.add(column);
				}
			}

			queryObj.setSelect(columnList);
		}

		if (ValueUtil.isNotEmpty(sort)) {
			queryObj.addOrder(this.jsonParser.parse(sort, Order[].class));
		}

		// Entity가 domainId 필드를 가졌다면 무조건 domainId를 기본 검색 조건으로 지정
		if (this.isDomainBased(table) && ValueUtil.isNotEmpty(Domain.currentDomain())) {
			queryObj.addFilter(new Filter(SysConstants.TABLE_FIELD_DOMAIN_ID, Domain.currentDomain().getId()));
		}

		if (limit >= 0 && ValueUtil.isNotEmpty(query)) {
			queryObj.addFilter(this.jsonParser.parse(query, Filter[].class));
		}
		
		return queryObj;
	}

	/**
	 * Create, Update, Delete를 한번에 처리
	 * 
	 * @param table
	 * @param dataList
	 * @return
	 */
	protected Boolean cudMultipleData(Table table, List<Map<?, ?>> dataList) {
		List<Map<?, ?>> createList = new ArrayList<Map<?, ?>>();
		List<Map<?, ?>> updateList = new ArrayList<Map<?, ?>>();
		List<Map<?, ?>> deleteList = new ArrayList<Map<?, ?>>();

		for (Map<?, ?> data : dataList) {
			String cudFlag = ValueUtil.toString(data.get("cud_flag_"));

			if (ValueUtil.isEqual(cudFlag, OrmConstants.CUD_FLAG_CREATE)) {
				createList.add(data);

			} else if (ValueUtil.isEqual(cudFlag, OrmConstants.CUD_FLAG_UPDATE)) {
				updateList.add(data);

			} else if (ValueUtil.isEqual(cudFlag, OrmConstants.CUD_FLAG_DELETE)) {
				deleteList.add(data);
			}
		}

		this.multipleCud(table, createList, updateList, deleteList);
		return true;
	}
	
	/**
	 * multiple data create/update/delete
	 * 
	 * @param table
	 * @param createList
	 * @param updateList
	 * @param deleteList
	 */
	protected void multipleCud(Table table, List<Map<?, ?>> createList, List<Map<?, ?>> updateList, List<Map<?, ?>> deleteList) {
		if (!ValueUtil.isEmpty(deleteList)) {
			for (Map<?, ?> data : deleteList) {
				this.deleteOne(table, this.getPkValues(table, data));
			}
		}

		if (!ValueUtil.isEmpty(updateList)) {
			for (Map<?, ?> data : updateList) {
				this.updateOne(table, data);
			}
		}

		if (!ValueUtil.isEmpty(createList)) {
			for (Map<?, ?> data : createList) {
				this.createOne(table, data);
			}
		}
	}
	
	/**
	 * entity object로 부터 Primary Key값을 추출한다.
	 * 
	 * @param table
	 * @param entity
	 * @return
	 */
	protected Object[] getPkValues(Table table, Map<?, ?> entity) {
		String[] pkFields = table.getPkFieldNames();
		Object[] pkValues = new Object[pkFields.length];

		for (int i = 0; i < pkFields.length; i++) {
			try {
				String pkColumn = ValueUtil.toDelimited(pkFields[i], SysConstants.CHAR_UNDER_SCORE);
				pkValues[i] = entity.get(pkColumn);
			} catch (Exception e) {
				throw ThrowUtil.newInvalidKey(pkValues);
			}
		}

		return pkValues;
	}
	
	/**
	 * Entity 조회 시 keys로 찾을 수 없는 경우 예외 발생
	 * 
	 * @param withException
	 * @param includeDefaultFields
	 * @param table
	 * @param keys
	 * @return
	 */
	protected Map<?, ?> getOne(boolean withException, Table table, Object... keys) {
		// 1. Check Key
		this.checkEmptyKey(keys);

		// 2. 조회
		return (Map<?, ?>)this.queryManager.select(table.getName(), keys, this.entityClass());
	}
	
	/**
	 * Entity 삭제
	 * 
	 * @param table
	 * @param entityClass
	 * @param keys
	 */
	protected void deleteOne(Table table, Object... keys) {
		// 삭제
		this.queryManager.deleteByCondition(table.getName(), keys);
	}
	
	/**
	 * Entity 수정
	 * 
	 * @param table
	 * @param input
	 * @return
	 */
	protected Map<?, ?> updateOne(Table table, Map<?, ?> input) {
		// 1. Check Empty
		AssertUtil.assertNotEmpty("terms.label.parameter", input);
		
		// 2. cud_flag_, __dirty__, __dirtyfields__ 필드 삭제
		input.remove("cud_flag_");
		input.remove("__dirty__");
		input.remove("__dirtyfields__");
		input.remove("__seq__");
		input.remove("__origin__");
		
		// 3. 키 추출
		Object[] keys = this.getPkValues(table, input);
		
		// 4. 조회
		@SuppressWarnings("unchecked")
		Map<String, Object> oneToUpdate = (Map<String, Object>) this.queryManager.select(table.getName(), keys, this.entityClass());

		// 5. 값 복사
		Iterator<?> keyIter = input.keySet().iterator();
		while(keyIter.hasNext()) {
			String fieldName = (String)keyIter.next();
			Object fieldValue = input.get(fieldName);
			String updateFieldName = ValueUtils.toCamelCase(fieldName.toString(), SysConstants.CHAR_UNDER_SCORE);
			oneToUpdate.put(updateFieldName, fieldValue);
		}
		
		// 6. 업데이트 사용자 설정
		if(this.isColumnExist(table, SysConstants.TABLE_FIELD_UPDATER_ID)) {
			oneToUpdate.put(SysConstants.ENTITY_FIELD_UPDATER_ID, User.currentUser().getId());
		}
		
		// 7. 업데이트 필드 설정
		List<String> fieldNames = new ArrayList<String>();
		keyIter = oneToUpdate.keySet().iterator();
		while(keyIter.hasNext()) {
			String fieldName = (String)keyIter.next();
			if(!ENTITY_FIELD_LIST_TO_REMOVE_WHEN_UPDATE.contains(fieldName)) {
				fieldNames.add(fieldName);
			}
		}
		
		// 8. 업데이트
		String[] updateFields = fieldNames.toArray(new String[fieldNames.size()]);
		this.queryManager.update(table.getName(), oneToUpdate, updateFields);
		
		// 9. 업데이트 시간 설정
		if(this.isColumnExist(table, SysConstants.TABLE_FIELD_UPDATED_AT)) {
			this.updateTimestamp(table, keys, false, true);
		}

		// 10. 리턴
		return oneToUpdate;
	}
	
	/**
	 * Entity 생성
	 * 
	 * @param table
	 * @param input
	 * @return
	 */
	protected Map<?, ?> createOne(Table table, Map<?, ?> input) {
		// 1. Check Empty
		AssertUtil.assertNotEmpty("terms.label.parameter", input);

		// 2. cud_flag_, __dirty__, __dirtyfields__ 필드 삭제
		input.remove("cud_flag_");
		input.remove("__dirty__");
		input.remove("__dirtyfields__");
		input.remove("__seq__");
		input.remove("__origin__");

		@SuppressWarnings("unchecked")
		Map<String, Object> inputMap = (Map<String, Object>)input;
		
		// 3. id 필드 추가
		if(this.isColumnExist(table, OrmConstants.ENTITY_FIELD_ID) && ValueUtil.isEmpty(input.get(OrmConstants.ENTITY_FIELD_ID))) {
			inputMap.put(OrmConstants.ENTITY_FIELD_ID, UUID.randomUUID().toString());
		}
		
		// 4. 도메인 ID 값 추가
		if(this.isDomainBased(table) && ValueUtil.isEmpty(input.get(OrmConstants.TABLE_FIELD_DOMAIN_ID))) {
			inputMap.put(OrmConstants.TABLE_FIELD_DOMAIN_ID, Domain.currentDomainId());
		}
		
		// 5. 생성자, 업데이트 사용자 설정
		String currentUserId = User.currentUser().getId();
		
		if(this.isColumnExist(table, SysConstants.TABLE_FIELD_CREATOR_ID)) {
			inputMap.put(SysConstants.TABLE_FIELD_CREATOR_ID, currentUserId);
		}
		
		if(this.isColumnExist(table, SysConstants.TABLE_FIELD_UPDATER_ID)) {
			inputMap.put(SysConstants.TABLE_FIELD_UPDATER_ID, currentUserId);
		}
		
		// 6. 생성
		this.queryManager.insert(table.getName(), input);
		
		// 7. 생성시간, 업데이트 시간 설정
		Object[] keys = this.getPkValues(table, input);
		boolean createdAtFlag = this.isColumnExist(table, SysConstants.TABLE_FIELD_CREATED_AT);
		boolean updatedAtFlag = this.isColumnExist(table, SysConstants.TABLE_FIELD_UPDATED_AT);
		this.updateTimestamp(table, keys, createdAtFlag, updatedAtFlag);
		
		// 8. 리턴
		return input;
	}
	
	/**
	 * 기본적인 시간 필드 업데이트
	 * 
	 * @param table
	 * @param keys
	 * @param createdAtFlag
	 * @param updatedAtFlag
	 */
	private void updateTimestamp(Table table, Object[] keys, boolean createdAtFlag, boolean updatedAtFlag) {
		if(!createdAtFlag && !updatedAtFlag) return;
		
		// TODO DB별로 별도 처리 ...
		StringBuffer sql = new StringBuffer("update ").append(table.getName()).append(" set ");
		if(createdAtFlag && updatedAtFlag) sql.append("created_at = now(), updated_at = now()");
		else if(createdAtFlag) sql.append("created_at = now()");
		else if(updatedAtFlag) sql.append("updated_at = now()");
		sql.append(" where ");
		
		String[] pkFields = table.getPkFieldNames();
		Map<String, Object> pkParams = new HashMap<String, Object>();
		
		for(int i = 0 ; i < pkFields.length ; i++) {
			String pkField = pkFields[i];
			if(i > 0) sql.append(" and ");
			sql.append(pkField).append(" = :").append(pkField);
			pkParams.put(pkField, keys[i]);
		}
		
		this.queryManager.executeBySql(sql.toString(), pkParams);
	}
	
	/**
	 * 키 중에 빈 값이 있는지 체크
	 * 
	 * @param keys
	 */
	protected void checkEmptyKey(Object... keys) {
		for (Object key : keys) {
			if (ValueUtil.isEmpty(key)) {
				throw ThrowUtil.newNotFoundKey();
			}
		}
	}

}
