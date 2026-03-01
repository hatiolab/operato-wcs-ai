select
	id
from
	job_instances
where
	domain_id = :domainId
	and batch_id = :batchId
	and com_cd = :comCd
	and sku_cd = :skuCd