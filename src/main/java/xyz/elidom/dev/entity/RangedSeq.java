/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.dev.entity;


import java.util.Date;
import java.util.UUID;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.ColumnType;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.exception.client.ElidomRecordNotFoundException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.DomainStamp;
import xyz.elidom.orm.entity.basic.IEntityHook;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

@Table(name = "ranged_seqs", idStrategy = GenerationRule.UUID, uniqueFields = "domainId,key1,value1,key2,value2,key3,value3", indexes = { 
	@Index(name = "ix_ranged_seqs_0", columnList = "domain_id,key1,value1,key2,value2,key3,value3", unique = true)
})
public class RangedSeq extends DomainStamp implements IEntityHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 3772655070717578614L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column (name = "key1", nullable = false, length = OrmConstants.FIELD_SIZE_NAME)
	private String key1;

	@Column (name = "value1", nullable = false, length = OrmConstants.FIELD_SIZE_VALUE_100)
	private String value1;

	@Column (name = "key2", length = OrmConstants.FIELD_SIZE_NAME)
	private String key2;

	@Column (name = "value2", length = OrmConstants.FIELD_SIZE_VALUE_100)
	private String value2;

	@Column (name = "key3", length = OrmConstants.FIELD_SIZE_NAME)
	private String key3;

	@Column (name = "value3", length = OrmConstants.FIELD_SIZE_VALUE_100)
	private String value3;

	@Column (name = "seq", nullable = false)
	private Integer seq;
	
	@Column(name = "updated_at", type = ColumnType.DATETIME)
	private Date updatedAt;	
	
	public RangedSeq() {
	}
	
	public RangedSeq(String id) {
		this.id = id;
	}
	
	public RangedSeq(String key1, String value1) {
		this(Domain.currentDomain().getId(), key1, value1);
	}	
	
	public RangedSeq(Long domainId, String key1, String value1) {
		this.domainId = domainId;
		this.key1 = key1;
		this.value1 = value1;
	}
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getKey1() {
		return key1;
	}

	public void setKey1(String key1) {
		this.key1 = key1;
	}

	public String getValue1() {
		return value1;
	}

	public void setValue1(String value1) {
		this.value1 = value1;
	}

	public String getKey2() {
		return key2;
	}

	public void setKey2(String key2) {
		this.key2 = key2;
	}

	public String getValue2() {
		return value2;
	}

	public void setValue2(String value2) {
		this.value2 = value2;
	}

	public String getKey3() {
		return key3;
	}

	public void setKey3(String key3) {
		this.key3 = key3;
	}

	public String getValue3() {
		return value3;
	}

	public void setValue3(String value3) {
		this.value3 = value3;
	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}
	
	/**
	 * @return the updatedAt
	 */
	public Date getUpdatedAt() {
		return updatedAt;
	}

	/**
	 * @param updatedAt the updatedAt to set
	 */
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}

	/**
	 * id로 RangedSeq를 찾아서 시퀀스를 올려서 리턴 
	 * 
	 * @param condition
	 * @return
	 */
	public static Integer increaseSequence(String id) {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		RangedSeq rs = queryManager.selectWithLock(new RangedSeq(id));
		
		if(rs == null) {
			throw new ElidomRecordNotFoundException();
		}
		
		rs.setSeq(rs.getSeq() + 1);
		queryManager.update(rs);
		return rs.getSeq();
	}	
	
	/**
	 * condition으로 RangedSeq를 찾아서 시퀀스를 올려서 리턴 
	 * 
	 * @param condition
	 * @return
	 */
	public static Integer increaseSequence(RangedSeq condition) {
		if(ValueUtil.isEmpty(condition.getDomainId())) {
			condition.setDomainId(Domain.currentDomain().getId());
		}
				
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		RangedSeq rs = queryManager.selectByConditionWithLock(RangedSeq.class, condition);
		
		if(rs == null) {
			rs = condition;
			rs.setSeq(1);
			queryManager.insert(rs);
		} else {
			rs.setSeq(rs.getSeq() + 1);
			queryManager.update(rs);
		}
		
		return rs.getSeq();
	}
	
	public static Integer increaseSequence(Long domainId, String key1, String value1, String key2, String value2, String key3, String value3) {
		RangedSeq condition = new RangedSeq(domainId, key1, value1);
		condition.setKey2(key2);
		condition.setKey3(key3);
		condition.setValue2(value2);
		condition.setValue3(value3);
		return RangedSeq.increaseSequence(condition);
	}

	@Override
	public void beforeCreate() {
		this.id = UUID.randomUUID().toString();
		this.updatedAt = new Date();
		if(this.domainId == null) {
			this.domainId = Domain.currentDomainId();
		}
	}

	@Override
	public void afterCreate() {		
	}

	@Override
	public void beforeUpdate() {
		this.updatedAt = new Date();		
	}

	@Override
	public void afterUpdate() {		
	}

	@Override
	public void beforeDelete() {		
	}

	@Override
	public void afterDelete() {		
	}

	@Override
	public void beforeFind() {		
	}

	@Override
	public void afterFind() {		
	}

	@Override
	public void beforeSearch() {		
	}

	@Override
	public void afterSearch() {		
	}

}