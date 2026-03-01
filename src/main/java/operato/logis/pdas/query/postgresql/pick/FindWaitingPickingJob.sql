select
	*
from
	job_instances
where
	domain_id = :domainId
	and batch_id = :batchId
	and com_cd = :comCd
	and sku_cd = :skuCd
	and status = :status
	and equip_type = :equipType
	and equip_cd = :equipCd
	and (sub_equip_cd is null or sub_equip_cd = '')
order by
	pick_qty asc, class_cd asc