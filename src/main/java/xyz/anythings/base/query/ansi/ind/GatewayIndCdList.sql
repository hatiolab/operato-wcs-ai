SELECT
	I.IND_CD
FROM
	CELLS C
	INNER JOIN INDICATORS I ON C.DOMAIN_ID = I.DOMAIN_ID AND C.IND_CD = I.IND_CD
	INNER JOIN GATEWAYS G ON I.DOMAIN_ID = G.DOMAIN_ID AND I.GW_CD = G.GW_CD
WHERE
	I.DOMAIN_ID = :domainId
	AND C.ACTIVE_FLAG = :activeFlag
	#if($equipType)
	AND C.EQUIP_TYPE = :equipType
	#end
	#if($equipCd)
	AND C.EQUIP_CD = :equipCd
	#end
	#if($stationCd)
	AND C.STATION_CD = :stationCd
	#end
	#if($gwCd)
	AND G.GW_CD = :gwCd
	#end
	#if($gwNm)
	AND G.GW_NM = :gwNm
	#end
	#if($stationCd)
	AND C.STATION_CD = :stationCd
	#end
	#if($equipZone)
	AND C.EQUIP_ZONE = :equipZone
	#end
	#if($sideCd)
	AND C.SIDE_CD = :sideCd
	#end
ORDER BY
	G.GW_CD ASC, C.CELL_CD