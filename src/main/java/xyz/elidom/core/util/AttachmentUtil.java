/* Copyright © HatioLab Inc. All rights reserved. */
/**
 * 
 */
package xyz.elidom.core.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.web.multipart.MultipartFile;

import xyz.elidom.core.CoreConfigConstants;
import xyz.elidom.core.CoreConstants;
import xyz.elidom.core.entity.Attachment;
import xyz.elidom.core.entity.Storage;
import xyz.elidom.core.system.controller.handler.file.upload.IFileUploadHandler;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.BeanUtil;

/**
 * Attachment 관련 파일 핸들링 유틸리티
 * 
 * @author shortstop
 */
public class AttachmentUtil {

	/**
	 * Request ContextPath와 첨부파일 ID로 Download Full URL을 리턴한다.
	 * 
	 * @param contextPath
	 * @param attachmentId
	 * @return
	 */
	public static String getFullDownloadUrl(String contextPath, String attachmentId) {
		String baseUrl = contextPath == null ? SettingUtil.getValue("server.contrext.path", "localhost") : contextPath;
		return baseUrl + (baseUrl.endsWith(OrmConstants.SLASH) ? OrmConstants.EMPTY_STRING : OrmConstants.SLASH) + "/rest/download/public/" + attachmentId;
	}

	/**
	 * File 경로명을 통하여, 실제 File 저장 주소를 추출.
	 * 
	 * @param storageName
	 * @return
	 */
	public static String getStoragePath(String storageName) {
		Storage storageInfo = getStorageByName(storageName);
		return (ValueUtil.isEmpty(storageInfo) || ValueUtil.isEmpty(storageInfo.getPath())) ? null : storageInfo.getPath();
	}

	/**
	 * storageName으로 Storage 조회
	 * 
	 * @param storageName
	 * @return
	 */
	public static Storage getStorageByName(String storageName) {
		IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
		Storage storage = queryManager.selectByCondition(Storage.class, new Storage(Domain.currentDomainId(), storageName));
		return storage;
	}

	/**
	 * Attachment ID를 통해, 실제 저장되어 있는 파일 경로를 추출.
	 * 
	 * @param attchmentId
	 * @return
	 */
	public static String getAttachmentFileFullPath(String attchmentId) {
		Attachment attachment = BeanUtil.get(IQueryManager.class).select(Attachment.class, attchmentId);
		return getAttachmentFileFullPath(attachment);
	}

	/**
	 * Attachment 정보로 실제 저장되어 있는 파일 경로를 추출.
	 * 
	 * @param attachment
	 * @return
	 */
	public static String getAttachmentFileFullPath(Attachment attachment) {
		if (ValueUtil.isEmpty(attachment.getPath())) {
			attachment = BeanUtil.get(IQueryManager.class).select(attachment);
		}

		return AttachmentUtil.getRootPath() + attachment.getPath();
	}

	/**
	 * ID를 이용한 File 삭제
	 * 
	 * @param id
	 * @return
	 */
	public static boolean deleteFile(String id) {
		String physicalDeletable = SettingUtil.getValue(CoreConfigConstants.ATTACH_FILE_DELETE, SysConstants.TRUE_STRING);
		boolean deletable = ValueUtil.isEqual(physicalDeletable, SysConstants.TRUE_STRING);
		return deleteFile(id, !deletable);
	}

	/**
	 * ID를 이용한 File 삭제
	 * 
	 * @param id
	 * @param isDbOnly 실제 데이터와 데이터 베이스의 레코드를 함꼐 삭제 할지 여부
	 * @return
	 */
	public static boolean deleteFile(String id, boolean isDbOnly) {
		boolean result = true;

		try {
			String filePath = AttachmentUtil.getAttachmentFileFullPath(id);

			if (!isDbOnly) {
				File file = new File(filePath);
				result = file.delete();
			}

			if (result) {
				Attachment attachment = new Attachment();
				attachment.setId(id);
				IQueryManager queryManager = BeanUtil.get(IQueryManager.class);
				queryManager.delete(attachment);
			}
		} catch (Exception e) {
			result = false;
		}

		return result;
	}

	/**
	 * File 저장소의 Root Path 가져오기 실행.
	 * 
	 * @return
	 */
	public static String getRootPath() {
		String rootPath = SettingUtil.getValue(CoreConfigConstants.FILE_ROOT_PATH);
		if (ValueUtil.isEmpty(rootPath)) {
			throw ThrowUtil.newEmptyRootPath();
		}

		if (!rootPath.endsWith(CoreConstants.FILE_SEPARATOR)) {
			rootPath += CoreConstants.FILE_SEPARATOR;
		}

		return rootPath;
	}

	/**
	 * File Path를 통하여 ID 추출
	 * 
	 * @param path
	 * @return
	 */
	public static String getIdByFilePath(String path) {
		return path.substring(path.lastIndexOf(OrmConstants.SLASH) + 1, path.lastIndexOf(OrmConstants.DOT));
	}

	/**
	 * prevAttach 파일을 name으로 Copy.
	 * 
	 * @param name
	 * @param type
	 * @param prevAttach
	 * @return
	 */
	public static Attachment copyFile(String name, String type, Attachment prevAttach) {
		// 마지막 파일명만 수정하여 파일 복사
		File prevFile = new File(AttachmentUtil.getRootPath() + prevAttach.getPath());
		String prevFileName = prevFile.getName();
		String extention = prevFileName.substring(prevFileName.lastIndexOf(OrmConstants.DOT) + 1, prevFileName.length());
		String newAttachPath = prevAttach.getPath();
		newAttachPath = newAttachPath.substring(0, newAttachPath.lastIndexOf(OrmConstants.SLASH) + 1) + UUID.randomUUID().toString() + OrmConstants.DOT + extention;
		File newFile = new File(AttachmentUtil.getRootPath() + newAttachPath);

		try {
			FileUtils.copyFile(prevFile, newFile);
		} catch (IOException e) {
			throw ThrowUtil.newFailToCopyFile();
		}

		// Attachment 생성
		Attachment newAttachment = new Attachment();
		ValueUtil.populate(prevAttach, newAttachment);
		newAttachment.setId(null);
		newAttachment.setName(name + OrmConstants.DOT + extention);
		newAttachment.setPath(newAttachPath);
		newAttachment.setOnType(type);
		newAttachment.setOnId(null);
		BeanUtil.get(IQueryManager.class).insert(newAttachment);
		return newAttachment;
	}

	/**
	 * Contents에 해당하는 내용을 FileName으로 StorageName 경로에 Upload 실행
	 * 
	 * @param fileName
	 * @param contents
	 * @param storageName
	 * @return
	 */
	public static Attachment upload(String fileName, String contents, String storageName) {
		return AttachmentUtil.upload(fileName, contents, storageName, null, null);
	}

	/**
	 * Contents에 해당하는 내용을 FileName으로 StorageName 경로에 리소스 유형이 onType 이고 리소스 아이디인 onId인 Upload 실행
	 * 
	 * @param fileName
	 * @param contents
	 * @param storageName
	 * @param onType
	 * @param onId
	 * @return
	 */
	public static Attachment upload(String fileName, String contents, String storageName, String onType, String onId) {
		Map<String, String> fileInfoMap = new HashMap<String, String>();
		fileInfoMap.put(CoreConstants.FILE_SAVE_PATH_NAME, storageName);

		if (ValueUtil.isNotEmpty(onType) && ValueUtil.isNotEmpty(onId)) {
			fileInfoMap.put(CoreConstants.FILE_ON_TYPE, onType);
			fileInfoMap.put(CoreConstants.FILE_ON_ID, onId);
		}

		MultipartFile file = null;
		try {
			InputStream inputStream = new ByteArrayInputStream(contents.getBytes());
			file = new SimpleMultipartFile(fileName, fileName, fileName.substring(fileName.lastIndexOf(OrmConstants.DOT), fileName.length()), inputStream);
		} catch (IOException e) {
			throw new ElidomServiceException(e.getMessage(), e);
		}

		// File Upload 후 결과 값 Return
		Map<String, Object> resultMap = BeanUtil.get(IFileUploadHandler.class).handleRequest(null, null, fileInfoMap, file);
		Attachment attachment = new Attachment();
		attachment.setPath((String) resultMap.get(fileName));

		// 저장된 File ID를 Label Thumbnail에 저장
		return BeanUtil.get(IQueryManager.class).selectByCondition(Attachment.class, attachment);
	}
	
	/**
	 * Base64로 전송된 Image를 첨부파일(Attachment)로 저장 후 Attachments ID 정보를 리턴.
	 * 
	 * @param fileName
	 * @param image
	 * @return
	 */
	public static String uploadFileByBase64(String fileName, String image) {
		return uploadFileByBase64(fileName, image, null);
	}

	/**
	 * Base64로 전송된 Image를 첨부파일(Attachment)로 저장 후 Attachments ID 정보를 리턴.
	 * 
	 * @param fileName
	 * @param image
	 * @param pathName
	 * @return
	 */
	public static String uploadFileByBase64(String fileName, String image, String pathName) {
		return uploadFileByBase64(fileName, image, pathName, null, null);
	}

	/**
	 * Base64로 전송된 Image를 첨부파일(Attachment)로 저장 후 Attachments ID 정보를 리턴.
	 * 
	 * @param fileName
	 * @param image
	 * @param pathName
	 * @param onId
	 * @param onType
	 * @return
	 */
	public static String uploadFileByBase64(String fileName, String image, String pathName, String onId, String onType) {
		String contents = image.substring(image.indexOf(OrmConstants.COMMA) + 1);
		String extension = image.substring(image.indexOf(OrmConstants.SLASH) + 1, (image.indexOf(OrmConstants.COLON)));
		String fullName = new StringBuilder(fileName).append(OrmConstants.DOT).append(extension).toString();

		Map<String, String> fileInfoMap = new HashMap<String, String>();
		fileInfoMap.put(CoreConstants.FILE_SAVE_PATH_NAME, pathName);
		fileInfoMap.put(CoreConstants.FILE_ON_ID, onId);
		fileInfoMap.put(CoreConstants.FILE_ON_TYPE, onType);

		MultipartFile file = null;
		try {
			InputStream inputStream = new ByteArrayInputStream(Base64.decodeBase64(contents));
			file = new SimpleMultipartFile(fullName, fullName, extension, inputStream);
		} catch (IOException e) {
			throw new ElidomServiceException(e.getMessage(), e);
		}

		// File Upload 후 결과 값 Return
		Map<String, Object> resultMap = BeanUtil.get(IFileUploadHandler.class).handleRequest(null, null, fileInfoMap, file);
		Attachment attachment = new Attachment();
		attachment.setPath((String) resultMap.get(fullName));
		return BeanUtil.get(IQueryManager.class).selectByCondition(Attachment.class, attachment).getId();
	}
	
}