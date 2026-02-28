/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.dev.rest;

import java.util.List;
import java.util.Map;

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

import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dev.entity.RangedSeq;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/ranged_seqs")
@ServiceDesc(description = "RangedSeq Service API")
public class RangedSeqController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return RangedSeq.class;
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
	public RangedSeq findOne(@PathVariable("id") String id) {
		return this.getOne(true, this.entityClass(), id);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create")
	public RangedSeq create(@RequestBody RangedSeq input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public RangedSeq update(@PathVariable("id") String id, @RequestBody RangedSeq input) {
		return this.updateOne(input);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.getClass(), id);
	}	
	
	@RequestMapping(value = "/{id}/current", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public Integer getSequence(@PathVariable("id") String id) {
		RangedSeq rs = this.getOne(true, RangedSeq.class, id);
		return rs.getSeq();
	}
	
	@RequestMapping(value = "/current", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find current sequence by conditions")
	public Integer currentSequence(
			@RequestParam(name="key1", required = true) String key1, 
			@RequestParam(name="value1", required = true) String value1, 
			@RequestParam(name="key2", required = false) String key2, 
			@RequestParam(name="value2", required = false) String value2, 
			@RequestParam(name="key3", required = false) String key3, 
			@RequestParam(name="value3", required = false) String value3) {
		
		Map<String, Object> paramMap = ValueUtil.newMap("key1,value1", key1, value1);
		StringBuffer sql = new StringBuffer("SELECT SEQ FROM RANGED_SEQ WHERE DOMAIN_ID = :domainId AND KEY1 = :key1 AND VLAUE1 = :value1 ");
		if(ValueUtil.isNotEmpty(key2)) {
			sql.append(" AND KEY2 = :key2 AND VALUE2 = :value2 ");
			paramMap.put(key2, value2);
		}
		
		if(ValueUtil.isNotEmpty(key3)) {
			sql.append(" AND KEY3 = :key3 AND VALUE3 = :value3 ");
			paramMap.put(key3, value3);
		}
		
		Integer seq = this.queryManager.selectBySql(sql.toString(), paramMap, Integer.class);
		return seq;
	}
	
	@RequestMapping(value = "/{id}/increase", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Increase sequence")
	public Integer increaseSequence(@PathVariable("id") String id) {
		return RangedSeq.increaseSequence(id);
	}

	
	@RequestMapping(value = "/increase", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Increase sequence")
	public Integer sequenceUp(@RequestBody RangedSeq input) {
		return RangedSeq.increaseSequence(input);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<RangedSeq> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
}