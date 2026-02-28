package xyz.elidom.mw.rabbitmq.service.model.elastic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.BooleanUtils;

import xyz.elidom.dbist.dml.Filter;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * elastic 조회 쿼리 생성
 * 
 * @author yang
 */
public class RangeStatisticsQueryMaster {

/*{
 "size": 0,
 "query": {
	"match": {
	  "type.keyword": "trace_pub"
	}
  },
  "aggs": {
    "logTimeLong": {
      "range": {
        "field": "logTimeLong",
        "ranges": [ {
            "from": 1542002848044,
            "to": 1543002848044 
          }, {
            "from": 1543002848044,
            "to": 1544002848044 
          }, {
            "from": 1544002848044,
            "to": 1544417795536 
          }
        ]
      }
    }
  }
}*/
	
	// select row size
	private int size;

	// 조회 조건 
	private Map<String, Object> query;
	
	// 통계 조건 
	private Map<String,Object> aggs;

	public RangeStatisticsQueryMaster() {
		this.size = 0;
	}
	
	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public Map<String, Object> getQuery() {
		return query;
	}

	public void setQuery(Map<String, Object> query) {
		this.query = query;
	}
	
	public Map<String, Object> getAggs() {
		return aggs;
	}

	public void setAggs(Map<String, Object> aggs) {
		this.aggs = aggs;
	}
	
	public void setRanges(String name, String fieldName, List<Long> rangeList) {
		Map<String, Object> range = ValueUtil.newMap("field", fieldName);

		List<Map<String, Object>> ranges = new ArrayList<Map<String, Object>>(rangeList.size() -1);
		for(int i = 0 ; i < rangeList.size() - 1 ; i++) {
			ranges.add(ValueUtil.newMap("from,to", rangeList.get(i), rangeList.get(i+1)));
		}
			
		range.put("ranges", ranges)	;
		this.aggs = ValueUtil.newMap(name, ValueUtil.newMap("range", range));
	}
	
	@SuppressWarnings("unchecked")
	public void addFilter(Filter filter) {
		if(ValueUtil.isEmpty(this.query)) {
			this.query = ValueUtil.newMap("");
		}
		
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
				
				Map<String, Object> bool = ValueUtil.newMap("must", must);
				this.query.put("bool", bool); 
				this.addFilter(filter);
				
			} else if(this.query.size() == 0) {
				// ex) "query" : { "match" : {"type":"trace_pub"} }
				this.query = this.parseFilterToQuery(filter);
			}
		}
	}

	public void addFilters(Filter[] filters) {
		if(ValueUtil.isEmpty(filters)) return;
		
		for(Filter f : filters) {
			this.addFilter(f);
		}
	}

	private Map<String, Object> parseFilterToQuery(Filter filter) {
		String ope = filter.getOperator();
		
		if(ope.equalsIgnoreCase("eq") || ope.equalsIgnoreCase("=")) {			
			return ValueUtil.newMap("match", this.setFilterFieldMatch(filter.getLeftOperand(), filter.getRightOperand().get(0)));
			
		} else if(ope.equalsIgnoreCase("like")) {
			return ValueUtil.newMap("wildcard", this.setFilterFieldLike(filter.getLeftOperand(), filter.getRightOperand().get(0)));
			
		} else {
			return ValueUtil.newMap("");
		}
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
