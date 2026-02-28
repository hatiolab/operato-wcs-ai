WITH STOCK_TAKING AS
	(SELECT
		*
	FROM
		STOCKTAKINGS
	WHERE
		DOMAIN_ID = :domainId
		AND JOB_DATE = :date
		#if($stageCD)
		AND STAGE_CD = :stageCd
		#end
		#if($equipType)
		AND EQUIP_TYPE = :equipType
		#end
		#if($equipCd)
		AND EQUIP_CD = :equipCd
		#end
	)
	
SELECT
	*
FROM
	STOCK_TAKING
WHERE
	JOB_SEQ=(
		SELECT
			MAX(JOB_SEQ)
		FROM
			STOCKTAKINGS
		WHERE
			DOMAIN_ID = :domainId
			AND JOB_DATE = :date
		#if($stageCD)
			AND STAGE_CD = :stageCd
		#end
		#if($equipType)
			AND EQUIP_TYPE = :equipType
		#end
		#if($equipCd)
			AND EQUIP_CD = :equipCd
		#end
	)