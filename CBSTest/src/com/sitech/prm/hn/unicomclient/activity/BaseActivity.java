package com.sitech.prm.hn.unicomclient.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;

import com.sitech.prm.hn.unicomclient.application.GlobalApplication;

public class BaseActivity extends Activity {
	public GlobalApplication application;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		application = (GlobalApplication) getApplication();
	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if(keyCode == KeyEvent.KEYCODE_BACK){
	// AlertDialog isExit = new AlertDialog.Builder(this).create();
	// isExit.setTitle("系统提示");
	// isExit.setMessage("确定要退出吗？");
	//
	// isExit.setButton("确定", listener);
	// isExit.setButton2("取消", listener);
	//
	// isExit.show();
	// }
	// return false;
	// }

	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				GlobalApplication.finishAllActivity();
				break;
			case AlertDialog.BUTTON_NEGATIVE:
				break;
			default:
				break;
			}
		}
	};
}
