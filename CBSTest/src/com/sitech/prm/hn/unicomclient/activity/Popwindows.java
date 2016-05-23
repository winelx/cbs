package com.sitech.prm.hn.unicomclient.activity;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.cbstest.unicomclient.R;

public class Popwindows extends Activity {
	private Intent intent;
	private static final int PHOTO_REQUEST_CAREMA = 5;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int REQUESTCODE_PICK = 1;
	private Bitmap bitMap;
	public static final int NONE = 0;
	private Uri uritempFile;
	public static final String IMAGE_UNSPECIFIED = "image/*";
	public static final int PHOTOHRAPH = 1;// 拍照

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popwindow);
		gallery();
	}

	/*
	 * 图片获取
	 */
	public void gallery() {
		AlertDialog.Builder builder = new AlertDialog.Builder(Popwindows.this);
		builder.setIcon(R.drawable.ic_launcher);
		// 指定下拉列表的显示数据
		final String[] cities = { "相机", "相册" };
		// 设置一个下拉的列表选择项
		builder.setItems(cities, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == 0) {
					Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
					startActivityForResult(camera, 5);
				} else {
					Intent intent = new Intent(Intent.ACTION_PICK, null);
					intent.setDataAndType(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							IMAGE_UNSPECIFIED);
					startActivityForResult(intent, 2);
				}
			}
		});
		builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		builder.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent datas) {

		if (requestCode == 5 // 拍照
				&& resultCode == Activity.RESULT_OK) {

			Bundle bundle = datas.getExtras();
			// 获取相机返回的数据，并转换为图片格式s
			Bitmap bitmap = (Bitmap) bundle.get("data");

			JSONObject data = new JSONObject();
			byte[] bytes = Bitmap2Bytes(bitmap);
			data.put("img", bytes);// 图片bytes流
			Intent intent = new Intent();
			intent.putExtra("data", data.toString());
			setResult(31, intent);
			finish();

		} else {

			// 拍照
			// 读取相册缩放图片
			if (requestCode == 2) {
				startPhotoZoom(datas.getData());
			}
			// 处理结果
			if (requestCode == 1) {
				Bundle extras = datas.getExtras();
				if (extras != null) {
					Bitmap photo = extras.getParcelable("data");
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					photo.compress(Bitmap.CompressFormat.JPEG, 5, stream);// (0
					byte[] bytes = stream.toByteArray(); // 将图片流以字符串形式存储下来
					Toast.makeText(getApplicationContext(), bytes.length, 0)
							.show();
					JSONObject data = new JSONObject();
					data.put("img", bytes);// 图片bytes流
					Intent intent = new Intent();
					intent.putExtra("data", data.toString());
					setResult(31, intent);// 回调
					finish();
				}
			}
			super.onActivityResult(requestCode, resultCode, datas);
		}

	}

	/**
	 * 裁剪图片方法实现
	 * 
	 * @param uri
	 */
	public void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
		intent.putExtra("crop", "true");
		intent.putExtra("aspectX", 150);
		intent.putExtra("aspectY", 100);
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 100);
		intent.putExtra("outputFormat", "JPEG");
		intent.putExtra("noFaceDetection", true);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, 5);

	}

	// /**
	// * 保存裁剪之后的图片数据
	// */
	// private void setPicToView(Intent picdata) {
	// Bundle extras = picdata.getExtras();
	// if (extras != null) {
	// Bitmap photos = extras.getParcelable("data");
	// Bitmap photo = compressImage(photos);
	// /**
	// * 下面注释的方法是将裁剪之后的图片上传
	// */
	// ByteArrayOutputStream stream = new ByteArrayOutputStream();
	// photo.compress(Bitmap.CompressFormat.JPEG, 1, stream);
	// byte[] bytes = stream.toByteArray(); // 将图片流以字符串形式存储下来
	// Toast.makeText(getApplicationContext(), bytes.length, 0).show();
	// JSONObject data = new JSONObject();
	// data.put("img", bytes);// 图片bytes流
	// Intent intent = new Intent();
	// intent.putExtra("data", data.toString());
	// setResult(31, intent);// 回调
	// finish();
	// }
	// }

	public byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 1, baos);
		return baos.toByteArray();
	}

}
