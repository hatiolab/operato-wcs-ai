select
	a.cell_cd
from
	(select
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
		#if($stationCd)
		and cell_cd in (select cell_cd from cells where domain_id = :domainId and station_cd = :stationCd)
		#end
		and (class_cd is null or class_cd = '')
	order by
		cell_seq asc
	) a
limit 1