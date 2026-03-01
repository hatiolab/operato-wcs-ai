package operato.logis.bms.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import operato.logis.bms.LogisBmsConstants;
import operato.logis.bms.model.BmsBoxType;
import operato.logis.bms.model.BmsOrder;
import operato.logis.bms.model.BmsRequest;
import operato.logis.bms.model.BmsSku;
import operato.logis.bms.util.RecommandBoxUtil;

@Component
public class RecommandBoxService {
	
	public Map<String, BmsOrder> recommandBox(BmsRequest bmsRequest, String splitType) {
		return this.recommandBox(bmsRequest.getBoxTypeMap(), bmsRequest.getSkuMap(), bmsRequest.getOrderMap(), splitType);
	}
	
	public Map<String, BmsOrder> recommandBox(Map<String, BmsBoxType> boxTypeMap, Map<String, BmsSku> skuMap, Map<String, BmsOrder> orderMap, String splitType) {
		Map<String, BmsOrder> resultOrderMap = new HashMap<String, BmsOrder>();
		
		if ( RecommandBoxUtil.isNotExistSkuMaster(skuMap, orderMap) ) {
			/* 1. 상품 마스터 누락 체크 */
			resultOrderMap = RecommandBoxUtil.setOrderBoxType(orderMap, LogisBmsConstants.NOT_EXIST_SKU_MASTER_BOX_TYPE);
		} else if ( RecommandBoxUtil.getEnableLengthBoxTypeMap(boxTypeMap, skuMap, orderMap) == null ) {
			/* 2. 이형 주문 체크 */
			resultOrderMap = RecommandBoxUtil.setOrderBoxType(orderMap, LogisBmsConstants.NOT_EXIST_ENABLE_BOX_TYPE);
		} else {
			/* 3. 박스 추천 */
			
			// 주문 전체 부피 
            int orderTotalVolume = RecommandBoxUtil.getOrderTotalVolume(orderMap, skuMap);
			
            // 주문 전체 부피에 사용 가능한 박스가 있는지 확인
			Map<String, BmsBoxType> enableBoxTypeMap = RecommandBoxUtil.getEnableBoxTypeMap(boxTypeMap, skuMap, orderMap, orderTotalVolume);
			
			if ( enableBoxTypeMap != null && enableBoxTypeMap.keySet().size() != 0 ) {
				// 주문 전체 부피를 허용하는 박스가 존재하는 경우
				
				/* 3.1 단일 박스 */ 
				String enableMinVolumBoxType = RecommandBoxUtil.getSortBoxByVolume(enableBoxTypeMap, false).get(0);
				resultOrderMap = RecommandBoxUtil.setOrderBoxType(orderMap, enableMinVolumBoxType);
			} else {
				// 주문 전체 부피를 허용하는 박스가 없는 경우 (분리포)
				
				/* 3.2 분리포 */
				if ( LogisBmsConstants.SPLIT_BY_VOLUME.equalsIgnoreCase(splitType) ) {
					/* 3.2.1 부피 기준 분리포 */
					resultOrderMap = this.splitBoxByVolume(boxTypeMap, skuMap, orderMap);
				} else {
					// Default Split Type : VOLUME
					resultOrderMap = this.splitBoxByVolume(boxTypeMap, skuMap, orderMap);
				}
			}
			
		}
		
		return resultOrderMap;
	}
	
	/**
	 * [부피 값 기준 분리포] 
	 * 분리포 할 때 부피 값이 허용되는지 체크하여 분리 
	 * 분리포 할 때 상품이 쪼개지지 않도록 분리 
	 * boxTypeMap 	: 전체 박스 정보
	 * skuMap 		: 주문에 대한 상품 마스터 정보 
	 * orderMap 	: 주문 정보
	 * @param boxTypeMap
	 * @param skuMap
	 * @param orderMap
	 * @return
	 */
	protected Map<String, BmsOrder> splitBoxByVolume(Map<String, BmsBoxType> boxTypeMap, Map<String, BmsSku> skuMap, Map<String, BmsOrder> orderMap ) {

		// 분리포 결과
		Map<String, BmsOrder> splitOrderMap = new HashMap<String, BmsOrder>();
		
		// 최초 가장 큰 박스 타입과 부피를 구함 
		Map<String, BmsBoxType> enableBoxTypeMap = RecommandBoxUtil.getEnableLengthBoxTypeMap(boxTypeMap, skuMap, orderMap);
		String maxBoxType = RecommandBoxUtil.getSortBoxByVolume(enableBoxTypeMap, true).get(0);
		int restBoxVolume = boxTypeMap.get(maxBoxType).getVolume();
		
		// 잔여 주문 및 부피에 대한 초기화 
		Map<String, BmsOrder> restOrderMap = new HashMap<String, BmsOrder>(orderMap);
		// 부피가 큰 주문 (상품 부피 * 수량) 순서로 정렬 
		List<String> restOrderKeyList = RecommandBoxUtil.getSortOrderByVolume(restOrderMap, skuMap, true);
		int restOrderVolume = RecommandBoxUtil.getOrderTotalVolume(orderMap, skuMap);
		
		int splitSeq = 1;
		
		while ( restOrderKeyList.size() > 0 ) {
			if ( restOrderVolume > boxTypeMap.get(maxBoxType).getVolume() ) {
				/* 잔여 부피가 가장 큰 박스 부피보다 큰 경우 */
				
				// 임시 분할 데이터 초기화 
				Map<String, BmsOrder> tempSplitOrderMap = new HashMap<String, BmsOrder>();
				// 현재 할당된 부피 초기화 
				int sumAssignVolume = 0;
				
				for ( String restOrderKey : restOrderKeyList )  {
					BmsOrder restOrder = restOrderMap.get(restOrderKey);
					BmsSku restSku = skuMap.get(restOrder.getSkuCd());
					int restSkuTotalVol = restOrder.getOrderQty() * restSku.getVolume();

					if ( restBoxVolume >= restSkuTotalVol ) {
						/* 박스 잔여 부피보다 상품 총 부피가 작은 경우 (모두 담을 수 있는 경우) */ 
						
						// 박스 잔여 부피에서 할당된 부피만큼 차감 
						restBoxVolume = restBoxVolume - restSkuTotalVol;
						// 남은 주문 부피에서 할당된 부피만큼 차감 
						restOrderVolume = restOrderVolume - restSkuTotalVol;
						// 현재 할당된 부피 합 계산 
						sumAssignVolume = sumAssignVolume + restSkuTotalVol;
						
						BmsOrder splitOrder = new BmsOrder(restOrder.getOrderId() + "-" + splitSeq, restOrder.getOrderLine(), restOrder.getSkuCd(), restOrder.getOrderQty());
						splitOrder.setBoxTypeCd(maxBoxType);
						// 분리포 주문 할당
						tempSplitOrderMap.put(splitSeq + "-" + restOrderKey, splitOrder);
						// 잔여 주문에서 할당 주문 삭제 
						restOrderMap.remove(restOrderKey);
					} else {
						/* 잔여 부피보다 상품 총 부피가 큰 경우 (현재 남은 부피에 모두 안담기는 경우) */ 
						if ( restSkuTotalVol > boxTypeMap.get(maxBoxType).getVolume() ) {
							/* 상품 전체 부피 (상품 부피 * 주문 수량) 가 가장 큰 박스 부피를 초과 하는 경우 주문 수량 분리 */ 
							if ( tempSplitOrderMap.keySet().size() == 0 ) {
								/* 현재 박스가 비어있는 경우에만 할당 (박스에 할당 할 수 있는 만큼 분리) */
								int maxInputCnt = restBoxVolume / restSku.getVolume();

								if ( maxInputCnt != 0 ) {
									// 박스 잔여 부피에서 할당된 부피만큼 차감 
									restBoxVolume = restBoxVolume - (restSku.getVolume() * maxInputCnt);
									// 남은 주문 부피에서 할당된 부피만큼 차감 
									restOrderVolume = restOrderVolume - (restSku.getVolume() * maxInputCnt);
									// 현재 할당된 부피 합 계산
									sumAssignVolume = sumAssignVolume + (restSku.getVolume() * maxInputCnt);

									// 분리포 주문 생성 
									BmsOrder splitOrder = new BmsOrder(restOrder.getOrderId() + "-" + splitSeq, restOrder.getOrderLine(), restOrder.getSkuCd(), maxInputCnt);
									splitOrder.setBoxTypeCd(maxBoxType);
									
									// 분리포 주문 할당
									tempSplitOrderMap.put(splitSeq + "-" + restOrderKey, splitOrder);
									
									// 잔여 주문에서 할당 수량 차감  
									restOrder.setOrderQty(restOrder.getOrderQty() - maxInputCnt);
									
									// 잔여 수량이 0인 경우 할당 주문 삭제
									if ( restOrder.getOrderQty() == 0 ) {
										restOrderMap.remove(restOrderKey);
									}
								}
							}
						}
					}
				}
				
				/* 최적화 작업 */
				String enableMinVolumBoxType = RecommandBoxUtil.getSortBoxByVolume(RecommandBoxUtil.getEnableBoxTypeMap(boxTypeMap, skuMap, tempSplitOrderMap, sumAssignVolume), false).get(0);

				List<String> orderKeyList = new ArrayList<String>(tempSplitOrderMap.keySet());
				for ( String orderKey : orderKeyList) {
					BmsOrder order = tempSplitOrderMap.get(orderKey);
					order.setBoxTypeCd(enableMinVolumBoxType);
					
					splitOrderMap.put(orderKey, order);
				}
				
				// 분리포 시퀀스 증가 
				splitSeq++;
				// 박스 잔여 부피 리셋
				restBoxVolume = boxTypeMap.get(maxBoxType).getVolume();
			} else {
				/* 
				 * 잔여 부피가 가장 큰 박스 부피보다 작은 경우 (마지막 박스)
				 * 잔여 부피를 충족하는 가장 작은 박스 추천 
				 */ 
				String enableMinVolumBoxType = RecommandBoxUtil.getSortBoxByVolume(RecommandBoxUtil.getEnableBoxTypeMap(boxTypeMap, skuMap, restOrderMap, restOrderVolume), false).get(0);
				
				restBoxVolume = boxTypeMap.get(enableMinVolumBoxType).getVolume();
				
				for ( String restOrderKey : restOrderKeyList) {
					BmsOrder restOrder = restOrderMap.get(restOrderKey);
					BmsSku restSku = skuMap.get(restOrder.getSkuCd());
					
					// 박스 잔여 부피에서 할당된 부피만큼 차감 
					restBoxVolume = restBoxVolume - (restSku.getVolume() * restOrder.getOrderQty());
					// 남은 주문 부피에서 할당된 부피만큼 차감 
					restOrderVolume = restOrderVolume - (restSku.getVolume() * restOrder.getOrderQty());
					
					// 분리포 주문 생성 
					BmsOrder splitOrder = new BmsOrder(restOrder.getOrderId() + "-" + splitSeq, restOrder.getOrderLine(), restOrder.getSkuCd(), restOrder.getOrderQty());
					splitOrder.setBoxTypeCd(enableMinVolumBoxType);
					
					// 분리포 주문 할당
					splitOrderMap.put(splitSeq + "-" + restOrderKey, splitOrder);
					
					// 잔여 주문에서 할당 주문 삭제 
					restOrderMap.remove(restOrderKey);
				}
			}
			
			// 잔여 주문 수 계산
			restOrderKeyList = RecommandBoxUtil.getSortOrderByVolume(restOrderMap, skuMap, true);
		}
		
		return splitOrderMap;
	}
	
}