SELECT *
  FROM SKU X
 WHERE X.DOMAIN_ID = :domainId
   AND X.COM_CD = :comCd
   AND (X.DOMAIN_ID, X.COM_CD , X.SKU_CD ) 
        in (
            SELECT DOMAIN_ID, COM_CD, SKU_CD 
              FROM JOB_INSTANCES 
             WHERE DOMAIN_ID = :domainId
               AND COM_CD = :comCd
               AND BATCH_ID in (SELECT ID 
                                  FROM JOB_INSTANCES 
                                 WHERE DOMAIN_ID = :domainId
                                   AND BATCH_GROUP_ID = :batchGroupId ) 
             #if($status)
               AND STATUS in (:status)
             #end 
           )
   AND EXISTS (
                SELECT 1 FROM DUAL 
                 WHERE 1 != 1
	             #if($skuCd)
	             	OR X.SKU_CD = :skuCd
	             #end 
	             #if($skuBarcd)
                    OR X.SKU_BARCD = :skuBarcd
	             #end 
	             #if($boxBarcd)
                    OR X.BOX_BARCD = :boxBarcd
	             #end 
              )