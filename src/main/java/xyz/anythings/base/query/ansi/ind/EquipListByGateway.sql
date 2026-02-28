select 
	distinct(c.equip_cd) as equip_cd 
from 
	cells c
	inner join indicators i on c.domain_id = i.domain_id and c.ind_cd = i.ind_cd
where 
	c.domain_id = :domainId
	c.equip_type = :equipType
	and i.gw_cd = :gwCd
order by
	c.equip_cd