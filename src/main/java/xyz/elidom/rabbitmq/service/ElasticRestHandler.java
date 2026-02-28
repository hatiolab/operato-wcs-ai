package xyz.elidom.rabbitmq.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.model.trace.ITraceModel;
import xyz.elidom.rabbitmq.service.model.elastic.Acknowledged;
import xyz.elidom.rabbitmq.service.model.elastic.RangeStatisticsQueryMaster;
import xyz.elidom.rabbitmq.service.model.elastic.SelectQueryMaster;
import xyz.elidom.rabbitmq.service.model.elastic.SelectTraceDeadResult;
import xyz.elidom.rabbitmq.service.model.elastic.SelectTraceErrorResult;
import xyz.elidom.rabbitmq.service.model.elastic.SelectTracePubChartResult;
import xyz.elidom.rabbitmq.service.model.elastic.SelectTracePubResult;
import xyz.elidom.rabbitmq.service.model.elastic.SelectTraceSubResult;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 엘라스틱 서비스 
 * @author yang
 *
 */
@Component
public class ElasticRestHandler {
	@Autowired
	private RabbitmqProperties mqProperties;
	
	private Logger logger = LoggerFactory.getLogger(ElasticRestHandler.class);
	
	RestClient elasticClient;

	// bulk make index set id
//	String indexMeta = "{\"index\": {\"_id\": \"%s\"}}\n";
	// bulk make index auto gen id
	String indexMeta = "{\"index\": {}}\n";
	
	/**
	 * 초기화 
	 */
	public void setElasticClient() {
		this.elasticClient = RestClient.builder(new HttpHost(this.mqProperties.getTraceElasticAddress(), this.mqProperties.getTraceElasticPort(), "http")).build();
	}
	
	/**
	 * bulk insert
	 * @param index
	 * @param type
	 * @param logs
	 * @throws Exception
	 */
	public void InsertBulk(String index, String type, List<ITraceModel> logs ) throws Exception{
		
		StringBuilder body = new StringBuilder();
		
		for(ITraceModel obj : logs) {
			body.append(indexMeta);
			body.append(obj.toJsonString());
			body.append("\n");
		}
		
		try {
			elasticRestRequest("POST", String.format(ElasticUrls.BULK_OPE, index, type), body.toString());
		}catch(Exception e) {
			logger.error("", e);
			throw e;
		}
	}
	
	/**
	 * 인덱스 리스트 조회 
	 * @return
	 * @throws Exception
	 */
	public List<String> getIndexs() throws Exception{
		
		List<String> indexList = new ArrayList<String>();
		
		try {
			Map<?,?> sss  = FormatUtil.jsonToObject(elasticRestRequest("GET", ElasticUrls.ALL_INDEXS, null), Map.class);
			Iterator<?> it = sss.keySet().iterator();
			while(it.hasNext()) indexList.add(it.next().toString());
			
			return indexList;
		} catch (Exception e) {
			logger.error("", e);
			throw e;
		}
	}
	
	/**
	 * 인덱스 삭제 
	 * @param index
	 * @return
	 */
	public boolean deleteIndex(String index) throws Exception{
		try {
			logger.info("delete elastic idx : " + index);
			
			Acknowledged result = FormatUtil.jsonToObject(elasticRestRequest("DELETE", String.format(ElasticUrls.INDEX_DELETE, index),null), Acknowledged.class);
			
			if(result.getAcknowledged()) return true;
			else return false;
		}catch(Exception e) {
			logger.error("", e);
			throw e;
		}
	}
	
	/**
	 * 퍼블리시 리스트 조회 
	 * @param index
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public SelectTracePubChartResult searchTracePublishChart(String index, RangeStatisticsQueryMaster query) throws Exception {
		SelectTracePubChartResult result = FormatUtil.jsonToObject(this.searchRangeChart(index, query), SelectTracePubChartResult.class);
		return result;
	}	
	/**
	 * 퍼블리시 리스트 조회 
	 * @param index
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public SelectTracePubResult searchTracePublishList(String index, SelectQueryMaster query) throws Exception {
		SelectTracePubResult result = FormatUtil.jsonToObject(this.searchList(index, query), SelectTracePubResult.class);
		return result;
	}
	/**
	 * 서브스크라이브 리스트 조회 
	 * @param index
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public SelectTraceSubResult searchTraceSubList(String index, SelectQueryMaster query) throws Exception {
		SelectTraceSubResult result = FormatUtil.jsonToObject(this.searchList(index, query), SelectTraceSubResult.class);
		return result;
	}
	/**
	 * dead 리스트 조회 
	 * @param index
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public SelectTraceDeadResult searchTraceDeadList(String index, SelectQueryMaster query) throws Exception {
		SelectTraceDeadResult result = FormatUtil.jsonToObject(this.searchList(index, query), SelectTraceDeadResult.class);
		return result;
	}
	/**
	 * 에러 리스트 조회 
	 * @param index
	 * @param query
	 * @return
	 * @throws Exception
	 */
	public SelectTraceErrorResult searchTraceErrorList(String index, SelectQueryMaster query) throws Exception{
		SelectTraceErrorResult result = FormatUtil.jsonToObject(this.searchList(index, query), SelectTraceErrorResult.class);
		return result;
	}
	
	/**
	 * 조회 
	 * @param index
	 * @param query
	 * @return
	 * @throws Exception
	 */
	private String searchRangeChart(String index, RangeStatisticsQueryMaster query) throws Exception{
		String queryString = FormatUtil.toUnderScoreJsonString(query);
		
		logger.info(String.format("elastic idx : %s \nquery : %s", (ValueUtil.isEmpty(index)?"NULL":index), queryString));
		
		String searchUrl = String.format(ElasticUrls.INDEX_SEARCH, index);
		
		try {
			String response = this.elasticRestRequest("POST", searchUrl, queryString);
			return response;
		}catch(ResponseException e) {
			throw e;
		}
	}
	
	/**
	 * 조회 
	 * @param index
	 * @param query
	 * @return
	 * @throws Exception
	 */
	private String searchList(String index, SelectQueryMaster query) throws Exception{
		String queryString = FormatUtil.toUnderScoreJsonString(query);
		
		logger.info(String.format("elastic idx : %s \nquery : %s", (ValueUtil.isEmpty(index)?"NULL":index), queryString));
		
		String searchUrl = ValueUtil.isEmpty(index) ? ElasticUrls._SEARCH : String.format(ElasticUrls.INDEX_SEARCH, index);
		
		while(true) {
			try {
				return this.elasticRestRequest("POST", searchUrl, queryString);
			}catch(ResponseException e) {
				String eMessage = e.getMessage().toLowerCase();
				
				if(eMessage.indexOf("query_phase_execution_exception") == -1) throw e;
				if(eMessage.indexOf("result window is too large") == -1 ) throw e;
				
				// max result 에 대한 lock 풀기 default 10000 
				// {"index.max_result_window" : "3000000"}
				//logger.info("max row Exception Catch");
				elasticRestRequest("PUT", String.format(ElasticUrls._SEARCH_MAX_ROW, index), "{\"index.max_result_window\" : \"3000000\"}");
			}
		}
	}
	
	private String elasticRestRequest(String method, String url, String qryString) throws Exception {
		Request request = new Request(method, url);
		if (qryString != null && !qryString.isEmpty()) {
			request.setEntity(getStringEntity(qryString));
		}
		Response response = elasticClient.performRequest(request);
		return EntityUtils.toString(response.getEntity());
	}
	
	private StringEntity getStringEntity(String queryString) {
		if(ValueUtil.isEmpty(queryString)) return null;
		return new StringEntity(queryString, ContentType.APPLICATION_JSON);
	}
	
	public void DeleteIndexs(String... indexs) {
	}
}
