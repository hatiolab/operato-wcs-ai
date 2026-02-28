package xyz.elidom.mw.rabbitmq.message.api;

/**
 * 미들웨어 메시지를 구성하는 요소 (IMwMsgHeader, IMwMsgBody) 중에 메시지 본문 정의 인터페이스
 * 비지니스 정보는 각 설비별 모듈에서 구현하고 여기서는 메시지 바디에 꼭 필요한 공통 정보만 다루고 바디 정보를 추출할 일이 없다.
 * 구체적인 메시지 바디 정보는 미들웨어와 I/F하는 개별 설비 모듈에서 구현하도록 한다. 
 * 메시지 Serialize / Deserialize 로직은 개별 설비 모듈에서 구현한다.
 * 
 * @author shortstop
 */
public interface IMwMsgBody {

}
