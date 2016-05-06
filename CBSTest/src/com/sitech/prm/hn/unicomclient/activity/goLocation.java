package com.sitech.prm.hn.unicomclient.activity;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.LocationClientOption.LocationMode;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.model.LatLng;
import com.cbstest.unicomclient.R;
import com.sitech.prm.hn.unicomclient.net.AppConfig;

public class goLocation extends Activity {
	public Button button;
	public ImageButton imgBtBack;
	public LocationClient mLocationClient = null;
	public MapView mMapView = null;
	public BaiduMap mBaiduMap = null;
	JW w = new JW();
	public String channel_id;
	public String login_no;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		SDKInitializer.initialize(getApplicationContext());
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_go_location);
		MyApplication.getInstance().addActivity(this);
		channel_id = getIntent().getExtras().getString("channel_id");
		login_no = getIntent().getExtras().getString("login_no");
		button = (Button) findViewById(R.id.bt_location_save);
		imgBtBack = (ImageButton) findViewById(R.id.img_back);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mBaiduMap = mMapView.getMap();
		mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
		mLocationClient = new LocationClient(getApplicationContext());
		initLocation();
		mLocationClient.registerLocationListener(licationListener);
		mLocationClient.start();
		button.setOnClickListener(btClickListener);
		imgBtBack.setOnClickListener(backClickListener);
	}

	// 保存 json请求
	private OnClickListener btClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			try {
				String lng = String.valueOf(w.lng);
				String lat = String.valueOf(w.lat);
				String q = AppConfig.BASE_URL;
				String url = q
						+ "/app/channelManage/updateLatitudeLongitude.jspa";
				// String url =
				// "http://cbstest.170.com/app/selfservice/s9p11_acctQry.jspa";
				HttpClient client = new DefaultHttpClient();
				// 链接超时
				client.getParams().setParameter(
						CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
				// 设置读取超时
				client.getParams().setParameter(
						CoreConnectionPNames.SO_TIMEOUT, 10000);
				HttpPost post = new HttpPost(url);
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("longitude", lng));
				pairs.add(new BasicNameValuePair("latitude", lat));
				pairs.add(new BasicNameValuePair("channel_id", channel_id));
				pairs.add(new BasicNameValuePair("login_no", login_no));
				HttpEntity entity = new UrlEncodedFormEntity(pairs);
				post.setEntity(entity);
				HttpResponse response = client.execute(post);
				int code = response.getStatusLine().getStatusCode();
				if (code == 200) {
					String strResult = EntityUtils.toString(response
							.getEntity());
					JSONObject object = new JSONObject(strResult);
					int retCode = object.getInt("retCode");
					if (retCode == 000000) {
						CustomDialog.Builder builder = new CustomDialog.Builder(
								goLocation.this);
						builder.setMessage("修改成功");
						builder.setTitle("提示");
						builder.setNegativeButton(
								"确定",
								new android.content.DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
										Intent intent = new Intent();
										setResult(16, intent);
										finish();
									}
								});
						builder.create().show();
					} else {
						CustomDialog.Builder builder = new CustomDialog.Builder(
								goLocation.this);
						builder.setMessage("修改失败");
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
					}
				} else {
					Toast.makeText(goLocation.this, "更新失败,请检查您的网络或反馈给我们",
							Toast.LENGTH_LONG).show();
					;
				}
			} catch (Exception e) {
				Log.i("", "------" + e.getMessage());
			}
		}
	};

	// 初始化option
	private void initLocation() {
		LocationClientOption option = new LocationClientOption();
		option.setLocationMode(LocationMode.Hight_Accuracy);
		option.setCoorType("bd09ll");
		option.setOpenGps(true); // 可选，默认false,设置是否使用gps
		mLocationClient.setLocOption(option);
	}

	private BDLocationListener licationListener = new BDLocationListener() {

		@Override
		public void onReceiveLocation(BDLocation location) {
			w.lng = location.getLongitude();
			w.lat = location.getLatitude();
			handler.sendMessage(handler.obtainMessage(1, w));
		}
	};

	// handler
	private Handler handler = new Handler(new Handler.Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				w = (JW) msg.obj;
				break;
			case 2:
				w = (JW) msg.obj;
				break;
			}
			show();
			return false;
		}
	});

	// 添加marker
	public void show() {

		mBaiduMap.clear();
		LatLng point = new LatLng(w.lat, w.lng);
		mBaiduMap.setMapStatus(MapStatusUpdateFactory
				.newMapStatus(new MapStatus.Builder().target(point).zoom(18)
						.build()));
		BitmapDescriptor bitmap = BitmapDescriptorFactory
				.fromResource(R.drawable.pt_icon);
		MarkerOptions options = new MarkerOptions().position(point)
				.icon(bitmap).zIndex(9).draggable(true);
		mBaiduMap.addOverlay(options);
		mBaiduMap.setOnMarkerDragListener(markerDragListener);
	}

	// 调用BaiduMap对象的setOnMarkerDragListener方法设置marker拖拽的监听
	OnMarkerDragListener markerDragListener = new OnMarkerDragListener() {

		public void onMarkerDrag(Marker marker) {
			// 拖拽中
		}

		public void onMarkerDragEnd(Marker marker) {
			// 拖拽结束
			w.lng = marker.getPosition().longitude;
			w.lat = marker.getPosition().latitude;
			handler.sendMessage(handler.obtainMessage(2, w));
		}

		public void onMarkerDragStart(Marker marker) {
			// 开始拖拽
		}

	};

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
		// mMapView.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// 在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
		mMapView.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
		mMapView.onPause();
	}

	// 存经纬度
	class JW {
		public double lng; // 经度
		public double lat; // 纬度
	}

	// 返回点击事件
	public OnClickListener backClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			finish();
		}
	};

	// @SuppressWarnings("deprecation")
	// public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// // 创建退出对话框
	// AlertDialog isExit = new AlertDialog.Builder(this).create();
	// // 设置对话框标题
	// isExit.setTitle("系统提示");
	// // 设置对话框消息
	// isExit.setMessage("确定要退出吗");
	// // 添加选择按钮并注册监听
	// isExit.setButton("确定", listener);
	// isExit.setButton2("取消", listener);
	// // 显示对话框
	// isExit.show();
	//
	// }
	//
	// return false;
	// };

	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:// "确认"按钮退出程序
				MyApplication.getInstance().exit();
				break;
			case AlertDialog.BUTTON_NEGATIVE:// "取消"第二个按钮取消对话框
				break;
			default:
				break;
			}
		}
	};

}
