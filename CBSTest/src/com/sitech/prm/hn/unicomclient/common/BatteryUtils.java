package com.sitech.prm.hn.unicomclient.common;


import com.sitech.prm.hn.unicomclient.service.BatteryListener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.Toast;
  
public class BatteryUtils {    
  public static boolean flag=true;
  public static void batteryLevel(Context context,BatteryListener bl) {
    BroadcastReceiver batteryLevelReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {  
            context.unregisterReceiver(this);  
            int rawlevel = intent.getIntExtra("level", -1);//获得当前电量  
            int scale = intent.getIntExtra("scale", -1);  
//获得总电量  
            int level = -1;  
            if (rawlevel >= 0 && scale > 0) {  
                level = (rawlevel * 100) / scale;  
            }  
            if(level<15){
            	Toast.makeText(context, "电量低相机打开失败！", 2000).show();
            	flag= false;
            }
        }  
    };  
    IntentFilter batteryLevelFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);  
    context.registerReceiver(batteryLevelReceiver, batteryLevelFilter);  
    bl.onBatteryCallback(flag);
  }  
      
}  

