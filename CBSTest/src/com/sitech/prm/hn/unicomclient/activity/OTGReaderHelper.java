package com.sitech.prm.hn.unicomclient.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import cn.com.senter.mediator.OTGCardReader;

public class OTGReaderHelper {

	private Context context;
	private OTGCardReader otgCardReader;

	public OTGReaderHelper(Context context, Handler handler) {
		this.context = context;
		otgCardReader = new OTGCardReader(handler, context);
	}

	public void close() {

	}

	// private static final String ACTION_USB_PERMISSION ="android.hardware.usb.action.USB_DEVICE_ATTACHED";
	public boolean registerotg(Activity activity) {

		boolean bRet = otgCardReader.registerOTGCard();

		return bRet;
	}

	public String Read() {

		return otgCardReader.readCard_Sync();
	}

	public void setServerAddress(String server_address) {
		otgCardReader.setServerAddress(server_address);
	}

	public void setServerPort(int server_port) {
		otgCardReader.setServerPort(server_port);
	}
	

}
