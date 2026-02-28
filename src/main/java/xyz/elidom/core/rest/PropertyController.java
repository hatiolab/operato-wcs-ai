/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.rest;

import java.util.List;

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

import xyz.elidom.core.entity.Property;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Order;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/properties")
@ServiceDesc(description="Property Service API")
public class PropertyController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return Property.class;
	}	
	
	@RequestMapping(method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Search Property (Pagination) By Search Conditions")	
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Property By ID")
	public Property findOne(@PathVariable("id") String id) {
		return this.getOne(true, this.entityClass(), id);
	}
	
	@RequestMapping(value="/{id}/exist", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Check if Property exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@RequestMapping(value="/{on_type}/{on_id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Properties by Entity Type and Entity Data ID")
	public List<Property> resourceProperties(@PathVariable("on_type") String resourceType, @PathVariable("on_id") String resourceId) {
		Query query = new Query();
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_DOMAIN_ID, Domain.currentDomain().getId()));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_TYPE, resourceType));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_ID, resourceId));
		query.addOrder(new Order(OrmConstants.ENTITY_FIELD_NAME, true));
		return this.queryManager.selectList(Property.class, query);
	}
	
	@RequestMapping(value="/{on_type}/{on_id}/{prop_type}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Find Properties by Entity Type and Entity Data ID and Prop Type")
	public List<Property> resourcePropertiesByType(@PathVariable("on_type") String resourceType, @PathVariable("on_id") String resourceId, @PathVariable("prop_type") String propType) {
		Query query = new Query();
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_DOMAIN_ID, Domain.currentDomain().getId()));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_TYPE, resourceType));
		query.addFilter(new Filter(OrmConstants.ENTITY_FIELD_ON_ID, resourceId));
		query.addFilter(new Filter("propType", propType));
		query.addOrder(new Order(OrmConstants.ENTITY_FIELD_NAME, true));
		return this.queryManager.selectList(Property.class, query);
	}
	
	@RequestMapping(method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description="Create Property")
	public Property create(@RequestBody Property property) {
		return this.createOne(property);
	}
	
	@RequestMapping(value="/{id}", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Update Property")
	public Property update(@PathVariable("id") String id, @RequestBody Property property) {
		return this.updateOne(property);
	}

	@RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Delete Property")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}
	
	@RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Create, Update or Delete multiple Property at one time")
	public Boolean multipleUpdate(@RequestBody List<Property> propertyList) {
		Property property = propertyList.get(0);
		
		for(Property pro : propertyList) {
			pro.setOnId(property.getOnId());
			pro.setOnType(property.getOnType());
		}
		
		return this.cudMultipleData(this.entityClass(), propertyList);
	}
	
	@RequestMapping(value="/replace", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Replace All Properties")
	public Boolean replace(@RequestBody List<Property> propertyList) {
		if(propertyList.isEmpty()) {
			return true;
		} else {
			Property prop = propertyList.get(0);
			this.removeProperties(Domain.currentDomain().getId(), prop.getOnType(), prop.getOnId());
			return this.cudMultipleData(this.entityClass(), propertyList);
		}
	}

	@ApiDesc(description="Delete by onType and onId")
	private void removeProperties(Object domainId, String onType, Object onId) {
		Property prop = new Property(ValueUtil.toLong(domainId), onType, ValueUtil.toString(onId), null, null);
		this.queryManager.deleteList(this.entityClass(), prop);
	}
}