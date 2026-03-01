select 
	*
from (
	select 
		a.domain_id, b.id, a.sub_equip_cd, a.class_cd, a.pick_qty, a.picking_qty, a.picked_qty, 
		case 
			when a.picking_qty = 0 and a.picked_qty = 0 then 'W'
			when a.picked_qty >= a.pick_qty then 'B'
			else 'P' 
		end as status
	from
		(select
			j.domain_id, j.sub_equip_cd, j.class_cd, sum(j.pick_qty) as pick_qty, sum(j.picking_qty) as picking_qty, sum(j.picked_qty) as picked_qty
		from
			job_instances j inner join cells c on j.domain_id = c.domain_id and j.equip_cd = c.equip_cd and j.sub_equip_cd = c.cell_cd
		where
			j.domain_id = :domainId
			and j.batch_id = :batchId
			and c.active_flag = true
			#if($cellCd)
			and (j.sub_equip_cd = :cellCd or j.ind_cd = :cellCd)
			#end
			#if($stationCd)
			and c.station_cd = :stationCd
			#end
		group by
			j.domain_id, j.sub_equip_cd, j.class_cd
		) a
		
		left outer join
		
		(select
			sub_equip_cd, class_cd, max(id) as id
		 from
		 	job_instances
		 where
            domain_id = :domainId   
            and batch_id = :batchId
		 	and status = 'P'
		group by
		 	sub_equip_cd, class_cd
		) b
		
		on a.sub_equip_cd = b.sub_equip_cd and a.class_cd = b.class_cd
) z
where
	z.domain_id = :domainId
	#if($workingCellOnly)
	and z.status = 'P'
	#end
	#if($pickingCellOnly)
	and z.picking_qty > 0
	#end
order by
	z.sub_equip_cd