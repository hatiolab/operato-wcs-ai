SELECT
	COALESCE(MAX(INPUT_SEQ), 0) AS INPUT_SEQ
FROM
	JOB_INPUTS
WHERE
	DOMAIN_ID = :domainId
	AND BATCH_ID = :batchId
	#if($stationCd)
	AND STATION_CD = :stationCd
	#end
	#if($comCd)
	AND COM_CD = :comCd
	#end
	#if($skuCd)
	AND SKU_CD = :skuCd
	#end
	#if($equipCd)
	AND EQUIP_CD = :equipCd
	#end