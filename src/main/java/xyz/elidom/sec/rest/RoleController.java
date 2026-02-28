/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.sec.rest;

import java.util.ArrayList;
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

import xyz.anythings.sys.entity.DomainUser;
import xyz.anythings.sys.model.BaseResponse;
import xyz.anythings.sys.util.AnyOrmUtil;
import xyz.elidom.base.entity.Menu;
import xyz.elidom.base.entity.Resource;
import xyz.elidom.core.entity.CodeDetail;
import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.dbist.dml.Query;
import xyz.elidom.exception.server.ElidomRuntimeException;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sec.entity.Permission;
import xyz.elidom.sec.entity.Role;
import xyz.elidom.sec.entity.UsersRole;
import xyz.elidom.sec.model.MenuAuth;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.sys.system.service.params.BasicInOut;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/roles")
@ServiceDesc(description="Role Service API")
public class RoleController extends AbstractRestService {

    /**
     * Users of role SQL
     */
    private static final String USERS_OF_ROLE_SQL = "select id, login, email, name from users where id in (select user_id from users_roles where domain_id = :domainId and role_id = :roleId)";
    /**
     * Delete Sub Menus of Parent Menu Permissions of role SQL
     */
    private static final String DELETE_PERMISSIONS_SQL = "DELETE FROM PERMISSIONS WHERE ROLE_ID = :roleId AND RESOURCE_TYPE = 'Menu' AND RESOURCE_ID IN (SELECT ID FROM MENUS WHERE ID = :parentMenuId or PARENT_ID = :parentMenuId)";
    /**
     * Delete Menu Permissions of role SQL
     */
    private static final String DELETE_MENU_PERMISSIONS_SQL = "DELETE FROM PERMISSIONS WHERE ROLE_ID = :roleId AND RESOURCE_TYPE = 'Menu' AND RESOURCE_ID = :menuId";
    /**
     * Permitted Resource SQL developer
     */
    private static final String PERMITTED_RESOURCES_SQL_DEV = 
        new StringBuffer("SELECT menus.id, menus.name, menus.parent_id, permissions.resource_type resource_type, permissions.resource_id resource_id, permissions.action_name action_name ")
        .append(" FROM menus LEFT OUTER JOIN permissions ON menus.id = permissions.resource_id and permissions.role_id = :roleId and permissions.resource_type='Menu' ")
        .append("WHERE menus.domain_id = :domainId and menus.parent_id = :parentMenuId ORDER BY menus.rank asc").toString();
    /**
     * Permitted Resource SQL user
     */
    private static final String PERMITTED_RESOURCES_SQL_USER = 
        new StringBuffer("SELECT menus.id, menus.name, menus.parent_id, permissions.resource_type resource_type, permissions.resource_id resource_id, permissions.action_name action_name ")
        .append(" FROM menus LEFT OUTER JOIN permissions ON menus.id = permissions.resource_id and permissions.role_id = :roleId and permissions.resource_type='Menu' ")
        .append("WHERE menus.domain_id = :domainId and menus.parent_id = :parentMenuId ORDER BY menus.rank asc").toString();
    
    /**
     * PERMISSION - show
     */
    private static final String PERMISSION_SHOW = "show";
    /**
     * PERMISSION - create
     */
    private static final String PERMISSION_CREATE = "create";
    /**
     * PERMISSION - update
     */
    private static final String PERMISSION_UPDATE = "update";
    /**
     * PERMISSION - delete
     */
    private static final String PERMISSION_DELETE = "delete";
    
    /**
     * userId Field Name - userId
     */
    private static final String FIELD_USER_ID = "userId";
    /**
     * roleId Field Name - userId
     */
    private static final String FIELD_ROLE_ID = "roleId";
    
    /**
     * items key - items
     */
    private static final String KEY_ITEMS = "items";    
    /**
     * Menu entity Name - Menu
     */
    private static final String MENU_ENTITY_NAME = "Menu";      
    /**
     * Menu Of Role Permissions Parameters Key - domainId,roleId,parentMenuId
     */
    private static final String MENU_OF_ROLE_PERMISSION_PARAMS_KEY = "domainId,roleId,parentMenuId";
    /**
     * Delete Parameters Key - roleId,parentMenuId
     */
    private static final String DELETE_PERMISSION_PARAMS_KEY = "roleId,parentMenuId";
    /**
     * 기본 소팅 조건 - '[{\"field\": \"name\", \"ascending\": true}]'
     */
    private static final String DEFAULT_SORT_COND = "[{\"field\": \"name\", \"ascending\": true}]"; 
    
    @Override
    protected Class<?> entityClass() {
        return Role.class;
    }   
    
    @RequestMapping(method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Search Role (Pagination) By Search Conditions")
    public Page<?> index(
            @RequestParam(name = "page", required = false) Integer page, 
            @RequestParam(name = "limit", required = false) Integer limit, 
            @RequestParam(name = "select", required = false) String select, 
            @RequestParam(name = "sort", required = false) String sort,
            @RequestParam(name = "query", required = false) String query) {
        
        if(ValueUtil.isEmpty(sort)) {
            sort = DEFAULT_SORT_COND;
        }
        
        return this.search(this.entityClass(), page, limit, select, sort, query);
    }
    
    @RequestMapping(value="/{id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Find one Role by ID")
    public Role findOne(@PathVariable("id") String id, @RequestParam(name = "name", required = false) String name) {
        if(SysConstants.SHOW_BY_NAME_METHOD.equalsIgnoreCase(id)) {
            AssertUtil.assertNotEmpty(SysConstants.TERM_LABEL_NAME, name);          
            return this.selectByCondition(true, Role.class, new Role(name));
        } else {
            return this.getOne(true, this.entityClass(), id);
        }
    }
    
    @RequestMapping(value="/{id}/exist", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Check if Role exists by ID")
    public Boolean isExist(@PathVariable("id") String id) {
        return this.isExistOne(this.entityClass(), id);
    }
    
    @RequestMapping(value = "/check_import", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description = "Check Before Import")
    public List<Role> checkImport(@RequestBody List<Role> list) {
        for (Role item : list) {
            this.checkForImport(Role.class, item);
        }
        
        return list;
    }
        
    @RequestMapping(method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ApiDesc(description="Create Role")
    public Role create(@RequestBody Role role) {
        return this.createOne(role);
    }
    
    @RequestMapping(value="/{id}", method=RequestMethod.PUT, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Update Role")
    public Role update(@PathVariable("id") String id, @RequestBody Role role) {
        return this.updateOne(role);
    }

    @RequestMapping(value="/{id}", method=RequestMethod.DELETE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Delete Role")
    public void delete(@PathVariable("id") String id) {
        this.deleteOne(this.entityClass(), id);
    }
    
    @RequestMapping(value="/update_multiple", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Create, Update or Delete multiple Role at one time")
    public Boolean multipleUpdate(@RequestBody List<Role> roleList) {
        return this.cudMultipleData(this.entityClass(), roleList);
    }

    @RequestMapping(value="/{role_id}/permitted_resources/{menu_id}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Get permitted resources")
    public Map<String, Object> permittedResources(@PathVariable("role_id") String id, @PathVariable("menu_id") String parentMenuId) {
    	
        Role role = this.getOne(true, Role.class, id);
        Map<String, Object> paramMap = ValueUtil.newMap(MENU_OF_ROLE_PERMISSION_PARAMS_KEY, role.getDomainId(), id, parentMenuId);
		String sql = (User.currentUser().getAdminFlag() == true) ? PERMITTED_RESOURCES_SQL_DEV : PERMITTED_RESOURCES_SQL_USER;
        List<Permission> permissionList = this.queryManager.selectListBySql(sql, paramMap, Permission.class, 0, 0);
        
        // 메뉴 이름을 다국어 번역 처리 ...
        for(Permission pms : permissionList) {
            if(ValueUtil.isNotEmpty(pms.getName())) {
                String menuName = MessageUtil.getTerm("terms.menu." + pms.getName(), null);
                if(ValueUtil.isNotEmpty(menuName)) {
                    pms.setName(menuName);
                }
            }
        }
        
        Map<String, Object> permittedResources = ValueUtil.newMap(KEY_ITEMS, permissionList);
        return permittedResources;
    }
    
    @RequestMapping(value="/{id}/role_users", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Find users by roleId")
    public List<User> roleUser(@PathVariable("id") String id) {
        Map<String, Object> paramMap = ValueUtil.newMap("domainId,roleId", Domain.currentDomainId(), id);
        return this.queryManager.selectListBySql(USERS_OF_ROLE_SQL, paramMap, User.class, 0, 0);
    }
    
    @RequestMapping(value="/{id}/update_users", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Update users")
    public BasicInOut updateUsers(@PathVariable("id") String id, @RequestBody List<User> users) {
        
        // 1. 사용자 리스트 체크
        if(ValueUtil.isEmpty(users)) {
            BasicInOut result = new BasicInOut();
            result.setResult(false);
            return result;
        }
        
        // 2. 역할이 존재하는지 체크
        Role role = this.getOne(true, Role.class, id);
        
        // 3. 사용자가 모두 존재하는지 체크, 도메인 사용자가 맞는지 체크
        for(User user : users) {
        	String userId = (user.getId() == null) ? user.getLogin() : user.getId();
            User account = this.getOne(false, User.class, userId);
            
            // 3.1 사용자 존재 체크
            if(account == null) {
                throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("USER_NOT_EXIST", "사용자가 존재하지 않습니다."));
            }
            
            // 3.2 사용자가 수퍼 유저인지 체크
            if(account.getSuperUser() && OrmConstants.CUD_FLAG_CREATE.equalsIgnoreCase(user.getCudFlag_())) {
            	throw ThrowUtil.newValidationErrorWithNoLog("This user [" + userId + "] is super user, so this user can't be added!");
            }
            
            // 3.3 도메인 사용자가 맞는지 체크
            DomainUser condition = new DomainUser();
            condition.setDomainId(role.getDomainId());
            condition.setUserId(userId);
            if(this.queryManager.selectSize(DomainUser.class, condition) == 0) {
                List<String> msgParams = ValueUtil.toList(MessageUtil.getTerm("terms.label.domain"), Domain.currentDomain().getDescription());
                throw ThrowUtil.newValidationErrorWithNoLog(MessageUtil.getMessage("HAS_NO_AUTHORITY", "사용자는 현재 도메인 접근 권한이 없습니다.", msgParams));
            }
        }
        
        // 4. 역할 매핑 생성 사용자와 삭제 사용자 구분
        List<UsersRole> createUsersRoleList = new ArrayList<UsersRole>();
        List<String> deleteUserIdList = new ArrayList<String>();
        
        for(int i = 0 ; i < users.size() ; i++) {
            User user = users.get(i);
            String userId = (user.getId() == null) ? user.getLogin() : user.getId();
            
            if(OrmConstants.CUD_FLAG_CREATE.equalsIgnoreCase(user.getCudFlag_())) {
                createUsersRoleList.add(new UsersRole(userId, id));
            } else if(OrmConstants.CUD_FLAG_DELETE.equalsIgnoreCase(user.getCudFlag_())) {
                deleteUserIdList.add(userId);
            }
        }
        
        // 5. 역할 삭제 사용자 제거
        if(!deleteUserIdList.isEmpty()) {
            Query query = AnyOrmUtil.newConditionForExecution(role.getDomainId());
            query.addFilter(new Filter(FIELD_ROLE_ID, id));
            query.addFilter(new Filter(FIELD_USER_ID, OrmConstants.IN, deleteUserIdList));
            List<UsersRole> deleteList = this.queryManager.selectList(UsersRole.class, query);
            queryManager.deleteBatch(deleteList);
        }
        
        // 6. 역할 추가 사용자 추가
        if(!createUsersRoleList.isEmpty()) {
            this.queryManager.insertBatch(createUsersRoleList);
        }
        
        // 7. 결과 리턴
        return new BasicInOut();
    }
        
    @RequestMapping(value="/{id}/update_permissions", method=RequestMethod.POST, consumes=MediaType.APPLICATION_JSON_VALUE, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Update permissions")
    public BasicInOut updatePermissions(
            @PathVariable("id") String id, 
            @RequestBody List<MenuAuth> authList,
            @RequestParam(name = "parent_menu_id", required = true) String parentMenuId,
            @RequestParam(name = "delete_all", required = false) Boolean deleteAll) {

        // 1. 삭제 플래그가 true 이면 모두 삭제
        if(deleteAll != null && deleteAll == true) {
            // Parent Menu 권한과 Parent Menu의 Sub Menu 권한을 모두 삭제
            Map<String, Object> paramMap = ValueUtil.newMap(DELETE_PERMISSION_PARAMS_KEY, id, parentMenuId);
            this.queryManager.executeBySql(DELETE_PERMISSIONS_SQL, paramMap);
        } else {
            deleteAll = false;
        }
        
        // 2. 권한 정보가 없다면 리턴
        if(ValueUtil.isEmpty(authList)) {
            return new BasicInOut();
        }
        
        // 3. 모두 삭제가 아니면 authList 정보로 Parent Menu 권한과 Parent Menu의 Sub Menu 권한을 모두 생성한다. 
        for(MenuAuth auth : authList) {
            String menuId = auth.getMenuId();
            
            if(auth.isShow()) {
                this.createPermission(id, menuId, PERMISSION_SHOW, !deleteAll);
            } else {
                if(!deleteAll) {
                    this.deletePermission(id, menuId, PERMISSION_SHOW);
                }
            }
            
            if(auth.isCreate()) {
                this.createPermission(id, menuId, PERMISSION_CREATE, !deleteAll);
            } else {
                if(!deleteAll) {
                    this.deletePermission(id, menuId, PERMISSION_CREATE);
                }
            }
            
            if(auth.isUpdate()) {
                this.createPermission(id, menuId, PERMISSION_UPDATE, !deleteAll);
            } else {
                if(!deleteAll) {
                    this.deletePermission(id, menuId, PERMISSION_UPDATE);
                }
            }
            
            if(auth.isDelete()) {
                this.createPermission(id, menuId, PERMISSION_DELETE, !deleteAll);
            } else {
                if(!deleteAll) {
                    this.deletePermission(id, menuId, PERMISSION_DELETE);
                }
            }
        }
        
        // 4. Parent Menu 권한도 추가
        this.createPermission(id, parentMenuId, PERMISSION_SHOW, true);
        
        // 5. 리턴
        return new BasicInOut();
    }
    
    /**
     * Create Permission
     * 
     * @param roleId
     * @param menuId
     * @param actionName
     * @param checkIfEmpty
     */
    private void createPermission(String roleId, String menuId, String actionName, boolean checkIfEmpty) {
        Permission permission = new Permission();
        permission.setRoleId(roleId);
        permission.setResourceType(MENU_ENTITY_NAME);
        permission.setResourceId(menuId);
        permission.setActionName(actionName);

        if(!checkIfEmpty) {
            this.createOne(permission);
        } else {
            if(this.queryManager.selectSize(Permission.class, permission) == 0) {
                this.createOne(permission);
            }
        }
    }
    
    /**
     * Delete Permission
     * 
     * @param roleId
     * @param menuId
     * @param actionName
     */
    private void deletePermission(String roleId, String menuId, String actionName) {
        Permission permission = new Permission();
        permission.setRoleId(roleId);
        permission.setResourceType(MENU_ENTITY_NAME);
        permission.setResourceId(menuId);
        permission.setActionName(actionName);
        this.queryManager.deleteByCondition(Permission.class, permission);
    }
    
    @RequestMapping(value="/{role_id}/assign_all/{parent_menu_id}", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Permit all menus by parent menu id")
    public BaseResponse assignAllPermissionsByMainMenu(
            @PathVariable("role_id") String roleId, 
            @PathVariable("parent_menu_id") String parentMenuId) {
        
        // 1. 삭제
        this.unassignAllPermissionsByMainMenu(roleId, parentMenuId);
        
        // 2. parent_menu_id 하위 메뉴를 모두 조회
        Menu menuCond = new Menu();
        menuCond.setParentId(parentMenuId);
        List<Menu> menuList = this.queryManager.selectList(Menu.class, menuCond);
        
        // 3. 역할에 조회한 권한 모두 할당
        for(Menu menu : menuList) {
            String menuId = menu.getId();
            this.createPermission(roleId, menuId, PERMISSION_SHOW, false);
            this.createPermission(roleId, menuId, PERMISSION_CREATE, false);
            this.createPermission(roleId, menuId, PERMISSION_UPDATE, false);
            this.createPermission(roleId, menuId, PERMISSION_DELETE, false);
        }
        
        // 4. Parent Menu 권한도 추가
        this.createPermission(roleId, parentMenuId, PERMISSION_SHOW, true);
        
        // 5. 리턴
        return new BaseResponse(true);
    }
    
    @RequestMapping(value="/{role_id}/unassign_all/{parent_menu_id}", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Revoke all menu permissions by parent menu id")
    public BaseResponse unassignAllPermissionsByMainMenu(
            @PathVariable("role_id") String roleId, 
            @PathVariable("parent_menu_id") String parentMenuId) {
        
        // 1. 역할 조회
        Role role = this.getOne(true, Role.class, roleId);
        if(role == null) {
            return new BaseResponse(false, "Role not found!");
        }

        // 2. 부모 권한과 부모 메뉴의 하위 메뉴 권한을 모두 삭제
        Map<String, Object> paramMap = ValueUtil.newMap(DELETE_PERMISSION_PARAMS_KEY, roleId, parentMenuId);
        this.queryManager.executeBySql(DELETE_PERMISSIONS_SQL, paramMap);
        
        // 3. 리턴
        return new BaseResponse(true);
    }
    
    @RequestMapping(value="/{role_id}/assign_menu/{menu_id}", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Permit all menus by menu id")
    public BaseResponse assignAllPermissionsByMenu(
            @PathVariable("role_id") String roleId, 
            @PathVariable("menu_id") String menuId) {
        
        // 1. 삭제
        this.unassignAllPermissionsByMenu(roleId, menuId);
        
        // 2. 메뉴의 권한 모두 할당
        this.createPermission(roleId, menuId, PERMISSION_SHOW, false);
        this.createPermission(roleId, menuId, PERMISSION_CREATE, false);
        this.createPermission(roleId, menuId, PERMISSION_UPDATE, false);
        this.createPermission(roleId, menuId, PERMISSION_DELETE, false);
        
        // 3. Parent Menu 권한이 있는지 체크
        String sql = "select parent_id from menus where id = :menuId";
        String parentMenuId = this.queryManager.selectBySql(sql, ValueUtil.newMap("menuId", menuId), String.class);
        
        // 4. 없으면 추가
        if(ValueUtil.isNotEmpty(parentMenuId)) {
            this.createPermission(roleId, parentMenuId, PERMISSION_SHOW, true);
        }
        
        // 5. 리턴
        return new BaseResponse(true);
    }
    
    @RequestMapping(value="/{role_id}/unassign_menu/{menu_id}", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Revoke all menu permissions by menu id")
    public BaseResponse unassignAllPermissionsByMenu(
            @PathVariable("role_id") String roleId, 
            @PathVariable("menu_id") String menuId) {
        
        // 1. 역할 조회
        Role role = this.getOne(true, Role.class, roleId); 
        if(role == null) {
            return new BaseResponse(false, "Role not found!");
        }

        // 2. 메뉴 권한을 모두 삭제
        Map<String, Object> paramMap = ValueUtil.newMap("roleId,menuId", roleId, menuId);
        this.queryManager.executeBySql(DELETE_MENU_PERMISSIONS_SQL, paramMap);
        
        // 3. 리턴
        return new BaseResponse(true);
    }
    
    @RequestMapping(value="/{role_id}/permissions/entity/{entity_name}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Search permitted data to role")
    public Page<?> searchPermittedEntityData(
            @PathVariable("role_id") String roleId,
            @PathVariable("entity_name") String entityName, 
            @RequestParam(name = "page", required = false) Integer page,
            @RequestParam(name = "limit", required = false) Integer limit) {
        
        Role role = this.getOne(true, Role.class, roleId);
        Resource entity = this.queryManager.selectByCondition(Resource.class, new Resource(role.getDomainId(), entityName));
        if(entity == null) {
            throw ThrowUtil.newNotFoundRecord("menu.Entity", entityName);
        }
        
        String tableName = entity.getTableName();
        String sql = "select case when p.resource_id is null then false else true end as has_permission, e.* from " + tableName + " e left outer join permissions p on e.domain_id = p.domain_id and e.id = p.resource_id and resource_type = :entityName and action_name = :actionName and p.role_id = :roleId where e.domain_id = :domainId";
        Map<String, Object> params = ValueUtil.newMap("domainId,roleId,entityName,actionName", role.getDomainId(), roleId, entityName, PERMISSION_SHOW);
        return this.queryManager.selectPageBySql(sql, params, Map.class, page, limit);
    }
    
    @SuppressWarnings("rawtypes")
    @RequestMapping(value="/{role_id}/update_permissions/entity/{entity_name}", method=RequestMethod.POST, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Update permitted data to role")
    public BaseResponse updatePermittedEntityData(
            @PathVariable("role_id") String roleId,
            @PathVariable("entity_name") String entityName,
            @RequestBody List<Map> authList) {
        
        // 1. 역할, 엔티티 조회
        Role role = this.getOne(true, Role.class, roleId);
        Resource entity = this.queryManager.selectByCondition(Resource.class, new Resource(role.getDomainId(), entityName));
        if(entity == null) {
            throw ThrowUtil.newNotFoundRecord("menu.Entity", entityName);
        }
        
        // 2. 선택된 데이터 리스트 모두 삭제
        for(Map auth : authList) {
            String resourceId = ValueUtil.toString(auth.get("id"));
            Permission permission = new Permission();
            permission.setRoleId(roleId);
            permission.setResourceType(entityName);
            permission.setResourceId(resourceId);
            permission.setActionName(PERMISSION_SHOW);
            this.queryManager.deleteByCondition(Permission.class, permission);
        }
        
        // 3. 선택된 데이터 리스트 중에 hasPermission 정보가 있는 것만 추가
        for(Map auth : authList) {
            String resourceId = ValueUtil.toString(auth.get("id"));
            Boolean hasPermission = ValueUtil.toBoolean(auth.get("has_permission"));
            
            if(hasPermission) {
                Permission permission = new Permission();
                permission.setRoleId(roleId);
                permission.setResourceType(entityName);
                permission.setResourceId(resourceId);
                permission.setActionName(PERMISSION_SHOW);
                this.createOne(permission);
            }
        }
        
        // 4. 리턴
        return new BaseResponse(true);
    }
    
    /**
     * 로그인 사용자의 데이터 권한 리스트
     * 
     * @param entityName
     * @return
     */
    @RequestMapping(value="/permitted_data/entity/{entity_name}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Search permitted data of current user")
    public List<?> getEntityPermittedDataByUser(@PathVariable("entity_name") String entityName) {
        Long domainId = Domain.currentDomainId();
        String currentUserId = User.currentUser().getId();
        
        Resource entity = this.queryManager.selectByCondition(Resource.class, new Resource(domainId, entityName));
        if(entity == null) {
            throw ThrowUtil.newNotFoundRecord("menu.Entity", entityName);
        }
        
        String tableName = entity.getTableName();
        
        if(User.isCurrentUserAdmin()) {
            String sql = "select * from " + tableName + " where domain_id = :domainId";
            Map<String, Object> params = ValueUtil.newMap("domainId", domainId);
            return this.queryManager.selectListBySql(sql, params, Map.class, 0, 0);
            
        } else {
            String sql = "select distinct e.* from " + tableName + " e join permissions p on e.domain_id = p.domain_id and e.id = p.resource_id and resource_type = :entityName and action_name = :actionName and p.role_id in (select distinct role_id from users_roles where domain_id = :domainId and user_id = :userId) where e.domain_id = :domainId";
            Map<String, Object> params = ValueUtil.newMap("domainId,userId,entityName,actionName", domainId, currentUserId, entityName, PERMISSION_SHOW);
            return this.queryManager.selectListBySql(sql, params, Map.class, 0, 0);
        }
    }
    /**
     * 로그인 사용자의 데이터 권한 리스트
     * 
     * @param entityName
     * @return
     */
    @RequestMapping(value="/permitted_data_as_code/entity/{entity_name}", method=RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE)
    @ApiDesc(description="Search permitted data of current user")
    public List<CodeDetail> getEntityPermittedDataByUserAsCode(@PathVariable("entity_name") String entityName) {
        Long domainId = Domain.currentDomainId();
        String currentUserId = User.currentUser().getId();
        
        Resource entity = this.queryManager.selectByCondition(Resource.class, new Resource(domainId, entityName));
        if(entity == null) {
            throw ThrowUtil.newNotFoundRecord("menu.Entity", entityName);
        }
        
		if(ValueUtil.isEmpty(entity.getTitleField())) {
			throw new ElidomRuntimeException("Title Field of Entity [" + entity.getName() + "] must not be empty!");
		}
		
		if(ValueUtil.isEmpty(entity.getDescField())) {
			throw new ElidomRuntimeException("Description Field of Entity [" + entity.getName() + "] must not be empty!");
		}
        
        
        String titleField = entity.getTitleField();
        String descField = entity.getDescField();
        String tableName = entity.getTableName();
        
        if(User.isCurrentUserAdmin()) {
    		StringBuffer sql = new StringBuffer("select ");
    		sql.append(titleField).append(" as name,")
    		   .append(descField).append(" as description")
    		   .append(" from ").append(tableName)
    		   .append(" where domain_id = :domainId")
    		   .append(" order by ").append(titleField);
    		
            Map<String, Object> params = ValueUtil.newMap("domainId", domainId);
            return this.queryManager.selectListBySql(sql.toString(), params, CodeDetail.class, 0, 0);
            
        } else {
    		StringBuffer sql = new StringBuffer("select distinct ");
    		sql.append("e.").append(titleField).append(" as name,")
    		   .append("e.").append(descField).append(" as description")
    		   .append(" from ").append(tableName).append(" e")
    		   .append(" join permissions p")
    		   .append("   on e.domain_id = p.domain_id")
    		   .append("  and e.id = p.resource_id ")
    		   .append("  and p.resource_type = :entityName")
    		   .append("  and p.action_name = :actionName")
    		   .append("  and p.role_id in (select distinct role_id ")
    		   .append("                      from users_roles")
    		   .append("                     where domain_id = :domainId")
    		   .append("                       and user_id = :userId)")
    		   .append(" order by e.").append(titleField);
    		
            Map<String, Object> params = ValueUtil.newMap("domainId,userId,entityName,actionName", domainId, currentUserId, entityName, PERMISSION_SHOW);
            return this.queryManager.selectListBySql(sql.toString(), params, CodeDetail.class, 0, 0);
        }
    }
}