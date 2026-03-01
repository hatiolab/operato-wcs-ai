package operato.logis.bms.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import xyz.elidom.sys.util.ValueUtil;

public class BmsRequest {
	
/*
{
  "box": [
    {"box_type_cd": "B07","box_nm": "7호","width": "400","length": "330","height": "320","error_range": "30","vol_plus": "0","vol_minus": "0"},
    {"box_type_cd": "B06","box_nm": "6호","width": "390","length": "320","height": "240","error_range": "30","vol_plus": "0","vol_minus": "0"},
    {"box_type_cd": "B05","box_nm": "7호","width": "330","length": "300","height": "270","error_range": "30","vol_plus": "0","vol_minus": "0"},
    {"box_type_cd": "B04","box_nm": "4호","width": "330","length": "300","height": "200","error_range": "30","vol_plus": "0","vol_minus": "0"},
    {"box_type_cd": "B03","box_nm": "3호","width": "300","length": "270","height": "190","error_range": "30","vol_plus": "0","vol_minus": "0"},
    {"box_type_cd": "B02","box_nm": "2호","width": "330","length": "200","height": "200","error_range": "30","vol_plus": "0","vol_minus": "0"}
  ],
  "sku": [
    {"sku_cd": "10630711","sku_nm": "(위)자연퐁 솔잎 R 3.04L(Y19)","width": "150","length": "107","height": "295"},
    {"sku_cd": "18290012","sku_nm": "긴노스푼 해피퓨레(참치&가다랑어포)","width": "223","length": "160","height": "110"},
    {"sku_cd": "18290023","sku_nm": "미쓰보시 구루메 쥬레 참치&가다랑어포","width": "135","length": "88","height": "75"},
    {"sku_cd": "18290014","sku_nm": "긴노스푼 해피퓨레(참치)","width": "183","length": "60","height": "105"}
  ],
  "order": [
    {"order_id": "A000001","order_line": "1","sku_cd": "10630711","order_qty": "10"},
    {"order_id": "A000001","order_line": "2","sku_cd": "18290012","order_qty": "10"},
    {"order_id": "A000001","order_line": "3","sku_cd": "18290023","order_qty": "10"},
    {"order_id": "A000001","order_line": "4","sku_cd": "18290014","order_qty": "10"}
  ]
}
*/

	private List<BmsBoxType> box;
	
	private List<BmsSku> sku;
	
	private List<BmsOrder> order;
	
	private Map<String, BmsBoxType> boxTypeMap;
	
	private Map<String, BmsSku> skuMap;
	
	private Map<String, BmsOrder> orderMap;

	public List<BmsBoxType> getBox() {
		return box;
	}

	public void setBox(List<BmsBoxType> box) {
		this.box = box;
	}

	public List<BmsSku> getSku() {
		return sku;
	}

	public void setSku(List<BmsSku> sku) {
		this.sku = sku;
	}

	public List<BmsOrder> getOrder() {
		return order;
	}

	public void setOrder(List<BmsOrder> order) {
		this.order = order;
	}
	
	/**
	 * 박스 추천에 사용될 박스 정보 파라미터
	 * @return
	 */
	public Map<String, BmsBoxType> getBoxTypeMap() {
		if ( ValueUtil.isEmpty(boxTypeMap) ) {
			boxTypeMap = new HashMap<String, BmsBoxType>();
		} else {
			boxTypeMap.clear();
		}
		for ( BmsBoxType item : this.getBox() ) {
			// 대각선 길이 계산 
			item.calDiagonal();
			// 부피 계산
			item.calVolume();
			boxTypeMap.put(item.getBoxTypeCd(), item);
		}
		
		return boxTypeMap;
	}
	
	/**
	 * 박스 추천에 사용될 상품 정보 파라미터
	 * @return
	 */
	public Map<String, BmsSku> getSkuMap() {
		if ( ValueUtil.isEmpty(skuMap) ) {
			skuMap = new HashMap<String, BmsSku>();
		} else {
			skuMap.clear();
		}
		for ( BmsSku item : this.getSku() ) {
			// 대각선 길이 계산
			item.calDiagonal();
			// 부피 계산
			item.calVolume();
			skuMap.put(item.getSkuCd(), item);
		}
		
		return skuMap;
	}
	
	/**
	 * 박스 추천에 사용될 주문 정보 파라미터
	 * @return
	 */
	public Map<String, BmsOrder> getOrderMap() {
		if ( ValueUtil.isEmpty(orderMap) ) {
			orderMap = new HashMap<String, BmsOrder>();
		} else {
			orderMap.clear();
		}
		for ( BmsOrder item : this.getOrder() ) {
			orderMap.put(ValueUtil.checkValue(item.getOrderLine(), UUID.randomUUID().toString()), item);
		}
		
		return orderMap;
	}
	
}
