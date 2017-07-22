package com.ryx.social.retail.service.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ryx.framework.util.Constants;
import com.ryx.framework.util.DateUtil;
import com.ryx.framework.util.IDUtil;
import com.ryx.framework.util.MapUtil;
import com.ryx.framework.util.ResponseUtil;
import com.ryx.social.retail.dao.IBaseDataDao;
import com.ryx.social.retail.dao.IFileDao;
import com.ryx.social.retail.dao.IMerchDao;
import com.ryx.social.retail.service.IFileService;
import com.ryx.social.retail.util.RetailConfig;
@Service
public class FileServiceImpl implements IFileService {
	
	private static final Logger LOG = LoggerFactory.getLogger(FileServiceImpl.class);
	
	@Resource
	private IFileDao fileDao;

	@Resource
	private IMerchDao merchDao;

	@Resource
	private IBaseDataDao baseDataDao;
	
	/**
	 * 梁凯 2014年5月26日11:56:49
	 * 备份数据库文件专用, 只保留最近7个记录
	 */
	@Override
	public List<Map<String, Object>> backupDB(HttpServletRequest request, Map<String, Object> backupParam) throws Exception {
		LOG.debug("FileServiceImp backupDB backupParam: " + backupParam);
		// 如果传的是lice_id, 则根据lice_id获取merch_id
		if(backupParam.containsKey("lice_id")) {
			Map<String, Object> merchParam = MapUtil.rename(backupParam, "lice_id");
			List<Map<String, Object>> merchList = baseDataDao.selectBaseMerch(merchParam);
			if(!merchList.isEmpty()) backupParam.put("merch_id", MapUtil.getString(merchList.get(0), "merch_id"));
		}
		List<Map<String, Object>> createFileList = this.uploadFile(request, backupParam);
		String merchId = createFileList.isEmpty() ? "" : (String) createFileList.get(0).get("merch_id");
		Map<String, Object> fileParam = new HashMap<String, Object>();
		fileParam.put("merch_id", merchId);
		fileParam.put("file_purpose", "88");
		List<Map<String, Object>> merchFileList = this.selectMerchFile(fileParam);
		if(merchFileList.size() > 7) {
			merchFileList = merchFileList.subList(7, merchFileList.size()); // 早插入的在后面, 删除后面的
			for(Map<String, Object> merchFileMap : merchFileList) { // 正常情况下一次只需要删除一个文件
				Map<String, Object> removeParam = new HashMap<String, Object>();
				removeParam.put("merch_id", merchFileMap.get("merch_id"));
				removeParam.put("file_id", merchFileMap.get("file_id"));
				this.removeFile(request, removeParam);
			}
		}
		return createFileList;
	}
	
	/**
	 * 梁凯
	 * 修改店铺头像, 先将所有店铺图片改为01, 然后再把传入file_id对应的图片改为02
	 */
	@Override
	public void modifyShopPortrait(Map<String, Object> fileParam) throws Exception {
		LOG.debug("FileServiceImp modifyShopPortrait fileParam:"+fileParam);
		Map<String, Object> updateFileParam = new HashMap<String, Object>();
		updateFileParam.put("merch_id", fileParam.get("merch_id"));
		updateFileParam.put("file_purpose", "01");
		fileDao.updateMerchFile(updateFileParam);
		Map<String, Object> updateMerchParam = new HashMap<String, Object>();
		updateFileParam.put("merch_id", fileParam.get("merch_id"));
		updateMerchParam.put("file_id", fileParam.get("file_id"));
		updateMerchParam.put("file_purpose", "02");
		fileDao.updateMerchFile(updateMerchParam);
		merchDao.updateMerchInfo(updateMerchParam);
	}
	
	/**
	 * 梁凯 2014年5月23日
	 * 如果批量上传文件则按上传顺序返回生成的merch_id, file_id和file_location
	 */	
	@Override
	public List<Map<String, Object>> uploadFile(HttpServletRequest request, Map<String, Object> uploadParam) throws Exception {
		LOG.debug("FileServiceImp uploadFile uploadParam: " + uploadParam);
		DiskFileItemFactory factory = new DiskFileItemFactory();
		factory.setSizeThreshold(4 * 1024 * 1024); // 设置缓冲区大小，这里是4MB
		ServletFileUpload upload = new ServletFileUpload(factory);
		upload.setSizeMax(MapUtil.getInt(uploadParam, "file_size", 4) * 1024l * 1024); // 设置最大文件尺寸，这里默认4MB

		String merchId = (String) uploadParam.get("merch_id");
		String filePurpose = (String) uploadParam.get("file_purpose");
		Map<String, Object> maxSequenceParam = new HashMap<String, Object>();
		maxSequenceParam.put("merch_id", merchId);
		maxSequenceParam.put("file_purpose", filePurpose);
		
		Map<String, Object> paramMap = null;
		List<Map<String, Object>> fileInfoList = new ArrayList<Map<String, Object>>();
		Map<String, Object> fileInfoMap = null;
		
		List<FileItem> fileItemList = upload.parseRequest(request);// 得到所有的文件
		Iterator<FileItem> fileIt = fileItemList.iterator();
		while(fileIt.hasNext()) { // 批量上传的时候无法保证插入数据库的效率, 因为转存文件必须一个一个的进行, 转存之后必须立刻插入数据库
			FileItem fileItem = fileIt.next();
			if(fileItem.isFormField()) { // isFormField如果为false才是文件
				if(merchId==null && "merch_id".equals(fileItem.getFieldName())) {
					merchId = fileItem.getString();
					maxSequenceParam.put("merch_id", merchId);
				}
			} else {
				paramMap = new HashMap<String, Object>();
				String fileName = fileItem.getName();
				String fileExt = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf(".")) : "";
				String fileId = IDUtil.getId();
				String fileFoldPath = RetailConfig.getNfsdataPath()+ "user_file/" + merchId;
				String filePath = fileFoldPath + "/" + fileId;
				File userFold = new File(fileFoldPath);
				File userFile = new File(filePath); // 新生成的文件不包含后缀
				if(userFold.exists() || userFold.mkdirs()) {
					int maxSequence = fileDao.searchMaxFileSequence(maxSequenceParam);
					paramMap.put("file_sequence", ++maxSequence); // 文件顺序
					paramMap.put("file_purpose", filePurpose);
					paramMap.put("merch_id", merchId);
					paramMap.put("file_id", fileId);
					paramMap.put("file_name", fileName); // 文件原名称
					paramMap.put("file_type", fileExt); // 文件后缀
					paramMap.put("file_location", "user_file/" + merchId + "/" + fileId); // 存放路径
					paramMap.put("upload_date", DateUtil.getToday()); // 日期
					paramMap.put("upload_time", DateUtil.getCurrentTime().substring(8)); // 时间
					paramMap.put("status", "1"); // 状态默认是可用的
					paramMap.put("file_height", MapUtil.getString(uploadParam, "file_height", "")); // 属性1
					paramMap.put("file_width", MapUtil.getString(uploadParam, "file_width", "")); // 属性2
					paramMap.put("file_description", MapUtil.getString(uploadParam, "file_description", "")); // 描述
					fileItem.write(userFile);
					fileDao.insertMerchFile(paramMap); // 将文件信息插入到数据库
					fileInfoMap = new HashMap<String, Object>();
					fileInfoMap.put("merch_id", merchId);
					fileInfoMap.put("file_id", fileId);
					fileInfoMap.put("file_location", filePath);
					fileInfoList.add(fileInfoMap);
				} else {
					throw new RuntimeException("文件目录不存在且创建错误");
				}
			}
		}
		return fileInfoList;
	}
	
	/**
	 * 梁凯 2014年5月26日
	 * 通过merch_id和file_id下载文件, 如果查询到多个文件, 则下载最新插入的
	 */
	@Override
	public void downloadFile(HttpServletRequest request, HttpServletResponse response, Map<String, Object> downloadParam) throws Exception {
		LOG.debug("FileServiceImp downloadFile downloadParam:" + downloadParam);
		if(downloadParam.containsKey("lice_id")) {
			Map<String, Object> merchParam = MapUtil.rename(downloadParam, "lice_id");
			List<Map<String, Object>> merchList = baseDataDao.selectBaseMerch(merchParam);
			if(!merchList.isEmpty()) downloadParam.put("merch_id", MapUtil.getString(merchList.get(0), "merch_id"));
		}
		InputStream merchFileInputStream = null;
		try{
			List<Map<String, Object>> merchFileList = this.selectMerchFile(downloadParam);
			if(merchFileList!=null && !merchFileList.isEmpty()) {
				Map<String, Object> merchFileMap = merchFileList.get(0);
				String fileLocation = RetailConfig.getNfsdataPath() + MapUtil.getString(merchFileMap, "file_location");
				String fileName = (String) merchFileMap.get("file_name");
				merchFileInputStream = new FileInputStream(fileLocation);
				int len = 0;
				byte[] buffer = new byte[2048];
				response.setContentType("application/x-msdownload");
				response.addHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
				while ((len = merchFileInputStream.read(buffer)) > 0) {
					response.getOutputStream().write(buffer, 0, len);
				}
//				merchFileInputStream.close();
			} else {
				ResponseUtil.write(request, response, Constants.SUCCESS, Constants.SUCCESS_MSG, null);
			}
		}catch(Exception e){
			LOG.error("downloadFile错误：",e);
		}finally{
			if(merchFileInputStream!=null){
				merchFileInputStream.close();
			}
		}
	}
	
	/**
	 * 梁凯 2014年5月26日11:52:48
	 * 删除物理文件和merch_file表中数据
	 */
	@Override
	public Map<String, Object> removeFile(HttpServletRequest request, Map<String, Object> removeParam) throws Exception {
		LOG.debug("FileServiceImp removeFile removeParam:" + removeParam);
		Map<String, Object> removedFileMap = new HashMap<String, Object>();
		List<Map<String, Object>> merchFileList = this.selectMerchFile(removeParam);
		if(merchFileList!=null && !merchFileList.isEmpty()) {
			// 先删除表中数据, 再删除文件, 保证用户看到的数据是正确的
			Map<String, Object> removeFileParam = new HashMap<String, Object>();
			removeFileParam.put("merch_id", removeParam.get("merch_id"));
			removeFileParam.put("file_id", removeParam.get("file_id"));
			this.deleteMerchFile(removeFileParam);
			
			Map<String, Object> merchFileMap = merchFileList.get(0);
			String fileLocation = RetailConfig.getNfsdataPath() + MapUtil.getString(merchFileMap, "file_location");
			removedFileMap.put("file_name", merchFileMap.get("file_name"));
			removedFileMap.put("file_description", merchFileMap.get("file_description"));
			File removedFile =  new File(fileLocation);
			if(!removedFile.exists() || !removedFile.delete()) {
				LOG.error("文件不存在或删除错误");
			}
		}
		return removedFileMap;
	}
	
	@Override
	public List<Map<String, Object>> searchMerchFile(Map<String, Object> fileParam) throws Exception {
		LOG.debug("FileServiceImp searchMerchFile fileParam: " + fileParam);
		return this.selectMerchFile(fileParam);
	}
	
	private List<Map<String, Object>> selectMerchFile(Map<String, Object> fileParam) throws Exception {
		LOG.debug("FileServiceImp selectMerchFile fileParam: " + fileParam);
		return fileDao.selectMerchFile(fileParam);
	}
	
	private void deleteMerchFile(Map<String, Object> fileParam) throws Exception {
		LOG.debug("FileServiceImp deleteMerchFile fileParam: " + fileParam);
		fileDao.deleteMerchFile(fileParam);
	}
	
	//查询merch_file表：可是是全部数据，不用merch_id
	@Override
	public List<Map<String, Object>> selectAllMerchFile(Map<String, Object> fileParam) throws Exception {
		LOG.debug("FileServiceImp selectAllMerchFile fileParam: " + fileParam);
		return fileDao.selectAllMerchFile(fileParam);
	}
	
	
    @Override
    public List<String> deleteMerchExcessFiles(Map<String, Object> fileParam) throws Exception {
    	LOG.debug("FileServiceImpl deleteMerchExcessFiles fileParam: " + fileParam);
    	//查询数据文件
		List<Map<String, Object>> fileList =  selectAllMerchFile(new HashMap<String, Object>());
		final Map<String, Object> filePathMap = new HashMap<String, Object>();
		String merchId = "";
		String fileId = "";

		String nfsdataPath = RetailConfig.getNfsdataPath();
		for(Map<String, Object> file : fileList) {
			merchId = MapUtil.getString(file, "merch_id");
			fileId = MapUtil.getString(file, "file_id");
			filePathMap.put(nfsdataPath+"user_file/"+merchId+"/"+fileId, null);
		}
		
		File root = new File(nfsdataPath+"user_file"); // 存的是目录
		
		File[] rootDirectory = root.listFiles();//实际文件目录
		List<File> shouldRemovedFiles = new ArrayList<File>();//应该删除的文件目录
		
		if(rootDirectory == null){
			LOG.debug("=========================deleteMerchExcessFiles:rootDirectory为空，找不到服务器文件目录");
		}
		for(File directory : rootDirectory) { // 每个都是以merch_id为名称的目录
			if(directory.isDirectory()) {//是否为文件夹
				//将要删除的文件目录放到map中
				shouldRemovedFiles.addAll(
					Arrays.asList(
						directory.listFiles(
							new FileFilter() { // 目录：merch_id/file_id
								@Override
								public boolean accept(File file) {
									//文件存在，并且，保留文件map(filePathMap)不包含
									return file.exists() && !filePathMap.containsKey(file.getAbsolutePath());
								}
							}
						)
					)
				);
				//删除本目录（如果是空目录可直接删除，如果不为空，无法删除）
				shouldRemovedFiles.add(directory);
			}
		}
		List<String> removedFileNames = new ArrayList<String>();
		for(File shouldRemovedFile : shouldRemovedFiles) {
			boolean flag = shouldRemovedFile.delete();
			//如果删除成功，便返回
			if(flag){
				removedFileNames.add(shouldRemovedFile.getAbsolutePath());
			}
		}
		return removedFileNames;
	}
	
}
