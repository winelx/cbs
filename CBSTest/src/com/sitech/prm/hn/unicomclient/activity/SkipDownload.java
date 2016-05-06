package com.sitech.prm.hn.unicomclient.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;

import com.cbstest.unicomclient.R;


public class SkipDownload extends Activity{
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent intent=new Intent();
        String url = getIntent().getExtras().getString("url");
		intent.putExtra("url", url);
		setResult(13, intent);
		finish();
    }
}
