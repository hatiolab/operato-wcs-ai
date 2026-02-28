package xyz.anythings.gw.service.model;

/**
 * 게이트웨이 표시기 초기화 정보 모델 인터페이스
 * 
 * @author shortstop
 */
public interface IGwIndInit {

	public String getId();

	public void setId(String id);

	public String getChannel();

	public void setChannel(String channel);

	public String getPan();

	public void setPan(String pan);

	public String getBizType();

	public void setBizType(String bizType);

	public String getViewType();

	public void setViewType(String viewType);

}
