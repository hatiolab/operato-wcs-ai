package xyz.elidom.core.model;

/**
 * 공통 코드 다국어 라벨 정보
 */
public class CodeLabel {
	/**
	 * 공통 상세 코드 (CommonCodeDetail ID)
	 */
	private String id;
	/**
	 * 공통 코드 명
	 */
	private String name;
	/**
	 * 공통 코드 설명
	 */
	private String description;
	
	public CodeLabel() {
	}
	
	public CodeLabel(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public CodeLabel(String id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}
	
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
}
