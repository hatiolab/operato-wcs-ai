package xyz.anythings.gw.entity;

import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Table;

@Table(name = "attach_temp", idStrategy = GenerationRule.UUID)
public class AttachTemp extends xyz.elidom.orm.entity.basic.DomainCreateStampHook {
	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = 100137286742186217L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = 40)
	private String id;

	@Column (name = "file_name", nullable = false, length = 64)
	private String fileName;

	@Column (name = "file_size")
	private Long fileSize;
	
	@Column (name = "file_data")
	private byte[] fileData;
  
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public byte[] getFileData() {
		return fileData;
	}

	public void setFileData(byte[] fileData) {
		this.fileData = fileData;
	}
	
	public void updateFileData() {
		// binary 데이터 File Data를 업데이트 처리한다. 
	}
	
}
