package xyz.elidom.core.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

import xyz.elidom.core.CoreConstants;
import xyz.elidom.core.entity.Procedure;
import xyz.elidom.dbist.ddl.Ddl;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.IDataSourceManager;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.manager.DataSourceQueryManager;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.ClassUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/procedures")
@ServiceDesc(description = "Procedure Service API")
public class ProcedureController extends AbstractRestService {
	
	@Autowired
	protected Ddl ddl;
	
	@Autowired
	protected IDataSourceManager dataSourceManager;

	@Override
	protected Class<?> entityClass() {
		return Procedure.class;
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
	public Procedure findOne(@PathVariable("id") String id) {
		Procedure procedure = this.getOne(this.entityClass(), id);
		
		Ddl ddl = this.getDdl(procedure);
		String procedureName = procedure.getName();
		String script = ddl.getProcedureDef(procedureName);
		
		procedure.setScript(ValueUtil.checkValue(script, ddl.getCreateProcedureTemplate(procedureName)));
		return procedure;
	}

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public Procedure create(@RequestBody Procedure input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Procedure update(@PathVariable("id") String id, @RequestBody Procedure input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.getClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Procedure> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	@RequestMapping(value = "/{id}/create_procedure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Boolean createProcedure(@RequestBody Procedure input) {
		return this.getDdl(input).createProcedureBySql(input.getScript());
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{id}/delete_procedure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public Boolean deleteProcedure(@PathVariable("id") String id) {
		Procedure procedure = queryManager.select(Procedure.class, id);

		List<String> paramTypeList = new ArrayList<String>();
		List<Map> paramInfoMap = this.listProcedureParams(id);
		for (Map param : paramInfoMap) {
			paramTypeList.add(ValueUtil.toString(param.get("data_type")));
		}

		return this.getDdl(procedure).dropProcedureByName(procedure.getName(), paramTypeList);
	}
	
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/{id}/invoke_procedure", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map> invokeProcedure(@PathVariable("id") String id, @RequestBody Map<String, Object> paramMap) {
		Procedure procedure = queryManager.select(Procedure.class, id);
		Map<String, Object> inputMap = ValueUtil.checkValue(paramMap, new HashMap<String, Object>());
		return this.getQueryManager(procedure).callReturnListProcedure(procedure.getName(), inputMap, Map.class);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@RequestMapping(value = "/{id}/procedure_params", method = RequestMethod.GET, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<Map<String, String>> procedureParams(@PathVariable("id") String id) {
		List<Map<String, String>> resultList = new ArrayList<Map<String, String>>();

		List<Map> list = this.listProcedureParams(id);
		if (ValueUtil.isEmpty(list)) {
			return resultList;
		}

		for (Map map : list) {
			Map<String, String> resultMap = new HashMap<String, String>();

			map.forEach((k, v) -> {
				String key = ValueUtil.toString(k);
				String value = ValueUtil.toString(v);

				resultMap.put(key, ValueUtil.isEqual(key, CoreConstants.DATA_TYPE) ? this.parseType(value) : value);
			});

			resultList.add(resultMap);
		}

		return resultList;
	}
	
	/**
	 * Procedure Parameter 가져오기 실행.
	 * 
	 * @param id
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private List<Map> listProcedureParams(String id) {
		Procedure procedure = queryManager.select(Procedure.class, id);
		List<Map> paramList = this.getQueryManager(procedure).getProcedureParameters(procedure.getName());
		if (ValueUtil.isEmpty(paramList)) {
			return new ArrayList<Map>();
		}

		// Parameter에 대한 Alias 정보 추출.
		int size = paramList.size() > 10 ? 10 : paramList.size();
		for (int i = 0; i < size; i++) {
			Map paramInfoMap = paramList.get(i);
			String value = ValueUtil.toString(ClassUtil.getFieldValue(procedure, CoreConstants.PARAM_FIELD + (i + 1)));
			if (ValueUtil.isNotEmpty(value)) {
				paramInfoMap.put(CoreConstants.PARAMETER_ALIAS, value);
			}
		}

		return paramList;
	}

	/**
	 * Type의 종류에 따라, string, number, boolean, date 타입으로 변경.
	 * 
	 * @param type
	 * @return
	 */
	private String parseType(String type) {
		String inputType = type.toLowerCase();
		if (CoreConstants.STRING_DB_COLUMN_TYPES.contains(inputType)) {
			return "string";
		} else if (CoreConstants.NUMBER_DB_COLUMN_TYPES.contains(inputType)) {
			return "number";
		} else if (CoreConstants.BOOLEAN_DB_COLUMN_TYPES.contains(inputType)) {
			return "boolean";
		}

		for (String value : CoreConstants.DATE_DB_COLUMN_TYPES) {
			if (value.startsWith(type)) {
				return "date";
			}
		}
		return "string";
	}

	/**
	 * DataSource ID에 해당하는 DDL 가져오기 실행.
	 * 
	 * @param procedure
	 * @return
	 */
	private Ddl getDdl(Procedure procedure) {
		if (ValueUtil.isEmpty(procedure.getDataSrcId()))
			return ddl;

		DataSourceQueryManager dataSourceQueryManager = (DataSourceQueryManager) dataSourceManager.getQueryManager(procedure.getDataSrc().getName());
		return dataSourceQueryManager.getDdl();
	}

	/**
	 * DataSource ID에 해당하는 QueryManager 가져오기 실행.
	 * 
	 * @param procedure
	 * @return
	 */
	private IQueryManager getQueryManager(Procedure procedure) {
		return ValueUtil.isEmpty(procedure.getDataSrcId()) ? queryManager : dataSourceManager.getQueryManager(procedure.getDataSrc().getName());
	}
}