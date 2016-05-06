package com.sitech.prm.hn.unicomclient.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sitech.prm.hn.unicomclient.common.MainUtils;

public class UpdateService {

	private int fileSize; // 文件大小
	private int readSize; // 读取长度
	private int downSize; // 已下载大小
	private File downFile; // 下载文件
	private boolean cancelled;
	private Map<String, String> versionInfo; // 版本信息

	public enum versionInfoField {
		filename, filetype, version, description
	}
	

	@SuppressLint("HandlerLeak")
	private Handler handMessage = new Handler() {
		public void handleMessage(Message msg) {
			
		}
	};


	/**
	 * 下载模块
	 * @param url 
	 */
	public static File startDownload(String url) {
		
		// 初始化数据
		int fileSize = 0;
		int readSize = 0;
		int downSize = 0;
		File downFile=null;
		Map<String, String> versionInfo = null; // 版本信息
		boolean cancelled = false;
		InputStream is = null;
		FileOutputStream fos = null;
		Context mContext = null;
		try {
			URL myURL = new URL(url); // 取得URL
			URLConnection conn = myURL.openConnection(); // 建立联机
			conn.connect();
			fileSize = conn.getContentLength(); // 获取文件长度
			is = conn.getInputStream(); // InputStream 下载文件

			if (is == null) {
				Log.d("tag", "error");
				throw new RuntimeException("stream is null");
			}
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在

			String path;
			if (sdCardExist) {
				path = MainUtils.PICFOLDER
						+ "TYAgentClient.apk";
				downFile = new File(path);
			} else {
				String string = mContext.getFilesDir().getAbsolutePath();
				// /data/data/com.sitech.prm.wgyx/files
				downFile = new File(string,
						versionInfo.get(versionInfoField.filename.toString())
								+ ".apk");
			}

			if (downFile.exists()) {
				downFile.delete();
			}

			fos = new FileOutputStream(downFile);

			// 将文件写入临时盘

			byte buf[] = new byte[1024 * 1024];
			while (!cancelled && (readSize = is.read(buf)) > 0) {
				fos.write(buf, 0, readSize);
				downSize += readSize;
			}

			if (cancelled) {
				downFile.delete();
			} else {
			}
			return downFile;
		} catch (MalformedURLException e) {
			return null;
		} catch (IOException e) {
			return null;
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (null != fos)
					fos.close();
				if (null != is)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
