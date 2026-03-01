select
	j.*
from
	job_instances j inner join cells c 
	on j.domain_id = c.domain_id and j.equip_cd = c.equip_cd and j.sub_equip_cd = c.cell_cd
where
	j.domain_id = :domainId
	and j.batch_id = :batchId
	and j.com_cd = :comCd
	and j.sku_cd = :skuCd
	and j.sub_equip_cd is not null
	and c.active_flag = true
	#if($stationCd)
	and c.station_cd = :stationCd
	#end
	#if($statuses)
	and j.status in (:statuses)
	#end
	#if($status)
	and j.status = :status
	#end
order by
	j.status asc, j.sub_equip_cd asc