package xyz.elidom.mw.rabbitmq.logger;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;

import xyz.elidom.mw.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.mw.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ThreadUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 트레이스 로그 메시지 삭제 관리자
 * 
 * @author yang
 */
@Component
public class TraceDeleteManager {
	/**
	 * Logger
	 */
	private Logger logger = LoggerFactory.getLogger(TraceDeleteManager.class);
	/**
	 * RabbitMQ 프로퍼티
	 */
	@Autowired
	private RabbitmqProperties mqProperties;
	/**
	 * Date Format
	 */
	private SimpleDateFormat dateFormat;
	/**
	 * 삭제 체크 주기
	 */
	private int checkPeriodSec = 600 * 1000;
	/**
	 * 트레이스 파일이 보관되어 있는 루트 패스
	 */
	private String traceFileRootPath;
	/**
	 * 마지막 삭제일
	 */
	private String befDeleteDate = "";
	/**
	 * 트레이스 모드
	 */
	private String[] traceModes = new String[] { "trace_pub", "trace_sub", "trace_dead" };
	
	/**
	 * 삭제 프로세스 시작 
	 */
	public void processStart() {
		String tracekeepData = this.mqProperties.getTraceKeepDate();
		
		if(ValueUtil.isNotEmpty(tracekeepData) && !tracekeepData.equals("0")) {
	 		this.dateFormat = new SimpleDateFormat("yyyyMMdd");
	 		
	 		if(this.mqProperties.getTraceType().equalsIgnoreCase("db")) {
	 			BeanUtil.get(TraceDeleteManager.class).deleteDbHistory();
	 			
	 		} else if (mqProperties.getTraceType().equals("file")) {
	 			BeanUtil.get(TraceDeleteManager.class).deleteFileHistory();
	 			
	 		} else if (mqProperties.getTraceType().equals("elastic")) {
				BeanUtil.get(TraceDeleteManager.class).deleteElasticHistory();
	 		}
		}
	}
	
	/**
	 * 엘라스틱 삭제 
	 */
	@Async("mwTracePool")
	public void deleteElasticHistory() {
		while(true) {
			try {
				String currentDate = this.dateFormat.format(new Date());
				boolean isAction = this.actionDeleteProcess(currentDate);
				
				if(isAction) {
					String delDate = this.addDate(Integer.parseInt(this.mqProperties.getTraceKeepDate()) * -1);
					long del = Long.parseLong(delDate);
					List<String> indexs = BeanUtil.get(ElasticRestHandler.class).getIndexs();
					
					boolean delFlag = false;
					for(String index : indexs) {
						delFlag = false;
						try {
							long indexDate = Long.parseLong(index);
							if(indexDate < del) {
								delFlag = true;
							}
						} catch(Exception e) {
							delFlag = true;
						}
						
						if(delFlag) {
							BeanUtil.get(ElasticRestHandler.class).deleteIndex(index);
						}
					}
					
					this.befDeleteDate = currentDate;
				}
			} catch(Exception e) {
				logger.error("Error - Delete elastic trace histories : " , e);
				
			} finally {
				ThreadUtil.sleep(this.checkPeriodSec);
			}
		}
	}

	/**
	 * DB 히스토리 삭제 
	 */
	@Async("mwTracePool")
	public void deleteDbHistory() {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		int onceMaxRow = 10000;
		long dbTransactionInterval = 60 * 1000;
		
		Map<String, String> qryMap = new HashMap<String, String>();
		qryMap.put("trace_pub", "DELETE FROM MQ_TRACE_PUBLISH_LOG WHERE LOG_TIME < :del_date AND ROWNUM <= " + onceMaxRow);
		qryMap.put("trace_sub", "DELETE FROM MQ_TRACE_DELIVER_LOG WHERE LOG_TIME < :del_date AND ROWNUM <= " + onceMaxRow);
		qryMap.put("trace_dead", "DELETE FROM MQ_TRACE_DEAD_LOG WHERE LOG_TIME < :del_date AND ROWNUM <= " + onceMaxRow);
		qryMap.put("trace_err", "DELETE FROM MQ_TRACE_ERROR_LOG WHERE ERR_DATE < :del_date AND ROWNUM <= " + onceMaxRow);
		
		while(true) {
			try {
				String currentDate = this.dateFormat.format(new Date());
				boolean isAction = this.actionDeleteProcess(currentDate);
				
				if(isAction == true) {
					String delDate = this.addDate(Integer.parseInt(mqProperties.getTraceKeepDate()) * -1);
					Map<String, Object> params = ValueUtil.newMap("del_date", delDate);
					
					for(String trace_mode : traceModes) {
						this.deleteDbTraceData(queryManager, qryMap.get(trace_mode), params, onceMaxRow, dbTransactionInterval);
					}
					
					this.deleteDbTraceData(queryManager, qryMap.get("trace_err"), params, onceMaxRow, dbTransactionInterval);
					this.befDeleteDate = currentDate;
				}
				
			} catch(Exception e) {
				logger.error("Error - Delete database trace data histories :" , e);
				
			} finally {
				ThreadUtil.sleep(this.checkPeriodSec);
			}
		}
	}
	
	/**
	 * 파일 삭제 
	 */
	@Async("mwTracePool")
	public void deleteFileHistory() {
		this.traceFileRootPath = this.mqProperties.getTraceFileRoot().endsWith(SysConstants.SLASH) ? mqProperties.getTraceFileRoot() : mqProperties.getTraceFileRoot() + SysConstants.SLASH;
		this.traceFileRootPath = this.traceFileRootPath + "%s" + File.separator;
		
		while(true) {
			try {
				String currentDate = this.dateFormat.format(new Date());
				boolean isAction = this.actionDeleteProcess(currentDate);
				
				if(isAction) {
					String delDate = this.addDate(Integer.parseInt(mqProperties.getTraceKeepDate()) * -1);
					long del = Long.parseLong(delDate);
					
					for(String trace_mode : traceModes) {
						String path = String.format(this.traceFileRootPath, trace_mode);
						File traceRoot = new File(path);
						File[] logDirs = traceRoot.listFiles(directoryFileFilter);
						
						for(File logDir : logDirs) {
							long dir = Long.parseLong(logDir.getName());
							if(dir < del) FileSystemUtils.deleteRecursively(logDir);
						}
					}
					
					this.befDeleteDate = currentDate;
				}
				
			} catch(Exception e) {
				logger.error("Error - Delete file trace histories : " , e);
				
			} finally {
				ThreadUtil.sleep(this.checkPeriodSec);
			}
		}
	}
	
	/**
	 * 삭제 일시 확인
	 * 
	 * @param currentDate
	 * @return
	 */
	private boolean actionDeleteProcess(String currentDate) {
		String currentHour = DateUtil.dateTimeStr(new Date(), "HH");
		if(!this.befDeleteDate.equalsIgnoreCase(currentDate) && this.mqProperties.getTraceDelTime().equalsIgnoreCase(currentHour)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * 데이터베이스 트레이스 데이터 삭제
	 * 
	 * @param queryManager
	 * @param qry
	 * @param params
	 * @param onceMaxRow
	 * @param dbTransactionInterval
	 */
	private void deleteDbTraceData(IQueryManager queryManager, String qry, Map<String,Object> params, int onceMaxRow, long dbTransactionInterval) {
		ThreadUtil.sleep(dbTransactionInterval);
		
		while(true) {
			int delCnt = queryManager.executeBySql(qry, params);
			queryManager.executeBySql("commit", ValueUtil.newMap(""));
			
			if(delCnt == onceMaxRow) {
				ThreadUtil.sleep(dbTransactionInterval);
				continue;
			} else {
				break;
			}
		}
	}
	
	private String addDate(int addDate) {
		Calendar c = Calendar.getInstance(); 
		c.setTime(new Date());
		c.add(Calendar.DATE, addDate);
		Date date = c.getTime();
		return dateFormat.format(date);
	}
	
	FileFilter directoryFileFilter = new FileFilter() {
	    public boolean accept(File file) {
	        return file.isDirectory();
	    }
	};
	
	FileFilter fileFileFilter = new FileFilter() {
	    public boolean accept(File file) {
	        return !file.isDirectory();
	    }
	};
}
