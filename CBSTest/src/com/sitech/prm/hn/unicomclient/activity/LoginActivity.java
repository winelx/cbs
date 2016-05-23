package com.sitech.prm.hn.unicomclient.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnDismissListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.cbstest.unicomclient.R;
import com.sitech.prm.hn.unicomclient.js.JavaScriptinterface;
import com.sitech.prm.hn.unicomclient.net.AppConfig;
import com.sitech.prm.hn.unicomclient.service.HttpServive;
import com.sitech.prm.hn.unicomclient.service.UpdateService;
import com.sitech.prm.hn.unicomclient.service.UploadPicListener;

@SuppressLint({ "SetJavaScriptEnabled", "HandlerLeak" })
public class LoginActivity extends BaseActivity {
	@SuppressWarnings("unused")
	private String longitude, latitude = null;
	private SharedPreferences spf;
	@SuppressWarnings("unused")
	private double jingdu, weidu;
	@SuppressWarnings("unused")
	private LocationMode mCurrentMode;// 定位模式
	BitmapDescriptor mCurrentMarker;// Marker图标
	public MyLocationListenner myListener = new MyLocationListenner();
	BaiduMap mBaiduMap;
	private MapView mapView;
	LocationClient mLocClient;
	private FileStorage filestorage;
	private String FileName = "Storage.txt";
	JSONObject map;
	private WebView myWebView;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
		}
	};

	@SuppressWarnings("static-access")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		application.activityList.add(this);
		MyApplication.getInstance().addActivity(this);

		mLocClient = new LocationClient(this);
		init();
	}

	private void baiduMap() {

		mCurrentMode = LocationMode.NORMAL;// 设置定位模式为普通
		mCurrentMarker = BitmapDescriptorFactory// 构建mark图标
				.fromResource(R.drawable.icon_marka);
		// 地图初始化
		mapView = (MapView) findViewById(R.id.my_location_bmapView);
		mapView.setVisibility(View.GONE);
		mBaiduMap = mapView.getMap();
		// 定位初始化
		mLocClient = new LocationClient(this);
		mLocClient.registerLocationListener(myListener);// 注册监听函数：

		LocationClientOption option = new LocationClientOption();
		int span = 3600;
		option.setScanSpan(span);//
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		// option.setScanSpan(t);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocClient.setLocOption(option);
		mLocClient.start();

	}

	/**
	 * 定位SDK监听函数
	 */
	public class MyLocationListenner implements BDLocationListener {

		@Override
		public void onReceiveLocation(BDLocation location) {
			// map view 销毁后不在处理新接收的位置
			if (location == null || mapView == null)
				return;
			MyLocationData locData = new MyLocationData.Builder()
					.accuracy(location.getRadius())
					// 此处设置开发者获取到的方向信息，顺时针0-360
					.direction(100).latitude(location.getLatitude())
					.longitude(location.getLongitude()).build();
			mBaiduMap.setMyLocationData(locData);
			// 经度
			latitude = location.getLatitude() + "";
			// 纬度
			longitude = location.getLongitude() + "";
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@SuppressLint("JavascriptInterface")
	private void init() {
		myWebView = (WebView) findViewById(R.id.myWebView);

		application.webView = myWebView;

		myWebView.getSettings().setJavaScriptEnabled(true);
		myWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		myWebView.getSettings().setDomStorageEnabled(true);
		myWebView.getSettings().setDatabaseEnabled(true);
		myWebView.requestFocus();
		myWebView.setWebViewClient(new MyWebViewClient());
		myWebView.setDownloadListener(new MyWebViewDownLoadListener());

		String dir = myWebView.getContext()
				.getDir("database", Context.MODE_PRIVATE).getPath();
		myWebView.getSettings().setDatabasePath(dir);
		// 与js交互，JavaScriptinterface 是个接口，与js交互时用到的，这个接口实现了从网页跳到ape中的activity 的
		// 方法，特别重要
		myWebView.addJavascriptInterface(new JavaScriptinterface(this,
				application), "JSEngine");
		String ts = new SimpleDateFormat("yyMMddHHmmss").format(new Date());
		// 测试环境 http://testapp.170.com 正式环境 http://apph5.170.com

		SharedPreferences preferences = this.getSharedPreferences(
				"SHARE_APP_TAG", 0);
		Boolean isFirst = preferences.getBoolean("FIRSTStart", true);
		baiduMap();// 百度定位
		// myWebView
		// .loadUrl("http://testapp.170.com/pub-page/login1.html?ts="
		// + ts);
		//
		if (isFirst) {// 第一次
			preferences.edit().putBoolean("FIRSTStart", false).commit();
			myWebView
					.loadUrl("http://cbstest.170.com/b2b-page/FlashScreen.html?ts="
							+ ts);
			Log.i("", "--------------第一次登陆");
		} else {

			myWebView.loadUrl("http://cbstest.170.com/b2b-page/login.html?ts="
					+ ts);
			Log.i("", "--------------第好几次登陆");

		}

		/*
		 * myWebView .loadUrl("http://cbsapp.170.com/pub-page/native.html?ts=" +
		 * ts); Log.i("", "--------------第一次登陆");
		 */

		myWebView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}
		});

		myWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					JsResult result) {
				// Toast.makeText(getApplicationContext(), message,
				// Toast.LENGTH_SHORT).show();
				result.confirm();
				return true;
			}
		});
	}

	public SharedPreferences getSharedPreferences(String name, Double latitude2) {
		return null;
	}

	@SuppressWarnings({ "unused", "unchecked", "rawtypes" })
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 1:
			if (data != null) {
				String param = data.getStringExtra("param");
				String[] arr = param.split("###");
				String certNumber = arr[0];
				String flag = arr[1];
				String idCardscan = arr[2];
				String login_no = arr[3];
				final Map map = new HashMap<String, String>();
				map.put("certNumber", certNumber);
				map.put("flag", flag);
				if ("1".equals(flag)) {
					map.put("uploadName", "idCardFront");
				} else {
					map.put("uploadName", "idCardReverse");
				}
				map.put("idCardscan", idCardscan);
				map.put("login_no", login_no);
				final String path = AppConfig.TY_ACT_UPLOAD;
				byte[] _pic = (byte[]) data.getSerializableExtra("picture");
				Bitmap bitmap = BitmapFactory.decodeByteArray(_pic, 0,
						_pic.length);
				final File file = savebitmap(bitmap);
				new Thread() {
					@Override
					public void run() {
						HttpServive.uploadPicFile(path, map, 200, file,
								new UploadPicListener() {
									@Override
									public void onUploadCallback(
											final String info) {
										if (info != null) {
											handler.post(new Runnable() {
												@Override
												public void run() {
													application.webView
															.loadUrl("javascript:"
																	+ application.success
																	+ "("
																	+ info
																	+ ");");
												}
											});
										} else {
											handler.post(new Runnable() {
												@Override
												public void run() {
													application.webView
															.loadUrl("javascript:"
																	+ application.fail
																	+ "();");
												}
											});

										}
									}
								});
					}
				}.start();
			} else {
				handler.post(new Runnable() {
					@Override
					public void run() {
						application.webView.loadUrl("javascript:"
								+ application.fail + "();");
					}
				});
			}
			break;
		case 2:
			if (data != null) {
				application.webView.loadUrl("javascript:" + application.success
						+ "('" + AppConfig.LOGIN_HTML_MENU4 + "');");
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 3:
			if (data != null) {
				String url = data.getStringExtra("url");
				String flag = data.getStringExtra("flag");
				if ("back".equals(flag)) {
					application.webView.loadUrl(url);
				} else {
					application.webView.loadUrl("javascript:"
							+ application.success + "('" + url + "');");
				}
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 4:
			if (data != null) {
				String url = data.getStringExtra("url");
				String flag = data.getStringExtra("flag");
				if ("back".equals(flag)) {
					application.webView.loadUrl(url);
				} else {
					application.webView.loadUrl("javascript:"
							+ application.success + "('" + url + "');");
				}
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 11:
			if (data != null) {
				System.out.println("end========================="
						+ System.currentTimeMillis());
				String datas = data.getStringExtra("data");

				application.webView.loadUrl("javascript:" + application.success
						+ "('" + datas + "');");
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 31:
			if (data != null) {
				System.out.println("end========================="
						+ System.currentTimeMillis());
				String datas = data.getStringExtra("data");
				application.webView.loadUrl("javascript:" + application.success
						+ "('" + datas + "');");
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 112:
			if (data != null) {
				System.out.println("end========================="
						+ System.currentTimeMillis());
				String datas = data.getStringExtra("data");
				application.webView.loadUrl("javascript:" + application.success
						+ "('" + datas + "');");
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 15:
			if (data != null) {
				String datas = data.getStringExtra("code");
				application.webView.loadUrl("javascript:" + application.success
						+ "('" + datas + "');");
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 12:
			if (data != null) {
				String code = data.getStringExtra("code");
				application.webView.loadUrl("javascript:" + application.success
						+ "('" + code + "');");
			} else {
				application.webView.loadUrl("javascript:" + application.fail
						+ "();");
			}
			break;
		case 13:

			final String url = data.getStringExtra("url");
			if (data != null) {
				File a = UpdateService.startDownload(url); // 下载
				if (a != null) {
					installApk(a);
					handler.post(new Runnable() {
						@Override
						public void run() {
							application.webView.loadUrl("javascript:"
									+ application.success + "(success);");
						}
					});
				} else {
					handler.post(new Runnable() {
						@Override
						public void run() {
							application.webView.loadUrl("javascript:"
									+ application.fail + "();");
						}
					});
				}
			} else {
				handler.post(new Runnable() {
					@Override
					public void run() {
						application.webView.loadUrl("javascript:"
								+ application.fail + "();");
					}
				});
			}
			break;
		case 16:
			application.webView.loadUrl("javascript:" + application.success
					+ "();");
			break;
		default:
			break;
		}
	}

	public Bitmap Bytes2Bimap(byte[] b) {
		if (b.length != 0) {
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

	public File savebitmap(Bitmap bitmap) {
		String pic = "picture.jpg";
		File f = new File(absolutionPath(), pic);
		if (f.exists()) {
			f.delete();
		}
		FileOutputStream out;
		try {
			out = new FileOutputStream(f);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return f;
	}

	public static String absolutionPath() {
		File parent = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			parent = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/baseApplication/download");
		} else {
			parent = new File(Environment.getDataDirectory().getAbsolutePath()
					+ "/baseApplication/download");
		}
		if (!parent.exists()) {
			parent.mkdirs();
		}

		String saveDir = parent.getAbsolutePath();
		return saveDir;
	}

	/**
	 * 安装APK
	 */
	private void installApk(File file) {
		Intent intent = new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		startActivity(intent);
	}

	// 内部类
	public class MyWebViewClient extends WebViewClient {
		// 如果页面中链接，如果希望点击链接继续在当前browser中响应，
		// 而不是新开Android的系统browser中响应该链接，必须覆盖 webview的WebViewClient对象。
		public boolean shouldOverviewUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// showProgress();
		}

		public void onPageFinished(WebView view, String url) {
			// super.onPageFinished(view, url);
		}

		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			// closeProgress();
		}
	}

	// 如果不做任何处理，浏览网页，点击系统“Back”键，整个Browser会调用finish()而结束自身，
	// 如果希望浏览的网 页回退而不是推出浏览器，需要在当前Activity中处理并消费掉该Back事件。

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		String urlString = myWebView.getUrl();
		String[] url = urlString.split("[?]");
		if (url[0].equals("http://cbsapp.170.com/b2b-page/shopFront.html")
				|| url[0]
						.equals("http://cbsapp.170.com/b2b-page/workTable.html")
				|| url[0]
						.equals("http://cbsapp.170.com/b2b-page/channelManage.html")
				|| url[0]
						.equals("http://cbsapp.170.com/b28/b-page/channelStatic.html")
				|| url[0].equals("http://cbsapp.170.com/b2b-page/myMenu.html")
				|| url[0]
						.equals("http://cbsapp.170.com/b2b-page/businessManage.html")
				|| url[0].equals("http://cbsapp.170.com/b2b-page/login.html")) {
			new AlertDialog.Builder(this)
					.setTitle("确认退出吗？")
					.setIcon(android.R.drawable.ic_dialog_info)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// 点击“确认”后的操作
									LoginActivity.this.finish();
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).show();
			return true;
		} else {
			if ((keyCode == KeyEvent.KEYCODE_BACK) && myWebView.canGoBack()) {
				myWebView.goBack();
				return true;
			}
		}
		return false;
	}

	// 内部类
	private class MyWebViewDownLoadListener implements DownloadListener {

		@Override
		public void onDownloadStart(String url, String userAgent,
				String contentDisposition, String mimetype, long contentLength) {
			if (!Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				Toast t = Toast.makeText(LoginActivity.this, "需要SD卡。",
						Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				return;
			}
			DownloaderTask task = new DownloaderTask();
			task.execute(url);
		}

	}

	int FileLength = 0;

	// 内部类
	private class DownloaderTask extends AsyncTask<String, Void, String> {

		public DownloaderTask() {
		}

		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			try {
				URLConnection connection;
				URL u = new URL(url);
				connection = u.openConnection();
				FileLength = connection.getContentLength();

			} catch (MalformedURLException e1) {
				e1.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			Log.i("tag", "url=" + url);
			String fileName = url.substring(url.lastIndexOf("/") + 1);
			fileName = URLDecoder.decode(fileName);
			Log.i("tag", "fileName=" + fileName);

			File directory = Environment.getExternalStorageDirectory();
			File file = new File(directory, fileName);
			if (file.exists()) {
				Log.i("tag", "The file has already exists.");
				file.delete();
				// return fileName;
			}
			try {
				HttpClient client = new DefaultHttpClient();
				// client.getParams().setIntParameter("http.socket.timeout",3000);//设置超时
				HttpGet get = new HttpGet(url);
				HttpResponse response = client.execute(get);
				if (HttpStatus.SC_OK == response.getStatusLine()
						.getStatusCode()) {
					HttpEntity entity = response.getEntity();
					InputStream input = entity.getContent();

					writeToSDCard(fileName, input);

					input.close();
					// entity.consumeContent();
					return fileName;
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}

		@Override
		protected void onCancelled() {
			super.onCancelled();
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			closeProgressDialog();
			if (result == null) {
				Toast t = Toast.makeText(LoginActivity.this, "连接错误！请稍后再试！",
						Toast.LENGTH_LONG);
				t.setGravity(Gravity.CENTER, 0, 0);
				t.show();
				return;
			}
			File directory = Environment.getExternalStorageDirectory();
			File file = new File(directory, result);
			Log.i("tag", "Path=" + file.getAbsolutePath());
			Intent intent = getFileIntent(file);
			startActivity(intent);

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showProgressDialog();
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
		}

	}

	private ProgressDialog mDialog;

	private void showProgressDialog() {

		if (mDialog == null) {
			mDialog = new ProgressDialog(LoginActivity.this);
			mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			mDialog.setMessage("正在下载 ，请等待...");
			mDialog.setMax(100);
			mDialog.setCancelable(false);// 设置进度条是否可以按退回键取消
			mDialog.setCanceledOnTouchOutside(false);
			mDialog.setOnDismissListener(new OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					mDialog = null;
				}
			});
			mDialog.show();

		}
	}

	private void closeProgressDialog() {
		if (mDialog != null) {
			mDialog.dismiss();
			mDialog = null;
		}
	}

	public Intent getFileIntent(File file) {
		// Uri uri = Uri.parse("http://m.ql18.com.cn/hpf10/1.pdf");
		Uri uri = Uri.fromFile(file);
		String type = getMIMEType(file);
		Log.i("tag", "type=" + type);
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(uri, type);
		return intent;
	}

	private Handler jdHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				mDialog.setProgress(msg.arg1);
				break;
			default:
				break;
			}
		}

	};

	public void writeToSDCard(String fileName, InputStream input) {

		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File directory = Environment.getExternalStorageDirectory();
			File file = new File(directory, fileName);
			// if(file.exists()){
			// Log.i("tag", "The file has already exists.");
			// return;
			// }
			long arg = 0;

			try {
				FileOutputStream fos = new FileOutputStream(file);
				byte[] b = new byte[2048];
				int j = 0;
				while ((j = input.read(b)) != -1) {
					fos.write(b, 0, j);
					Message message = jdHandler.obtainMessage();
					message.what = 1;
					arg += j;
					message.arg1 = (int) ((float) arg / FileLength * 100);
					jdHandler.sendMessage(message);
				}
				fos.flush();
				fos.close();
			} catch (FileNotFoundException e) {
				Log.i("", "==========" + e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.i("tag", "NO SDCard.");
		}
	}

	private String getMIMEType(File f) {
		String type = "";
		String fName = f.getName();
		/* 取得扩展名 */
		String end = fName
				.substring(fName.lastIndexOf(".") + 1, fName.length())
				.toLowerCase();

		/* 依扩展名的类型决定MimeType */
		if (end.equals("pdf")) {
			type = "application/pdf";//
		} else if (end.equals("m4a") || end.equals("mp3") || end.equals("mid")
				|| end.equals("xmf") || end.equals("ogg") || end.equals("wav")) {
			type = "audio/*";
		} else if (end.equals("3gp") || end.equals("mp4")) {
			type = "video/*";
		} else if (end.equals("jpg") || end.equals("gif") || end.equals("png")
				|| end.equals("jpeg") || end.equals("bmp")) {
			type = "image/*";
		} else if (end.equals("apk")) {
			/* android.permission.INSTALL_PACKAGES */
			type = "application/vnd.android.package-archive";
		}
		// else if(end.equals("pptx")||end.equals("ppt")){
		// type = "application/vnd.ms-powerpoint";
		// }else if(end.equals("docx")||end.equals("doc")){
		// type = "application/vnd.ms-word";
		// }else if(end.equals("xlsx")||end.equals("xls")){
		// type = "application/vnd.ms-excel";
		// }
		else {
			// /*如果无法直接打开，就跳出软件列表给用户选择 */
			type = "*/*";
		}
		return type;
	}

}
