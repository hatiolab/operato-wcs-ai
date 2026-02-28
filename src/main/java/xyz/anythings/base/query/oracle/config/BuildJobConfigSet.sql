WITH T_CONFIG_BASE AS (
    SELECT ID, COM_CD, STAGE_CD, JOB_TYPE, EQUIP_TYPE, EQUIP_CD, DEFAULT_FLAG
      FROM JOB_CONFIG_SET
     WHERE DOMAIN_ID = :domainId
       AND (DEFAULT_FLAG = 1 OR (COM_CD IS NOT NULL 
                                OR STAGE_CD IS NOT NULL
                                OR JOB_TYPE IS NOT NULL
                                OR EQUIP_TYPE IS NOT NULL
                                OR EQUIP_CD IS NOT NULL ))
),
T_CONFIG_RANK AS (
    SELECT X.ID
         , NVL(COM_CD, 'NULL') AS COM_CD
         , NVL(STAGE_CD, 'NULL') AS STAGE_CD
         , NVL(JOB_TYPE, 'NULL') AS JOB_TYPE
         , NVL(EQUIP_TYPE, 'NULL') AS EQUIP_TYPE
         , NVL(EQUIP_CD, 'NULL') AS EQUIP_CD
         , DEFAULT_FLAG
         , CASE WHEN DEFAULT_FLAG = 1 THEN 100
                WHEN COM_CD IS NOT NULL 
                     AND STAGE_CD IS NOT NULL 
                     AND JOB_TYPE IS NOT NULL 
                     AND EQUIP_TYPE IS NOT NULL 
                     AND EQUIP_CD IS NOT NULL THEN 10
                WHEN STAGE_CD IS NOT NULL 
                     AND JOB_TYPE IS NOT NULL 
                     AND EQUIP_TYPE IS NOT NULL 
                     AND EQUIP_CD IS NOT NULL THEN 20
                WHEN COM_CD IS NOT NULL 
                     AND JOB_TYPE IS NOT NULL 
                     AND EQUIP_TYPE IS NOT NULL 
                     AND EQUIP_CD IS NOT NULL THEN 30
                WHEN COM_CD IS NOT NULL 
                     AND STAGE_CD IS NOT NULL 
                     AND JOB_TYPE IS NOT NULL THEN 40
                WHEN STAGE_CD IS NOT NULL 
                     AND JOB_TYPE IS NOT NULL THEN 50
                WHEN STAGE_CD IS NOT NULL 
                     AND EQUIP_TYPE IS NOT NULL 
                     AND EQUIP_CD IS NOT NULL THEN 60
                WHEN COM_CD IS NOT NULL 
                     AND STAGE_CD IS NOT NULL THEN 70
                WHEN STAGE_CD IS NOT NULL THEN 80
                ELSE 90
            END AS SORT_RANK
      FROM T_CONFIG_BASE X
),
T_BATCH AS (
     SELECT COM_CD, STAGE_CD, JOB_TYPE, EQUIP_TYPE, EQUIP_CD
       FROM JOB_BATCHES
      WHERE DOMAIN_ID = :domainId
        AND ID = :batchId
),
T_BATCHES AS (
    SELECT COM_CD, STAGE_CD, JOB_TYPE, EQUIP_TYPE, EQUIP_CD, 0 AS DEFAULT_FLAG   -- 1. comCd, stageCd, jobType, equipType, equipCd
      FROM T_BATCH
     UNION ALL
    SELECT 'NULL', STAGE_CD, JOB_TYPE, EQUIP_TYPE, EQUIP_CD, 0   -- 2. stageCd, jobType, equipType, equipCd
      FROM T_BATCH
     UNION ALL
    SELECT COM_CD, 'NULL', JOB_TYPE, EQUIP_TYPE, EQUIP_CD, 0    -- 3. comCd, jobType, equipType, equipCd
      FROM T_BATCH
     UNION ALL
    SELECT COM_CD, STAGE_CD, JOB_TYPE, 'NULL', 'NULL', 0         -- 4. comCd, stageCd, jobType
      FROM T_BATCH
     UNION ALL
    SELECT 'NULL', STAGE_CD, JOB_TYPE, 'NULL', 'NULL', 0         -- 5. stageCd, jobType
      FROM T_BATCH
     UNION ALL
    SELECT 'NULL', STAGE_CD, 'NULL', EQUIP_TYPE, EQUIP_CD, 0     -- 6. stageCd, equipType, equipCd
      FROM T_BATCH
     UNION ALL
    SELECT COM_CD, STAGE_CD, 'NULL', 'NULL', 'NULL', 0           -- 7. comCd, stageCd
      FROM T_BATCH
     UNION ALL
    SELECT 'NULL', STAGE_CD, 'NULL', 'NULL', 'NULL', 0           -- 8. stageCd
      FROM T_BATCH       
     UNION ALL
    SELECT 'NULL', 'NULL', 'NULL', 'NULL', 'NULL', 1           -- 9. DEFAULT
      FROM T_BATCH       
),
T_CONFIG_SET AS (
    SELECT * 
      FROM T_CONFIG_RANK
     WHERE (COM_CD, STAGE_CD, JOB_TYPE, EQUIP_TYPE, EQUIP_CD, DEFAULT_FLAG ) 
            in (SELECT COM_CD, STAGE_CD, JOB_TYPE,EQUIP_TYPE, EQUIP_CD, DEFAULT_FLAG FROM T_BATCHES)
)
SELECT ID, JOB_CONFIG_SET_ID, NAME, VALUE
  FROM (
        SELECT X.ID
             , X.JOB_CONFIG_SET_ID
             , X.CATEGORY
             , X.NAME
             , X.VALUE
             , Y.SORT_RANK
             , RANK() OVER (PARTITION BY X.NAME ORDER BY Y.SORT_RANK ASC) AS FILTER_X
          FROM JOB_CONFIGS X
             , T_CONFIG_SET Y
         WHERE X.JOB_CONFIG_SET_ID = Y.ID
       )
 WHERE FILTER_X = 1 