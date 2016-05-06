package com.sitech.prm.hn.unicomclient.application;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.Application;
import android.webkit.WebView;

import com.baidu.location.LocationClient;
import com.baidu.mapapi.SDKInitializer;
import com.sitech.prm.hn.unicomclient.bean.ServSession;

public class GlobalApplication extends Application {
	
	public static List<Activity> activityList;// 记录需要管理的activity列表
	public static ServSession session;
	public String success;//回调成功js函数
	public String fail;//回调失败js函数
	public WebView webView;
	public static LocationClient locationClient;
	public static String locatinInfo;
	public static String type;//定位类型0：表示返回到js，1表示java代码获取地址信息
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		if (activityList == null) {
			activityList = new ArrayList<Activity>();
		}
		
		if (session == null) {
			session = new ServSession();
		}
		SDKInitializer.initialize(this);
	}
	
	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
		
		activityList.removeAll(activityList);
		activityList = null;
	}
	
	public static void finishAllActivity() {
		if (locationClient != null && locationClient.isStarted()) {
			 locationClient.stop();
			 locationClient = null;
		 }
		// 关闭list中的activity
		int l = activityList.size();
		for (int i = 0; i < l; i++) {
			Activity activity = activityList.get(i);
			activity.finish();
		}
		activityList.removeAll(activityList);
	}
	
}
