package com.sitech.prm.hn.unicomclient.common;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ResourceBundle;

import android.os.Environment;

public class SdCardPath {
	
	public static File getSDPathFile(byte[] buf,String fileName){
		BufferedOutputStream bos = null;  
        FileOutputStream fos = null;  
        File file = null;  
        try{  
            file = getSDPathFile(fileName);
            fos = new FileOutputStream(file);  
            bos = new BufferedOutputStream(fos);  
            bos.write(buf);  
        } catch (Exception e){  
            e.printStackTrace();  
        } finally{  
            if (bos != null)  
            {  
                try  
                {  
                    bos.close();  
                }  
                catch (IOException e)  
                {  
                    e.printStackTrace();  
                }  
            }  
            if (fos != null)  
            {  
                try  
                {  
                    fos.close();  
                }  
                catch (IOException e)  
                {  
                    e.printStackTrace();  
                }  
            }  
        }  
        return file;
	}
	public static File getSDPathFile(String fileName){
		File file = new File(absolutionPath(), fileName);
		return file;
	}
	
	public static String absolutionPath(){
		File parent = null;
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			parent = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath()
					+ "/baseApplication/download");
		} else {
			parent = new File(Environment.getDataDirectory().getAbsolutePath()
					+ "/baseApplication/download");
		}
		if (!parent.exists()) {
			parent.mkdirs();
		}

		String saveDir = parent.getAbsolutePath();
		return saveDir;
	}
	public static String convertStreamToString(InputStream is) {   
	   BufferedReader reader = new BufferedReader(new InputStreamReader(is));   
	   StringBuilder sb = new StringBuilder();   
	   String line = null;   
	        try {   
	            while ((line = reader.readLine()) != null) {   
	            	sb.append(line + "/n");   
	            }   
	        } catch (IOException e) {   
	            e.printStackTrace();   
	        } finally {   
	            try {   
	                is.close();   
	            } catch (IOException e) {   
	                e.printStackTrace();   
	            }   
	        }   
	        return sb.toString();   
	    }    
}
