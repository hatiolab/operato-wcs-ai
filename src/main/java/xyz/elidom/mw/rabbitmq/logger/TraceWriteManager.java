package xyz.elidom.mw.rabbitmq.logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;

import xyz.elidom.mw.rabbitmq.model.trace.ITraceModel;
import xyz.elidom.mw.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.FormatUtil;

/**
 * 트레이스 메시지 기록 클래스
 * 
 * @author yang
 */
public class TraceWriteManager {
	
	@SuppressWarnings("unused")
	private Logger logger;
	private IQueryManager queryManager;
	private SimpleDateFormat logFileFormat;
	private SimpleDateFormat dateFormat;
	
	private String traceType;
	@SuppressWarnings("unused")
	private String traceMode;
	private String path ;
	private String fileName = "%s.log";
	private String errFileName = "error_%s.log";	
		
	/**
	 * 초기화  - db, file, elastic
	 * 
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
	}

	/**
	 * 트레이스 기록
	 * 
	 * @param objList
	 * @throws Exception
	 */
	public void write(List<ITraceModel> objList) throws Exception {
		if(objList.size() == 0) {
			return;
		}
		
		String logDate = this.dateFormat.format(new Date());
		
		if(traceType.equalsIgnoreCase("db")) {
			this.queryManager.insertBatch(objList);
			
		} else if(traceType.equalsIgnoreCase("file")) {
			String currentFileName = String.format(this.fileName, this.logFileFormat.format(new Date()));
			FileUtils.writeStringToFile(new File(path  + logDate + File.separator + currentFileName), FormatUtil.toJsonString(objList), "UTF-8", true);
			
		} else if(traceType.equals("elastic")) {
			BeanUtil.get(ElasticRestHandler.class).InsertBulk(logDate, "trace", objList);
		}
	}
	
	/**
	 * 에러 기록
	 * 
	 * @param objList
	 * @throws Exception
	 */
	public void writeError(List<ITraceModel> objList) throws Exception {
		String logDate = this.dateFormat.format(new Date());
		
		if(traceType.equalsIgnoreCase("db")) {
			this.queryManager.insertBatch(objList);
			
		} else if(traceType.equalsIgnoreCase("file")) {
			String currentFileName = String.format(this.errFileName, this.logFileFormat.format(new Date()));
			FileUtils.writeStringToFile(new File(path  + logDate + File.separator + currentFileName), FormatUtil.toJsonString(objList), "UTF-8", true);
			
		} else if(traceType.equals("elastic")) {
			BeanUtil.get(ElasticRestHandler.class).InsertBulk(logDate, "trace", objList);
		}
	}
}
