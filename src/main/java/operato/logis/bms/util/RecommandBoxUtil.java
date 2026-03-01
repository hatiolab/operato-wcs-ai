package operato.logis.bms.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import operato.logis.bms.model.BmsBoxType;
import operato.logis.bms.model.BmsOrder;
import operato.logis.bms.model.BmsSku;

public class RecommandBoxUtil {

	/**
	 * 상품 길이 값 기준 가능한 박스 체크 
	 * @param boxTypeMap
	 * @param skuMap
	 * @param orderMap
	 * @return
	 */
	public static Map<String, BmsBoxType> getEnableLengthBoxTypeMap(Map<String, BmsBoxType> boxTypeMap, Map<String, BmsSku> skuMap, Map<String, BmsOrder> orderMap) {
		Map<String, BmsBoxType> enableBoxTypeMap = null;
		List<String> boxKeyList = new ArrayList<String>(boxTypeMap.keySet());
		List<String> orderKeyList = new ArrayList<String>(orderMap.keySet());
		
		for ( String boxKey : boxKeyList ) {
			BmsBoxType box = boxTypeMap.get(boxKey);
			boolean isEnable = true;
			for ( String orderKey : orderKeyList ) {
				BmsOrder order = orderMap.get(orderKey);
				BmsSku sku = skuMap.get(order.getSkuCd());
				if ( box.getDiagonal() < sku.getDiagonal() ) {
					isEnable = false;
				}
			}
			
			if ( isEnable ) {
				if ( enableBoxTypeMap == null ) {
					enableBoxTypeMap = new HashMap<String, BmsBoxType>();
				}
				
				enableBoxTypeMap.put(box.getBoxTypeCd(), box);
			}
		}
		
		return enableBoxTypeMap;
	}
	
	/**
	 * 박스 유형을 부피 기준으로 정렬
	 * @param map
	 * @param isDesc
	 * @return
	 */
	public static List<String> getSortBoxByVolume(Map<String, BmsBoxType> map, boolean isDesc) {
		List<String> keyList = new ArrayList<String>(map.keySet());
		
		if (isDesc ) {
			keyList.sort((o1, o2) -> map.get(o2).getVolume() - map.get(o1).getVolume());
		} else {
			keyList.sort((o1, o2) -> map.get(o1).getVolume() - map.get(o2).getVolume());
		}
		
		return keyList;
	}
	
	/**
	 * 주문에서 각 상품의 총 부피 기준으로 정렬  
	 * @param orderMap
	 * @param skuMap
	 * @param isDesc
	 * @return
	 */
	public static List<String> getSortOrderByVolume(Map<String, BmsOrder> orderMap, Map<String, BmsSku> skuMap, boolean isDesc) {
		List<String> keyList = new ArrayList<String>(orderMap.keySet());
		
		if (isDesc ) {
			keyList.sort((o1, o2) -> (orderMap.get(o2).getOrderQty() * skuMap.get(orderMap.get(o2).getSkuCd()).getVolume()) - (orderMap.get(o1).getOrderQty() * skuMap.get(orderMap.get(o1).getSkuCd()).getVolume()));
		} else {
			keyList.sort((o1, o2) -> (orderMap.get(o1).getOrderQty() * skuMap.get(orderMap.get(o1).getSkuCd()).getVolume()) - (orderMap.get(o2).getOrderQty() * skuMap.get(orderMap.get(o2).getSkuCd()).getVolume()));
		}
		
		return keyList;
	}
	
	/**
	 * 주문 정보의 전체 부피 계산 
	 * @param orderMap
	 * @param skuMap
	 * @return
	 */
	public static int getOrderTotalVolume(Map<String, BmsOrder> orderMap, Map<String, BmsSku> skuMap) {
		List<String> keyList = new ArrayList<String>(orderMap.keySet());
		
		int orderTotalVolume = 0;
		
		for( String orderKey : keyList ) {
			BmsOrder order = orderMap.get(orderKey);
			BmsSku sku = skuMap.get(order.getSkuCd());
			orderTotalVolume += order.getOrderQty() * sku.getVolume();
		}
		
		
		return orderTotalVolume;
	}
	
	/**
	 * 1. 상품 길이 값 기준 가능한 박스 체크 
	 * 2. 부피 값 기준 가능한 박스 체크
	 * @param boxTypeMap
	 * @param skuMap
	 * @param orderMap
	 * @param minVolume
	 * @return
	 */
	public static Map<String, BmsBoxType> getEnableBoxTypeMap(Map<String, BmsBoxType> boxTypeMap, Map<String, BmsSku> skuMap, Map<String, BmsOrder> orderMap, int minVolume) {
		Map<String, BmsBoxType> enableBoxTypeMap = getEnableLengthBoxTypeMap(boxTypeMap, skuMap, orderMap);
		
		if ( enableBoxTypeMap != null ) {
			enableBoxTypeMap = getEnableVolumeBoxTypeMap(enableBoxTypeMap, minVolume);
		}
		
		return enableBoxTypeMap;
	}
	
	/**
	 * 부피 값 기준 가능한 박스 체크 
	 * @param enableBoxTypeMap
	 * @param minVolume
	 * @return
	 */
	public static Map<String, BmsBoxType> getEnableVolumeBoxTypeMap(Map<String, BmsBoxType> enableBoxTypeMap, int minVolume) {
		
		if ( enableBoxTypeMap != null && enableBoxTypeMap.keySet().size() != 0 ) {
			List<String> boxKeyList = new ArrayList<String>(enableBoxTypeMap.keySet());
			
			for ( String boxKey : boxKeyList ) {
				BmsBoxType box = enableBoxTypeMap.get(boxKey);
				if ( box.getVolume() < minVolume ) {
					enableBoxTypeMap.remove(boxKey);
				}
			}
			
			if ( enableBoxTypeMap.keySet().size() == 0 ) {
				enableBoxTypeMap = null;
			}
		}
		
		return enableBoxTypeMap;
	}
	
	/**
	 * 주문 정보에서 상품 정보가 없는 것이 존재하는지 체크 
	 * @param skuMap
	 * @param orderMap
	 * @return
	 */
	public static boolean isNotExistSkuMaster(Map<String, BmsSku> skuMap, Map<String, BmsOrder> orderMap) {
		boolean result = false;
		List<String> orderKeyList = new ArrayList<String>(orderMap.keySet());
		for ( String orderKey : orderKeyList ) {
			BmsSku sku = skuMap.get(orderMap.get(orderKey).getSkuCd());
			if ( sku == null ) {
				result = true;
			}
		}
		
		return result;
	}
	
	/**
	 * 주문 정보에 박스 타입을 설정 
	 * @param orderMap
	 * @param boxTypeCd
	 * @return
	 */
	public static Map<String, BmsOrder> setOrderBoxType(Map<String, BmsOrder> orderMap, String boxTypeCd) {
		List<String> orderKeyList = RecommandBoxUtil.getSortOrderByOrderId(orderMap, false);
		for ( String orderKey : orderKeyList) {
			BmsOrder order = orderMap.get(orderKey);
			order.setBoxTypeCd(boxTypeCd);
		}
		
		return orderMap;
	}
	
	/**
	 * 주문 정보에서 주문 번호 기준으로 정렬 
	 * @param map
	 * @param isDesc
	 * @return
	 */
	public static List<String> getSortOrderByOrderId(Map<String, BmsOrder> map, boolean isDesc) {
		List<String> keyList = new ArrayList<String>(map.keySet());
		
		if (isDesc ) {
			keyList.sort((o1, o2) -> map.get(o2).getOrderId().compareTo(map.get(o1).getOrderId()));
		} else {
			keyList.sort((o1, o2) -> map.get(o1).getOrderId().compareTo(map.get(o2).getOrderId()));
		}
		
		return keyList;
	}
}
