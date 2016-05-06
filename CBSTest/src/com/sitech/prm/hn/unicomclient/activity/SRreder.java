package com.sitech.prm.hn.unicomclient.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;

import sunrise.bluetooth.SRBluetoothCardReader;
import sunrise.otg.SRotgCardReader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.model.LatLng;
import com.cbstest.unicomclient.R;
import com.sunrise.icardreader.helper.ConsantHelper;
import com.sunrise.icardreader.model.IdentityCardZ;

public class SRreder extends Activity {
	LocationClient mLocClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	private LocationMode mCurrentMode;// 定位模式
	BitmapDescriptor mCurrentMarker;// Marker图标
	private MapView mapView;
	BaiduMap mBaiduMap;
	private String latitude, longitude = "none";
	private double jingdu, weidu;
	boolean isFirstLoc = true;// 是否首次定位
	private TextView readername, tv_info;
	// 功能

	public static Handler uiHandler;
	private SRBluetoothCardReader BluetoothReader;
	private SRotgCardReader OTGReader;
	private BluetoothAdapter mBluetoothAdapter = null; // /蓝牙适配器
	private Button buttonNFC, buttonOTG, buttonBT, bt_select;
	private String Blueaddress = null;
	private String server_address = "";
	private String server_selected = "";
	private int server_port = 0;
	public IdentityCardZ mIdentityCardZ;
	private static final String TAG = "cbs";
	private Intent mNfcIntent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_loading);
		readername = (TextView) findViewById(R.id.title);
		readername.setText("森锐身份识别");
		uiHandler = new MyHandler(this);
		buttonNFC = (Button) findViewById(R.id.addnfc_button);
		buttonNFC.setVisibility(View.GONE);
		tv_info = (TextView) findViewById(R.id.tv_info);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		baiduMap();// 百度定位
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		// 初始化阅读器
		OTGReader = new SRotgCardReader(uiHandler, this);
		BluetoothReader = new SRBluetoothCardReader(uiHandler, this);
		initViews();
		Blueaddress = null; // SR5201571000242
		if (server_selected.equals("")) {
			server_address = "210.14.131.172";
			server_port = 6000;

			if (server_address.equals("")) {
				server_address = "210.14.131.172";
				server_port = 6000;
			}
		}

		initShareReference();
	}

	/**
	 * 获取经纬度
	 */
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
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(1000);// 设置发起定位请求的间隔时间为5000ms
		option.setIsNeedAddress(true);// 返回的定位结果包含地址信息
		option.setNeedDeviceDirect(true);// 返回的定位结果包含手机机头的方向
		mLocClient.setLocOption(option);
		mLocClient.start();

	}

	private void overlay(LatLng point, BitmapDescriptor bitmap,
			BaiduMap baiduMap) {
		// 构建MarkerOption，用于在地图上添加Marker
		OverlayOptions option = new MarkerOptions().position(point)
				.icon(bitmap);
		// 在地图上添加Marker，并显示
		baiduMap.addOverlay(option);
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
			jingdu = location.getLatitude();
			latitude = jingdu + ",";
			latitude.split(",");
			// 纬度
			weidu = location.getLongitude();
			longitude = weidu + ",";
			longitude.split(",");
			LatLng ll = new LatLng(jingdu, weidu);
			// 设置缩放比例,更新地图状态
			float f = mBaiduMap.getMaxZoomLevel();// 19.0
			MapStatusUpdate u = MapStatusUpdateFactory.newLatLngZoom(ll, f - 2);
			mBaiduMap.animateMapStatus(u);
			overlay(ll, mCurrentMarker, mBaiduMap);
		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	private static final int REQUEST_CONNECT_DEVICE = 1;

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e("MAIN", "onActivityResult: requestCode=" + requestCode
				+ ", resultCode=" + resultCode);
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {

				Blueaddress = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				if (!Blueaddress
						.matches("([0-9a-fA-F][0-9a-fA-F]:){5}([0-9a-fA-F][0-9a-fA-F])")) {
					tv_info.setText("address:" + Blueaddress
							+ " is wrong, length = " + Blueaddress.length());
					return;
				}
				try {
					BluetoothDevice device = BluetoothAdapter
							.getDefaultAdapter().getRemoteDevice(Blueaddress);
					if (device.getBondState() == BluetoothDevice.BOND_NONE) {
						Method createBondMethod = device.getClass().getMethod(
								"createBond");
						createBondMethod.invoke(device);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
			break;
		case 2:
			if (resultCode == 100) {

				this.server_address = data.getExtras().getString("address");
				this.server_port = data.getExtras().getInt("port");

				Log.e("MAIN", "onActivityResult: " + server_address);
				Log.e("MAIN", "onActivityResult: " + server_port);

				initShareReference();
			}
			break;
		}
	}

	private void initShareReference() {
		// 设置解密服务器

		OTGReader.setTheServer(this.server_address, this.server_port);

		BluetoothReader.setTheServer(this.server_address, this.server_port);
	}

	@SuppressWarnings("deprecation")
	private void initViews() {
		tv_info = (TextView) findViewById(R.id.tv_info);
		buttonOTG = (Button) findViewById(R.id.addotg_button);
		buttonNFC = (Button) findViewById(R.id.addnfc_button);
		buttonBT = (Button) findViewById(R.id.add_button);
		bt_select = (Button) findViewById(R.id.bt_select);
		bt_select.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent serverIntent = new Intent(SRreder.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			}
		});
		buttonOTG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				readCardOTG();
			}
		});

		buttonBT.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Blueaddress == null) {
					Intent serverIntent = new Intent(SRreder.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
					return;
				}
				readCardBlueTooth();
			}
		});
	}

	Object nfcLock = new Object();

	/**
	 * OTG读取
	 */
	protected void readCardOTG() {
		boolean bRet = OTGReader.registerOTGCard();
		if (bRet == true) {
			buttonOTG.setEnabled(false);
			buttonNFC.setEnabled(false);
			buttonBT.setEnabled(false);
			new Thread() {
				public void run() {
					OTGReader.readCard();
				}
			}.start();

		} else {
			Toast.makeText(this, "请确认USB设备已经连接并且已授权，再读卡!", Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * OTG 同步读取方式读卡
	 */
	protected void readCardSyncOTG() {

		boolean bRet = OTGReader.registerOTGCard();
		if (bRet == true) {
			buttonOTG.setEnabled(false);
			buttonBT.setEnabled(false);
			new AsyncTask<Object, Object, IdentityCardZ>() {
				protected IdentityCardZ doInBackground(Object... arg0) {
					IdentityCardZ cardZ = OTGReader.readCardSync();
					return cardZ;
				}

				protected void onPostExecute(IdentityCardZ result) {
					if (result.resCode == 0) {
						readCardSuccess(result);
					} else {
						uiHandler.obtainMessage(ConsantHelper.READ_CARD_FAILED,
								result.resCode, 0, "读出失败");
					}
					buttonOTG.setEnabled(true);
					buttonNFC.setEnabled(true);
					buttonBT.setEnabled(true);
				}
			}.execute();
		} else {
			Toast.makeText(this, "请确认USB设备已经连接并且已授权，再读卡!", Toast.LENGTH_LONG)
					.show();
		}
	}

	/**
	 * 同步读卡 同步读卡 返回null即读取失败
	 */
	protected void readCardSyncBluetooth() {

		if (Blueaddress == null) {
			Toast.makeText(this, "请选择蓝牙设备，再读卡!", Toast.LENGTH_LONG).show();
			return;
		}

		if (Blueaddress.length() <= 0) {
			Toast.makeText(this, "请选择蓝牙设备，再读卡!", Toast.LENGTH_LONG).show();
			return;
		}
		// 连接蓝牙
		if (BluetoothReader.registerBlueCard(Blueaddress)) {
			Log.d(TAG, "连接状态:" + BluetoothReader.connectStatus());
			buttonOTG.setEnabled(false);
			buttonNFC.setEnabled(false);
			buttonBT.setEnabled(false);
			new AsyncTask<Object, Object, IdentityCardZ>() {
				protected IdentityCardZ doInBackground(Object... arg0) {
					// 读取
					IdentityCardZ cardZ = BluetoothReader.readCardSync();
					return cardZ;
				}

				protected void onPostExecute(IdentityCardZ result) {
					if (result.resCode == 0) {
						// 读取成功
						readCardSuccess(result);
					} else {
						// 读取失败
						uiHandler.obtainMessage(ConsantHelper.READ_CARD_FAILED,
								result.resCode, 0, "读出失败");
					}
					buttonOTG.setEnabled(true);
					buttonNFC.setEnabled(true);
					buttonBT.setEnabled(true);
				}
			}.execute();
		} else {
			Toast.makeText(this, "请确认蓝牙设备已经连接，再读卡!", Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * 蓝牙读卡方式
	 */
	protected void readCardBlueTooth() {

		if (Blueaddress == null) {
			Toast.makeText(this, "请选择蓝牙设备，再读卡!", Toast.LENGTH_LONG).show();
			return;
		}

		if (Blueaddress.length() <= 0) {
			Toast.makeText(this, "请选择蓝牙设备，再读卡!", Toast.LENGTH_LONG).show();
			return;
		}
		if (BluetoothReader.registerBlueCard(Blueaddress)) {
			buttonOTG.setEnabled(false);
			buttonNFC.setEnabled(false);
			buttonBT.setEnabled(false);
			new Thread() {
				public void run() {
					BluetoothReader.readCard();
					tv_info.setText("开始读卡");
				}
			}.start();
		} else {
			Toast.makeText(this, "请确认蓝牙设备已经连接，再读卡!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_blue:
			Intent serverIntent = new Intent(this, DeviceListActivity.class);
			startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
			break;

		}
		return super.onOptionsItemSelected(item);
	}

	class MyHandler extends Handler {
		private SRreder activity;

		@SuppressLint("HandlerLeak")
		MyHandler(SRreder activity) {
			this.activity = activity;
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ConsantHelper.READ_PHOTO_SUCESS:
				// 身份证读取成功，返回图片信息
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);

				mIdentityCardZ = (IdentityCardZ) msg.obj;
				readCardSuccess((IdentityCardZ) msg.obj);
				break;
			case ConsantHelper.READ_CARD_SUCCESS:
				// 身份证读取成功，返回文本信息
				Log.i(TAG, "READ_CARD_SUCCESS:"
						+ ((IdentityCardZ) msg.obj).name);
				break;

			case ConsantHelper.SERVER_CANNOT_CONNECT:
				tv_info.setText("服务器连接失败! 请检查网络。");
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				break;

			case ConsantHelper.READ_CARD_FAILED:
				Object error = msg.obj;
				tv_info.setText("CODE:" + msg.arg1 + "," + error);
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				break;

			case ConsantHelper.READ_CARD_WARNING:
				String str = (String) msg.obj;

				if (str.indexOf("card") > -1) {
					tv_info.setText("读卡失败: 卡片丢失,或读取错误!");
				} else {
					String[] datas = str.split(":");
					tv_info.setText("网络超时 错误码: "
							+ Integer.toHexString(new Integer(datas[1])));
				}
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				break;

			case ConsantHelper.READ_CARD_PROGRESS:

				int progress_value = (Integer) msg.obj;

				break;

			case ConsantHelper.READ_CARD_START:
				break;

			}
		}

	}

	private void readCardSuccess(IdentityCardZ identityCard) {
		if (identityCard != null) {
			JSONObject data = new JSONObject();
			tv_info.setText("读卡成功");
			data.put("Name", identityCard.name.trim());// 姓名
			data.put("SexL", identityCard.sex.trim());// 性别，“男”或“女”
			data.put("NationL", identityCard.ethnicity.trim());// 民族，例：“汉”
			data.put("Born", identityCard.birth.trim()); // 生日，格式：yyyymmdd
			data.put("Address", identityCard.address.trim());// 地址
			data.put("CardNo", identityCard.cardNo.trim());// 身份证号码
			data.put("Police", identityCard.authority.trim());// 签发机关
			data.put("Activity", identityCard.period.trim());// 有效期限，格式：yyyymmddyyyymmdd
			data.put("latitude", latitude.trim());
			data.put("longitude", longitude.trim());
			byte[] bytes = getCode(identityCard.avatar);
			// 加密串无
			data.put("img", bytes);// 图片bytes流
			data.put("deviceType", "TY");
			Intent intent = new Intent();
			intent.putExtra("data", data.toString());
			setResult(11, intent);
			finish();
		}
	}

	public byte[] getCode(byte[] by) {
		Bitmap bitMap = BitmapFactory.decodeByteArray(by, 0, by.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitMap.compress(Bitmap.CompressFormat.JPEG, 60, baos);
		byte[] result = baos.toByteArray();
		for (int i = 0; i <= result.length; i++) {
			System.out.println(result);
		}
		try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	@Override
	protected void onDestroy() {
		// 退出时销毁定位
		mLocClient.stop();
		// 关闭定位图层
		mBaiduMap.setMyLocationEnabled(false);
		mapView.onDestroy();
		mapView = null;
		finish();
		super.onDestroy();
	}
}
