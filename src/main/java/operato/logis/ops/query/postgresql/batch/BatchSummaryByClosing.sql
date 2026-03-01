SELECT
	CASE WHEN A.RUN_MIN = 0 THEN 0 ELSE ROUND(A.RESULT_PCS * (60.0 / A.RUN_MIN), 1) END AS UPH,
	A.RUN_MIN AS EQUIP_RUNTIME
FROM (
	SELECT
		RESULT_PCS,
		ROUND(extract(epoch from (now() - INSTRUCTED_AT) / 3600)::integer, 1) AS RUN_HOUR,
		ROUND(extract(epoch from (now() - INSTRUCTED_AT) / 60)::integer, 1) AS RUN_MIN
	FROM 
		JOB_BATCHES
	WHERE
		DOMAIN_ID = :domainId
		AND ID = :batchId
) A