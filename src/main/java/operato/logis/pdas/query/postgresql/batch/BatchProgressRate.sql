select 
	B.plan_order,
	B.actual_order,
	B.rate_order,
	B.plan_pcs,
	B.actual_pcs,
	round(B.RATE_PCS::numeric, 2) as rate_pcs
from (
	select
		A.plan_order,
		A.actual_order,
		A.rate_order,
		A.plan_pcs,
		A.actual_pcs,
		(CASE WHEN A.PLAN_PCS = 0 THEN 0 ELSE A.ACTUAL_PCS::float/A.PLAN_PCS::float END) * 100.0 AS RATE_PCS
	from (
		select
			batch_order_qty as plan_order,
			result_order_qty as actual_order,
			progress_rate as rate_order,
			batch_pcs as plan_pcs,
			result_pcs as actual_pcs
		from
			job_batches
		where
			domain_id = :domainId
			and id = :batchId
	) A
) B