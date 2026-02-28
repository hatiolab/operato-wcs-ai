/* Copyright © HatioLab Inc. All rights reserved. */
package xyz.elidom.core.system.controller.file;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import xyz.elidom.core.CoreConstants;
import xyz.elidom.core.entity.Storage;
import xyz.elidom.core.system.controller.handler.file.upload.IFileUploadHandler;
import xyz.elidom.orm.IQueryManager;
import xyz.elidom.sys.SysConstants;
import xyz.elidom.sys.system.service.params.BasicOutput;
import xyz.elidom.sys.util.ValueUtil;
import xyz.elidom.util.FormatUtil;

/**
 * Constroller For File Upload
 * 
 * @author Minu.Kim
 */
@RestController
@Transactional
public class FileUploadController {

	@Autowired
	private IQueryManager queryManager;

	@Autowired
	private IFileUploadHandler fileUploadloadHandler;

	@RequestMapping(value = "/rest/upload", method = RequestMethod.POST)
	public @ResponseBody Object fileUpload(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestParam("file") MultipartFile[] files,
			@RequestParam(name = "keep_filename", required = false) Boolean keepFilename) {
		
		return doFileUpload(req, res, req.getParameter("path"), null, keepFilename, files);
	}

	@RequestMapping(value = "/rest/upload/{path}", method = RequestMethod.POST)
	public @ResponseBody Object fileUploadWithPath(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@PathVariable("path") String path, 
			@RequestParam("file") MultipartFile[] files,
			@RequestParam(name = "keep_filename", required = false) Boolean keepFilename) {
		
		return doFileUpload(req, res, path, null, keepFilename, files);
	}
	
	@RequestMapping(value = "/rest/upload/{path}/{customPath}", method = RequestMethod.POST)
	public @ResponseBody Object fileUploadWithPath(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@PathVariable("path") String path,
			@PathVariable("customPath") String customPath, 
			@RequestParam("file") MultipartFile[] files,
			@RequestParam(name = "keep_filename", required = false) Boolean keepFilename) {
		
		return doFileUpload(req, res, path, customPath, keepFilename, files);
	}
	
	@RequestMapping(value = "/rest/upload/attach/{path}", method = RequestMethod.POST)
	public @ResponseBody Object fileUploadWithPath(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@PathVariable("path") String path,
			@RequestParam(name = "keep_filename", required = false) Boolean keepFilename) {
		
		StandardMultipartHttpServletRequest request = (StandardMultipartHttpServletRequest) req;
		String onType = request.getParameterValues("attachment[on_type]")[0];
		String onId = request.getParameterValues("attachment[on_id]")[0];
		MultipartFile file = request.getFile("attachment[path]");		
		return doFileUpload(req, res, path, onId, onType, null, keepFilename, file);
	}
	
	@RequestMapping(value = "/rest/upload_resource", method = RequestMethod.POST)
	public @ResponseBody Object fileUploadByResource(
			HttpServletRequest req, 
			HttpServletResponse res, 
			@RequestParam("file") MultipartFile[] files,
			@RequestParam(name = "keep_filename", required = false) Boolean keepFilename) {
		
		// Find Resource Path
		Storage cond = new Storage();
		cond.setResourceFlag(true);
		Storage resourceStorage = queryManager.selectByCondition(Storage.class, cond);		
		String path = (resourceStorage != null) ? resourceStorage.getName() : null;
		return doFileUpload(req, res, path, null, keepFilename, files);
	}
	
	/**
	 * File Upload 실행.
	 * 
	 * @param req
	 * @param res
	 * @param path
	 * @param customPath
	 * @param keepFilename
	 * @param files
	 * @return
	 */
	private Object doFileUpload(
			HttpServletRequest req, 
			HttpServletResponse res, 
			String path, 
			String customPath, 
			Boolean keepFilename,
			MultipartFile... files) {
		
		String uploadPath = ValueUtil.isEmpty(path) ? req.getParameter("path") : path;
		String onId = req.getParameter("onId");
		String onType = req.getParameter("onType");
		return doFileUpload(req, res, uploadPath, onId, onType, customPath, keepFilename, files);
	}
	
	/**
	 * File Upload 실행.
	 * 
	 * @param req
	 * @param res
	 * @param path
	 * @param onId
	 * @param onType
	 * @param customPath
	 * @param keepFilename
	 * @param files
	 * @return
	 */
	@SuppressWarnings("all")
	private Object doFileUpload(
			HttpServletRequest req,
			HttpServletResponse res, 
			String path, 
			String onId, 
			String onType, 
			String customPath,
			Boolean keepFilename,
			MultipartFile... files) {
		
		String getPath = ValueUtil.isEmpty(path) ? req.getParameter("path") : path;
		Map<String, String> fileInfoMap = new HashMap<String, String>();
		fileInfoMap.put(CoreConstants.FILE_ON_ID, onId);
		fileInfoMap.put(CoreConstants.FILE_ON_TYPE, onType);
		fileInfoMap.put(CoreConstants.FILE_SAVE_PATH_NAME, getPath);
		fileInfoMap.put(CoreConstants.FILE_SAVE_CUSTOM_PATH, customPath);
		
		if(keepFilename != null && keepFilename == true) {
			fileInfoMap.put("keep_filename", SysConstants.YES_STRING);
		}
		
		fileUploadloadHandler.handleRequest(req, res, fileInfoMap, files);
		return FormatUtil.toJsonString(new BasicOutput());
	}
	
}