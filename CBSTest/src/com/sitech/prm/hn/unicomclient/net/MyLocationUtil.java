package com.sitech.prm.hn.unicomclient.net;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;

/**
 * 获取当前位置信息工具类 Created by Bob on 2016-03-23
 */
public class MyLocationUtil implements BDLocationListener {

	private double latitude;
	private double longitude;

	private LocationClient mLocationClient = null;

	private static MyLocationUtil instance = null;

	public static MyLocationUtil getInstance() {
		if (instance == null) {
			instance = new MyLocationUtil();
		}
		return instance;
	}

	private MyLocationUtil() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(CiatWildApplication
					.getInstance().getApplicationContext());
			LocationClientOption option = new LocationClientOption();
			option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
			option.setOpenGps(true);
			option.setCoorType("gcj02");
			option.setScanSpan(5000);
			option.setIsNeedAddress(true);
			option.setNeedDeviceDirect(true);
			mLocationClient.setLocOption(option);
			mLocationClient.start();
		}
	}

	@Override
	public void onReceiveLocation(BDLocation location) {
		if (location == null) {
			return;
		}
		latitude = location.getLatitude();
		longitude = location.getLongitude();
	}

	/**
	 * 停止定位
	 */
	public void stopLocation() {
		if (mLocationClient != null)
			mLocationClient.stop();
	}

	/**
	 * 返回一个经纬度
	 */
	public LatLng getLatLng() {
		return new LatLng(longitude, latitude);
	}
}
