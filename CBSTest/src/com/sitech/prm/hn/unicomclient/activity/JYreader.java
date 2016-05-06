package com.sitech.prm.hn.unicomclient.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.alibaba.fastjson.JSONObject;
import com.caihua.cloud.common.User;
import com.caihua.cloud.common.enumerate.ConnectType;
import com.caihua.cloud.common.reader.IDReader;
import com.cbstest.unicomclient.R;
import com.ivsign.android.IDCReader.BitmapDecoder;

public class JYreader extends Activity {
	// private static BluetoothDevice remoteDevice=null;
	private static final int REQUEST_CONNECT_DEVICE = 100;
	// 滤掉组件无法响应和处理的Intent
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static String addressmac = "";
	public SharedPreferences sp = null;
	private String loginTicket;
	private Button lanya, otg, bt_select, addnfc_button;
	String state = "1";
	CustomProgressDialog dialog;
	private CustomDialog.Builder builder;
	private IDReader reader;
	private User user;
	private BluetoothAdapter btAdapt;
	private TextView title;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.bt_loadingjy);
		title = (TextView) findViewById(R.id.title);
		title.setText("军一身份识别");
		sp = getSharedPreferences("address", 0);
		btAdapt = BluetoothAdapter.getDefaultAdapter();// 初始化本机蓝牙功能
		addressmac = sp.getString("address", null);
		addnfc_button = (Button) findViewById(R.id.addnfc_button);
		addnfc_button.setVisibility(View.GONE);
		lanya = (Button) findViewById(R.id.add_button);
		addnfc_button = (Button) findViewById(R.id.addnfc_button);
		otg = (Button) findViewById(R.id.addotg_button);
		bt_select = (Button) findViewById(R.id.bt_select);
		loginTicket = getIntent().getExtras().getString("loginTicket");
		BitmapDecoder.InitDecoder(getApplicationContext());// 解码器初始化
		// 蓝牙设备的点击事件
		lanya.setEnabled(true);
		lanya.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 初始化lany
				if (!btAdapt.isEnabled()) {
					Intent serverIntent2 = new Intent(JYreader.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent2,
							REQUEST_CONNECT_DEVICE);
					return;
				}

				reader.connect(ConnectType.BLUETOOTH, addressmac);

				if (loginTicket != null) {
					getWindow()
							.addFlags(
									android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					dialog = new CustomProgressDialog(JYreader.this, "正在读卡",
							R.anim.frame);
					dialog.setCanceledOnTouchOutside(false);
					dialog.setCancelable(false);
					dialog.show();
				}
			};
		});

		// otg设备的点击事件
		otg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				reader.connect(ConnectType.OTG);
			}
		});

		// 搜索蓝牙的点击事件。
		bt_select.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Bluetooth();
				Intent serverIntent2 = new Intent(JYreader.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent2, REQUEST_CONNECT_DEVICE);
			}
		});

		addnfc_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(JYreader.this, JYreader.class);
				startActivity(intent);
			}
		});
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CONNECT_DEVICE) {
			if (resultCode != Activity.RESULT_OK)
				return;
			String address = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
			addressmac = address;
			sp.edit().putString("address", address).commit();
			if (" have been paired".equals(address)) {
				new AlertDialog.Builder(JYreader.this).setTitle("提示")
						.setMessage("未设置蓝牙读卡设备！").setPositiveButton("确定", null)
						.show();
				return;
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		reader = new IDReader(getApplicationContext(), mHandler);// 实例化
	}

	@SuppressLint("HandlerLeak")
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case IDReader.MESSAGE_CONNECT_SUCCESS:

				break;
			case IDReader.MESSAGE_CONNECT_FAILED:
				dialog.dismiss();
				if (reader.strErrorMsg != null) {
					builder = new CustomDialog.Builder(JYreader.this);
					builder.setTitle("提示");
					builder.setMessage("读卡失败");
					builder.setNegativeButton(
							"确定",
							new android.content.DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							});
					builder.create().show();
					break;
				}
			case IDReader.MESSAGE_READCARD_COMPLETED:// 读卡完成

				lanya.setEnabled(false);
				if (msg.obj != null) {
					Toast.makeText(getApplicationContext(), "读卡成功",
							Toast.LENGTH_SHORT).show();
					user = (User) msg.obj;

					JSONObject data = new JSONObject();
					Log.i("For Test", " ConnectStatus TT=" + data);
					String exper = user.exper + user.exper2;
					data.put("Name", user.name);// 姓名
					data.put("SexL", user.sexL);// 性别，“男”或“女”
					data.put("NationL", user.nationL);// 民族，例：“汉”
					data.put("Born", user.nationL);// 生日，格式：yyyymmdd
					data.put("Address", user.address);// 地址
					data.put("CardNo", user.id);// 身份证号码
					data.put("Police", user.issue);// 签发机关
					data.put("Activity", exper);// 有效期限，格式：yyyymmddyyyymmdd
					// bytes = getCode(user.headImg);

					Intent intent = new Intent();
					intent.putExtra("data", data.toString());
					setResult(112, intent);

					mHandler.sendEmptyMessageDelayed(22222, 2000);
					break;
				} else {
					builder = new CustomDialog.Builder(JYreader.this);
					builder.setMessage("没有连接设备");
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
					lanya.setEnabled(false);
					break;
				}
			case 22222:
				dialog.dismiss();
				finish();
				break;
			}
		}

	};

}
