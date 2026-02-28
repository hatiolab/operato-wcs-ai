package xyz.anythings.base.model;

import xyz.anythings.base.entity.JobBatch;

/**
 * Equip 타입 / 코드 로 배치를 조회 하는 경우 
 * 리턴 모델
 * @author yang
 */
public class EquipBatchSet {
	
	private JobBatch batch;
	private Object equipEntity;
	
	public JobBatch getBatch() {
		return batch;
	}
	public void setBatch(JobBatch batch) {
		this.batch = batch;
	}
	public Object getEquipEntity() {
		return equipEntity;
	}
	public void setEquipEntity(Object equipEntity) {
		this.equipEntity = equipEntity;
	}
}
