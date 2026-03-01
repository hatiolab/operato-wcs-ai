select
	*
from (
	select
		j.equip_type, j.equip_cd, j.sub_equip_cd, j.class_cd, sum(j.pick_qty) as pick_qty, sum(j.picked_qty) as picked_qty, 'P' as status
	from
		job_instances j inner join cells c on j.domain_id = c.domain_id and j.equip_cd = c.equip_cd and j.sub_equip_cd = c.cell_cd and j.class_cd = c.class_cd
	where
		j.domain_id = :domainId
		and j.batch_id = :batchId
		and c.active_flag = true
		and c.class_cd is not null
		#if($cellCd)
		and j.sub_equip_cd = :cellCd
		#end
		#if($stationCd)
		and c.station_cd = :stationCd
		#end
	group by
		j.equip_type, j.equip_cd, j.sub_equip_cd, j.class_cd
	
	union

	select
		equip_type, equip_cd, cell_cd as sub_equip_cd, '' as class_cd, 0 as pick_qty, 0 as picked_qty, 'W' as status
	from
		cells
	where
		domain_id = :domainId
		and equip_cd = :equipCd
		and active_flag = true
		and class_cd is null
		#if($cellCd)
		and cell_cd = :cellCd
		#end
		#if($stationCd) 
		and station_cd = :stationCd 
		#end
		#if($workingCellOnly)
		and cell_cd = '__'
		#end
) a
order by
	sub_equip_cd