package xyz.anythings.base.rest;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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

import xyz.anythings.base.LogisConstants;
import xyz.anythings.base.entity.JobBatch;
import xyz.anythings.base.entity.Kiosk;
import xyz.anythings.sys.util.AnyEntityUtil;
import xyz.anythings.sys.util.AnyValueUtil;
import xyz.elidom.dbist.dml.Page;
import xyz.elidom.orm.system.annotation.service.ApiDesc;
import xyz.elidom.orm.system.annotation.service.ServiceDesc;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.system.service.AbstractRestService;
import xyz.elidom.util.ValueUtil;

@RestController
@Transactional
@ResponseStatus(HttpStatus.OK)
@RequestMapping("/rest/kiosks")
@ServiceDesc(description = "Kiosk Service API")
public class KioskController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return Kiosk.class;
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
	public Kiosk findOne(@PathVariable("id") String id) {
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
	public Kiosk create(@RequestBody Kiosk input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Kiosk update(@PathVariable("id") String id, @RequestBody Kiosk input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Kiosk> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}

	@RequestMapping(value = "/update/setting", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update setting")
	public Kiosk updateSetting(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestParam(name = "equip_type", required = true) String equipType,
			@RequestParam(name = "equip_cd", required = true) String equipCd, 
			@RequestParam(name = "side_cd", required = false) String sideCd) {
		
		Long domainId = Domain.currentDomainId();
		String kioskIp = AnyValueUtil.getRemoteIp(req);
		sideCd = LogisConstants.checkSideCdForQuery(domainId, sideCd);
		String kioskCd = equipCd;
		
		if(!this.updatableKioskStatus(domainId, equipType, equipCd, kioskIp)) {
			return null;
		}
		
		JobBatch batch = AnyEntityUtil.findEntityBy(domainId, false, JobBatch.class, "stage_cd,equip_nm", "equipType,equipCd,status", equipType, equipCd, JobBatch.STATUS_RUNNING);
		Kiosk kiosk = AnyEntityUtil.findEntityBy(domainId, false, Kiosk.class, "id,domain_id,stage_cd,kiosk_cd,kiosk_nm,equip_type,equip_cd,side_cd,status", "kioskIp", kioskIp);

		String kioskNm = (batch == null) ? kioskCd : batch.getEquipNm();
		if(ValueUtil.isNotEmpty(sideCd)) {
			kioskCd = kioskCd + SysConstants.DASH + sideCd;
			kioskNm = kioskNm + SysConstants.DASH + sideCd;
		}
		
		if(kiosk == null) {
			kiosk = new Kiosk();
		}
		
		if(batch != null) {
			kiosk.setStageCd(batch.getStageCd());
		}
		
		kiosk.setDomainId(domainId);
		kiosk.setKioskCd(kioskCd);
		kiosk.setKioskNm(kioskNm);
		kiosk.setKioskIp(kioskIp);
		kiosk.setEquipType(equipType);
		kiosk.setEquipCd(equipCd);
		kiosk.setSideCd(sideCd);
		kiosk.setStatus(LogisConstants.EQUIP_STATUS_OK);
		
		if(ValueUtil.isEmpty(kiosk.getId())) {
			this.queryManager.insert(kiosk);
		} else {
			this.queryManager.update(kiosk);
		}
		
		return kiosk;
	}
	
	/**
	 * KIOSK 상태 정보 업데이트 가능 여부 체크
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @param kioskIp
	 * @return
	 */
	private boolean updatableKioskStatus(Long domainId, String equipType, String equipCd, String kioskIp) {
		return !ValueUtil.isEqualIgnoreCase(kioskIp, "127.0.0.1");
	}

}