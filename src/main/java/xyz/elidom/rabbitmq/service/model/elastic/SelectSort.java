package xyz.elidom.rabbitmq.service.model.elastic;

/**
 * 엘라스틱 조회 정렬 모델 
 * @author yang
 *
 */
public class SelectSort {
	// asc or desc
	private String order;
	private String unmappedType;

	public <T> SelectSort(String order, Class<T> inputType) {
		this.setOrder(order);
		this.setUnmappedType(inputType.getName());
	}
	
	public String getOrder() {
		return order;
	}

	public void setOrder(String order) {
		this.order = order;
	}

	public String getUnmappedType() {
		return unmappedType;
	}

	public void setUnmappedType(String unmappedType) {
		this.unmappedType = unmappedType;
	}
}
