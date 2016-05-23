package com.sitech.prm.hn.unicomclient.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Executors;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.alibaba.fastjson.JSONObject;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.cbstest.unicomclient.R;
import com.sitech.prm.hn.unicomclient.activity.JYreader.MyLocationListenner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.senter.helper.ConsantHelper;
import cn.com.senter.helper.ShareReferenceSaver;
import cn.com.senter.mediator.BluetoothReader;
import cn.com.senter.model.IdentityCardZ;
import cn.com.senter.sdkdefault.helper.Error;

public class Blutoothreader extends Activity {
	private final static String SERVER_KEY1 = "CN.COM.SENTER.SERVER_KEY1";
	private final static String PORT_KEY1 = "CN.COM.SENTER.PORT_KEY1";
	private final static String BLUE_ADDRESSKEY = "CN.COM.SENTER.BLUEADDRESS";
	private final static String KEYNM = "CN.COM.SENTER.KEY";

	// private IdentityCardZ identityCard
	private TextView tv_info;
	private Button buttonNFC, buttonOTG, buttonBT, bt_selet;
	private static final int SETTING_BT = 22;
	private ImageView simplify_img_back;

	private String server_address = "";
	private int server_port = 0;
	private BluetoothReader bluecardreader;

	public static Handler uiHandler;
	Intent serverIntent2;
	private NFCReaderHelper mNFCReaderHelper;
	private OTGReaderHelper mOTGReaderHelper;
	private BlueReaderHelper mBlueReaderHelper;
	public SharedPreferences sp = null;
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	private AsyncTask<Void, Void, String> nfcTask = null;
	// ----蓝牙功能有关的变量----
	private BluetoothAdapter mBluetoothAdapter = null; // /蓝牙适配器
	private String loginTicket;
	private String Blueaddress = null;
	private boolean isNFC;
	private IdentityCardZ mIdentityCardZ;
	private IdentityCardZ identityCard;
	private JSONObject data;
	private Intent intent;
	CustomProgressDialog dialog;
	private NfcAdapter adapter;

	private LocationMode mCurrentMode;// 定位模式
	BitmapDescriptor mCurrentMarker;// Marker图标
	public MyLocationListenner myListener = new MyLocationListenner();
	BaiduMap mBaiduMap;
	String latitude, longitude = "null";
	@SuppressWarnings("unused")
	private MapView mapView;
	LocationClient mLocClient;

	@Override
	protected void onNewIntent(Intent intent) {
		Log.e("Blutoothreader", "NFC 返回调用 onNewIntent");
		super.onNewIntent(intent);

		if (mNFCReaderHelper.isNFC(intent)) {

			if (nfcTask == null) {
				Log.e("Blutoothreader", "返回的intent可用");
				nfcTask = new NFCReadTask(intent).executeOnExecutor(Executors
						.newCachedThreadPool());
			}
		} else {
			Log.e("Blutoothreader", "返回的intent不可用");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_loadingjy);

		uiHandler = new MyHandler(this);
		identityCard = new IdentityCardZ();
		sp = getSharedPreferences("address", 0);
		Blueaddress = sp.getString("address", null);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		mNFCReaderHelper = new NFCReaderHelper(this, uiHandler);
		mOTGReaderHelper = new OTGReaderHelper(this, uiHandler);
		mBlueReaderHelper = new BlueReaderHelper(this, uiHandler);

		// getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		initViews();
		// Blueaddress = ShareReferenceSaver.getData(this, BLUE_ADDRESSKEY);
		initShareReference();
	}

	private void initShareReference() {
		if (this.server_address.length() <= 0) {
			if (ShareReferenceSaver.getData(this, SERVER_KEY1).trim().length() < 1) {
				this.server_address = "senter-online.cn";
			} else {
				this.server_address = ShareReferenceSaver.getData(this,
						SERVER_KEY1);
			}
			if (ShareReferenceSaver.getData(this, PORT_KEY1).trim().length() < 1) {
				this.server_port = 10002;
			} else {
				this.server_port = Integer.valueOf(ShareReferenceSaver.getData(
						this, PORT_KEY1));
			}
		}

		mNFCReaderHelper.setServerAddress(this.server_address);
		mNFCReaderHelper.setServerPort(this.server_port);

		mOTGReaderHelper.setServerAddress(this.server_address);
		mOTGReaderHelper.setServerPort(this.server_port);
		// ----实例化help类---
		mBlueReaderHelper.setServerAddress(this.server_address);
		mBlueReaderHelper.setServerPort(this.server_port);

	}

	private void initViews() {
		tv_info = (TextView) findViewById(R.id.tv_info);
		buttonOTG = (Button) findViewById(R.id.addotg_button);
		buttonNFC = (Button) findViewById(R.id.addnfc_button);
		buttonBT = (Button) findViewById(R.id.add_button);
		bt_selet = (Button) findViewById(R.id.bt_select);
		simplify_img_back = (ImageView) findViewById(R.id.simplify_img_back);
		/**
		 * 后退键
		 */
		simplify_img_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		/**
		 * 搜索蓝牙
		 */
		bt_selet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serverIntent2 = new Intent(Blutoothreader.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent2, SETTING_BT);
			}
		});

		tv_info.setTextColor(Color.rgb(240, 65, 85));
		/**
		 * NFC读卡
		 */
		buttonNFC.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				isNFC = true;
				readCardNFC();

			}
		});

		/**
		 * OTG读卡
		 */
		buttonOTG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				readCardOTG();
			}
		});

		/**
		 * 蓝牙读卡
		 */
		buttonBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Blueaddress == null) {
					Intent serverIntent2 = new Intent(Blutoothreader.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent2, SETTING_BT);
				} else {
					if ("have been paired".equals(Blueaddress)) {
						Intent serverIntent2 = new Intent(Blutoothreader.this,
								DeviceListActivity.class);
						startActivityForResult(serverIntent2, SETTING_BT);
					}

					if (Blueaddress.length() <= 0) {
						Toast.makeText(getApplicationContext(), "请选择蓝牙设备，再读卡!",
								Toast.LENGTH_LONG).show();
						return;
					}

					if (mBlueReaderHelper.openBlueConnect(Blueaddress) == true) {

						buttonOTG.setEnabled(false);
						buttonNFC.setEnabled(false);
						buttonBT.setEnabled(false);
						new BlueReadTask().executeOnExecutor(Executors
								.newCachedThreadPool());

					} else {
						Log.e("", "close ok");
						Toast.makeText(getApplicationContext(),
								"请确认蓝牙设备已经连接，再读卡!", Toast.LENGTH_SHORT).show();
					}
				}
			}
		});
	}

	/**
	 * NFC 方式读卡
	 */
	protected void readCardNFC() {
		mNFCReaderHelper.read(this);
	}

	/**
	 * OTG方式读卡
	 * 
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	protected void readCardOTG() {
		if (isNFC) {
			mNFCReaderHelper.disable(Blutoothreader.this);
			isNFC = false;
		}

		boolean bRet = mOTGReaderHelper.registerotg(this);
		if (bRet == true) {
			buttonOTG.setEnabled(false);
			buttonNFC.setEnabled(false);
			buttonBT.setEnabled(false);
			new OTGReadTask()
					.executeOnExecutor(Executors.newCachedThreadPool());
		}
	}

	private class OTGReadTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPostExecute(String strCardInfo) {
			buttonOTG.setEnabled(true);
			buttonNFC.setEnabled(true);
			buttonBT.setEnabled(true);

			if (TextUtils.isEmpty(strCardInfo)) {
				uiHandler.sendEmptyMessage(ConsantHelper.READ_CARD_FAILED);
				nfcTask = null;
				return;
			}
			if (strCardInfo.length() <= 2) {
				readCardFailed(strCardInfo);
				nfcTask = null;
				return;
			}

			ObjectMapper objectMapper = new ObjectMapper();
			mIdentityCardZ = new IdentityCardZ();
			try {
				mIdentityCardZ = (IdentityCardZ) objectMapper.readValue(
						strCardInfo, IdentityCardZ.class);
			} catch (Exception e) {
				e.printStackTrace();
				nfcTask = null;
				return;
			}
			readCardSuccess(mIdentityCardZ);
			nfcTask = null;
			super.onPostExecute(strCardInfo);
		}

		@Override
		protected String doInBackground(Void... params) {
			String strCardInfo = mOTGReaderHelper.Read();
			return strCardInfo;
		}
	};

	private class NFCReadTask extends AsyncTask<Void, Void, String> {

		private Intent mIntent = null;

		public NFCReadTask(Intent i) {
			mIntent = i;
		}

		@Override
		protected String doInBackground(Void... params) {

			String strCardInfo = mNFCReaderHelper.readCardWithIntent(mIntent);
			return strCardInfo;
		}

		@Override
		protected void onPostExecute(String strCardInfo) {

			if (TextUtils.isEmpty(strCardInfo)) {
				uiHandler.sendEmptyMessage(ConsantHelper.READ_CARD_FAILED);
				nfcTask = null;
				return;
			}
			if (strCardInfo.length() <= 2) {
				readCardFailed(strCardInfo);
				nfcTask = null;
				return;
			}
			ObjectMapper objectMapper = new ObjectMapper();
			mIdentityCardZ = new IdentityCardZ();

			try {
				mIdentityCardZ = (IdentityCardZ) objectMapper.readValue(
						strCardInfo, IdentityCardZ.class);
			} catch (Exception e) {
				e.printStackTrace();
				nfcTask = null;
				return;
			}

			readCardSuccess(mIdentityCardZ);
			nfcTask = null;
			super.onPostExecute(strCardInfo);
		}
	}

	/**
	 * 蓝牙读卡方式
	 */
	private class BlueReadTask extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPostExecute(String strCardInfo) {
			buttonOTG.setEnabled(true);
			buttonNFC.setEnabled(true);
			buttonBT.setEnabled(true);
			/*
			 * if (TextUtils.isEmpty(strCardInfo)) {
			 * uiHandler.sendEmptyMessage(ConsantHelper.READ_CARD_FAILED);
			 * nfcTask = null; return; }
			 */
			if (strCardInfo.length() <= 2) {
				readCardFailed(strCardInfo);
				nfcTask = null;
				return;
			}
			ObjectMapper objectMapper = new ObjectMapper();
			mIdentityCardZ = new IdentityCardZ();
			try {
				mIdentityCardZ = (IdentityCardZ) objectMapper.readValue(
						strCardInfo, IdentityCardZ.class);

			} catch (Exception e) {
				e.printStackTrace();
				Log.e(ConsantHelper.STAGE_LOG, "mIdentityCardZ failed");
				nfcTask = null;
				mBlueReaderHelper.closeBlueConnect();
				return;
			}
			readCardSuccess(mIdentityCardZ);
		}

		@Override
		protected String doInBackground(Void... params) {
			String strCardInfo = mBlueReaderHelper.read();
			return strCardInfo;
		}
	};

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
		option.setScanSpan(10000);// 设置发起定位请求的间隔时间为5000ms
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
			Toast.makeText(getApplicationContext(), latitude + "_" + longitude,
					0).show();

		}

		public void onReceivePoi(BDLocation poiLocation) {
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SETTING_BT) {
			if (resultCode != Activity.RESULT_OK)
				return;
			String address = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
			Blueaddress = address;
			sp.edit().putString("address", address).commit();
			if (" have been paired".equals(address)) {
				new AlertDialog.Builder(Blutoothreader.this).setTitle("提示")
						.setMessage("未设置蓝牙读卡设备！").setPositiveButton("确定", null)
						.show();
				return;
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		finish();
	}

	class MyHandler extends Handler {
		private Blutoothreader activity;

		MyHandler(Blutoothreader activity) {
			this.activity = activity;
		}

		@SuppressLint("UseValueOf")
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case ConsantHelper.READ_CARD_SUCCESS:
				// Toast.makeText(getApplicationContext(),
				// "读卡成功------------------11-", Toast.LENGTH_SHORT).show();
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);

				break;
			case ConsantHelper.SERVER_CANNOT_CONNECT:
				activity.tv_info.setText("服务器连接失败! 请检查网络。");
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				dialog.dismiss();
				break;

			case ConsantHelper.READ_CARD_FAILED:
				activity.tv_info.setText("无法读取信息请重试!");
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				break;

			case ConsantHelper.READ_CARD_WARNING:
				String str = (String) msg.obj;
				if (str.indexOf("card") > -1) {
					activity.tv_info.setText("读卡失败: 卡片丢失,或读取错误!");
				} else {
					String[] datas = str.split(":");
					activity.tv_info.setText("网络超时 错误码: "
							+ Integer.toHexString(new Integer(datas[1])));
				}
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				dialog.dismiss();
				break;

			case ConsantHelper.READ_CARD_PROGRESS:

				int progress_value = (Integer) msg.obj;
				activity.tv_info.setText("正在读卡......,进度：" + progress_value
						+ "%");

				break;

			case ConsantHelper.READ_CARD_START:
				activity.tv_info.setText("开始读卡......");
				break;
			case Error.ERR_CONNECT_SUCCESS:
				String devname = (String) msg.obj;
				activity.tv_info.setText(devname + "连接成功!");
				break;
			case Error.ERR_CONNECT_FAILD:
				String devname1 = (String) msg.obj;
				activity.tv_info.setText(devname1 + "连接失败!");
				break;
			case Error.ERR_CLOSE_SUCCESS:
				activity.tv_info.setText((String) msg.obj + "断开连接成功");
				break;
			case Error.ERR_CLOSE_FAILD:
				activity.tv_info.setText((String) msg.obj + "断开连接失败");
				dialog.dismiss();
				break;
			case Error.RC_SUCCESS:
				String devname12 = (String) msg.obj;

				activity.tv_info.setText(devname12 + "连接成功!");
				break;
			}
		}
	}

	// 读卡解析
	private void readCardFailed(String strcardinfo) {
		int bret = Integer.parseInt(strcardinfo);
		switch (bret) {
		case -1:
			tv_info.setText("服务器连接失败!");
			dialog.dismiss();
			break;
		case 1:
			tv_info.setText("读卡失败!");
			dialog.dismiss();
			break;
		case 2:
			tv_info.setText("读卡失败!");
			dialog.dismiss();
			break;
		case 3:
			tv_info.setText("网络超时!");
			dialog.dismiss();
			break;
		case 4:
			tv_info.setText("读卡失败!");
			dialog.dismiss();
			break;
		case -2:
			tv_info.setText("读卡失败!");
			dialog.dismiss();
			break;
		case 5:
			tv_info.setText("照片解码失败!");
			Toast.makeText(getApplicationContext(), "图片解码失败",
					Toast.LENGTH_SHORT).show();
			break;
		}
	}

	private void readCardSuccess(IdentityCardZ identityCard) {
		if (identityCard != null) {
			Log.e(ConsantHelper.STAGE_LOG, "读卡成功!");

			Log.i("For Test", " ConnectStatus TT=" + data);
			JSONObject data = new JSONObject();
			String s = identityCard.birth;
			String b = s.replace("年", "");
			String ss = b.replace("月", "");
			String Born = ss.replace("日", "");
			Born = Born.replaceAll(" ", "");
			String per = identityCard.period;
			String perio = per.replace(".", "");
			String period = perio.replace("-", "").trim();
			data.put("Name", identityCard.name.trim());// 姓名
			data.put("SexL", identityCard.sex.trim());// 性别，“男”或“女”
			data.put("NationL", identityCard.ethnicity.trim());// 民族，例：“汉”
			data.put("Born", Born.trim()); // 生日，格式：yyyymmdd
			data.put("Address", identityCard.address.trim());// 地址
			data.put("CardNo", identityCard.cardNo.trim());// 身份证号码
			data.put("Police", identityCard.authority.trim());// 签发机关
			data.put("Activity", period.trim());// // 有效期限，格式：yyyymmddyyyymmdd
			byte[] bytes = getCode(identityCard.avatar);
			// 加密串无
			data.put("img", bytes);// 图片bytes流
			data.put("deviceType", "XT");
			Intent intent = new Intent();
			intent.putExtra("data", data.toString());
			setResult(112, intent);
			finish();
		}
		tv_info.setText("读取成功!");
		Log.e(ConsantHelper.STAGE_LOG, "读卡成功!");
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.setbtconfig:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent2 = new Intent(Blutoothreader.this,
					DeviceListActivity.class);
			startActivityForResult(serverIntent2, SETTING_BT);
			return true;
		}
		return false;
	}

	public byte[] getCode(byte[] by) {
		Bitmap bitMap = BitmapFactory.decodeByteArray(by, 0, by.length);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
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

}
