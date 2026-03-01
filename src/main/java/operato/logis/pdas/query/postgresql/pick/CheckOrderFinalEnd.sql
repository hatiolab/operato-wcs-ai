select 
	id 
from 
	job_instances 
where 
	domain_id = :domainId 
	and batch_id = :batchId 
	and class_cd = :classCd 
	and status not in (:statuses)