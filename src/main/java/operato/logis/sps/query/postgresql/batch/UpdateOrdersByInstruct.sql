update
	orders
set
	status = :toStatus
	#if($equipGroupCd)
	, equip_group_cd = :equipGroupCd
	#end
	#if($equipCd)
	, equip_cd = :equipCd
	#end
	#if($equipNm)
	, equip_nm = :equipNm
	#end
	, updated_at = now()
where 
	domain_id = :domainId
	and batch_id in (
		select
			id
		from
			job_batches
		where
			domain_id = :domainId
			and job_type = 'SPS'
			#if($batchId)
			and id = :batchId
			#end
			#if($batchGroupId)
			and batch_group_id = :batchGroupId
			#end
	)