package com.sitech.prm.hn.unicomclient.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.tech.NfcB;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cbstest.unicomclient.R;
import com.otg.idcard.OTGReadCardAPI;

public class ReadCardInfo extends Activity {
	// private static BluetoothDevice remoteDevice=null;
	private static final int SETTING_BT = 22;
	private ArrayList<String> IPArray = null;
	public static String remoteIPA = "";
	public static String remoteIPB = "";
	public static String remoteIPC = "";
	private NfcAdapter mAdapter = null;
	private OTGReadCardAPI ReadCardAPI;
	private PendingIntent pi = null;
	// 滤掉组件无法响应和处理的Intent
	private IntentFilter tagDetected = null;
	private String[][] mTechLists;
	private Intent inintent = null;
	private BluetoothAdapter btAdapt;
	// private static final int REQUEST_ENABLE_BT = 2;
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	public static String addressmac = "";
	public SharedPreferences sp = null;
	private ImageView simplify_img_back;
	public static final int MESSAGE_VALID_OTGBUTTON = 15;
	public static final int MESSAGE_VALID_NFCBUTTON = 16;
	public static final int MESSAGE_VALID_BTBUTTON = 17;
	public static final int MESSAGE_VALID_PROCESS = 1001;
	Button lanya,otg,nfc,bt_select;
	TextView message;
	String state = "1";
	NfcAdapter nfcAdapter;
	CustomProgressDialog dialog;
	// loginTicket
	private String loginTicket;
	private NfcAdapter adapter;
	private CustomDialog.Builder builder;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bt_loading);
		NfcManager manager = (NfcManager) getApplication().getSystemService(
				getApplication().NFC_SERVICE);
		adapter = manager.getDefaultAdapter();
		loginTicket = getIntent().getExtras().getString("loginTicket");
		sp = getSharedPreferences("address", 0);
		addressmac = sp.getString("address", null);
		lanya = (Button) findViewById(R.id.add_button);
		otg = (Button) findViewById(R.id.addotg_button);
		nfc = (Button) findViewById(R.id.addnfc_button);
		bt_select = (Button) findViewById(R.id.bt_select);
		simplify_img_back=(ImageView) findViewById(R.id.simplify_img_back);
		simplify_img_back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			finish();
			}
		});
		IPArray = new ArrayList<String>();
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		IPArray.add("103.21.119.78");
		// IPArray.add("112.25.233.122");
		ReadCardAPI = new OTGReadCardAPI(getApplicationContext(), IPArray);
		btAdapt = BluetoothAdapter.getDefaultAdapter();// 初始化本机蓝牙功能
		// 蓝牙设备的点击事件
		lanya.setEnabled(true);
		lanya.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ReadCardAPI = new OTGReadCardAPI(getApplicationContext(),
						IPArray);
				if (mAdapter != null) {
					stopNFC_Listener();
				}
				// 初始化lany
				if (!btAdapt.isEnabled()) {
					btAdapt.enable();
				}
				if ( addressmac== null) {
					Intent serverIntent2 = new Intent(ReadCardInfo.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent2, SETTING_BT);
				} else {
					if ("have been paired".equals(addressmac)) {
						Intent serverIntent2 = new Intent(ReadCardInfo.this,
								DeviceListActivity.class);
						startActivityForResult(serverIntent2, SETTING_BT);
					}
					new Thread() {
						@Override
						public void run() {
							mHandler.sendEmptyMessageDelayed(
									MESSAGE_VALID_BTBUTTON, 0);
						}
					}.start();
				}
			};
		});
		// otg设备的点击事件
		otg.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ReadCardAPI = new OTGReadCardAPI(getApplicationContext(),
						IPArray);
				if (mAdapter != null) {
					stopNFC_Listener();
				}
				new Thread() {
					public void run() {
						mHandler.sendEmptyMessageDelayed(
								MESSAGE_VALID_OTGBUTTON, 0);
					};
				}.start();
			}
		});
		// 搜索蓝牙的点击事件。
		bt_select.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// Bluetooth();
				Intent serverIntent2 = new Intent(ReadCardInfo.this,
						DeviceListActivity.class);
				startActivityForResult(serverIntent2, SETTING_BT);
			}
		});

		nfc.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ReadCardAPI = new OTGReadCardAPI(getApplicationContext(),
						IPArray);
				if (adapter != null && adapter.isEnabled()) {
					// adapter存在，能启用
					startNFC_Listener();
					new AlertDialog.Builder(ReadCardInfo.this).setTitle("NFC")
							.setMessage("请将身份证放置在NFC识别区域！")
							.setPositiveButton("确定", null).show();
				} else {
					Toast.makeText(getApplicationContext(), "设备不支持NFC！",
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		mAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());
		if (mAdapter == null) {
		} else {
			init_NFC();
		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		inintent = intent;
		super.onNewIntent(intent);
		mHandler.sendEmptyMessageDelayed(MESSAGE_VALID_NFCBUTTON, 0);
	}

	private void init_NFC() {
		pi = PendingIntent.getActivity(ReadCardInfo.this, 0, new Intent(this,
				getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		tagDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
		tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		mTechLists = new String[][] { new String[] { NfcB.class.getName() } };
	}

	private void startNFC_Listener() {
		mAdapter.enableForegroundDispatch(this, pi,
				new IntentFilter[] { tagDetected }, mTechLists);
	}

	private void stopNFC_Listener() {
		mAdapter.disableForegroundDispatch(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.option_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.setbtconfig:
			// Launch the DeviceListActivity to see devices and do scan
			Intent serverIntent2 = new Intent(ReadCardInfo.this,
					DeviceListActivity.class);
			startActivityForResult(serverIntent2, SETTING_BT);
			return true;
		}
		return false;
	}

	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == SETTING_BT) {
			if (resultCode != Activity.RESULT_OK)
				return;
			String address = data.getStringExtra(EXTRA_DEVICE_ADDRESS);
			addressmac = address;
			sp.edit().putString("address", address).commit();
			ReadCardAPI.setmac(address);
			if (" have been paired".equals(address)) {
				new AlertDialog.Builder(ReadCardInfo.this).setTitle("提示")
						.setMessage("未设置蓝牙读卡设备！").setPositiveButton("确定", null)
						.show();
				return;
			}
		}
	}

	private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		@SuppressLint({ "InlinedApi", "HandlerLeak" })
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							mHandler.sendEmptyMessageDelayed(
									MESSAGE_VALID_OTGBUTTON, 0);
						}
					}
				}
			}
		}
	};
	int tt = 41;
	private final Handler mHandler = new Handler() {
		@SuppressLint("Wakelock")
		public void handleMessage(Message msg) {
			ReadCardAPI.setlogflag(1);
			switch (msg.what) {
			case MESSAGE_VALID_BTBUTTON:
				ReadCardAPI.setmac(addressmac);
				// 动画显示
				if (loginTicket != null) {
					getWindow()
							.addFlags(
									android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
					dialog = new CustomProgressDialog(ReadCardInfo.this,
							"正在读卡", R.anim.frame);
					dialog.setCanceledOnTouchOutside(false);
					//dialog.setCancelable(false);
					dialog.show();
					new Thread() {
						public void run() {
							tt = ReadCardAPI.BtReadCard(btAdapt, loginTicket);
							mHandler.sendMessage(mHandler.obtainMessage(3300,
									tt));
						};
					}.start();
				} else {
					builder = new CustomDialog.Builder(ReadCardInfo.this);
					builder.setMessage("loginTicket=null");
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
				break;
			case 3300:
				getWindow()
						.clearFlags(
								android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
				dialog.dismiss();
				request(tt);
				break;
			case MESSAGE_VALID_NFCBUTTON:
				if (loginTicket != null) {
					ReadCardAPI.writeFile("come into MESSAGE_CLEAR_ITEMS 1");
					dialog = new CustomProgressDialog(ReadCardInfo.this,
							"正在读卡", R.anim.frame);
					dialog.setCanceledOnTouchOutside(false);
					dialog.setCancelable(false);
					dialog.show();
					new Thread() {
						public void run() {
							tt = ReadCardAPI.NfcReadCard(inintent, loginTicket);
							mHandler.sendMessage(mHandler.obtainMessage(3300,
									tt));
						};
					}.start();
				}
				ReadCardAPI.writeFile("come into MESSAGE_CLEAR_ITEMS 2");
				break;
			case MESSAGE_VALID_OTGBUTTON:
				tt = ReadCardAPI.ConnectStatus();
				Log.e("For Test", " ConnectStatus TT=" + tt);
				if (tt == 0) {
					builder = new CustomDialog.Builder(ReadCardInfo.this);
					builder.setMessage("设备未连接!");
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
					break;
				}
				if (tt == 2) {
					builder = new CustomDialog.Builder(ReadCardInfo.this);
					builder.setMessage("读取数据超时");
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
					break;
				}
				if (loginTicket != null) {
					tt = ReadCardAPI.OTGReadCard(loginTicket);
				}
				Log.e("For Test", " ReadCard TT=" + tt);
				request(tt);
				break;
			case 22222:
				finish();
				break;
			}
		}
	};

	public byte[] getCode(byte[] by) {
		// bytes=Base64.decode(by, Base64.DEFAULT);
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
	public void onPause() {
		super.onPause();
		if (mAdapter != null)
			stopNFC_Listener();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mAdapter != null)
			startNFC_Listener();
		ReadCardAPI.writeFile("come into onResume 1");
		ReadCardAPI.writeFile("come into onResume 2");
		ReadCardAPI.writeFile("pass onNewIntent 1.111111 action="
				+ getIntent().getAction());
	}

	@Override
	protected void onDestroy() {
		this.unregisterReceiver(mUsbReceiver);
		super.onDestroy();
	}

	// 根据响应码给出响应的处理
	private void request(int tt) {
		if (tt == 2) {
			builder = new CustomDialog.Builder(ReadCardInfo.this);
			builder.setMessage("读取数据超时");
			builder.setTitle("提示");

			builder.setNegativeButton("确定",
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});

			builder.create().show();

		}
		if (tt == 41) {
			builder = new CustomDialog.Builder(ReadCardInfo.this);
			builder.setMessage("读卡失败!");
			builder.setTitle("提示");

			builder.setNegativeButton("确定",
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
		if (tt == 42) {
			builder = new CustomDialog.Builder(ReadCardInfo.this);
			builder.setMessage("没有找到服务器!");
			builder.setTitle("提示");

			builder.setNegativeButton("确定",
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
		if (tt == 43) {
			builder = new CustomDialog.Builder(ReadCardInfo.this);
			builder.setMessage("服务器正码忙!");
			builder.setTitle("提示");

			builder.setNegativeButton("确定",
					new android.content.DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			builder.create().show();
		}
		if (tt == 90) {
			lanya.setEnabled(false);
			JSONObject data = new JSONObject();
			data.put("Name", ReadCardAPI.Name().trim());// 姓名
			data.put("Sex", ReadCardAPI.Sex());// 性别编码 “1”：男；“0”：女]h
			data.put("SexL", ReadCardAPI.SexL());// 性别，“男”或“女”
			data.put("NationL", ReadCardAPI.NationL());// 民族，例：“汉”
			data.put("Born", ReadCardAPI.Born());// 生日，格式：yyyymmdd
			data.put("Address", ReadCardAPI.Address().trim());// 地址
			data.put("CardNo", ReadCardAPI.CardNo());// 身份证号码
			data.put("Police", ReadCardAPI.Police().trim());// 签发机关
			data.put("Activity", ReadCardAPI.Activity());// 有效期限，格式：yyyymmddyyyymmdd
			data.put("key", ReadCardAPI.Key());// key
			byte[] bytes = getCode(ReadCardAPI.GetImage());
			data.put("img", bytes);// 图片bytes流
			data.put("deviceNo", ReadCardAPI.Serialcode());// DEVICEnO
			data.put("deviceType", "YS");
			Intent intent = new Intent();
			intent.putExtra("data", data.toString());
			setResult(11, intent);
			mHandler.sendEmptyMessageDelayed(22222, 2000);
			ReadCardAPI.release();
		}
	}
}
