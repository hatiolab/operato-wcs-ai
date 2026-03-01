update
	orders
set
	status = :toStatus
	#if($equipCd)
	, equip_cd = :equipCd
	#end
	#if($equipNm)
	, equip_nm = :equipNm
	#end
	, updated_at = now()
where 
	domain_id = :domainId
	and batch_id = :batchId