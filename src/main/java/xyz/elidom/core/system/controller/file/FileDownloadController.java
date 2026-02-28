/* Copyright © HatioLab Inc. All rights reserved. */
/**
 * 
 */
package xyz.elidom.core.system.controller.file;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import xyz.elidom.core.CoreMessageConstants;
import xyz.elidom.core.entity.Attachment;
import xyz.elidom.core.entity.Storage;
import xyz.elidom.core.system.controller.handler.file.download.IPathBasedDownloadHandler;
import xyz.elidom.core.system.controller.handler.file.download.IStreamingHandler;
import xyz.elidom.core.util.AttachmentUtil;
import xyz.elidom.exception.client.ElidomBadRequestException;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.system.service.params.BasicOutput;
import xyz.elidom.sys.util.MessageUtil;
import xyz.elidom.util.BeanUtil;

/**
 * Controller For File Download 
 * 
 * @author Minu.Kim
 */
@RestController
@Transactional
public class FileDownloadController {
	
	/**
	 * logger
	 */
	private Logger logger = LoggerFactory.getLogger(FileDownloadController.class);
	
	@Autowired
	private IPathBasedDownloadHandler pathBasedDownloadHandler;
	
	@Autowired
	private IStreamingHandler streamingHandler;
	
	/**
	 * FilePath 기반으로 파일을 다운로드한다.  
	 * 
	 * @param req
	 * @param res
	 * @param path
	 * @return
	 */
	@RequestMapping(value = "/download")
	public @ResponseBody Object downloadFile(HttpServletRequest req, HttpServletResponse res, @RequestParam("path") String path) {
		return this.downloadFileByPath(req, res, path, null);
	}
	
	/**
	 * FilePath 기반으로 파일을 스트리밍한다.  
	 * 
	 * @param req
	 * @param res
	 * @param path
	 * @return
	 */
	@RequestMapping(value = "/stream")
	public @ResponseBody Object streamingFile(HttpServletRequest req, HttpServletResponse res, @RequestParam("path") String path) {
		return this.streamingFileByPath(req, res, path);
	}
	
	/**
	 * Attachment Object의 아이디를 받아서 Attachment 기반으로 파일을 다운로드한다.  
	 * 
	 * @param req
	 * @param res
	 * @param id
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/rest/download/{id}")
	public @ResponseBody Object downloadFileByAttachment(HttpServletRequest req, HttpServletResponse res, @PathVariable("id") String id) {
		Attachment attachment = BeanUtil.get(IQueryManager.class).select(Attachment.class, id);
		if(attachment == null) {
			logger.warn(MessageUtil.getMessage(CoreMessageConstants.FILE_NOT_FOUND, "File({0}) Not Found.", MessageUtil.params(id)));
			return null;
		}
		
		String filePath = AttachmentUtil.getAttachmentFileFullPath(attachment);
		return this.downloadFileByPath(req, res, filePath, attachment.getName());
	}
	
	/**
	 * Public File Download
	 * Attachment Object의 아이디를 받아서 Attachment 기반으로 파일을 다운로드한다.  
	 * 
	 * @param req
	 * @param res
	 * @param id
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/rest/download/public/{id}")
	public @ResponseBody Object downloadPublicFileByAttachment(HttpServletRequest req, HttpServletResponse res, @PathVariable("id") String id) {
		IQueryManager qm = BeanUtil.get(IQueryManager.class);
		Attachment attachment = qm.select(Attachment.class, id);
		
		if(attachment == null) {
			logger.warn(MessageUtil.getMessage(CoreMessageConstants.FILE_NOT_FOUND, "File({0}) Not Found.", MessageUtil.params(id)));
			return null;
		}
		
		Storage storage = qm.select(Storage.class, attachment.getStorageInfoId());

		if (!storage.getPublicFlag()) {
			throw new ElidomBadRequestException("This storage [" + storage.getName() + "] is not public storage, so it can't be open to annonymous user!");
		}
		
		String filePath = AttachmentUtil.getAttachmentFileFullPath(attachment);
		return this.downloadFileByPath(req, res, filePath, attachment.getName());
	}
	
	/**
	 * Attachment Object의 아이디를 받아서 Attachment 기반으로 파일을 스트리밍한다.  
	 * 
	 * @param req
	 * @param res
	 * @param id
	 * @return
	 */
	@CrossOrigin
	@RequestMapping(value = "/rest/stream/{id}")
	public @ResponseBody Object streamingFileByAttachment(HttpServletRequest req, HttpServletResponse res, @PathVariable("id") String id) {
		Attachment attachment = BeanUtil.get(IQueryManager.class).select(Attachment.class, id);
		if(attachment == null) {
			logger.warn(MessageUtil.getMessage(CoreMessageConstants.FILE_NOT_FOUND, "File({0}) Not Found.", MessageUtil.params(id)));
			return null;
		}
		
		String filePath = AttachmentUtil.getAttachmentFileFullPath(attachment);
		return this.streamingFileByPath(req, res, filePath);
	}

	/**
	 * FilePath 기반으로 파일을 다운로드한다. 
	 * 
	 * @param req
	 * @param res
	 * @param filePath
	 * @return
	 */
	private BasicOutput downloadFileByPath(HttpServletRequest req, HttpServletResponse res, String filePath, String fileName) {
		this.pathBasedDownloadHandler.handleRequest(req, res, filePath, fileName);
		return new BasicOutput();
	}
	
	/**
	 * FilePath 기반으로 파일을 스트리밍한다. 
	 * 
	 * @param req
	 * @param res
	 * @param filePath
	 * @return
	 */
	private BasicOutput streamingFileByPath(HttpServletRequest req, HttpServletResponse res, String filePath) {	 
		this.streamingHandler.handleRequest(req, res, filePath);
	    return new BasicOutput();
	}
}