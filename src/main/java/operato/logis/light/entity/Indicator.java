package operato.logis.light.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "indicators", idStrategy = GenerationRule.UUID, uniqueFields="gwCd,indCd", indexes = {
	@Index(name = "ix_indicators_0", columnList = "gw_cd,ind_cd", unique = true),
	@Index(name = "ix_indicators_1", columnList = "domain_id,gw_cd", unique = false)
})
public class Indicator extends xyz.elidom.orm.entity.basic.ElidomStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 402679366038559187L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "gw_cd", nullable = false, length = 30)
	private String gwCd;

	@Column (name = "ind_cd", nullable = false, length = 30)
	private String indCd;

	@Column (name = "ind_nm", nullable = false, length = 100)
	private String indNm;

	@Column (name = "version", length = 15)
	private String version;

	@Column (name = "status", length = 10)
	private String status;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getGwCd() {
		return gwCd;
	}

	public void setGwCd(String gwCd) {
		this.gwCd = gwCd;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}

	public String getIndNm() {
		return indNm;
	}

	public void setIndNm(String indNm) {
		this.indNm = indNm;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
}
