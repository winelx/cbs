package com.sitech.prm.hn.unicomclient.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

public class NetworkManager {

	private ConnectivityManager mConnectivityManager;
	private WifiManager mWifiManager;

	public NetworkManager(Context mContext) {
		mConnectivityManager = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		mWifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
	}

	public boolean isAvailable() {
		NetworkInfo info = mConnectivityManager.getActiveNetworkInfo();
		boolean conn = false;
		if (info != null) {
			conn = info.isConnected();
			return conn;
		}
		return conn;
	}

	public boolean isWifiActive() {
		return mWifiManager.isWifiEnabled();
	}

	public void startWifi() {
		try {
			mWifiManager.setWifiEnabled(true);
		} catch (Exception e) {
		}
	}

	public void closeWifi(String success, String fail) {
		try {
			mWifiManager.setWifiEnabled(false);
		} catch (Exception e) {
		}
	}
}
