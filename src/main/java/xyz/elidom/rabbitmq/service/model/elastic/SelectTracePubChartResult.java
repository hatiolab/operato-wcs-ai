package xyz.elidom.rabbitmq.service.model.elastic;

import java.util.List;
import java.util.Map;

/**
 * 엘라스틱 publish 메시지 조회 결과 모델 
 * @author yang
 *
 */
public class SelectTracePubChartResult {
	
	private Map<String,Map<String,List<SelectTracePubChartResultBucket>>> aggregations;

	public Map<String, Map<String, List<SelectTracePubChartResultBucket>>> getAggregations() {
		return aggregations;
	}

	public void setAggregations(Map<String, Map<String, List<SelectTracePubChartResultBucket>>> aggregations) {
		this.aggregations = aggregations;
	}

}
