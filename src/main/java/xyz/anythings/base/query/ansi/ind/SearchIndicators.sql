SELECT
	#if($indQueryFlag)
		C.IND_CD, G.GW_NM AS GW_PATH
	#else
		DISTINCT(G.GW_NM) AS GW_PATH 
	#end
FROM
	CELLS C
	INNER JOIN INDICATORS I ON C.DOMAIN_ID = I.DOMAIN_ID AND C.IND_CD = I.IND_CD
	INNER JOIN GATEWAYS G ON I.DOMAIN_ID = G.DOMAIN_ID AND I.GW_CD = G.GW_CD
WHERE
	I.DOMAIN_ID = :domainId
	AND C.ACTIVE_FLAG = :activeFlag
	#if($stageCd)
	AND G.STAGE_CD = :stageCd
	#end
	#if($rackCd)
	AND C.EQUIP_CD = :rackCd
	#end
	#if($stationCd)
	AND C.STATION_CD = :stationCd
	#end
	#if($gwCd)
	AND G.GW_CD = :gwCd
	#end
	#if($equipZone)
	AND C.EQUIP_ZONE = :equipZone
	#end
	#if($sideCd)
	AND C.SIDE_CD = :sideCd
	#end
	#if($indCd)
	AND I.IND_CD = :indCd
	#end
	#if($cellCd)
	AND C.CELL_CD = :cellCd
	#end
ORDER BY
	G.GW_NM ASC
#if($indQueryFlag)
	, C.CELL_SEQ ASC
#end