package com.sitech.prm.hn.unicomclient.activity;

import java.io.ByteArrayOutputStream;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;

import com.alibaba.fastjson.JSONObject;
import com.cbstest.unicomclient.R;

public class Popwindows extends Activity {
	private Intent intent;
	private static final int PHOTO_REQUEST_CAREMA = 5;// 拍照
	private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
	private static final int REQUESTCODE_PICK = 1;
	private static final int REQUESTCODE_CUTTING = 2;
	private Bitmap bitMap;
	private Uri uritempFile;

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
					Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
					pickIntent.setDataAndType(
							MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							"image/*");
					startActivityForResult(pickIntent, REQUESTCODE_PICK);
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
			// ((ImageView) findViewById(R.id.qrcode_bitmap))
			// .setImageBitmap(bitmap);// 将图片显示在ImageView里
			JSONObject data = new JSONObject();
			byte[] bytes = Bitmap2Bytes(bitmap);
			data.put("img", bytes);// 图片bytes流
			Intent intent = new Intent();
			intent.putExtra("data", data.toString());
			setResult(31, intent);
			finish();

		} else {

			if (requestCode == REQUESTCODE_PICK) {// 相册

				if (datas == null || datas.getData() == null) {
					return;
				}
				startPhotoZoom(datas.getData());

			} else if (requestCode == REQUESTCODE_CUTTING) {
				if (datas != null) {
					setPicToView(datas);
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
		intent.setDataAndType(uri, "image/*");
		// 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
		intent.putExtra("crop", "true");
		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);
		// outputX outputY 是裁剪图片宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		startActivityForResult(intent, REQUESTCODE_CUTTING);
	}

	/**
	 * 192. * 保存裁剪之后的图片数据 193. * @param picdata 194.
	 */
	private void setPicToView(Intent picdata) {
		Bundle extras = picdata.getExtras();
		if (extras != null) {
			Bitmap photo = extras.getParcelable("data");

			/**
			 * 下面注释的方法是将裁剪之后的图片以Base64Coder的字符方式上 传到服务器，QQ头像上传采用的方法跟这个类似
			 */
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
			byte[] bytes = stream.toByteArray(); // 将图片流以字符串形式存储下来
			JSONObject data = new JSONObject();
			data.put("img", bytes);// 图片bytes流
			Intent intent = new Intent();
			intent.putExtra("data", data.toString());
			//
			setResult(31, intent);// 回调
			finish();
		}
	}

	public byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 10, baos);
		return baos.toByteArray();
	}

}
