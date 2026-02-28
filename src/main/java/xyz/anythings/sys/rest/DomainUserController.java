package xyz.anythings.sys.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import xyz.anythings.sys.entity.DomainUser;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/domain_users")
@ServiceDesc(description = "DomainUser Service API")
public class DomainUserController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return DomainUser.class;
	}
	
	@RequestMapping(value = "/search_domain_users", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Domain Users")
	public List<User> searchDomainUsers() {
		StringJoiner sql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		sql.add("SELECT")
			.add("	U.ID, DU.DOMAIN_ID, U.LOGIN, U.EMAIL, U.NAME")
			.add("FROM")
			.add("	USERS U INNER JOIN DOMAIN_USERS DU ON U.ID = DU.USER_ID")
			.add("WHERE")
			.add("	DU.DOMAIN_ID = :domainId")
			.add("ORDER BY")
			.add("	U.ID ASC");

		Map<String, Object> params = ValueUtil.newMap("domainId", Domain.currentDomainId());
		List<User> userList = this.queryManager.selectListBySql(sql.toString(), params, User.class, 0, 0);
		return userList;
	}

	@RequestMapping(value = "/search_by_user/{user_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Search Domain User List By Search User ID")
	public List<DomainUser> searchByUserId(@PathVariable("user_id") String userId) {
		StringJoiner sql = new StringJoiner(SysConstants.LINE_SEPARATOR);
		sql.add("SELECT")
			.add("	DU.ID AS ID, D.ID AS DOMAIN_ID, D.NAME AS SITE_CD, D.BRAND_NAME AS SITE_NM, D.DESCRIPTION AS CENTER, DU.USER_ID AS USER_ID")
			.add("FROM")
			.add("	DOMAINS D")
			.add("		LEFT OUTER JOIN DOMAIN_USERS DU ON D.ID = DU.DOMAIN_ID")
			.add("		LEFT OUTER JOIN USERS U ON DU.USER_ID = U.ID")
			.add("WHERE")
			.add("	DU.USER_ID = :userId")
			.add("ORDER BY")
			.add("	D.ID ASC");

		List<DomainUser> duList = this.queryManager.selectListBySql(sql.toString(), ValueUtil.newMap("userId", userId),
				DomainUser.class, 0, 0);
		List<Domain> domainList = this.queryManager.selectList(Domain.class, new Query());
		List<DomainUser> result = new ArrayList<DomainUser>();

		for (Domain domain : domainList) {
			DomainUser du = this.findDomainUser(duList, domain);
			if (du == null) {
				du = new DomainUser();
				du.setDomainId(domain.getId());
				du.setSiteCd(domain.getName());
				du.setSiteNm(domain.getBrandName());
				du.setCenter(domain.getDescription());
				du.setHasPermission(false);
			} else {
				du.setHasPermission(true);
			}

			result.add(du);
		}

		return result;
	}

	private DomainUser findDomainUser(List<DomainUser> domainUserList, Domain domain) {
		for (DomainUser du : domainUserList) {
			if (ValueUtil.isEqual(domain.getId(), du.getDomainId())) {
				return du;
			}
		}

		return null;
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Find one by ID")
	public DomainUser findOne(@PathVariable("id") String id) {
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
	public DomainUser create(@RequestBody DomainUser input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public DomainUser update(@PathVariable("id") String id, @RequestBody DomainUser input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<DomainUser> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

}