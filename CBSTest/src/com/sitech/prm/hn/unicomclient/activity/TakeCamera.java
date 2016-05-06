package com.sitech.prm.hn.unicomclient.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import com.cbstest.unicomclient.R;

public class TakeCamera extends Activity {
	// private GlobalApplication application;
	private SurfaceView screenSV;
	private Camera camera;
	private boolean isPreview = false;
	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

		};

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.take_camera);
		screenSV = (SurfaceView) findViewById(R.id.screenSV);

		screenSV.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		screenSV.getHolder().setKeepScreenOn(true);
		screenSV.getHolder().addCallback(new MyCallerBack());
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().detectLeakedSqlLiteObjects()
				.penaltyLog().penaltyDeath().build());
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	public void onClick(View view) {
		camera.autoFocus(new MyAutoFocusCallBack());
	}

	private class MyAutoFocusCallBack implements AutoFocusCallback {
		@Override
		public void onAutoFocus(boolean success, Camera carame) {
			camera.takePicture(null, null, new MyPictureCallback());
		}

	}

	private class MyPictureCallback implements PictureCallback {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			if (data != null) {
				String str = getIntent().getExtras().getString("param");
				System.out.println("str ---------------->" + str);
				Intent intent = new Intent();
				// Bundle bundle = new Bundle();
				// bundle.putSerializable("picture",data);
				intent.putExtra("picture", data);
				intent.putExtra("param", str);
				setResult(1, intent);
				finish();
			}

		}

		public String getSDPath() {
			File sdDir = null;
			boolean sdCardExist = Environment.getExternalStorageState().equals(
					android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			if (sdCardExist) {
				sdDir = Environment.getExternalStorageDirectory();// 获取跟目录
			}
			return sdDir.toString();

		}

	}

	private class MyCallerBack implements Callback {
		@Override
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2,
				int arg3) {

		}

		@Override
		public void surfaceCreated(SurfaceHolder arg0) {
			try {
				if (!isPreview) {
					camera = Camera.open();
				}
				if (!isPreview && camera != null) {
					camera.setPreviewDisplay(screenSV.getHolder());
					Parameters params = camera.getParameters();
					for (int i = 0; i < params.getSupportedPictureSizes()
							.size(); i++) {
						Size size = params.getSupportedPictureSizes().get(i);
						if (size.width <= 1000) {
							// System.out.println("width="+size.width+"height:"
							// + size.height);
							params.setPictureSize(size.width, size.height);
							break;
						}
					}
					camera.setParameters(params);
					camera.startPreview();
				}
				isPreview = true;

			} catch (Exception e) {
				Toast.makeText(TakeCamera.this, "相机打开失败！", 2000).show();
				finish();
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder arg0) {
			if (isPreview && camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
				isPreview = false;
			}
		}

	}

}
