package xyz.anythings.sys.model;

import com.fasterxml.jackson.annotation.JsonInclude;

import xyz.elidom.base.entity.MenuButton;
import xyz.elidom.sys.util.ValueUtil;

/**
 * 메뉴 메타 - 버튼 액션 정보
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OperatoAction implements IOperatoConfig{
	/**
	 * 정렬 순서 
	 */
	private int rank;
	/**
	 * 연결 함수 
	 */
	private String name;
	/**
	 * 스타일
	 */
	private String style;
	/**
	 * 버튼 라벨
	 */
	private String title;
	
	/**
	 * 메쏘드 ( Auth 값과 연결 )
	 */
	private String method;
	
	/**
	 * 버튼 타입 
	 */
	private String type;
	
	/**
	 * 권한 
	 */
	private String auth;
	
	/**
	 * 버튼 클릭시 확인 창 표시 여부
	 */
	private Boolean confirmFlag;
	
	/**
	 * 처리 로직 
	 */
	private String logic;
	
	public OperatoAction(MenuButton button) {
		this.setRank(button.getRank());
		this.setAuth(button.getAuth());
		this.setName(button.getText());
		this.setType(button.getButtonType());
		this.setLogic(button.getLogic());
		this.setTitle(button.getTitle());
		this.setConfirmFlag(button.getConfirmFlag() == null ? false : button.getConfirmFlag());
		this.setStyle(ValueUtil.isNotEmpty(button.getStyle()) ? button.getStyle() : null);
		
		if(ValueUtil.isNotEqual(button.getButtonType(), "basic")) {
			this.setMethod(this.getMethod(button.getAuth()));
		}
	}
	
	/**
	 * auth 를 http 메소드로 변경  
	 * @param auth
	 * @return
	 */
	private String getMethod(String auth) {
		
		switch(auth) {
			case "show" :
				return "GET";
			case "create" :
				return "PUT";
			case "update" :
				return "POST";
			case "delete" :
				return "DELETE";
		}
		
		return null;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Boolean getConfirmFlag() {
		return confirmFlag;
	}

	public void setConfirmFlag(Boolean confirmFlag) {
		this.confirmFlag = confirmFlag;
	}

	public String getLogic() {
		return logic;
	}

	public void setLogic(String logic) {
		this.logic = logic;
	}

	public String getAuth() {
		return auth;
	}

	public void setAuth(String auth) {
		this.auth = auth;
	}
}
