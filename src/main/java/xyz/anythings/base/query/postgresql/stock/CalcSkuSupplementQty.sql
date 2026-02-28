select
	stock.equip_type,
	stock.equip_cd,
	stock.com_cd,
	stock.sku_cd,
	stock.sku_nm,
	stock.order_qty,
	stock.load_qty,
	stock.alloc_qty,
	stock.picked_qty,
	stock.stock_qty,
	stock.need_stock_qty,
	stock.need_stock_qty as input_qty
from (
	select
		s.equip_type,
		s.equip_cd,
		s.com_cd,
		s.sku_cd,
		s.sku_nm,
		s.order_qty,
		s.load_qty,
		s.alloc_qty,
		s.picked_qty,
		s.stock_qty,
		s.order_qty - s.load_qty as need_stock_qty
	from (
		select
			c.equip_type,
			COALESCE(b.equip_cd, c.equip_cd) as equip_cd,
			c.com_cd,
			c.sku_cd,
			c.sku_nm,
			c.order_qty,
			(c.alloc_qty + COALESCE(b.stock_qty, 0)) as load_qty,
			c.alloc_qty,
			c.picked_qty,
			COALESCE(b.stock_qty, 0) as stock_qty
		from
			(select
				o.equip_type,
				o.equip_cd,
				o.com_cd,
				o.sku_cd,
				max(o.sku_nm) as sku_nm,
				sum(o.order_qty) as order_qty,
				sum(COALESCE(o.assign_qty, 0)) as alloc_qty,
				sum(COALESCE(o.picked_qty, 0)) as picked_qty
			from
				job_batches j inner join orders o on j.domain_id = o.domain_id and j.id = o.batch_id
			where
				j.domain_id = :domainId
				and j.job_type = 'DPS'
				and j.status = 'RUN'
				and o.order_type = 'MT'
				#if($equipType)
				and o.equip_type = :equipType
				#end
				#if($equipCd)
				and o.equip_cd = :equipCd
				#end
				#if($comCd)
				and o.com_cd = :comCd
				#end
				#if($skuCd)
				and o.sku_cd = :skuCd
				#end
			group by
				o.equip_type, o.equip_cd, o.com_cd, o.sku_cd) c
			
			left outer join

			(select
				s.equip_type,
				s.equip_cd,
				s.com_cd,
				s.sku_cd,
				sum(load_qty - alloc_qty) as stock_qty
			from
				stocks s inner join cells c on
				s.domain_id = c.domain_id and s.equip_type = c.equip_type and s.equip_cd = c.equip_cd and s.cell_cd = c.cell_cd
			where
				s.domain_id = :domainId
				and c.active_flag = true
				#if($equipType)
				and s.equip_type = :equipType
				#end
				#if($equipCd)
				and s.equip_cd = :equipCd
				#end
				#if($comCd)
				and s.com_cd = :comCd
				#end
				#if($skuCd)
				and s.sku_cd = :skuCd
				#end
			group by
				s.equip_type, s.equip_cd, s.com_cd, s.sku_cd
			) b

			on c.equip_type = b.equip_type and c.equip_cd = b.equip_cd and c.com_cd = b.com_cd and c.sku_cd = b.sku_cd
	) s
) stock
order by
	stock.equip_type,
	stock.equip_cd,
	stock.need_stock_qty desc,
	stock.order_qty desc,
	stock.sku_cd