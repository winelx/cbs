/*************************************************************************************************
 * ��Ȩ���� (C)2012,  �����п��Ѽ��Źɷ����޹�˾ 
 * 
 * �ļ���ƣ�UpdateService.java
 * ����ժҪ�������
 * ��ǰ�汾��
 * ��         �ߣ� hexiaoming
 * ������ڣ�2012-12-24
 * �޸ļ�¼��
 * �޸����ڣ�
 * ��   ��  �ţ�
 * ��   ��  �ˣ�
 * �޸����ݣ�
 ************************************************************************************************/
package com.sitech.prm.hn.unicomclient.service;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.cbstest.unicomclient.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;
import android.widget.Toast;


/***
 * �����
 * 
 * @author hexiaoming
 * 
 */
public class UpdateVersionService extends Service {
	
	public static final String Install_Apk = "Install_Apk";
	/********download progress step*********/
	private static final int down_step_custom = 3;
	
	private static final int TIMEOUT = 10 * 1000;// ��ʱ
	private static String down_url;
	private static final int DOWN_OK = 1;
	private static final int DOWN_ERROR = 0;
	
	private String app_name;
	
	private NotificationManager notificationManager;
	private Notification notification;
	private Intent updateIntent;
	private PendingIntent pendingIntent;
	private RemoteViews contentView;

		
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	/** 
	* ����������onStartCommand����
	* @param   Intent intent, int flags, int startId
	* @return    int
	* @see     UpdateVersionService
	*/
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		app_name = intent.getStringExtra("Key_App_Name");
		down_url = intent.getStringExtra("Key_Down_Url");
		
		// create file,Ӧ��������ط���һ������ֵ���ж�SD���Ƿ�׼���ã��ļ��Ƿ񴴽��ɹ����ȵȣ�
		FileUtil.createFile(app_name);
		
		if(FileUtil.isCreateFileSucess == true){			
			createNotification();
			createThread();
		}else{
			Toast.makeText(this, R.string.insert_card, Toast.LENGTH_SHORT).show();
			/***************stop service************/
			stopSelf();
			
		}
		
		return super.onStartCommand(intent, flags, startId);
	}


	
	/********* update UI******/		 
	private final Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case DOWN_OK:
				
				/*********������ɣ������װ***********/
				Uri uri = Uri.fromFile(FileUtil.updateFile);
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(uri,"application/vnd.android.package-archive");
				pendingIntent = PendingIntent.getActivity(UpdateVersionService.this, 0, intent, 0);
				
				notification.flags = Notification.FLAG_AUTO_CANCEL; 				 
				notification.setLatestEventInfo(UpdateVersionService.this,app_name, getString(R.string.down_sucess), pendingIntent);
				//notification.setLatestEventInfo(UpdateService.this,app_name, app_name + getString(R.string.down_sucess), null);			
				notificationManager.notify(R.layout.notification_item, notification);	
				
				/*****��װAPK******/
				//installApk();	
				
				//stopService(updateIntent);
				/***stop service*****/
				stopSelf();
				break;
				
			case DOWN_ERROR:
				notification.flags = Notification.FLAG_AUTO_CANCEL; 
				//notification.setLatestEventInfo(UpdateService.this,app_name, getString(R.string.down_fail), pendingIntent);
				notification.setLatestEventInfo(UpdateVersionService.this,app_name, getString(R.string.down_fail), null);
				
				/***stop service*****/
				//onDestroy();
				stopSelf();
				break;
				
			default:
				//stopService(updateIntent);
				/******Stop service******/
				//stopService(intentname)
				//stopSelf();
				break;
			}
		}
	};
	
	private void installApk() {
		// TODO Auto-generated method stub
		/*********������ɣ������װ***********/
		Uri uri = Uri.fromFile(FileUtil.updateFile);
		Intent intent = new Intent(Intent.ACTION_VIEW);
		
		/**********�������������Ϊʹ��Context��startActivity�����Ļ�������Ҫ����һ���µ�task**********/
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
		
		intent.setDataAndType(uri,"application/vnd.android.package-archive");			        
        UpdateVersionService.this.startActivity(intent);	       
	}
	
	/** 
	* ����������createThread����, ���߳�����
	* @param   
	* @return   
	* @see     UpdateVersionService
	*/
	public void createThread() {
		new DownLoadThread().start();
	}

	
	private class DownLoadThread extends Thread{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message message = new Message();
			try {								
				long downloadSize = downloadUpdateFile(down_url,FileUtil.updateFile.toString());
				if (downloadSize > 0) {					
					// down success										
					message.what = DOWN_OK;
					handler.sendMessage(message);																		
				}
			} catch (Exception e) {
				e.printStackTrace();
				message.what = DOWN_ERROR;
				handler.sendMessage(message);
			}						
		}		
	}
	


	/** 
	* ����������createNotification����
	* @param   
	* @return    
	* @see     UpdateVersionService
	*/
	public void createNotification() {
		
		//notification = new Notification(R.drawable.dot_enable,app_name + getString(R.string.is_downing) ,System.currentTimeMillis());
		notification = new Notification(
				//R.drawable.video_player,//Ӧ�õ�ͼ��
				R.drawable.ic_launcher,//Ӧ�õ�ͼ��
				app_name + getString(R.string.is_downing),
				System.currentTimeMillis());
		notification.flags = Notification.FLAG_ONGOING_EVENT; 
		//notification.flags = Notification.FLAG_AUTO_CANCEL;
		
		 /*** �Զ���  Notification ����ʾ****/		 
		contentView = new RemoteViews(getPackageName(),R.layout.notification_item);
		contentView.setTextViewText(R.id.notificationTitle, app_name + getString(R.string.is_downing));
		contentView.setTextViewText(R.id.notificationPercent, "0%");
		contentView.setProgressBar(R.id.notificationProgress, 100, 0, false);
		notification.contentView = contentView;

//		updateIntent = new Intent(this, AboutActivity.class);
//		updateIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//		//updateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		pendingIntent = PendingIntent.getActivity(this, 0, updateIntent, 0);
//		notification.contentIntent = pendingIntent;
		
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(R.layout.notification_item, notification);
	}

	/***
	 * down file
	 * 
	 * @return
	 * @throws MalformedURLException
	 */
	public long downloadUpdateFile(String down_url, String file)throws Exception {
		
		int down_step = down_step_custom;// ��ʾstep
		int totalSize;// �ļ��ܴ�С
		int downloadCount = 0;// �Ѿ����غõĴ�С
		int updateCount = 0;// �Ѿ��ϴ����ļ���С
		
		InputStream inputStream;
		OutputStream outputStream;

		URL url = new URL(down_url);
		HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
		httpURLConnection.setConnectTimeout(TIMEOUT);
		httpURLConnection.setReadTimeout(TIMEOUT);
		// ��ȡ�����ļ���size
		totalSize = httpURLConnection.getContentLength();
		
		if (httpURLConnection.getResponseCode() == 404) {
			throw new Exception("fail!");
			//����ط�Ӧ�ü�һ������ʧ�ܵĴ��?���ǣ���Ϊ�������������һ��try---catch���Ѿ�������Exception,
			//���Բ��ô���						
		}
		
		inputStream = httpURLConnection.getInputStream();
		outputStream = new FileOutputStream(file, false);// �ļ������򸲸ǵ�
		
		byte buffer[] = new byte[1024];
		int readsize = 0;
		
		while ((readsize = inputStream.read(buffer)) != -1) {
									
			outputStream.write(buffer, 0, readsize);
			downloadCount += readsize;// ʱʱ��ȡ���ص��Ĵ�С
			/*** ÿ������3%**/
			if (updateCount == 0 || (downloadCount * 100 / totalSize - down_step) >= updateCount) {
				updateCount += down_step;
				// �ı�֪ͨ��
				contentView.setTextViewText(R.id.notificationPercent,updateCount + "%");
				contentView.setProgressBar(R.id.notificationProgress, 100,updateCount, false);			
				notification.contentView = contentView;
				notificationManager.notify(R.layout.notification_item, notification);			
			}
		}
		if (httpURLConnection != null) {
			httpURLConnection.disconnect();
		}
		inputStream.close();
		outputStream.close();
		
		return downloadCount;
	}

}