SELECT *
  FROM (
        SELECT *
          FROM JOB_INPUTS
        WHERE DOMAIN_ID = :domainId
          AND BATCH_ID = :batchId
          AND EQUIP_TYPE = :equipType
        #if($equipCd)
          AND EQUIP_CD = :equipCd
        #end
        ORDER BY INPUT_SEQ DESC
       )
 WHERE ROWNUM = 1