select
	j.*, c.station_cd
from
	job_instances j
	left outer join
	cells c
	on j.domain_id = c.domain_id and j.equip_cd = c.equip_cd and j.sub_equip_cd = c.cell_cd
where
	j.domain_id = :domainId
	and j.batch_id = :batchId
	and j.com_cd = :comCd
	and j.sku_cd = :skuCd
	and j.box_class_cd is null
	and j.equip_type = :equipType
	and j.equip_cd = :equipCd
	and c.active_flag = true
	and c.station_cd != :stationCd
	#if($status)
	and j.status = :status
	#end
	#if($statuses)
	and j.status in (:statuses)
	#end
order by
	j.pick_qty asc, j.class_cd asc