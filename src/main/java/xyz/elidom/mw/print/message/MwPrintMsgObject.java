package xyz.elidom.mw.print.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.mw.rabbitmq.message.MessageProperties;
import xyz.elidom.mw.rabbitmq.message.MwMsgObject;

/**
 * 인쇄용 미들웨어 메시지 구현
 * 
 * @author shortstop
 */
public class MwPrintMsgObject extends MwMsgObject {
    /**
     * 메시지 바디
     */
	private IPrintBody body;
	
    /**
     * 생성자
     */
    public MwPrintMsgObject() {
        super();
    }
    
    /**
     * 생성자
     * 
     * @param header 메시지 헤더
     */
    public MwPrintMsgObject(MessageProperties properties) {
        this.properties = properties;
    }
    
    /**
     * 생성자
     * 
     * @param header 메시지 헤더
     * @param body 메시지 바디
     */
    public MwPrintMsgObject(MessageProperties properties, IPrintBody body) {
        this.properties = properties;
        this.body = body;
    }

    public IPrintBody getBody() {
		return body;
	}

	public void setBody(IPrintBody body) {
		this.body = body;
	}

	@Override
    public String toJsonString() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ElidomServiceException(e.getMessage(), e);
        }
    }
}
