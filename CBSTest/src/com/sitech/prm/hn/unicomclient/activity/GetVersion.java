package com.sitech.prm.hn.unicomclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;

public class GetVersion extends Activity {
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

		};

	};

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Intent intent = new Intent();
		String code = getVersion();
		intent.putExtra("code", code);
		setResult(12, intent);
		finish();
	}

	private String getVersion() {
		/**
		 * 获取版本号
		 * 
		 * @return 当前应用的版本号
		 */
		PackageManager manager;

		PackageInfo info = null;

		manager = this.getPackageManager();

		try {

			info = manager.getPackageInfo(this.getPackageName(), 0);

		} catch (NameNotFoundException e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		}
		return info.versionName;
	}
}
