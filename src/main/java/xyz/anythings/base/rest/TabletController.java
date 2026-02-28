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
import xyz.anythings.base.entity.Tablet;
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
@RequestMapping("/rest/tablets")
@ServiceDesc(description = "Tablet Service API")
public class TabletController extends AbstractRestService {

	@Override
	protected Class<?> entityClass() {
		return Tablet.class;
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
	public Tablet findOne(@PathVariable("id") String id) {
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
	public Tablet create(@RequestBody Tablet input) {
		return this.createOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update")
	public Tablet update(@PathVariable("id") String id, @RequestBody Tablet input) {
		return this.updateOne(input);
	}

	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Delete")
	public void delete(@PathVariable("id") String id) {
		this.deleteOne(this.entityClass(), id);
	}

	@RequestMapping(value = "/update_multiple", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Create, Update or Delete multiple at one time")
	public Boolean multipleUpdate(@RequestBody List<Tablet> list) {
		return this.cudMultipleData(this.entityClass(), list);
	}
	
	@RequestMapping(value = "/update/setting", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	@ApiDesc(description = "Update setting")
	public Tablet updateSetting(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestParam(name = "equip_type", required = true) String equipType,
			@RequestParam(name = "equip_cd", required = true) String equipCd,
			@RequestParam(name = "station_cd", required = true) String stationCd) {
		
		Long domainId = Domain.currentDomainId();
		String tabletIp = AnyValueUtil.getRemoteIp(req);
		String tabletCd = equipCd;
		
		if(!this.updatableTabletStatus(domainId, equipType, equipCd, stationCd, tabletIp)) {
			return null;
		}
		
		JobBatch batch = AnyEntityUtil.findEntityBy(domainId, false, JobBatch.class, "stage_cd,equip_nm", "equipType,equipCd,status", equipType, equipCd, JobBatch.STATUS_RUNNING);
		Tablet tablet = AnyEntityUtil.findEntityBy(domainId, false, Tablet.class, "id,domain_id,stage_cd,tablet_cd,tablet_nm,equip_type,equip_cd,station_cd,status", "tabletIp", tabletIp);
		
		String tabletNm = (batch == null) ? tabletCd : batch.getEquipNm();
		if(ValueUtil.isNotEmpty(stationCd)) {
			tabletCd = tabletCd + SysConstants.DASH + stationCd;
			tabletNm = tabletNm + SysConstants.DASH + stationCd;
		}
		
		if(tablet == null) {
			tablet = new Tablet();
		}
		
		if(batch != null) {
			tablet.setStageCd(batch.getStageCd());
		}
		
		tablet.setTabletCd(tabletCd);
		tablet.setTabletNm(tabletNm);
		tablet.setTabletIp(tabletIp);
		tablet.setEquipType(equipType);
		tablet.setEquipCd(equipCd);
		tablet.setStationCd(stationCd);
		tablet.setStatus(LogisConstants.EQUIP_STATUS_OK);
		this.queryManager.upsert(tablet);
		return tablet;
	}
	
	/**
	 * Tablet 상태 정보 업데이트 가능 여부 체크
	 * 
	 * @param domainId
	 * @param equipType
	 * @param equipCd
	 * @param equipZone
	 * @param tabletIp
	 * @return
	 */
	private boolean updatableTabletStatus(Long domainId, String equipType, String equipCd, String equipZone, String tabletIp) {
		return !ValueUtil.isEqualIgnoreCase(tabletIp, "127.0.0.1");
	}

}