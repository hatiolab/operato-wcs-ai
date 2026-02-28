select
	s.id, s.domain_id, s.equip_type, s.equip_cd, s.cell_cd, s.com_cd, s.sku_cd, s.sku_barcd, s.sku_nm,
	s.load_qty, s.alloc_qty, s.picked_qty, s.min_stock_qty, s.max_stock_qty,
	(COALESCE(s.load_qty, 0) - COALESCE(s.alloc_qty, 0)) as stock_qty,
	s.fixed_flag, c.active_flag
from
	stocks s
		inner join
	cells c
		on s.domain_id = c.domain_id and s.equip_type = c.equip_type and s.equip_cd = c.equip_cd and s.cell_cd = c.cell_cd
where
	s.domain_id = :domainId
	and c.active_flag = true
	#if($equipType)
	and s.equip_type = :equipType
	#end
	#if($equipCd)
	and s.equip_cd = :equipCd
	#end
	#if($cellCd)
	and s.cell_cd = :cellCd
	#end
	#if($comCd)
	and s.com_cd = :comCd
	#end
	#if($skuCd)
	and s.sku_cd = :skuCd
	#end
	#if($fixedFlag)
	and s.fixed_flag = :fixedFlag
	#end
	#if($status)
	and s.status = :status
	#end
	#if($minStockQty)
	and s.min_stock_qty < :minStockQty
	#end
	#if($maxStockQty)
	and s.max_stock_qty < :maxStockQty
	#end
	#if($batchId)
	and s.equip_cd in (select rack_cd from racks where domain_id = :domainId and batch_id = :batchId and status = 'RUN')
	#end