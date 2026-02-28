/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sys.rest;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.client.ElidomBadRequestException;
import xyz.elidom.exception.client.ElidomRecordNotFoundException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sec.util.SecurityUtil;
import xyz.elidom.sys.SysConfigConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.SysMessageConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.auth.model.CheckPassword;
import xyz.elidom.sys.system.engine.ITemplateEngine;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.system.transport.sender.MailSender;
import xyz.elidom.sys.util.FileUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SessionUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;

/**
 * UserController
 * 
 * @author shortstop
 */
@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/users")
@ServiceDesc(description = "User Service API")
public class UserController extends AbstractRestService {
	
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(UserController.class);
	
	@Autowired
	@Qualifier("basic")
    private ITemplateEngine templateEngine;
	
	@Autowired
	private MailSender mailSender;
	
	@Override
	protected Class<?> entityClass() {
		return User.class;
	}

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search User (Pagination) By Search Conditions")
	public Page<?> index(
			@RequestParam(name = "page", required = false) Integer page, 
			@RequestParam(name = "limit", required = false) Integer limit,
			@RequestParam(name = "select", required = false) String select, 
			@RequestParam(name = "sort", required = false) String sort,
			@RequestParam(name = "query", required = false) String query) {
		
        page = (page == null) ? 1 : page.intValue();
        limit = (limit != null) ? limit.intValue() : ValueUtil.toInteger(SettingUtil.getValue("screen.pagination.page.limit", "50"));
		Query queryObj = this.parseQuery(this.entityClass(), page, limit, select, sort, query);
		queryObj.addFilter(new Filter("accountType", "noteq", SysConstants.ACCOUNT_TYPE_TOKEN));
		return this.search(this.entityClass(), queryObj);
	}
	
	@Override
	protected <T> T beforeSearchEntities(T t) {
		Query queryObj = (Query) t;
		Boolean isIgnoreDomain = Domain.currentDomain().getSystemFlag() == true ? true : false;
		queryObj.removeFilter("domainId");
		
		if (!isIgnoreDomain) {
		    String sql = "select user_id from domain_users where domain_id = :currentDomainId";
		    List<String> userIdList = this.queryManager.selectListBySql(sql, ValueUtil.newMap("currentDomainId", Domain.currentDomainId()), String.class, 0, 0);
		    queryObj.addFilter("id", "in", userIdList);
		}

		return t;
	}

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find User By ID")
	public User findOne(@PathVariable("id") String id) {
		return this.getOne(true, this.entityClass(), id);
	}

	@RequestMapping(value = "/exist/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check if Users exists By ID")
	public Boolean isExist(@PathVariable("id") String id) {
		return this.isExistOne(this.entityClass(), id);
	}
	
	@RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check Before Import")
	public List<User> checkImport(@RequestBody List<User> list) {
		for (User item : list) {
			this.checkForImport(User.class, item);
		}
		
		return list;
	}

	@RequestMapping(method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create User")
	public User create(@RequestBody User user) {
		return this.createOne(user);
	}
	
	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	@ApiDesc(description = "Create User")
	public User createUser(@RequestBody User user) {
		try {
			String pass = new String(Base64.decodeBase64(user.getPassword()), SysConstants.CHAR_SET_UTF8);
			pass = SecurityUtil.encodePassword(pass);
			user.setEncryptedPassword(pass);
			user.setPassword(null);
			user.setAccountType(SysConstants.ACCOUNT_TYPE_USER);
		} catch(Exception e) {
			throw new ElidomServiceException(e);
		}		
		
		return this.createOne(user);
	}
	

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update User")
	public User update(@PathVariable("id") String id, @RequestBody User user) {
	    if(ValueUtil.isEmpty(user.getId())) {
	        user.setId(id);
	    }
	    
		return this.updateOne(user);
	}

	@RequestMapping(value = "/{id:.+}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete User By ID")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple Users at one time")
	public Boolean multipleUpdate(@RequestBody List<User> userList) {
		return this.cudMultipleData(this.entityClass(), userList);
	}
	
	@RequestMapping(value = "/{id:.+}/check_password_expired", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Check if Users exists By ID")
	public Map<String, Object> checkPasswordExpired(@PathVariable("id") String id) {
	    User user = this.findUserExceptionIfNotFound(id);
		Map<String, Object> result = ValueUtil.newMap("password_expired", false);
		boolean isEnable = ValueUtil.toBoolean(SettingUtil.getValue(SysConfigConstants.USER_PASSWORD_EXPIRE_ENABLE), false);
		result.put("user_password_expired", isEnable);
		
		if (ValueUtil.isNotEqual(user.getAccountType(), SysConstants.ACCOUNT_TYPE_USER)) {
			return result;
		}
		
		if(isEnable) {
			String todayStr = DateUtil.todayStr();
			String pwExpiredDate = user.getPasswordExpireDate();
			if(pwExpiredDate == null) {
				pwExpiredDate = todayStr;
			}
			
			result.put("password_expired_date", pwExpiredDate);
			if(todayStr.compareTo(pwExpiredDate) > 0) {
				result.put("password_expired", true);
			}
		}
		
		return result;
	}
	
    @RequestMapping(value = "/initialize_pass/{id:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Initialize Password.")
    public boolean initializePassword(@PathVariable("id") String id) {
        // 1. 사용자 조회
        User user = this.findUserExceptionIfNotFound(id);
        
        // 2. 비밀번호 초기화
        user.changePassword(false, null);
        
        // 3. 사용자 업데이트
        this.updateOne(user);
        
        // 4. 세션 킬
        SessionUtil.removeAttribute(SysConstants.ACCOUNT_STATUS);
        
        // 5. 결과 리턴
        return true;
    }
		
	@RequestMapping(value = "/change_pass/{id:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Change Password.")
	public boolean changePass(@PathVariable("id") String id, @RequestBody CheckPassword checkPass) {
	    // 1. 사용자 조회
	    User user = this.findUserExceptionIfNotFound(id);
	    
	    // 2. 비밀번호 체크 
        if(ValueUtil.isNotEmpty(checkPass.getCurrentPass())) {
            String encCurrentPass =  checkPass.getCurrentPass();
            
            if(ValueUtil.isNotEqual(user.getEncryptedPassword(), encCurrentPass)) {
                throw new ElidomServiceException(MessageUtil.getTerm("terms.text.password_mismatch"));
            }
        }
		
	    // 3. 비밀번호 변경
        user.changePassword(false, checkPass.getNewPass());
		
		// 4. 사용자 업데이트
		this.updateOne(user);
		
        // 5. 세션 킬
        SessionUtil.removeAttribute(SysConstants.ACCOUNT_STATUS);
		
		// 6. 결과 리턴
		return true;
	}
	
	@RequestMapping(value = "/change_pass_later/{id:.+}", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Change Password Later.")
	public boolean changePassLater(@PathVariable("id") String id) {
	    User user = this.findUserExceptionIfNotFound(id);
	    
		String laterDay = SettingUtil.getValue(SysConfigConstants.USER_PASSWORD_CHANGE_LATER_DAY, "30");
		String parseDate = DateUtil.addDateToStr(new Date(), ValueUtil.toInteger(laterDay));
		user.setPasswordExpireDate(parseDate);
		this.updateOne(user);

		SessionUtil.removeAttribute(SysConstants.ACCOUNT_STATUS);
		return true;
	}
	
    @RequestMapping(value = "/activate/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Activate account")
    public String activate(@PathVariable("id") String id) {
        return this.activeAccount(id);
    }
    
    @RequestMapping(value = "/deactivate/{id:.+}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Deactivate account")
    public String deactivate(@PathVariable("id") String id) {
        return this.inactiveAccount(id);
    }
	
	@RequestMapping(value = "/active/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Activate account")
	public String activeAccount(@PathVariable("id") String id) {
	    User user = this.findUserExceptionIfNotFound(id);
        
        if (user.getActiveFlag() && ValueUtil.isEqualIgnoreCase(user.getStatus(), "activated")) {
            throw new ElidomBadRequestException(SysMessageConstants.USER_ALREADY_ACTIVATED, "Already activated account");
        }

        if(user.getAccountExpireDate() != null) {
            user.setAccountExpireDate(null);
        }
        
        user.setStatus("activated");
        user.setActiveFlag(true);
        user.setFailCount(0);
        this.queryManager.update(user, "accountExpireDate", "status", "activeFlag", "failCount");
		
		// 계정 활성화, 비밀번호 초기화등의 요청 및 승인 메일 발송 여부.
		boolean sendEmail = ValueUtil.toBoolean(SettingUtil.getValue("user.mail.request.auth.enable", SysConstants.FALSE_STRING));
		if(sendEmail) {
		    String title = MessageUtil.getMessage(SysMessageConstants.USER_COMPLETE_ACTIVE_ACCOUNT, "Your account is activated!");
    		String loginLink = SettingUtil.getValue(SysConfigConstants.CLIENT_CONTEXT_PATH, "http://factory.hatiolab.com");
    		Map<String, Object> templateParams = ValueUtil.newMap("loginLink,title,systemName,domain,email,userId,userName", loginLink, title, user.getDomain().getBrandName(), user.getDomain().getBrandName(), user.getEmail(), user.getId(), user.getName());
    		UserController ctrl = BeanUtil.get(UserController.class);
    		ctrl.sendMailToRequester(SysConfigConstants.MAIL_TEMPLATE_ACCOUNT_ACTIVATION_APPROVED, templateParams);
    		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");
		} else {
		    return MessageUtil.getMessage("Account [" + id + "] is activated.");
		}
	}
	
	@RequestMapping(value = "/inactive/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Deactivate account")
	public String inactiveAccount(@PathVariable("id") String id) {
	    User account = this.findUserExceptionIfNotFound(id);
		
		if(!account.getActiveFlag() && ValueUtil.isEqualIgnoreCase(account.getStatus(), "locked")) {
			throw new ElidomBadRequestException(SysMessageConstants.USER_INACTIVATED_ACCOUNT, "Deactivated account.");
		}
				
		account.setActiveFlag(false);
		account.setStatus("locked");
		this.queryManager.update(account, "activeFlag", "status");
		
        // 계정 활성화, 비밀번호 초기화등의 요청 및 승인 메일 발송 여부.
        boolean sendEmail = ValueUtil.toBoolean(SettingUtil.getValue("user.mail.request.auth.enable", SysConstants.FALSE_STRING));
        if(sendEmail) {
    		String title = MessageUtil.getMessage(SysMessageConstants.USER_INACTIVE_ACCOUNT, "Your account is deactivated");
    		String loginLink = SettingUtil.getValue(SysConfigConstants.CLIENT_CONTEXT_PATH, "http://factory.hatiolab.com");
    		Map<String, Object> templateParams = ValueUtil.newMap("loginLink,title,systemName,domain,email,userId,userName", loginLink, title, account.getDomain().getBrandName(), account.getDomain().getBrandName(), account.getEmail(), account.getId(), account.getName());
    		UserController ctrl = BeanUtil.get(UserController.class);
    		ctrl.sendMailToRequester(SysConfigConstants.MAIL_TEMPLATE_ACCOUNT_INACTIVATION_RESULT, templateParams);
    		return MessageUtil.getMessage(SysMessageConstants.RESULT_SENT_TO_REQUESTER, "Results of processing your request has been sent.");
        } else {
            return MessageUtil.getMessage("Account [" + id + "] is deactivated.");
        }
	}
	
	/**
	 * Send mail to requester
	 * 
	 * @param templatePath
	 * @param templateParams
	 */
	@Async
	public void sendMailToRequester(String templatePath, Map<String, Object> templateParams) {
		templateParams.put("processedAt", DateUtil.currentTimeStr());
		String title = (String)templateParams.get("title");
		String to = (String)templateParams.get("email");
		String content = this.convertTemplate(templatePath, templateParams);
		this.logger.info(content);
		this.mailSender.send(title, null, to, content, templateParams, ValueUtil.newMap("mimeType", "text/html"));
	}
	
	/**
	 * translate template
	 * 
	 * @param templatePath
	 * @param templateParams
	 * @return
	 */
	private String convertTemplate(String templatePath, Map<String, Object> templateParams) {
		templatePath = this.makeTemplatePath(templatePath);
		String template = FileUtil.readClassPathResource(templatePath);
		StringWriter writer = new StringWriter();
		this.templateEngine.processTemplate(template, writer, templateParams, null);
		return writer.toString();
	}
	
	/**
	 * 메일 templatePath를 완성하여 리턴 
	 * 
	 * @param templatePath
	 * @return
	 */
	private String makeTemplatePath(String templatePath) {
		templatePath = SysConstants.MAIL_TEMPLATE_PATH_PREFIX + templatePath; 
		templatePath = templatePath.replace(OrmConstants.DOT, OrmConstants.SLASH);
		templatePath += SysConstants.MAIL_TEMPLATE_PATH_SUFFIX;
		return templatePath;
	}
	
	/**
	 * 사용자 조회 && 존재하지 않으면 에러 발생
	 * 
	 * @param id
	 * @return
	 */
	private User findUserExceptionIfNotFound(String id) {
        User user = this.getOne(false, this.entityClass(), id);
        
        if (user == null) {
            throw new ElidomRecordNotFoundException(SysMessageConstants.USER_NOT_EXIST, "User does not exist");
        }
        
        return user;
	}
}