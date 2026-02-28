/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.rest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/common_code_details")
@ServiceDesc(description="CommonCodeDetail Service API")
public class CodeDetailController extends AbstractRestService {
	
	@Override
	protected Class<?> entityClass() {
		return CodeDetail.class;
	}	
	
	@GetMapping( produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search CommonCodeDetail (Pagination) by Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}
	
	@GetMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find one CommonCode by ID")
	public CodeDetail findOne(@PathVariable("id") String id) {
		return this.getOne(true, this.entityClass(), id);
	}
	
	@GetMapping(value="/{id}/exist", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check if CommonCode exists by ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@PostMapping( consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create CommonCodeDetail")
	public Boolean create(@RequestBody CodeDetail commonCodeDetail) {
		List<CodeDetail> list = new ArrayList<CodeDetail>();
		commonCodeDetail.setCudFlag_(OrmConstants.CUD_FLAG_CREATE);
		list.add(commonCodeDetail);
		
		return BeanUtil.get(CodeController.class).multipleUpdateCodes(commonCodeDetail.getParentId(), list);
	}
	
	@PutMapping(value="/{id}", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update CommonCodeDetail")
	public Boolean update(@PathVariable("id") String id, @RequestBody CodeDetail commonCodeDetail) {
		List<CodeDetail> list = new ArrayList<CodeDetail>();
		commonCodeDetail.setCudFlag_(OrmConstants.CUD_FLAG_UPDATE);
		list.add(commonCodeDetail);
		
		return BeanUtil.get(CodeController.class).multipleUpdateCodes(commonCodeDetail.getParentId(), list);
	}
	

	@DeleteMapping(value="/{id}", produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete CommonCodeDetail by ID")
	public boolean delete(@PathVariable("id") String id) {
		
		CodeDetail commonCodeDetail = this.findOne(id);
		
		List<CodeDetail> list = new ArrayList<CodeDetail>();
		commonCodeDetail.setCudFlag_(OrmConstants.CUD_FLAG_DELETE);
		list.add(commonCodeDetail);
		
		return BeanUtil.get(CodeController.class).multipleUpdateCodes(commonCodeDetail.getParentId(), list);
	}
	
	@PostMapping(value="/update_multiple", consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple CommonCodeDetail at one time")
	public Boolean multipleUpdate(@RequestBody List<CodeDetail> commonCodeDetailList) {
		return BeanUtil.get(CodeController.class).multipleUpdateCodes(commonCodeDetailList.get(0).getParentId(), commonCodeDetailList);
	}
}