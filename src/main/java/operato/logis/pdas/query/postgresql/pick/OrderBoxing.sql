update 
	job_instances 
set 
	status = :status, 
	box_id = :boxId, 
	boxed_at = :currentTime 
where 
	domain_id = :domainId 
	and batch_id = :batchId 
	and class_cd = :classCd