SELECT
	COALESCE(MAX(job_seq::integer), 0)
FROM
	JOB_BATCHES
WHERE
	DOMAIN_ID    = :domainId
	AND 	JOB_TYPE 	= :jobType
	AND 	JOB_DATE 	= :jobDate
	#if($bizType)
	AND    	BIZ_TYPE	= :bizType
	#end
	#if($areaCd)
	AND    	AREA_CD		= :areaCd
	#end
	#if($stageCd)
	AND    	STAGE_CD	= :stageCd
	#end
	#if($equipGroupCd)
	AND    	EQUIP_GROUP_CD	= :equipGroupCd
	#end
	#if($equipCd)
	AND    	EQUIP_CD	= :equipCd
	#end