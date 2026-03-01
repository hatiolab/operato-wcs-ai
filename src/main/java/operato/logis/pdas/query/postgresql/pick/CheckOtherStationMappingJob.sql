select
	id
from
	job_instances
where
	domain_id = :domainId
	and batch_id = :batchId
	and status = :status
	and com_cd = :comCd
	and sku_cd = :skuCd
	and sub_equip_cd not in (
		select
			cell_cd
		from
			cells
		where
			domain_id = :domainId
			and active_flag = true
			#if($equipType)
			and equip_type = :equipType
			#end
			#if($equipCd)
			and equip_cd = :equipCd
			#end
			and station_cd = :stationCd
	)