package xyz.elidom.orm.entity.basic;

import xyz.anythings.sys.service.ICustomService;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * CRUD Custom Service Hook
 * 
 * @author shortstop
 */
public abstract class ElidomCustomCrudHook extends ElidomStampHook implements ICustomCrudHook {

	private static final long serialVersionUID = -7272286515696329872L;

	@Override
	public String getCustomCrudServiceName(String point, String crud) {
		// 도메인 ID가 있는 경우 커스텀 CRUD 사용 여부 설정에 따라서 커스텀 서비스 적용 / 비적용 처리
		if(ValueUtil.toBoolean(SettingUtil.getValue(this.getDomainId(), "entity.custom-" + crud + ".enable", SysConstants.FALSE_STRING))) {
			String entityName = this.getClass().getSimpleName().toLowerCase();
			return "entity-" + point + SysConstants.DASH + crud + SysConstants.DASH + entityName;
		} else {
			return null;
		}
	}

	@Override
	public void doCustomCrudService(String customSvcName) {
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(this.domainId, customSvcName, ValueUtil.newMap("data", this));
		}		
	}
	
	@Override
	public void beforeCreate() {
		super.beforeCreate();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_BEFORE, SysConstants.ENTITY_ACTION_CREATE);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}

	@Override
	public void afterCreate() {
		super.afterCreate();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_AFTER, SysConstants.ENTITY_ACTION_CREATE);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}

	@Override
	public void beforeUpdate() {
		super.beforeUpdate();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_BEFORE, SysConstants.ENTITY_ACTION_UPDATE);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}

	@Override
	public void afterUpdate() {
		super.afterUpdate();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_AFTER, SysConstants.ENTITY_ACTION_UPDATE);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}

	@Override
	public void beforeDelete() {
		super.beforeDelete();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_BEFORE, SysConstants.ENTITY_ACTION_DELETE);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}

	@Override
	public void afterDelete() {
		super.afterDelete();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_AFTER, SysConstants.ENTITY_ACTION_DELETE);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}

	@Override
	public void beforeFind() {
		super.beforeFind();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_BEFORE, SysConstants.ENTITY_ACTION_FIND);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}

	@Override
	public void afterFind() {
		super.afterFind();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName(SysConstants.ACTION_POINT_AFTER, SysConstants.ENTITY_ACTION_FIND);
		if(customSvcName != null) {
			this.doCustomCrudService(customSvcName);
		}
	}
}
