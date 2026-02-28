/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.entity;

import java.io.File;

import org.slf4j.LoggerFactory;

import xyz.elidom.core.entity.relation.StorageRef;
import xyz.elidom.core.util.AttachmentUtil;
import xyz.elidom.dbist.annotation.Column;
import xyz.elidom.dbist.annotation.GenerationRule;
import xyz.elidom.dbist.annotation.Index;
import xyz.elidom.dbist.annotation.PrimaryKey;
import xyz.elidom.dbist.annotation.Relation;
import xyz.elidom.dbist.annotation.Table;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.orm.entity.basic.ElidomStampHook;
import xyz.elidom.sys.util.ValueUtil;

@Table(name = "attachments", idStrategy = GenerationRule.UUID, uniqueFields = "path", indexes = {
	@Index(name = "ix_attach_0", columnList = "domain_id,path", unique = true), 
	@Index(name = "ix_attach_1", columnList = "domain_id,name"),
	@Index(name = "ix_attach_2", columnList = "domain_id,on_type,on_id"),
	@Index(name = "ix_attach_3", columnList = "domain_id,storage_info_id"),
	@Index(name = "ix_attach_4", columnList = "domain_id,created_at"),
	@Index(name = "ix_attach_5", columnList = "domain_id,updated_at"), 
	@Index(name = "ix_attach_6", columnList = "domain_id,tag"),
	@Index(name = "ix_attach_7", columnList = "domain_id,category,name"),
	@Index(name = "ix_attach_8", columnList = "domain_id,ref_by"),
	@Index(name = "ix_attach_9", columnList = "domain_id,ref_type,ref_by"),
})
public class Attachment extends ElidomStampHook {

	/**
	 * SerialVersion UID
	 */
	private static final long serialVersionUID = -5039431627619522665L;

	@PrimaryKey
	@Column (name = "id", nullable = false, length = OrmConstants.FIELD_SIZE_UUID)
	private String id;

	@Column(name = "name", nullable = false, length = OrmConstants.FIELD_SIZE_LONG_NAME)
	private String name;

	@Column(name = "description", length = OrmConstants.FIELD_SIZE_DESCRIPTION)
	private String description;

	@Column(name = "storage_info_id", length = OrmConstants.FIELD_SIZE_UUID)
	private String storageInfoId;

	@Relation(field = "storageInfoId")
	private StorageRef storageInfo;

	@Column(name = "mimetype", length = 30)
	private String mimetype;

	@Column(name = "file_size")
	private Integer fileSize;

	@Column(name = "path", length = OrmConstants.FIELD_SIZE_FILE_PATH, nullable = false)
	private String path;

	@Column(name = "on_id", length = OrmConstants.FIELD_SIZE_MEANINGFUL_ID)
	private String onId;

	@Column(name = "on_type", length = OrmConstants.FIELD_SIZE_NAME)
	private String onType;

	@Column(name = "tag", length = 128)
	private String tag;
	
	@Column(name = "encoding", length = 128)
	private String encoding;
	
	@Column(name = "category", length = 128)
	private String category;

	@Column(name = "size", length = 128)
	private String size;
	
	@Column(name = "ref_type", length = 128)
	private String refType;

	@Column(name = "ref_by", length = OrmConstants.FIELD_SIZE_UUID)
	private String refBy;
	
	@Column(name = "contents")
	private byte[] contents;
	

	public Attachment() {
	}

	public Attachment(String id) {
		this.id = id;
	}

	public Attachment(Long domainId, String name) {
		this.domainId = domainId;
		this.name = name;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the storageInfoId
	 */
	public String getStorageInfoId() {
		return storageInfoId;
	}

	/**
	 * @param storageInfoId
	 *            the storageInfoId to set
	 */
	public void setStorageInfoId(String storageInfoId) {
		this.storageInfoId = storageInfoId;
	}

	/**
	 * @return the storageInfo
	 */
	public StorageRef getStorageInfo() {
		return storageInfo;
	}

	/**
	 * @param storageInfo
	 *            the storageInfo to set
	 */
	public void setStorageInfo(StorageRef storageInfo) {
		this.storageInfo = storageInfo;
	}

	/**
	 * @return the mimetype
	 */
	public String getMimetype() {
		return mimetype;
	}

	/**
	 * @param mimetype
	 *            the mimetype to set
	 */
	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

	/**
	 * @return the fileSize
	 */
	public Integer getFileSize() {
		return fileSize;
	}

	/**
	 * @param fileSize
	 *            the fileSize to set
	 */
	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path
	 *            the path to set
	 */
	public void setPath(String path) {
		this.path = path;

		if (ValueUtil.isEmpty(this.id))
			this.id = AttachmentUtil.getIdByFilePath(path);
	}

	/**
	 * @return the onId
	 */
	public String getOnId() {
		return onId;
	}

	/**
	 * @param onId
	 *            the onId to set
	 */
	public void setOnId(String onId) {
		this.onId = onId;
	}

	/**
	 * @return the onType
	 */
	public String getOnType() {
		return onType;
	}

	/**
	 * @param onType
	 *            the onType to set
	 */
	public void setOnType(String onType) {
		this.onType = onType;
	}

	/**
	 * @return the tag
	 */
	public String getTag() {
		return tag;
	}

	/**
	 * @param tag
	 *            the tag to set
	 */
	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public void beforeCreate() {
		super.beforeCreate();

		if (this.storageInfoId != null && this.storageInfoId.equals("")) {
			this.storageInfoId = null;
		}
	}

	@Override
	public void beforeUpdate() {
		super.beforeUpdate();

		if (this.storageInfoId != null && this.storageInfoId.equals("")) {
			this.storageInfoId = null;
		}
	}
	
	@Override
	public void afterDelete() {
		try {
			String filePath = AttachmentUtil.getAttachmentFileFullPath(this);
			if (ValueUtil.isNotEmpty(filePath)) {
				File file = new File(filePath);
				if (file.exists()) {
					file.delete();
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(this.getClass()).warn(e.getMessage());
		}
	}

	public String getEncoding() {
		return encoding;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getRefType() {
		return refType;
	}

	public void setRefType(String refType) {
		this.refType = refType;
	}

	public String getRefBy() {
		return refBy;
	}

	public void setRefBy(String refBy) {
		this.refBy = refBy;
	}

	public byte[] getContents() {
		return contents;
	}

	public void setContents(byte[] contents) {
		this.contents = contents;
	}
}