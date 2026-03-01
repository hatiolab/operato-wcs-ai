package operato.logis.sps.model;

import java.util.List;

import xyz.anythings.base.entity.JobInstance;

/**
 * 현재 작업 단포 SKU 서머리 정보 + 작업 처리 정보
 * 
 * @author shortstop
 */
public class SpsJobSummary {
	/**
	 * 작업 현황 정보
	 */
	private List<SpsSkuSummary> summary;
	/**
	 * 현재 작업 정보
	 */
	private JobInstance jobInstance;
	
	public SpsJobSummary(List<SpsSkuSummary> summary, JobInstance jobInstance) {
		this.summary = summary;
		this.jobInstance = jobInstance;
	}
	
	public List<SpsSkuSummary> getSummary() {
		return summary;
	}
	
	public void setSummary(List<SpsSkuSummary> summary) {
		this.summary = summary;
	}
	
	public JobInstance getJobInstance() {
		return jobInstance;
	}
	
	public void setJobInstance(JobInstance jobInstance) {
		this.jobInstance = jobInstance;
	}

}
