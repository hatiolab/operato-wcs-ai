select
	a.batch_id,
	b.batch_order_qty as pick_qty,
	a.result_pcs as picked_qty
from
	(select
		:batchId as batch_id,
		COALESCE(sum(picked_qty), 0) as result_pcs 
	from 
		job_instances 
	where 
		domain_id = :domainId 
		and batch_id = :batchId
	) a
	
	inner join
	
	(select 
		:batchId as batch_id,
		COALESCE(count(distinct(class_cd)), 0) as batch_order_qty 
	from 
		job_instances 
	where 
		domain_id = :domainId 
		and batch_id = :batchId 
		and status in (:statuses)
	) b

	on a.batch_id = b.batch_id