/* Copyright © HatioLab Inc. All rights reserved. */
/**
 * 
 */
package xyz.elidom.core.system.controller.handler.file.upload;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import xyz.elidom.core.CoreConfigConstants;
import xyz.elidom.core.CoreConstants;
import xyz.elidom.core.CoreMessageConstants;
import xyz.elidom.core.entity.Attachment;
import xyz.elidom.core.entity.Storage;
import xyz.elidom.core.util.AttachmentUtil;
import xyz.elidom.exception.client.ElidomBadRequestException;
import xyz.elidom.exception.client.ElidomClientException;
import xyz.elidom.exception.client.ElidomInputException;
import xyz.elidom.exception.client.ElidomServiceNotFoundException;
import xyz.elidom.exception.server.ElidomServiceException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.orm.OrmConstants;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.entity.Domain;
import xyz.elidom.sys.entity.User;
import xyz.elidom.sys.util.AssertUtil;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.sys.util.SettingUtil;
import xyz.elidom.sys.util.ThrowUtil;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.DateUtil;

/**
 * IFileUploadHandler 기본 구현 
 * 
 * @author Minu.Kim
 */
@Component
public class BasicUploadHandler extends AbstractFileUploadHandler {
	
	@Autowired
	private IQueryManager queryManager;

	@Override
	public void validationCheck(HttpServletRequest req, MultipartFile[] files) throws ElidomClientException {
		AssertUtil.assertNotEmpty("terms.label.file", files);

		// Limit File Size
		Long limitSize = ValueUtil.toLong(SettingUtil.getValue(CoreConfigConstants.UPLOAD_FILE_LIMIT_SIZE, "10"));
		limitSize *= CoreConstants.MB;
		Long currentSize = 0L;

		for (MultipartFile file : files) {
			// File Size Check : Container에서 지정한 범위 안에 있을 경우만 해당.(Container 설정 : application.properties)
			currentSize += ValueUtil.toLong(file.getSize());
			if (currentSize > limitSize) {
				throw new ElidomBadRequestException(CoreMessageConstants.UPLOAD_FILE_SIZE_LIMIT_ERROR, "Excess limit file size.");
			}
		}
	}

	@Override
	public String getUploadDirectory(HttpServletRequest req, Map<String, String> fileInfoMap) throws ElidomServiceNotFoundException {
		Storage storage = this.findStorage(fileInfoMap.get(CoreConstants.FILE_SAVE_PATH_NAME));
		String storagePath = storage.getPath();
		return this.fileUploadPath(storagePath, storage.getDirRule(), fileInfoMap);		
	}

	@Override
	public Map<String, String> saveFileInfo(String savePath, Map<String, String> fileInfoMap, MultipartFile[] files) throws ElidomInputException {
		String onId = fileInfoMap.get(CoreConstants.FILE_ON_ID);
		String onType = fileInfoMap.get(CoreConstants.FILE_ON_TYPE);
		String pathName = ValueUtil.checkValue(fileInfoMap.get(CoreConstants.FILE_SAVE_PATH_NAME), "default");
		Storage storage = queryManager.selectByCondition(Storage.class, new Storage(Domain.currentDomainId(), pathName));
		String storageId = storage == null ? null : storage.getId();
		Map<String, String> uuidMap = new HashMap<String, String>();
		
		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				Attachment attach = this.newAttachment(file, storageId, savePath, fileInfoMap, onType, onId);
				queryManager.insert(attach);
				String newFilename = attach.getPath().substring(attach.getPath().lastIndexOf(SysConstants.FILE_SEPARATOR) + 1);
				uuidMap.put(file.getOriginalFilename(), newFilename);
			}
		}

		return uuidMap;
	}

	@Override
	public Map<String, Object> uploadFile(Map<String, String> uuidMap, String savePath, MultipartFile[] files) throws ElidomInputException {
		Map<String, Object> resultMap = new HashMap<String, Object>();

		for (MultipartFile file : files) {
			if (!file.isEmpty()) {
				try {
					String originFileName = file.getOriginalFilename();
					String rootPath = AttachmentUtil.getRootPath();
					String fileName = uuidMap.get(originFileName);
					String filePath = savePath + fileName;
					File newFile = new File(rootPath + filePath);
					FileUtils.copyInputStreamToFile(file.getInputStream(), newFile);
					resultMap.put(originFileName, filePath);
				} catch (Exception e) {
					throw new ElidomServiceException(e.getMessage(), e);
				}				
			}
		}

		return resultMap;
	}
	
	/**
	 * 첨부파일 생성 정보로 부터 Attachment 객체를 생성하여 리턴 
	 * 
	 * @param file
	 * @param storageId
	 * @param attachPath
	 * @param fileInfoMap
	 * @param onType
	 * @param onId
	 * @return
	 */
	private Attachment newAttachment(MultipartFile file, String storageId, String attachPath, Map<String, String> fileInfoMap, String onType, String onId) {
		String fileName = file.getOriginalFilename();
		String extention = fileName.substring(fileName.lastIndexOf(OrmConstants.DOT));
		
		boolean keepFilename = fileInfoMap.containsKey("keep_filename");
		String newFileName = keepFilename ? fileName : UUID.randomUUID().toString() + extention;

		Attachment attach = new Attachment();
		attach.setPath(new StringBuilder().append(attachPath).append(newFileName).toString());
		
		if(keepFilename) {
			this.checkAttachmentByFilePath(attach.getPath());
			attach.setId(UUID.randomUUID().toString());
		}
		
		attach.setName(fileName);
		attach.setFileSize(ValueUtil.toInteger(file.getSize()));
		attach.setMimetype(extention.replaceFirst(OrmConstants.DOT, OrmConstants.EMPTY_STRING));
		attach.setStorageInfoId(storageId);
		attach.setOnId(onId);
		attach.setOnType(onType);
		return attach;
	}
	
	/**
	 * filePath로 첨부된 파일이 이미 존재하는지 체크  
	 * 
	 * @param filePath
	 */
	private void checkAttachmentByFilePath(String filePath) {
		Map<String, Object> paramMap = ValueUtil.newMap("domainId,path", Domain.currentDomainId(), filePath);
		if(this.queryManager.selectSize(Attachment.class, paramMap) > 0) {
			throw ThrowUtil.newDataDuplicated("terms.menu.Attachment", filePath);
		}
	}
	
	/**
	 * storageName으로 storage 조회 
	 * 
	 * @param storageName
	 * @return
	 */
	private Storage findStorage(String storageName) {
		Storage storage = null;
		
		if (ValueUtil.isEmpty(storageName)) {
			Storage defaultStorage = new Storage();
			defaultStorage.setDefaultFlag(true);
			storage = queryManager.selectByCondition(false, Storage.class, defaultStorage);
			
			if (ValueUtil.isEmpty(storage)) {
				throw new ElidomServiceException(CoreMessageConstants.FILE_DEFAULT_PATH_IS_EMPTY, "Default Storage is empty.");
			}			
		} else {
			storage = queryManager.selectByCondition(true, Storage.class, new Storage(Domain.currentDomainId(), storageName));
		}
		
		return storage;
	}
	
	/**
	 * 파라미터로 파일 업로드 경로를 생성하여 리턴 
	 * 
	 * @param storagePath
	 * @param saveOption
	 * @param fileInfoMap
	 * @return
	 */
	private String fileUploadPath(String storagePath, String saveOption, Map<String, String> fileInfoMap) {
		StringBuilder realPath = new StringBuilder();
		realPath.append(storagePath);

		if (!storagePath.endsWith(CoreConstants.FILE_SEPARATOR)) {
			realPath.append(CoreConstants.FILE_SEPARATOR);
		}

		// Option이 존재 할 경우.
		if (ValueUtil.isNotEmpty(saveOption)) {
			// 일자벌 파일 경로 설정.
			if (saveOption.equalsIgnoreCase("year")) {
				realPath.append(DateUtil.getYear());
				
			} else if (saveOption.equalsIgnoreCase("month")) {
				realPath.append(DateUtil.getYear());
				realPath.append(CoreConstants.FILE_SEPARATOR);
				realPath.append(DateUtil.getMonth());
				
			} else if (saveOption.equalsIgnoreCase("day")) {
				realPath.append(DateUtil.getYear());
				realPath.append(CoreConstants.FILE_SEPARATOR);
				realPath.append(DateUtil.getMonth());
				realPath.append(CoreConstants.FILE_SEPARATOR);
				realPath.append(DateUtil.getDay());
				
			} else if (saveOption.equalsIgnoreCase("random")) {
				realPath.append(UUID.randomUUID().toString());
				
			} else if (saveOption.equalsIgnoreCase("user")) {
				realPath.append(User.currentUser().getId());
				
			} else if (saveOption.equalsIgnoreCase("custom")) {
				String customPath = fileInfoMap.get(CoreConstants.FILE_SAVE_CUSTOM_PATH);
				AssertUtil.assertNotEmpty("customPath", customPath);
				realPath.append(customPath);
				
			} else {
				throw new ElidomServiceException(CoreMessageConstants.NOT_SUPPORTED_TYPE, "Not Supported {0} Type. [{1}]", MessageUtil.params("Save", saveOption));
			}

			// 경로 마지막에 "/" 기호가 포함되어 있는지 확인.
			if (!realPath.toString().endsWith(CoreConstants.FILE_SEPARATOR)) {
				realPath.append(CoreConstants.FILE_SEPARATOR);
			}
		}

		return realPath.toString();		
	}
	
}