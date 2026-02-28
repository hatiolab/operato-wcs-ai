package xyz.elidom.orm.entity.basic;

import java.io.Serializable;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.orm.OrmConstants;

/**
 * Ref Entity 부모 도메인
 */
public class DomainRefStamp implements Serializable {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -4145292483715524133L;
	
	@Column(name = OrmConstants.TABLE_FIELD_DOMAIN_ID)
	protected Long domainId;

	/**
	 * @return the domainId
	 */
	public Long getDomainId() {
		return domainId;
	}

	/**
	 * @param domainId the domainId to set
	 */
	public void setDomainId(Long domainId) {
		this.domainId = domainId;
	}
}
