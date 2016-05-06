package com.sitech.prm.hn.unicomclient.common;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.MeasureSpec;
import android.widget.Toast;

public class MainUtils {

	public static final String PICFOLDER = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/";

	public static String USERINFO = "USERINFO";

	public static boolean isNullStr(String str) {
		if (str == null || str.trim().length() == 0) {
			return true;
		} else {
			return false;
		}
	}

	public static String doGet(String params) {
		return null;
	}

	public static int computeSampleSize(BitmapFactory.Options options,
	int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
		maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}
		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
	int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;
		int lowerBound = (maxNumOfPixels == -1) ? 1 :
		(int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 :
		(int) Math.min(Math.floor(w / minSideLength),
		Math.floor(h / minSideLength));
		if (upperBound < lowerBound) {
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) &&(minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static String getCurrTime() {
		String pattern = "yyyy-MM-dd HH:mm:ss";
		return (new SimpleDateFormat(pattern)).format(new Date());
	}

	public static String getSIMImsi(Context context) {
		TelephonyManager telMgr = null;
		telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imsi = null;
		if (telMgr.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
			imsi = telMgr.getSubscriberId();
		}
		if (null == imsi) {
			imsi = "000000000";
		}
		return imsi;
	}

	public static String getIMEIInfo(Context context) {
		TelephonyManager telMgr = null;
		telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = null;
		if (telMgr.getSimState() != TelephonyManager.SIM_STATE_ABSENT) {
			imei = telMgr.getDeviceId();
		}
		if (null == imei) {
			imei = "000000000";
		}
		return imei;
	}

	public static String getLoginNo(Context context) {
		SharedPreferences sp = context.getSharedPreferences(MainUtils.USERINFO,
				0);
		return sp.getString("loginno", null);
	}

	public static String getRoleType(Context context) {
		SharedPreferences sp = context.getSharedPreferences(MainUtils.USERINFO,
				0);
		return sp.getString("role_type", null);
	}

	public static String getMonthDay(Context context) {
		SharedPreferences sp = context.getSharedPreferences(MainUtils.USERINFO,
				0);
		return sp.getString("monReportEnd", null);
	}


	@SuppressWarnings("static-access")
	public static String getYesterday(String pattern) {
		Date date = new Date();
		Calendar calendarp = new GregorianCalendar();
		calendarp.setTime(date);
		calendarp.add(calendarp.DATE, -1);
		date = calendarp.getTime(); 
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String dateString = formatter.format(date);
		return dateString;
	}
	
	@SuppressWarnings("static-access")
	public static String getQianTian(String pattern) {
		Date date = new Date();
		Calendar calendarp = new GregorianCalendar();
		calendarp.setTime(date);
		calendarp.add(calendarp.DATE, -2);
		date = calendarp.getTime(); 
		SimpleDateFormat formatter = new SimpleDateFormat(pattern);
		String dateString = formatter.format(date);
		return dateString;
	}

	public static String getMonthFirstDay(String pattern) {
		Calendar cal = Calendar.getInstance();

		SimpleDateFormat datef = new SimpleDateFormat(pattern);
		cal.set(GregorianCalendar.DAY_OF_MONTH, 1);
		Date beginTime = cal.getTime();
		String beginTime1 = datef.format(beginTime);
		return beginTime1;
	}

	public static String getToday(String pattern) {
		Date d = new Date();
		SimpleDateFormat sf = new SimpleDateFormat(pattern);
		return sf.format(d);

	}

	public static String getPreMonty(String pattern) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		String preMonth = dateFormat.format(c.getTime());
		return preMonth;
	}

	public static String getSsMonty(String pattern) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.MONTH, -1);
		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		String preMonth = dateFormat.format(c.getTime());
		return preMonth;
	}

	public static String getMonth(String pattern, Context context) {
		Calendar c = Calendar.getInstance();
		String serverDay = getMonthDay(context);
		if (serverDay == null) {
			c.add(Calendar.MONTH, -1);
		} else {
			int day = Integer.parseInt(serverDay);
			int currentDay = c.get(Calendar.DATE);
			if (currentDay <= day) {
				c.add(Calendar.MONTH, -2);
			} else {
				c.add(Calendar.MONTH, -1);
			}
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
		String preMonth = dateFormat.format(c.getTime());
		return preMonth;

	}

	public static String strEncode(String str) {
		String retStr = null;
		try {
			retStr = URLEncoder.encode(str, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return retStr;
	}

	public static String getDeviceId(Context context) {

		String android_id = Secure.getString(context.getContentResolver(),
				Secure.ANDROID_ID);
		if (null == android_id) {
			android_id = "000000000";
		}
		return android_id;

	}

	public static void addTime4Jpg(String file, String time) {
		BufferedOutputStream bos = null;
		Bitmap icon = null;
		try {
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inPurgeable = true; 
			options.inInputShareable = true; 

			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file, options); 

			float percent = options.outHeight > options.outWidth ? options.outHeight / 960f
					: options.outWidth / 960f;

			if (percent < 1) {
				percent = 1;
			}
			int width = (int) (options.outWidth / percent);
			int height = (int) (options.outHeight / percent);
			icon = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

			Canvas canvas = new Canvas(icon);
			Paint photoPaint = new Paint();
			photoPaint.setDither(true);

			options.inSampleSize = MainUtils.computeSampleSize(options, -1,
					480 * 480);
			options.inJustDecodeBounds = false;

			Bitmap prePhoto = null;
			try {
				prePhoto = BitmapFactory.decodeFile(file, options);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			if (percent > 1) {
				prePhoto = Bitmap.createScaledBitmap(prePhoto, width, height,
						true);
			}

			canvas.drawBitmap(prePhoto, 0, 0, photoPaint);

			if (prePhoto != null && !prePhoto.isRecycled()) {
				prePhoto.recycle();
				prePhoto = null;
				System.gc();
			}

			Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG
					| Paint.DEV_KERN_TEXT_FLAG);
			textPaint.setTextSize(10.0f);
			textPaint.setTypeface(Typeface.DEFAULT);
			textPaint.setColor(Color.BLACK);
			float textWidth = textPaint.measureText(time);
			canvas.drawText(time, width - textWidth - 10, height - 26,
					textPaint);

			bos = new BufferedOutputStream(new FileOutputStream(file));

			int quaility = (int) (100 / percent > 80 ? 80 : 100 / percent);
			icon.compress(CompressFormat.JPEG, quaility, bos);
			bos.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bos != null) {
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (icon != null && !icon.isRecycled()) {
				icon.recycle();
				icon = null;
				System.gc();
			}
		}
	}

	/**
	 * uninstall apk file
	 * 
	 * @param packageName
	 * @param context
	 */
	public static void uninstallAPK(String packageName, Context context) {
		Uri uri = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		context.startActivity(intent);
	}

	/**
	 * isExsit apk file
	 * 
	 * @param packageName
	 * @param context
	 */
	public static boolean isApkExsit(String packageName, Context context) {
		PackageInfo packageInfo;
		try {
			packageInfo = context.getPackageManager().getPackageInfo(
					packageName, 0);

		} catch (NameNotFoundException e) {
			packageInfo = null;
			e.printStackTrace();
		}
		if (packageInfo == null) {
			return false;
		} else {
			return true;
		}
	}

	public static String getSDPath() {
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
		}
		if (sdDir != null) {
			return sdDir.toString();
		} else {
			return null;
		}

	}

	public static void saveBitmap(File f, Bitmap bm) {

		if (f.exists()) {
			f.delete();
		}
		try {

			FileOutputStream out = new FileOutputStream(f);

			bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
			out.flush();
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static Bitmap convertViewToBitmap(View view) {
		view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));

		view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		view.buildDrawingCache();
		Bitmap bitmap = view.getDrawingCache();

		return bitmap;
	}

	public static Bitmap loadBitmapFromView(View v) {
		if (v == null) {
			return null;
		}
		Bitmap screenshot;
		screenshot = Bitmap.createBitmap(v.getWidth(), v.getHeight(),
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(screenshot);
		c.translate(-v.getScrollX(), -v.getScrollY());
		v.draw(c);
		return screenshot;
	}

	// 鍒ゆ柇涓ゅぉ鏄惁鏄悓涓?釜鏈?
	public static boolean iftowsDayOneMonth(String day1, String day2) {
		boolean bool = false;

		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMM");// 灏忓啓鐨刴m琛ㄧず鐨勬槸鍒嗛挓
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");// 灏忓啓鐨刴m琛ㄧず鐨勬槸鍒嗛挓

			Date date1 = sdf.parse(day1);
			Date date2 = sdf.parse(day2);
			if (sdf2.format(date1).equals(sdf2.format(date2))) {
				bool = true;
			}

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return bool;
	}

	public static boolean compare2days(String day1, String day2, String partten) {

		boolean bool = false;
		// "yyyyMMdd"
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(partten);

			Date date1 = sdf.parse(day1);
			Date date2 = sdf.parse(day2);
			if (date1.getTime() < date2.getTime()
					|| date1.getTime() == date2.getTime()) {
				bool = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return bool;
	}

	public static void toast(Context mContext,String msg,int showTime){
		if (showTime == 0 ) {
			Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
		}else{
			Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
		}
	}
}
