package xyz.anythings.gw.service.model;

/**
 * 표시기 점등 정보 모델 인터페이스
 * 
 * @author shortstop
 */
public interface IIndOnInfo {
	
	public String getId();

	public void setId(String id);

	public String[] getSegRole();

	public void setSegRole(String[] segRole);

	public String getBizId();

	public void setBizId(String bizId);

	public Integer getOrgRelay();

	public void setOrgRelay(Integer orgRelay);

	public Integer getOrgBoxQty();

	public void setOrgBoxQty(Integer orgBoxQty);

	public Integer getOrgEaQty();

	public void setOrgEaQty(Integer orgEaQty);

	public Integer getOrgBoxinQty();

	public void setOrgBoxinQty(Integer orgBoxinQty);

	public Integer getOrgAccmQty();

	public void setOrgAccmQty(Integer orgAccmQty);

	public String getColor();

	public void setColor(String color);

	public String getAlignment();

	public void setAlignment(String alignment);

	public String getBtnMode();

	public void setBtnMode(String btnMode);

	public Integer getBtnIntvl();

	public void setBtnIntvl(Integer btnIntvl);

	public String getBfOnMsg();

	public void setBfOnMsg(String bfOnMsg);

	public Integer getBfOnMsgT();

	public void setBfOnMsgT(Integer bfOnMsgT);

	public Integer getBfOnDelay();

	public void setBfOnDelay(Integer bfOnDelay);

	public Integer getCnclDelay();

	public void setCnclDelay(Integer cnclDelay);

	public Boolean getBlinkIfFull();

	public void setBlinkIfFull(Boolean blinkIfFull);

	public Boolean getEndFullBox();

	public void setEndFullBox(Boolean endFullBox);

	public String getViewStr();

	public void setViewStr(String viewStr);

	public String getViewType();

	public void setViewType(String viewType);

}
