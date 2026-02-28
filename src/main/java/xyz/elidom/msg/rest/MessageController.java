/* Copyright © HatioLab Inc. All rights reserved. */
/**
 * 
 */
package xyz.elidom.msg.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

import xyz.anythings.sys.model.BaseResponse;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.msg.entity.Message;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.rest.DomainController;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/messages")
@ServiceDesc(description = "Message Service API")
public class MessageController extends AbstractRestService {
	/**
	 * 로케일 쿼리 
	 */
	private static final String LOCALE_QUERY = "SELECT distinct(LOCALE) locale FROM MESSAGES";	
	/**
	 * MESSAGE QUERY
	 */
	private static final String MSG_QUERY = "SELECT NAME, DISPLAY FROM MESSAGES WHERE DOMAIN_ID = :domainId AND LOCALE = :locale";

	@Override
	protected Class<?> entityClass() {
		return Message.class;
	}

	@RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find all messages")
	public Map<String, Object> all() {
		Map<String, Object> messageMap = new HashMap<String, Object>();
		List<String> locales = this.queryManager.selectListBySql(LOCALE_QUERY, null, String.class, 0, 0);
		MessageController ctrl = BeanUtil.get(MessageController.class);
		Long domainId = Domain.currentOrSystemDomainId();
		
		// locale별 message 추출
		for (String locale : locales) {
			locale = locale.replace(".json", SysConstants.EMPTY_STRING);
			messageMap.put(locale, ctrl.messagesByLocale(domainId, locale));
		}
		
		return messageMap;
	}

	@RequestMapping(value = "/all/{locale}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find All Messages By Locale")
	public Map<String, String> allByLocales(@PathVariable("locale") String locale) {
		MessageController ctrl = BeanUtil.get(MessageController.class);
		Long domainId = Domain.currentOrSystemDomainId();
		locale = locale.replace(".json", SysConstants.EMPTY_STRING);
		return ctrl.messagesByLocale(domainId, locale);
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Messages (Pagination) By Search Conditions")
	public Page<?> index(@RequestParam(name = "page", required = false) Integer page,
			@RequestParam(name = "limit", required = false) Integer limit, 
			@RequestParam(name = "select", required = false) String select,
			@RequestParam(name = "sort", required = false) String sort, 
			@RequestParam(name = "query", required = false) String query) {
		return this.search(this.entityClass(), page, limit, select, sort, query);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "find one Message by ID")
	public Message findOne(@PathVariable("id") String id) {
		return this.getOne(true, this.entityClass(), id);
	}	

	@RequestMapping(value = "/{id}/exist", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Message is exist by ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<Message> checkImport(@RequestBody List<Message> list) {
		for (Message item : list) {
			this.checkForImport(Message.class, item);
		}
		
		return list;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create Message")
	public Message create(@RequestBody Message Message) {
		return this.createOne(Message);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update Message")
	public Message update(@PathVariable("id") String id, @RequestBody Message Message) {
		return this.updateOne(Message);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete Message by ID")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple Message by one time")
	public Boolean multipleUpdate(@RequestBody List<Message> MessageList) {
		Boolean result = this.cudMultipleData(this.entityClass(), MessageList);
		
		if(result) {
			BeanUtil.get(MessageController.class).clearCache();
		}
		
		return result;
	}
	
	@RequestMapping(value="/sync_messages/to_other_domains", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description="Syncronize messages to other domains")
	public BaseResponse syncTemsToOtherDomains() {
		// 1. 쿼리 
		Long domainId = Domain.currentDomainId();
		Map<String, Object> params = ValueUtil.newMap("currentDomainId,creatorId", domainId, User.currentUser().getId());
		StringBuffer script = new StringBuffer();
		script.append("insert into messages(");
		script.append("	id, name, locale, display, domain_id, creator_id, updater_id, created_at, updated_at");
		script.append(") select");
		script.append("		uuid_generate_v4(), name, locale, display, :targetDomainId, :creatorId, :creatorId, now(), now()");
		script.append("	from");
		script.append("		messages");
		script.append(" where");
		script.append("		domain_id = :currentDomainId");
		script.append("		and (name, locale) in (");
		script.append("			select name, locale from messages where domain_id = :currentDomainId");
		script.append("			EXCEPT ");
		script.append("			select name, locale from messages where domain_id = :targetDomainId");
		script.append("		)");
		
		// 2. Domain 조회
		List<Domain> domains = this.queryManager.selectList(Domain.class, new Domain());
		
		// 3. 도메인 별로 동일한 이름의 엔티티 & 엔티티 컬럼 제거
		for(Domain domain : domains) {
			if(ValueUtil.isNotEqual(domainId, domain.getId())) {
				params.put("targetDomainId", domain.getId());
				this.queryManager.executeBySql(script.toString(), params);
			}
		}
		
		// 4. Clear Cache Resource Column
		BeanUtil.get(MessageController.class).clearCache();
		
		// 5. 리턴 
		return new BaseResponse(true, SysConstants.OK_STRING);
	}
	
	@RequestMapping(value = "/clear_cache", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public boolean messageClearCache() {
		return BeanUtil.get(DomainController.class).requestClearCache("messages");
	}


	@CacheEvict(cacheNames = "Message", allEntries = true)
	public boolean clearCache() {
		BeanUtil.get(MessageController.class).all();
		return true;
	}
	
	@RequestMapping(value = "/{locale}/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find message by locale and name")
	public String findBy(@PathVariable("locale") String locale, @PathVariable("name") String name) {
		Long domainId = Domain.currentOrSystemDomainId();
		return BeanUtil.get(MessageController.class).findByLocaleKey(domainId, locale, name);
	}
	
	@ApiDesc(description="Find message by locale and key and name")
	@Cacheable(cacheNames="Message", key="#p0 + #p1 + #p2")
	public String findByLocaleKey(Long domainId, String locale, String key) {
		MessageController ctrl = BeanUtil.get(MessageController.class);
		Map<String, String> messages = ctrl.messagesByLocale(domainId, locale);
		return (ValueUtil.isNotEmpty(messages) && messages.containsKey(key)) ? messages.get(key) : null;
	}
	
	@ApiDesc(description = "Message List")
	@Cacheable(cacheNames = "Message", key="#p0 + #p1")
	public Map<String, String> messagesByLocale(Long domainId, String locale) {
		Map<String, String> msgs = new HashMap<String, String>();
		List<Message> msgList = this.queryManager.selectListBySql(MSG_QUERY, ValueUtil.newMap("domainId,locale", domainId, locale), Message.class, 0, 0);
 
		for (Message msg : msgList) {
			msgs.put(msg.getName(), msg.getDisplay());
		}

		return msgs;
	}
}