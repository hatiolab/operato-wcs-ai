package xyz.elidom.rabbitmq.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import xyz.elidom.orm.IQueryManager;
import xyz.elidom.rabbitmq.model.trace.ITraceModel;
import xyz.elidom.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;

/**
 * 트레이스 메시지 기록 클래스  
 * @author yang
 *
 */
public class TraceWriteManager {
	
	IQueryManager queryManager;
	SimpleDateFormat logFileFormat;
	SimpleDateFormat dateFormat;
	
	String traceType ;
	String traceMode ;
	String path ;
	String fileName = "%s.log";
	String errFileName = "error_%s.log";
	
	
	Logger logger;
	
//	RestHighLevelClient restElastic;
	
	/**
	 * 초기화 
	 * db, file, elastic s
	 * @param logger
	 * @param traceType
	 * @param queryManager db매니저 
	 * @param traceElasticAddress 엘라스틱 접속 정보 
	 * @param traceElasticPort
	 * @param traceMode 
	 * @param traceFileRoot
	 */
	public TraceWriteManager(Logger logger, String traceType, IQueryManager queryManager, String traceElasticAddress, int traceElasticPort, String traceMode, String traceFileRoot) {
		
		this.logger = logger;
		
		this.traceType = traceType;
		this.traceMode = traceMode;
		
		this.queryManager = queryManager;
		
		this.path = traceFileRoot.endsWith("/") ? traceFileRoot : traceFileRoot+"/";
		this.path = this.path + "%s" + File.separator;
		
		this.path = String.format(this.path, traceMode) ;
		
		this.logFileFormat = new SimpleDateFormat("yyyyMMddHHmm");
		this.dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		
		if(traceType.equalsIgnoreCase("elastic")) {
			/*
			RestClientBuilder lowLevelRestClient = RestClient.builder(new HttpHost(traceElasticAddress, traceElasticPort, "http"));
			this.restElastic = new RestHighLevelClient(lowLevelRestClient);
			*/
		}
	}

	/**
	 * 트레이스 기록 
	 * @param objList
	 * @throws Exception
	 */
	public void write(List<ITraceModel> objList) throws Exception{
		if(objList.size() == 0 ) return;
		
		String logDate = this.dateFormat.format(new Date());
		
		if(traceType.equalsIgnoreCase("db")) queryManager.insertBatch(objList);
		else if(traceType.equalsIgnoreCase("file")) {
			String currentFileName = String.format(this.fileName, this.logFileFormat.format(new Date()));
			FileUtils.writeStringToFile(new File(path  + logDate + File.separator + currentFileName), FormatUtil.toJsonString(objList), "UTF-8", true);
		} else if(traceType.equals("elastic")) {
			BeanUtil.get(ElasticRestHandler.class).InsertBulk(logDate, "trace", objList);
		}
	}
	
	/**
	 * 에러 기록 
	 * @param objList
	 * @throws Exception
	 */
	public void writeError(List<ITraceModel> objList) throws Exception {
		String logDate = this.dateFormat.format(new Date());
		
		if(traceType.equalsIgnoreCase("db")) queryManager.insertBatch(objList);
		else if(traceType.equalsIgnoreCase("file")) {
			String currentFileName = String.format(this.errFileName, this.logFileFormat.format(new Date()));
			FileUtils.writeStringToFile(new File(path  + logDate + File.separator + currentFileName), FormatUtil.toJsonString(objList), "UTF-8", true);
		} else if(traceType.equals("elastic")) {
			BeanUtil.get(ElasticRestHandler.class).InsertBulk(logDate, "trace", objList);
		}
	}
}
