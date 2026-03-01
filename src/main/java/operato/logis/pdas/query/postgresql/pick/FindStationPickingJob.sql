select
	sub_equip_cd
from
	job_instances j inner join cells c 
	on j.domain_id = c.domain_id and j.equip_cd = c.equip_cd and j.sub_equip_cd = c.cell_cd
where
	j.domain_id = :domainId
	and j.batch_id = :batchId
	and j.status = :status
	and c.active_flag = true
	#if($stationCd)
	and c.station_cd = :stationCd
	#end