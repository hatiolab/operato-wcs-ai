package xyz.anythings.base.entity;

import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.sys.entity.relation.UserRef;

@Table(name = "station_workers", idStrategy = GenerationRule.UUID, uniqueFields="domainId,stationId,workerId", indexes = {
	@Index(name = "ix_station_workers_0", columnList = "domain_id,station_id,worker_id", unique = true)
})
public class StationWorker extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 877769282677713517L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "station_id", length = 40)
	private String stationId;

	@Column (name = "worker_id", length = 32)
	private String workerId;

	@Relation(field = "workerId")
	private UserRef worker;

	@Column (name = "std_uph")
	private Float stdUph;

	@Column (name = "avg_uph")
	private Float avgUph;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getStationId() {
		return stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public String getWorkerId() {
		return workerId;
	}

	public void setWorkerId(String workerId) {
		this.workerId = workerId;
	}

	public UserRef getWorker() {
		return worker;
	}

	public void setWorker(UserRef worker) {
		this.worker = worker;

		if(this.worker != null) {
			String refId = this.worker.getId();
			if (refId != null)
				this.workerId = refId;
		}
	
		if(this.workerId == null) {
			this.workerId = "";
		}
	}

	public Float getStdUph() {
		return stdUph;
	}

	public void setStdUph(Float stdUph) {
		this.stdUph = stdUph;
	}

	public Float getAvgUph() {
		return avgUph;
	}

	public void setAvgUph(Float avgUph) {
		this.avgUph = avgUph;
	}	
}
