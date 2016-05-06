package com.sitech.prm.hn.unicomclient.activity;

import com.cbstest.unicomclient.R;

import cn.com.senter.mediator.BluetoothReader;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

public class BlueReaderHelper {
	private Context context;

	private BluetoothReader bluecardreader;

	public BlueReaderHelper(Context context, Handler handler) {
		this.context = context;
		bluecardreader = new BluetoothReader(handler, context);
	}

	public String read() {
		return bluecardreader.readCard_Sync();
	}

	public boolean openBlueConnect(String address) {
		return bluecardreader.registerBlueCard(address);
	}

	public void closeBlueConnect() {
		Log.e("blue", "close blue");
		bluecardreader.unRegisterBlueCard();
	}

	public void setServerAddress(String server_address) {
		bluecardreader.setServerAddress(server_address);
	}

	public void setServerPort(int server_port) {
		bluecardreader.setServerPort(server_port);
	}

	public boolean isConnected() {
		return bluecardreader.isConnected();
	}

}
