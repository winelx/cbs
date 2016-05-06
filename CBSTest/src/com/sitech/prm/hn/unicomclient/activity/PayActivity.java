package com.sitech.prm.hn.unicomclient.activity;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;

import com.cbstest.unicomclient.R;
import com.sitech.prm.hn.unicomclient.net.AppConfig;
import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.constants.OpenConstants;
import com.tencent.mobileqq.openpay.data.pay.PayApi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

public class PayActivity extends Activity {

	IOpenApi openApi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pay);
		try {
			String str = "";
			String ip = getIp();
			int paySerial = 1;
			Pattern pat = Pattern.compile("[,]");
			String[] pStr = pat.split(str);

			String payFee = pStr[0];
			String acct_type_cd = pStr[1];
			String channel_id = pStr[2];
			String loginNo = pStr[3];
			HttpClient client = new DefaultHttpClient();
			// 链接超时
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
			// 设置读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,
					10000);

			// 请求数据
			String string = AppConfig.BASE_URL;
			HttpPost post = new HttpPost(string
					+ "app/selfservice/s9p11_acctQryAndr.jspa");
			List<NameValuePair> pairs = new ArrayList<NameValuePair>();
			pairs.add(new BasicNameValuePair("payFee", payFee));
			pairs.add(new BasicNameValuePair("sourceFlag", "app"));
			pairs.add(new BasicNameValuePair("payType", "2"));
			pairs.add(new BasicNameValuePair("acct_type_cd", acct_type_cd));
			pairs.add(new BasicNameValuePair("channel_id", channel_id));
			pairs.add(new BasicNameValuePair("loginNo", loginNo));
			pairs.add(new BasicNameValuePair("ipads", ip));
			HttpEntity entity = new UrlEncodedFormEntity(pairs);
			post.setEntity(entity);
			HttpResponse response = client.execute(post);
			int code = response.getStatusLine().getStatusCode();
			if (code == 200) {
				HttpEntity entity1 = response.getEntity();
				InputStream is = entity1.getContent();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] b = new byte[1024];
				int len = -1;
				while ((len = is.read(b)) != -1) {
					bos.write(b, 0, len);
				}
				b = bos.toByteArray();
				String strR = new String(b);
				String[] fStr = new String[7];
				Pattern pattern = Pattern.compile("[&]");
				String[] patStr = pattern.split(strR);
				for (int i = 0; i < patStr.length; i++) {
					fStr[i] = patStr[i].substring(patStr[i].indexOf("=") + 1);
				}
				String appid = fStr[0];
				String bargainorId = fStr[1];
				String nonceStr = fStr[2];
				String sig = fStr[3];
				sig = URLDecoder.decode(sig, "utf-8");

				// 逆向,无卵用
				// byte[] bt = sig.getBytes();
				// byte[] c = new byte[bt.length];
				// for (int i = bt.length - 1; i >= 0; i--) {
				// c[i] = bt[bt.length - i - 1];
				// }
				// sig = new String(c);
				String sigType = fStr[4];
				long timeStamp = Long.parseLong(fStr[5]);
				String tokenId = fStr[6].substring(0, fStr[6].indexOf('"'));

				// 支付序号,用于标识此次支付
				String serialNumber = "" + paySerial++;
				String callbackScheme = "null"; // QQ钱包支付结果回调给urlscheme为callbackScheme的activity.
				String pubAcc = ""; // 手Q公众帐号id.参与支付签名，签名关键字key为pubAcc
				String pubAccHint = ""; // 支付完成页面，展示给用户的提示语：提醒关注公众帐号
				openApi = OpenApiFactory.getInstance(this, appid);

				// 启动QQ支付
				if (!openApi.isMobileQQInstalled()) {
					CustomDialog.Builder builder = new CustomDialog.Builder(
							this);
					builder.setMessage("未安装QQ");
					builder.setTitle("提示");
					builder.setNegativeButton(
							"确定",
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
							this);
					builder.setMessage("不支持手Q支付");
					builder.setTitle("提示");
					builder.setNegativeButton(
							"确定",
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
			} else {
				Log.i("aaaa", "==else==失败");
			}
		} catch (Exception e) {
			Log.i("", "==catch==" + e.getMessage());
		}
	}

	// 获取本机IP
	private String getIp() {
		String ip = "";
		WifiManager wifiManager = (WifiManager) this
				.getSystemService(Context.WIFI_SERVICE);
		if (wifiManager.isWifiEnabled()) {
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int i = wifiInfo.getIpAddress();
			ip = (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "."
					+ ((i >> 16) & 0xFF) + "." + (i >> 24 & 0xFF);
			return ip;
		} else {
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()) {
							ip = inetAddress.getHostAddress().toString();
						}
					}
				}
			} catch (SocketException ex) {
				Log.e("====fk...", ex.toString());
			}
			return ip;
		}
	}
}
