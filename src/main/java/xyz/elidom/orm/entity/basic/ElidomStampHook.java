/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.orm.entity.basic;

import java.util.Date;

import xyz.anythings.sys.service.ICustomService;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.DateUtil;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.ValueUtil;

/**
 * Domain && User && Time Stamp && Entity Hook
 * 
 * @author shortstop
 */
public class ElidomStampHook extends ElidomStamp implements IEntityHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -5587467454709213060L;

	@Override
	public void beforeCreate() {
		this._setDefault_(true, false);
		this._setId_();
		this.validationCheck(OrmConstants.CUD_FLAG_CREATE);
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("before", "create");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void afterCreate() {
		this.setComplexKey();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("after", "create");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void beforeUpdate() {
		//this._setDefault_(false, true);
		
		// 수정 - 업데이트시에는 domainId 체크하지 않는다.
		Date now = DateUtil.getDate();
		this.setUpdatedAt(now);
		
		if (User.currentUser() != null) {
			this.setUpdaterId(User.currentUser().getId());
		}

		this.setPrimaryKeyByComplexKey();
		this.validationCheck(OrmConstants.CUD_FLAG_UPDATE);
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("before", "update");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void afterUpdate() {
		this.setComplexKey();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("after", "update");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void beforeDelete() {
		this.setPrimaryKeyByComplexKey();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("before", "delete");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void afterDelete() {
		this.deleteDetailResource();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("after", "delete");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void beforeFind() {
		this._setDomainId_();
		this.setPrimaryKeyByComplexKey();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("before", "find");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void afterFind() {
		this.setComplexKey();
		
		// 커스텀 서비스 실행
		String customSvcName = this.getCustomCrudServiceName("after", "find");
		if(customSvcName != null) {
			BeanUtil.get(ICustomService.class).doCustomService(domainId, customSvcName, ValueUtil.newMap("data", this));
		}
	}

	@Override
	public void beforeSearch() {
		this._setDomainId_();
	}

	@Override
	public void afterSearch() {
		this.setComplexKey();
	}

	/**
	 * default setting
	 * 
	 * @param domainFlag
	 * @param createFlag
	 * @param updateFlag
	 */
	public void _setDefault_(boolean createFlag, boolean updateFlag) {
		this._setDomainId_();

		if (!(createFlag || updateFlag))
			return;

		Date now = DateUtil.getDate();

		if (createFlag && ValueUtil.isEmpty(this.createdAt)) {
			this.setCreatedAt(now);
			this.setUpdatedAt(now);
		}

		if (createFlag && ValueUtil.isEmpty(this.creatorId) && User.currentUser() != null) {
			this.setCreatorId(User.currentUser().getId());
			this.setUpdaterId(User.currentUser().getId());
		}

		if (updateFlag) {
			this.setUpdatedAt(now);
		}

		if (updateFlag && User.currentUser() != null) {
			this.setUpdaterId(User.currentUser().getId());
		}
	}

	/**
	 * Set Default Domain ID
	 */
	private void _setDomainId_() {
		if ((this.domainId == null || this.domainId <= 0) && (Domain.currentDomain() != null)) {
			this.setDomainId(Domain.currentDomain().getId());
		}
	}
	
	/**
	 * 커스텀 CRUD 서비스 명 리턴
	 * 
	 * @param point
	 * @param crud
	 * @return
	 */
	public String getCustomCrudServiceName(String point, String crud) {
		// 도메인 ID가 있는 경우 커스텀 CRUD 사용 여부 설정에 따라서 커스텀 서비스 적용 / 비적용 처리
		if(ValueUtil.toBoolean("entity.custom-" + crud + ".enable", false)) {
			String entityName = this.getClass().getSimpleName().toLowerCase();
			return "custom-" + entityName + "-" + point + "-" + crud;
		} else {
			return null;
		}
	}
}