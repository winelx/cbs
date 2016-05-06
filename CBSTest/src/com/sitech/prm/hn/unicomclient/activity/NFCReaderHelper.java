package com.sitech.prm.hn.unicomclient.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import cn.com.senter.mediator.NFCardReader;

public class NFCReaderHelper {

	private Context context;

	private NFCardReader nfCardReader;

	public NFCReaderHelper(Context context, Handler handler) {
		this.context = context;
		nfCardReader = new NFCardReader(handler, context);
	}

	public void read(Activity activity) {
		nfCardReader.EnableSystemNFCMessage();
	}

	public void disable(Activity activity) {
		nfCardReader.DisableSystemNFCMessage();
	}

	public boolean isNFC(Intent intent) {

		return nfCardReader.isNFC(intent);

	}

	public String readCardWithIntent(Intent intent) {
		return nfCardReader.readCardWithIntent_Sync(intent);
	}

	public void setServerAddress(String server_address) {
		nfCardReader.setServerAddress(server_address);

	}

	public void setServerPort(int server_port) {
		nfCardReader.setServerPort(server_port);
	}
	

}
