SELECT
    JOB.DOMAIN_ID,
    JOB.ID,
    JOB.BATCH_ID,
    JOB.STAGE_CD,
    JOB.COM_CD,
    JOB.JOB_TYPE,
    JOB.INPUT_SEQ,
    JOB.EQUIP_TYPE,
    JOB.EQUIP_CD,
    JOB.SUB_EQUIP_CD,
    JOB.SHOP_CD,
    JOB.SHOP_NM,
    JOB.ORDERER_ID,
    JOB.ORDERER_NM,
    COALESCE(JOB.PICK_QTY, 0) AS PICK_QTY,
    COALESCE(JOB.PICKING_QTY, 0) AS PICKING_QTY,
    COALESCE(JOB.PICKED_QTY, 0) AS PICKED_QTY,
    JOB.BOX_IN_QTY,
    JOB.ORDER_NO,
    JOB.SKU_CD,
    JOB.SKU_BARCD,
    JOB.SKU_NM,
    JOB.STATUS,
    JOB.BOX_ID,
    JOB.BOX_TYPE_CD,
    JOB.INVOICE_ID,
    JOB.ORDER_TYPE,
    JOB.CLASS_CD,
    JOB.BOX_CLASS_CD,
    JOB.PICK_STARTED_AT,
    JOB.PICK_ENDED_AT
FROM
    JOB_INSTANCES JOB
WHERE
    JOB.DOMAIN_ID = :domainId
    #if($id)
    AND JOB.ID = :id
    #end
    #if($batchId)
    AND JOB.BATCH_ID = :batchId
    #end
    #if($comCd)
    AND JOB.COM_CD = :comCd
    #end
    #if($status)
    AND JOB.STATUS = :status
    #end
    #if($statuses)
    AND JOB.STATUS IN (:statuses)
    #end
    #if($classCd)
    AND JOB.CLASS_CD = :classCd
    #end
    #if($boxClassCd)
    AND JOB.BOX_CLASS_CD = :boxClassCd
    #end
    #if($boxId)
    AND JOB.BOX_ID = :boxId
    #end
    #if($orderNo)
    AND JOB.ORDER_NO = :orderNo
    #end
    #if($skuCd)
    AND JOB.SKU_CD = :skuCd
    #end
    #if($equipType)
    AND JOB.EQUIP_TYPE = :equipType
    #end
    #if($equipCd)
    AND JOB.EQUIP_CD = :equipCd
    #end
    #if($pickQty)
    AND JOB.PICK_QTY = :pickQty
    #end
    #if($boxTypeCd)
    AND JOB.BOX_TYPE_CD = :boxTypeCd
    #end
ORDER BY
	#if($pickQty)
    JOB.PICKED_QTY ASC
    #else
    JOB.ORDER_NO
    #end
#if($onlyOne)
LIMIT 1
#end