select 
  *
from
  gateways
where 
  id in 
  	(select 
    	distinct(g.id) as id
  	from 
    	gateways g
    	inner join indicators i on g.domain_id = i.domain_id and g.gw_cd = i.gw_cd
    	inner join cells c on g.domain_id = i.domain_id and i.ind_cd = c.ind_cd
  	where
    	g.domain_id = :domainId
    	#if($stageCd)
    	and g.stage_cd = :stageCd
    	#end
    	#if($equipType)
    	and c.equip_type = :equipType
    	#end
    	#if($equipCd)
    	and c.equip_cd = :equipCd
    	#end
    	#if($stationCd)
    	and c.station_cd = :stationCd
    	#end)