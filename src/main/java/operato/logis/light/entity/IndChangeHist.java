package operato.logis.light.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "ind_change_hists", idStrategy = GenerationRule.UUID, indexes = {
})
public class IndChangeHist extends xyz.elidom.orm.entity.basic.ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -5714154562352430572L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "from_gw_cd", nullable = false, length = 30)
	private String fromGwCd;
	
	@Column (name = "from_ind_cd", nullable = false, length = 100)
	private String fromIndCd;

	@Column (name = "to_gw_cd", nullable = false, length = 30)
	private String toGwCd;

	@Column (name = "to_ind_cd", nullable = false, length = 100)
	private String toIndCd;

	@Column (name = "remark", length = 100)
	private String indCd;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFromGwCd() {
		return fromGwCd;
	}

	public void setFromGwCd(String fromGwCd) {
		this.fromGwCd = fromGwCd;
	}

	public String getFromIndCd() {
		return fromIndCd;
	}

	public void setFromIndCd(String fromIndCd) {
		this.fromIndCd = fromIndCd;
	}

	public String getToGwCd() {
		return toGwCd;
	}

	public void setToGwCd(String toGwCd) {
		this.toGwCd = toGwCd;
	}

	public String getToIndCd() {
		return toIndCd;
	}

	public void setToIndCd(String toIndCd) {
		this.toIndCd = toIndCd;
	}

	public String getIndCd() {
		return indCd;
	}

	public void setIndCd(String indCd) {
		this.indCd = indCd;
	}
	
	
}
