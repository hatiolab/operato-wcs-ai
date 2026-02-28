package xyz.elidom.rabbitmq.client.event;

import org.springframework.amqp.core.Message;
import org.springframework.context.ApplicationEvent;

import xyz.elidom.rabbitmq.message.MessageProperties;
import xyz.elidom.rabbitmq.message.ReceiveMessage;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.util.FormatUtil;
import xyz.elidom.util.ValueUtil;

/**
 * system 메시지 event
 * @author yang
 *
 */
public class SystemMessageReceiveEvent extends ApplicationEvent {
	private static final long serialVersionUID = -5637200314112693717L;
	private final Message message;
	private final String vHost;
	public final String queueName;
	
	private boolean isExecuted;
	
	private final String stageCd;
	private final String equipType;
	private final String equipCd;
	private final String equipVendor;
	
	public SystemMessageReceiveEvent(Object source, Message message, String vHost) {
		super(source);
		
	    this.message = message;
	    this.vHost = vHost;
	    this.queueName = message.getMessageProperties().getConsumerQueue();
	    this.isExecuted = false;
	    
	    String bodyString = new String(message.getBody());
	    ReceiveMessage receiveMessage = FormatUtil.underScoreJsonToObject(bodyString, ReceiveMessage.class);
		MessageProperties properties = receiveMessage.getProperties();	    
	    String[] splitQueue = properties.getSourceId().split(SysConstants.SLASH);
	    this.stageCd = splitQueue[0];
	    
	    if(ValueUtil.isNotEmpty(splitQueue) && splitQueue.length > 1) {
	    	this.equipType = (splitQueue.length > 1) ? splitQueue[1] : SysConstants.EMPTY_STRING;
	    	this.equipVendor = (splitQueue.length > 2) ? splitQueue[2] : SysConstants.EMPTY_STRING;
	    	this.equipCd = (splitQueue.length > 3) ? splitQueue[3] : SysConstants.EMPTY_STRING;
	    	
	    } else {
	    	this.equipType = SysConstants.EMPTY_STRING;
	    	this.equipVendor = SysConstants.EMPTY_STRING;
	    	this.equipCd = SysConstants.EMPTY_STRING;
	    }
	}
	
	public Message getMessage() {
		return message;
	}
	
	public String getVhost() {
		return vHost;
	}
	
	public String getQueueName() {
		return queueName;
	}

	public boolean isExecuted() {
		return isExecuted;
	}

	public void setExecuted(boolean isExecuted) {
		this.isExecuted = isExecuted;
	}
	
	public String getStageCd() {
		return stageCd;
	}
	
	public String getEquipType() {
		return equipType;
	}
	
	public String getEquipCd() {
		return equipCd;
	}
	
	public String getEquipVendor() {
		return equipVendor;
	}
}