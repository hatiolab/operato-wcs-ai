package xyz.elidom.rabbitmq.logger;

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

import xyz.elidom.orm.IQueryManager;
import xyz.elidom.rabbitmq.config.RabbitmqProperties;
import xyz.elidom.rabbitmq.service.ElasticRestHandler;
import xyz.elidom.util.BeanUtil;
import xyz.elidom.util.DateUtil;
import xyz.elidom.util.ValueUtil;

/**
 * 트레이스 로그 메시지 삭제 관리자 
 * @author yang
 *
 */
@Component
public class TraceDeleteManager {

	@Autowired
	private RabbitmqProperties mqProperties;

	SimpleDateFormat dateFormat;
	
	String path ;
	String befDeleteDate = "";
	
	String[] traceModes = new String[] {"trace_pub","trace_sub","trace_dead"};
	
	int checkPeriodSec = 180;
	
	private Logger logger = LoggerFactory.getLogger(TraceDeleteManager.class);
	
	/**
	 * 시작 
	 * 삭제 시간 체크 
	 */
	public void processStart() {
		if(mqProperties.getTraceKeepDate().equals("0")) return;
		
 		this.dateFormat = new SimpleDateFormat("yyyyMMdd");
 		
 		if(mqProperties.getTraceType().equalsIgnoreCase("db")) BeanUtil.get(TraceDeleteManager.class).deleteDbHistory();
 		else if (mqProperties.getTraceType().equals("file")) BeanUtil.get(TraceDeleteManager.class).deleteFileHistory();
 		else if (mqProperties.getTraceType().equals("elastic")) {
			BeanUtil.get(TraceDeleteManager.class).deleteElasticHistory();
 		}
	}
	
	/**
	 * 엘라스틱 삭제 
	 */
	@Async("tracePool")
	public void deleteElasticHistory() {
		while(true) {
			try {
				String currentDate = this.dateFormat.format(new Date());
				boolean isAction = this.actionDeleteProcess(currentDate);
				
				if(isAction == true) {
					String delDate = this.addDate(Integer.parseInt(mqProperties.getTraceKeepDate()) * -1);
					long del = Long.parseLong(delDate);
					List<String> indexs = BeanUtil.get(ElasticRestHandler.class).getIndexs();
					
					boolean delFlag = false;
					for(String index : indexs) {
						delFlag = false;
						try {
							long indexDate = Long.parseLong(index);
							if(indexDate < del) delFlag = true;
						} catch(Exception e) {
							delFlag = true;
						}
						
						if(delFlag) BeanUtil.get(ElasticRestHandler.class).deleteIndex(index);
					}
					this.befDeleteDate = currentDate;
				}
			}catch(Exception e) {
				logger.error("deleteElasticHistory error : " , e);
			}
		}
	}

	/**
	 * 디비 삭제 
	 */
	@Async("tracePool")
	public void deleteDbHistory() {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		long checkPeriod = checkPeriodSec * 1000;
		int onceMaxRow = 10000;
		long dbTransactionInterval = 60 * 1000;
//		long dbTransactionInterval = 5 * 1000;
		
		HashMap<String,String> qryMap = new HashMap<String,String>();
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
					
					Map<String,Object> params = ValueUtil.newMap("del_date", delDate);
					
					for(String trace_mode : traceModes) {
						this.executeDelQeury(queryManager, qryMap.get(trace_mode), params, onceMaxRow,dbTransactionInterval);
					}
					this.executeDelQeury(queryManager, qryMap.get("trace_err"), params, onceMaxRow,dbTransactionInterval);
					
					this.befDeleteDate = currentDate;
				}
				
				this.sleepThread(checkPeriod);
				
			}catch(Exception e) {
				logger.error("deleteDbHistory error : " , e);
			}
		}
	}
	
	/**
	 * 파일 삭제 
	 */
	@Async("tracePool")
	public void deleteFileHistory() {
		this.path = mqProperties.getTraceFileRoot().endsWith("/") ? mqProperties.getTraceFileRoot() : mqProperties.getTraceFileRoot()+"/";
		this.path = this.path + "%s" + File.separator;
		long checkPeriod = checkPeriodSec * 1000;
		
		
		while(true) {
			try {
				String currentDate = this.dateFormat.format(new Date());
				boolean isAction = this.actionDeleteProcess(currentDate);
				
				if(isAction == true) {
					String delDate = this.addDate(Integer.parseInt(mqProperties.getTraceKeepDate()) * -1);
					
					long del = Long.parseLong(delDate);
					
					for(String trace_mode : traceModes) {
						String path = String.format(this.path, trace_mode);
						
						File traceRoot = new File(path);
						File[] logDirs = traceRoot.listFiles(directoryFileFilter);
						
						for(File logDir : logDirs) {
							long dir = Long.parseLong(logDir.getName());
							if(dir < del) FileSystemUtils.deleteRecursively(logDir);
						}
					}
					this.befDeleteDate = currentDate;
				}
				
				this.sleepThread(checkPeriod);
				
			}catch(Exception e) {
				logger.error("deleteFileHistory error : " , e);
			}
		}
	}
	
	/**
	 * 삭제 일시 확인 
	 * @param currentDate
	 * @return
	 */
	private boolean actionDeleteProcess(String currentDate) {
		
		String currentHour = DateUtil.dateTimeStr(new Date(), "HH");
		
		if(this.befDeleteDate.equalsIgnoreCase(currentDate) == false) {
			if(mqProperties.getTraceDelTime().equalsIgnoreCase(currentHour)) return true;
		}

		
		/*
		if(ValueUtil.isEmpty(this.befDeleteDate)) return true;
		else if(this.befDeleteDate.equalsIgnoreCase(currentDate) == false) {
			if(this.traceDelTime.equalsIgnoreCase(currentHour)) return true;
		}
		*/
		return false;
	}
	
	private void executeDelQeury (IQueryManager queryManager, String qry, Map<String,Object> params, int onceMaxRow, long dbTransactionInterval) {
		this.sleepThread(dbTransactionInterval);
		
		while(true) {
			int delCnt = queryManager.executeBySql(qry, params);
			queryManager.executeBySql("commit", ValueUtil.newMap(""));
			
			if(delCnt == onceMaxRow) {
				this.sleepThread(dbTransactionInterval);
				continue;
			}
			else break;
		}
	}
	
	private void sleepThread(long period) {
		try {
			Thread.sleep(period);
		} catch (InterruptedException e) {
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
