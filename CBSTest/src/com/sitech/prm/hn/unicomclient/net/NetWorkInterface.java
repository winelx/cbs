package com.sitech.prm.hn.unicomclient.net;


import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sitech.prm.hn.unicomclient.bean.NetInterfaceStatusDataStruct;

public class NetWorkInterface {
	private Context mContext;
	private ConnectivityManager connManager;
	private static NetInterfaceStatusDataStruct niStatusData;
	public static HttpClient client;
	private int REQUEST_COUNT = 0;

	public NetWorkInterface(Context mContext) {
		this.mContext = mContext;
		niStatusData = new NetInterfaceStatusDataStruct(
				NetInterfaceStatusDataStruct.STATUS_SUCCESS, "");
	}

	public boolean checkMobileNetStatus() {
		boolean success = false;
		if (connManager == null)
			connManager = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = connManager.getNetworkInfo(
				ConnectivityManager.TYPE_MOBILE).getState();
		if (State.CONNECTED == state) {
			success = true;
		}
		return success;
	}

	public boolean checkWifiNetStatus() {
		boolean success = false;
		if (connManager == null)
			connManager = (ConnectivityManager) mContext
					.getSystemService(Context.CONNECTIVITY_SERVICE);
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
				.getState();
		if (State.CONNECTED == state) {
			success = true;
		}
		return success;
	}

	public NetInterfaceStatusDataStruct CheckNewWork() {
		if (!checkMobileNetStatus() && !checkWifiNetStatus()) {
			niStatusData.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			niStatusData.setMessage("您的移动网络或WIFI网络状态不佳，请设置网络，来允许应用为您提供服务");
		}
		return niStatusData;
	}

	// 发生网络错误，返回null ;
	// 产品接口问题，返回空值；
	// 正常，返回json格式的数据及状态；
	public static synchronized NetInterfaceStatusDataStruct postHttp(String strUrl,
			LinkedHashMap<String, String> requestParams, Handler handler,int msgWhat) {
		NetInterfaceStatusDataStruct result = new NetInterfaceStatusDataStruct(
				NetInterfaceStatusDataStruct.STATUS_SUCCESS, "");
		// REQUEST_COUNT = 0;
		// while(REQUEST_COUNT < 3){
		HttpPost httpRequest = new HttpPost(strUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (requestParams != null) {
			for (Map.Entry<String, String> entry : requestParams.entrySet()){
				params.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
				//Log.i("cdp","paramName:"+entry.getKey()+"=>"+entry.getValue());
			}
		}
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			DefaultHttpClient dfc = new DefaultHttpClient();

			dfc.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 40000);
			dfc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 40000);

			HttpResponse httpResponse = dfc.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result.setStatus(NetInterfaceStatusDataStruct.STATUS_SUCCESS);
				result.setMessage(EntityUtils.toString(httpResponse.getEntity()));
			}else if(httpResponse.getStatusLine().getStatusCode() == 500){
				result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
				result.setMessage(httpResponse.getStatusLine().getReasonPhrase());
			}else{
				
			}		

			// REQUEST_COUNT = 3;

		} catch (SocketTimeoutException e) {
			// if(REQUEST_COUNT >= 2){
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
			// }
			// REQUEST_COUNT ++;
		} catch (ConnectTimeoutException e) {
			// if(REQUEST_COUNT >= 2){
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
			// }
			// REQUEST_COUNT ++;
		} catch (Exception e) {
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
			// REQUEST_COUNT = 3;
		} finally {
			httpRequest = null;
			// if (handler != null && REQUEST_COUNT >= 3) {
			Message msg = new Message();
			msg.what = msgWhat;
			msg.obj = result;
			if (handler != null) {

				handler.sendMessage(msg);
			}

			// }
		}
		// }
		return result;
	}

	/**
	 * 获得网络连接是否可用
	 * 
	 * @param context
	 * @return
	 */
	public boolean hasNetwork(Context context) {
		ConnectivityManager con = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workinfo = con.getActiveNetworkInfo();
		if (workinfo == null || !workinfo.isAvailable()) {
			return false;
		}
		return true;
	}

	public synchronized NetInterfaceStatusDataStruct postHttp(String strUrl,
			LinkedHashMap<String, String> requestParams) {
		NetInterfaceStatusDataStruct result;
		result = niStatusData;
		
		HttpPost httpRequest = new HttpPost(strUrl);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		if (requestParams != null) {
			for (Map.Entry<String, String> entry : requestParams.entrySet())
				params.add(new BasicNameValuePair(entry.getKey(), entry
						.getValue()));
		}
		try {
			httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			DefaultHttpClient dfc = new DefaultHttpClient();

			dfc.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 15000);
			dfc.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 15000);

			HttpResponse httpResponse = dfc.execute(httpRequest);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result.setStatus(NetInterfaceStatusDataStruct.STATUS_SUCCESS);
				result.setMessage(EntityUtils.toString(httpResponse.getEntity()));
				
				//Log.i("dx",EntityUtils.toString(httpResponse.getEntity()));

			}

			// REQUEST_COUNT = 3;

		} catch (SocketTimeoutException e) {
			// if(REQUEST_COUNT >= 2){
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
			// }
			// REQUEST_COUNT ++;
		} catch (ConnectTimeoutException e) {
			// if(REQUEST_COUNT >= 2){
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
			// }
			// REQUEST_COUNT ++;
		} catch (Exception e) {
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
			// REQUEST_COUNT = 3;
		} finally {
			httpRequest = null;
		}
		// }
		return result;
	}

	//以get方式请求网络
	public synchronized NetInterfaceStatusDataStruct getHttp(String strUrl,
			LinkedHashMap<String, String> requestParams, Handler handler,int msgWhat) {
		NetInterfaceStatusDataStruct result;
		result = niStatusData;	
		if (requestParams != null) {
			int count =0;
			for (Map.Entry<String, String> entry : requestParams.entrySet()){
				if(count == 0){
					strUrl += "?"+entry.getKey()+"="+entry.getValue();
				}else{
					strUrl += "&"+entry.getKey()+"="+entry.getValue();
				}	
				count++;
			}
			Log.i("cdp","get url=>"+strUrl);
		}
		
		try {
			HttpGet httpGet = new HttpGet(strUrl);
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				result.setStatus(NetInterfaceStatusDataStruct.STATUS_SUCCESS);
				result.setMessage(EntityUtils.toString(httpResponse.getEntity()));

			}

		} catch (SocketTimeoutException e) {
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
		} catch (ConnectTimeoutException e) {
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
		} catch (Exception e) {
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
			e.printStackTrace();
		} finally {
			Message msg = new Message();
			msg.what = msgWhat;
			msg.obj = result;
			if (handler != null) {

				handler.sendMessage(msg);
			}
		}
		return result;
	}
}
