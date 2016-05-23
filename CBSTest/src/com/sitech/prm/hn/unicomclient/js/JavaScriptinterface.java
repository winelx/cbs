package com.sitech.prm.hn.unicomclient.js;

import java.net.URLDecoder;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;

import com.sitech.prm.hn.unicomclient.activity.Blutoothreader;
import com.sitech.prm.hn.unicomclient.activity.CustomDialog;
import com.sitech.prm.hn.unicomclient.activity.GetVersion;
import com.sitech.prm.hn.unicomclient.activity.JYreader;
import com.sitech.prm.hn.unicomclient.activity.MipcaActivityCapture;
import com.sitech.prm.hn.unicomclient.activity.Popwindows;
import com.sitech.prm.hn.unicomclient.activity.ReadCardInfo;
import com.sitech.prm.hn.unicomclient.activity.SkipDownload;
import com.sitech.prm.hn.unicomclient.activity.SrReaderCard;
import com.sitech.prm.hn.unicomclient.activity.TakeCamera;
import com.sitech.prm.hn.unicomclient.activity.goLocation;
import com.sitech.prm.hn.unicomclient.application.GlobalApplication;
import com.sitech.prm.hn.unicomclient.common.BatteryUtils;
import com.sitech.prm.hn.unicomclient.service.BatteryListener;
import com.sitech.prm.hn.unicomclient.service.UpdateVersionService;
import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.constants.OpenConstants;
import com.tencent.mobileqq.openpay.data.pay.PayApi;

public class JavaScriptinterface {
	private Context context;
	private GlobalApplication application;
	private String param;
	// 这个一定要定义，要不在showToast()方法里没办法启动intent
	Activity activity;

	public JavaScriptinterface(Context c, GlobalApplication application) {
		this.context = c;
		this.application = application;
		activity = (Activity) c;
	}

	/***
	 * 此处写了JS调用Android原生代码 <script type="text/javascript"> function
	 * jsToAndriod() { var msg = 'jsToAndriod';
	 * javascript:JSEngine.getResult(msg,"success","fail"); }
	 * 
	 * function success(str){ alert("成功方法"); } function fail(str){
	 * alert("失败方法"); } </script>
	 * 
	 * @param str
	 * @param succfun
	 * @param failfun
	 */
	public void getResult(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		System.out.println("result = " + str);
		application.webView.loadUrl("javascript:" + application.success
				+ "('获取地址信息失败');");
	}

	public void startLocation(String str, String succfun, String failfun) {
		application.type = "0";
		application.success = succfun;
		application.fail = failfun;
		application.locationClient.start();
		application.locationClient.requestLocation();
	}

	public void xundian(String str, String succfun, String failfun) {
		application.type = "1";
		param = str;
		application.success = succfun;
		application.fail = failfun;
		application.locationClient.start();
		application.locationClient.requestLocation();
	}

	public void activiation(final String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		BatteryUtils.batteryLevel(context, new BatteryListener() {
			@Override
			public void onBatteryCallback(boolean flag) {
				if (!flag) {
					Intent intent = new Intent();
					((Activity) activity).startActivityForResult(intent, 1);
				} else {
					final Intent intent = new Intent(activity, TakeCamera.class);
					intent.putExtra("param", str);
					((Activity) activity).startActivityForResult(intent, 1);
				}
			}
		});
	}

	public void readcard(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, ReadCardInfo.class);
		intent.putExtra("loginTicket", str);
		((Activity) activity).startActivityForResult(intent, 11);
	}

	public void readcardtwo(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, Blutoothreader.class);
		intent.putExtra("loginTicket", str);
		((Activity) activity).startActivityForResult(intent, 11);
	}

	public void readcardJY(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, JYreader.class);
		intent.putExtra("loginTicket", str);
		((Activity) activity).startActivityForResult(intent, 11);
	}

	public void readcardSR(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, SrReaderCard.class);
		intent.putExtra("loginTicket", str);
		((Activity) activity).startActivityForResult(intent, 11);
	}

	public void camera(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, Popwindows.class);
		intent.putExtra("loginTicket", str);
		((Activity) activity).startActivityForResult(intent, 11);
	}

	public void checkVersion(String str, String succfun, String failfun) {

		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, GetVersion.class);
		((Activity) activity).startActivityForResult(intent, 12);
	}

	public void update(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, SkipDownload.class);
		intent.putExtra("url", str);
		((Activity) activity).startActivityForResult(intent, 13);
	}

	public void updateVersion(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, UpdateVersionService.class);
		intent.putExtra("Key_App_Name", "TYAgentClient");
		intent.putExtra("Key_Down_Url", str);
		activity.startService(intent);
	}

	public void scan(String str, String succfun, String failfun) {
		application.success = succfun;
		application.fail = failfun;
		Intent intent = new Intent(activity, MipcaActivityCapture.class);
		((Activity) activity).startActivityForResult(intent, 15);
	}

	Intent intentGoLocation = null;

	public void goLocation(String str, String succfun, String failfun) {
		String string[] = str.split(",");
		String login_no = string[0];
		String channel_id = string[1];
		if (intentGoLocation == null) {
			intentGoLocation = new Intent(activity, goLocation.class);
			application.success = succfun;
			application.fail = failfun;
			intentGoLocation.putExtra("channel_id", channel_id);
			intentGoLocation.putExtra("login_no", login_no);
			((Activity) activity).startActivityForResult(intentGoLocation, 16);
		} else {
			Log.i("", "--------------1");
			intentGoLocation = null;
		}
	}

	IOpenApi openApi;

	// 预存款充值
	public void qbPay(String str, String succfun, String failfun) {
		try {
			int paySerial = 2;
			String[] fStr = new String[7];
			Pattern pattern = Pattern.compile("[&]");
			String[] patStr = pattern.split(str);
			for (int i = 0; i < patStr.length; i++) {
				fStr[i] = patStr[i].substring(patStr[i].indexOf("=") + 1);
			}
			String appid = fStr[0];
			String bargainorId = fStr[1];
			String nonceStr = fStr[2];
			String sig = fStr[3];
			sig = URLDecoder.decode(sig, "utf-8");
			String sigType = fStr[4];
			long timeStamp = Long.parseLong(fStr[5]);
			// String tokenId = fStr[6].substring(0, fStr[6].indexOf('"'));
			String tokenId = fStr[6];
			// 支付序号,用于标识此次支付
			String serialNumber = "" + paySerial++;
			String callbackScheme = "qwallet"; // QQ钱包支付结果回调给urlscheme为callbackScheme的activity.
			String pubAcc = ""; // 手Q公众帐号id.参与支付签名，签名关键字key为pubAcc
			String pubAccHint = ""; // 支付完成页面，展示给用户的提示语：提醒关注公众帐号
			openApi = OpenApiFactory.getInstance(activity, appid);
			// 启动QQ支付
			if (!openApi.isMobileQQInstalled()) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						activity);
				builder.setMessage("未安装QQ");
				builder.setTitle("提示");
				builder.setNegativeButton("确定",
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
				return;
			}
			if (!openApi.isMobileQQSupportApi(OpenConstants.API_NAME_PAY)) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						activity);
				builder.setMessage("不支持手Q支付");
				builder.setTitle("提示");
				builder.setNegativeButton("确定",
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
				return;
			}
			PayApi api = new PayApi();
			api.appId = appid;
			api.bargainorId = bargainorId;
			api.nonce = nonceStr;
			api.sig = sig;
			api.sigType = sigType;
			api.timeStamp = timeStamp;
			api.tokenId = tokenId;
			api.serialNumber = serialNumber;
			api.callbackScheme = callbackScheme;
			api.pubAcc = pubAcc;
			api.pubAccHint = pubAccHint;
			if (api.checkParams()) {
				openApi.execApi(api);
			}
		} catch (Exception e) {
			Log.i("", "==catch==" + e.getMessage());
		}
	}

	// 空充充值
	public void kqPay(String str, String succfun, String failfun) {
		try {
			int paySerial = 1;
			String[] fStr = new String[7];
			Pattern pattern = Pattern.compile("[&]");
			String[] patStr = pattern.split(str);
			for (int i = 0; i < patStr.length; i++) {
				fStr[i] = patStr[i].substring(patStr[i].indexOf("=") + 1);
			}
			String appid = fStr[0];
			String bargainorId = fStr[1];
			String nonceStr = fStr[2];
			String sig = fStr[3];
			sig = URLDecoder.decode(sig, "utf-8");
			String sigType = fStr[4];
			long timeStamp = Long.parseLong(fStr[5]);
			String tokenId = fStr[6];

			// 支付序号,用于标识此次支付
			String serialNumber = "" + paySerial++;
			String callbackScheme = "qwallet"; // QQ钱包支付结果回调给urlscheme为callbackScheme的activity.
			String pubAcc = ""; // 手Q公众帐号id.参与支付签名，签名关键字key为pubAcc
			String pubAccHint = ""; // 支付完成页面，展示给用户的提示语：提醒关注公众帐号
			openApi = OpenApiFactory.getInstance(activity, appid);

			// 启动QQ支付
			if (!openApi.isMobileQQInstalled()) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						activity);
				builder.setMessage("未安装QQ");
				builder.setTitle("提示");
				builder.setNegativeButton("确定",
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
				return;
			}
			if (!openApi.isMobileQQSupportApi(OpenConstants.API_NAME_PAY)) {
				CustomDialog.Builder builder = new CustomDialog.Builder(
						activity);
				builder.setMessage("不支持手Q支付");
				builder.setTitle("提示");
				builder.setNegativeButton("确定",
						new android.content.DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.dismiss();
							}
						});
				builder.create().show();
				return;
			}
			PayApi api = new PayApi();
			api.appId = appid;
			api.bargainorId = bargainorId;
			api.nonce = nonceStr;
			api.sig = sig;
			api.sigType = sigType;
			api.timeStamp = timeStamp;
			api.tokenId = tokenId;
			api.serialNumber = serialNumber;
			api.callbackScheme = callbackScheme;
			api.pubAcc = pubAcc;
			api.pubAccHint = pubAccHint;
			if (api.checkParams()) {
				openApi.execApi(api);
			}
		} catch (Exception e) {
			Log.i("", "==catch==" + e.getMessage());
		}
	}

}
