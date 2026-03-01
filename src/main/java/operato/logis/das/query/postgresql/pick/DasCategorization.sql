select 
	equip_type,
	equip_cd,
	equip_nm,
	sku_cd,
	sku_nm,
	a.order_qty,
	a.picked_qty,
	#if($notAssortedMode)
	(a.order_qty - a.picked_qty) as pcs_qty
	#else
	a.order_qty as pcs_qty
	#end
from (
	select
		equip_type,
		equip_cd,
		equip_nm,
		sku_cd,
		sku_nm,
		sum(order_qty) as order_qty,
		COALESCE(sum(picked_qty), 0) as picked_qty
	from
		orders
	where
		domain_id = :domainId
		and com_cd = :comCd
		and sku_cd = :skuCd
		and batch_id in (:batchIdList)
	group by
		equip_type, equip_cd, equip_nm, sku_cd, sku_nm
	order by
		equip_cd
		#if($rackAsc)
		asc
		#else
		desc
		#end
) a