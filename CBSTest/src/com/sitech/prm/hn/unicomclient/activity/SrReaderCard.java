package com.sitech.prm.hn.unicomclient.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import sunrise.bluetooth.SRBluetoothCardReader;
import sunrise.nfc.SRnfcCardReader;
import sunrise.otg.SRotgCardReader;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.baidu.mapapi.model.LatLng;
import com.cbstest.unicomclient.R;
import com.sitech.prm.hn.unicomclient.net.MyLocationUtil;
import com.sunrise.icardreader.helper.ConsantHelper;
import com.sunrise.icardreader.model.IdentityCardZ;

public class SrReaderCard extends Activity {
	private TextView readername, tv_info;
	// 功能
	private static final int SETTING_BT = 22;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static Handler uiHandler;
	private SRBluetoothCardReader BluetoothReader;
	private SRnfcCardReader NFCReader;
	private SRotgCardReader OTGReader;
	private BluetoothAdapter mBluetoothAdapter = null; // /蓝牙适配器
	private Button buttonNFC, buttonOTG, buttonBT, bt_selet;
	private TextView mplaceHolder;
	private String Blueaddress = null;
	private String server_address = "";
	private String server_selected = "";
	private int server_port = 0;
	public IdentityCardZ mIdentityCardZ;
	private static final String TAG = "cbs";
	CustomProgressDialog dialog;
	public SharedPreferences sp = null;
	private TextView title;
	LatLng latl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_loading);
		latl = MyLocationUtil.getInstance().getLatLng();
		Toast.makeText(getApplicationContext(), "sss" + latl, Toast.LENGTH_LONG)
				.show();
		sp = getSharedPreferences("address", 0);
		Blueaddress = sp.getString("address", null);
		readername = (TextView) findViewById(R.id.title);
		readername.setText("森锐身份识别");
		uiHandler = new MyHandler(this);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			finish();
			return;
		}
		// 初始化阅读器
		NFCReader = new SRnfcCardReader(uiHandler, this);
		OTGReader = new SRotgCardReader(uiHandler, this);
		BluetoothReader = new SRBluetoothCardReader(uiHandler, this);

		initViews();
		// addressmac = null; // SR5201571000242
		if (server_selected.equals("")) {
			server_address = "210.14.131.172";
			server_port = 6000;
		}
		initShareReference();

	}

	private void initShareReference() {
		// 设置解密服务器
		NFCReader.setTheServer(this.server_address, this.server_port);

		OTGReader.setTheServer(this.server_address, this.server_port);

		BluetoothReader.setTheServer(this.server_address, this.server_port);
	}

	@SuppressWarnings("deprecation")
	private void initViews() {
		tv_info = (TextView) findViewById(R.id.tv_info);
		bt_selet = (Button) findViewById(R.id.bt_select);
		buttonOTG = (Button) findViewById(R.id.addotg_button);
		buttonNFC = (Button) findViewById(R.id.addnfc_button);
		buttonNFC.setVisibility(View.GONE);
		buttonBT = (Button) findViewById(R.id.add_button);
		/**
		 * 搜索蓝牙
		 */
		bt_selet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent serverIntent2 = new Intent(SrReaderCard.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent2, SETTING_BT);
			}
		});

		buttonOTG.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getWindow()
						.addFlags(
								android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				dialog = new CustomProgressDialog(SrReaderCard.this, "正在读卡",
						R.anim.frame);
				dialog.setCanceledOnTouchOutside(false);
				// dialog.setCancelable(false);
				dialog.show();
				readCardSyncOTG();

			}
		});

		/**
		 * 蓝牙读卡
		 */
		buttonBT.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (Blueaddress == null) {
					Intent serverIntent2 = new Intent(SrReaderCard.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent2, SETTING_BT);
				} else {
					if ("have been paired".equals(Blueaddress)) {
						Intent serverIntent2 = new Intent(SrReaderCard.this,
								DeviceListActivity.class);
						startActivityForResult(serverIntent2, SETTING_BT);
					}

					if (Blueaddress.length() <= 0) {
						Toast.makeText(getApplicationContext(), "请选择蓝牙设备，再读卡!",
								Toast.LENGTH_LONG).show();
						return;
					}
					getWindow()
							.addFlags(
									android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					dialog = new CustomProgressDialog(SrReaderCard.this,
							"正在读卡", R.anim.frame);
					dialog.setCanceledOnTouchOutside(false);
					// dialog.setCancelable(false);
					dialog.show();
					readCardSyncBluetooth();
				}
			}
		});
	}

	/**
	 * OTG 同步读取方式读卡
	 */
	protected void readCardSyncOTG() {

		boolean bRet = OTGReader.registerOTGCard();
		if (bRet == true) {
			buttonOTG.setEnabled(false);
			buttonNFC.setEnabled(false);
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
						dialog.dismiss();
						uiHandler.obtainMessage(ConsantHelper.READ_CARD_FAILED,
								result.resCode, 0, "读出失败");
					}
					buttonOTG.setEnabled(true);
					buttonNFC.setEnabled(true);
					buttonBT.setEnabled(true);
				}

			}.execute();

		} else {
			dialog.dismiss();
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

				;
			}.execute();
		} else {
			Toast.makeText(this, "请确认蓝牙设备已经连接，再读卡!", Toast.LENGTH_LONG).show();
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
				new AlertDialog.Builder(SrReaderCard.this).setTitle("提示")
						.setMessage("未设置蓝牙读卡设备！").setPositiveButton("确定", null)
						.show();
				return;
			}
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

	@Override
	public void onDestroy() {
		super.onDestroy();
		finish();
	}

	class MyHandler extends Handler {
		private SrReaderCard activity;

		@SuppressLint("HandlerLeak")
		MyHandler(SrReaderCard activity) {
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
				activity.tv_info.setText("服务器连接失败! 请检查网络。");
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				break;

			case ConsantHelper.READ_CARD_FAILED:
				Object error = msg.obj;
				activity.tv_info.setText("CODE:" + msg.arg1 + "," + error);
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
				// activity.tv_info.setText("请移动卡片在合适位置!");
				buttonOTG.setEnabled(true);
				buttonNFC.setEnabled(true);
				buttonBT.setEnabled(true);
				break;

			case ConsantHelper.READ_CARD_PROGRESS:

				int progress_value = (Integer) msg.obj;
				// Log.e("main", String.format("progress_value = %d",
				// progress_value));

				break;

			case ConsantHelper.READ_CARD_START:
				activity.tv_info.setText("开始读卡......");
				break;

			}
		}

	}

	private void readCardSuccess(IdentityCardZ identityCard) {
		if (identityCard != null) {

			JSONObject data = new JSONObject();

			data.put("Name", identityCard.name.trim());// 姓名
			data.put("SexL", identityCard.sex.trim());// 性别，“男”或“女”
			data.put("NationL", identityCard.ethnicity.trim());// 民族，例：“汉”
			data.put("Born", identityCard.birth.trim()); // 生日，格式：yyyymmdd
			data.put("Address", identityCard.address.trim());// 地址
			data.put("CardNo", identityCard.cardNo.trim());// 身份证号码
			data.put("Police", identityCard.authority.trim());// 签发机关
			data.put("Activity", identityCard.period.trim());// //
																// 有效期限，格式：yyyymmddyyyymmdd
			byte[] bytes = getCode(identityCard.avatar);
			// 加密串无
			data.put("img", bytes);// 图片bytes流
			data.put("latitude", latl);

			data.put("deviceType", "TY");
			Intent intent = new Intent();
			intent.putExtra("data", data.toString());
			setResult(11, intent);
			dialog.dismiss();
			finish();
		}
		// Log.e(TAG, "读卡成功:"+identityCard.originalString);
		tv_info.setText("读取成功!");
		Log.e(TAG, "读卡成功!");
		Object totalcount;
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

	@Override
	protected void onResume() {
		NFCReader.EnableSystemNFCMessage();
		super.onResume();
	}

	@Override
	public void onStop() {
		try {
			NFCReader.DisableSystemNFCMessage();// 关闭NFC感应
		} catch (Exception e) {
		}

		Log.e("blue", "activity onStop");
		super.onStop();
	}

	@Override
	public void onPause() {

		// isNFC = false;
		Log.e("blue", "onPause");
		NFCReader.DisableSystemNFCMessage();
		super.onPause();
	}
}
