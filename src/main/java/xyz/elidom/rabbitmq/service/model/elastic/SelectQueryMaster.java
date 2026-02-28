package xyz.elidom.rabbitmq.service.model.elastic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;

import com.google.gson.annotations.SerializedName;

import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.rabbitmq.service.ServiceUtil;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * elastic 조회 쿼리 생성 
 * @author yang
 *
 */
public class SelectQueryMaster {
	// select columns
	@SerializedName("_source")
	private String[] source;

	// select row size
	private int size;
	
	// select row from : from + 1
	private int from;
	
	// 정렬 
	private Map<String,SelectSort> sort;
	

	// 조회 조건 
	private Map<String,Object> query;
	
	/*
  "query" :{
    "bool" :{
      "must":[
        {"wildcard" : {"type":"*s*"}}
      ]
    }
  },
  
  
          "match": {
            "destId" : {
            	"query": "T042/T04288/2/GW93",
	            "minimum_should_match": "100%"
          	}
          }  
  
         {
         "range": {
           "pubTimeLong": {
             "gt": 1523318400000,
             "lte": 1523577600000
           }
         }
       }
  
  			// must : all true
			// should : or
			// must_not : false
			 * 
			//"wildcard" : { "user" : "ki*y" } : like
			//"match" : {}   : eq
			// "query" : { "match" : {"type":"trace_pub"} } ,



        {
          "query_string": {
              "default_field": "destId",
              "query": "///",
              "analyzer": "keyword"
          }
        }



		/*
  "query_string":{
    "default_operator" : "and",
    "analyze_wildcard": true,
    "default_field":"destId",
    "query":"\"?/T04281/4/?\""
  }          

			 * 
			 * 
			 * 
			 * 
			 * 
  	 */
	
	public String[] getSource() {
		return source;
	}

	public void setSource(String[] source) {
		this.source = source;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getFrom() {
		return from;
	}

	public void setFrom(int from) {
		this.from = from;
	}

	public Map<String, SelectSort> getSort() {
		return sort;
	}

	public void setSort(Map<String, SelectSort> sort) {
		this.sort = sort;
	}

	public Map<String, Object> getQuery() {
		return query;
	}

	public void setQuery(Map<String, Object> query) {
		this.query = query;
	}
	
	public <T> void addSort(String colName, Class<T> orderType, boolean isAsc) {
		if(ValueUtil.isEmpty(this.sort)) this.sort = new HashMap<String,SelectSort>();
		this.sort.put(FormatUtil.toCamelCase(colName), new SelectSort(isAsc ? "asc" :"desc", orderType));
	}
	
	@SuppressWarnings("unchecked")
	public void addFilter(Filter filter) {
		if(ValueUtil.isEmpty(this.query)) this.query = ValueUtil.newMap("");
		
		// 다중 조건  
		if(this.query.containsKey("bool")) {
			((Map<String, List<Object>>) this.query.get("bool")).get("must").add(this.parseFilterToQuery(filter));
		} else {
			// 단일 조건  D
			// 단일 조건 에서 추가 : 다중 조건으로 변경 
			if(this.query.size() == 1) {
				Map<String,Object> f = ValueUtil.newMap("");
				f.putAll(this.query);
				this.query.clear();
				
				List<Map<String,Object>> must = new ArrayList<Map<String,Object>>();
				must.add(f);
				
				Map<String,Object> bool = ValueUtil.newMap("must", must);
				
				this.query.put("bool", bool); 
				this.addFilter(filter);
				
			} else if(this.query.size() == 0 ){
				// ex ) "query" : { "match" : {"type":"trace_pub"} }
				this.query = this.parseFilterToQuery(filter);
			}
		}
	}

	public void addFilters(Filter[] filters) {
		if(ValueUtil.isEmpty(filters)) return;
		for(Filter f : filters) this.addFilter(f);
	}
	
	public void setSourceParseUnderscore(String[] source) {
		this.source = ServiceUtil.arrayStringToCamelCase(source);
	}

	private Map<String, Object> parseFilterToQuery(Filter filter) {
		String ope = filter.getOperator();
		
		if(ope.equalsIgnoreCase("eq") || ope.equalsIgnoreCase("=")) {			
			return ValueUtil.newMap("match", this.setFilterFieldMatch(filter.getLeftOperand(), filter.getRightOperand().get(0)));
		} else if(ope.equalsIgnoreCase("like")) {
			return ValueUtil.newMap("wildcard", this.setFilterFieldLike(filter.getLeftOperand(), filter.getRightOperand().get(0)));

		} else if(ope.equalsIgnoreCase("")) {
		}
		
		return ValueUtil.newMap("");
	}
	
	private Map<String, Object> setFilterFieldMatch(String col, Object value){
		if(BooleanUtils.toBooleanObject(value + "") == null) {
			return ValueUtil.newMap(FormatUtil.toCamelCase(col) + ".keyword", value);
		} else {
			return ValueUtil.newMap(FormatUtil.toCamelCase(col),value);
		}
	}
	
	private Map<String,Object> setFilterFieldLike(String col, Object value) {
		return ValueUtil.newMap(FormatUtil.toCamelCase(col) + ".keyword", "*" + value + "*");
	}
}
