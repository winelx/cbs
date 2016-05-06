package com.sitech.prm.hn.unicomclient.service;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sitech.prm.hn.unicomclient.bean.NetInterfaceStatusDataStruct;
import com.sitech.prm.hn.unicomclient.common.SdCardPath;
import com.sitech.prm.hn.unicomclient.net.AppConfig;

public class HttpServive {
	
	public boolean get(String path, String username, String password) throws Exception {
		URL url = new URL(path + "?username=" + URLEncoder.encode(username) + "&password=" + URLEncoder.encode(password));
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);
		conn.setRequestMethod("GET");
		return conn.getResponseCode() == 200;
	}
	
	public boolean post(String path, String username, String password) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(3000);
		conn.setRequestMethod("POST");
		
		String body = "username=" + URLEncoder.encode(username) + "&password=" + URLEncoder.encode(password);
		
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");		// 必须有这个请求头
		conn.setRequestProperty("Content-Length", body.getBytes().length + "");				// 这个也必须有
		conn.setDoOutput(true);
		conn.getOutputStream().write(body.getBytes());
		
		return conn.getResponseCode() == 200;
	}
	
	private static final String BOUNDARY = "---------------------------3965447928666";
	
	public static void uploadXundian(String path,Map<String,String> map,Map<String,byte[]> byteMap,Handler handler,int msgWhat) {
		NetInterfaceStatusDataStruct result = new NetInterfaceStatusDataStruct(
				NetInterfaceStatusDataStruct.STATUS_SUCCESS, "");
		OutputStream out = null;
		try {
			File file1 = null;
			File file2 = null;
			File file3 = null;
			StringBuilder sb = new StringBuilder();
			
			//所巡店渠道编码
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"groupID\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("groupID") + "\r\n");
			//即登录工号编码
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"managerNo\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("managerNo") + "\r\n");
			//电话号码 即登录接口返回的Phone字段 
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"managerMobile\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("managerMobile") + "\r\n");
			//营业环境签到地点
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"environment\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("environment") + "\r\n");
			//店面宣传
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"propaganda\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("propaganda") + "\r\n");
			//服务规范
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"pecifications\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("pecifications") + "\r\n");
			//经营规模
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"scaleoperation\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("scaleoperation") + "\r\n");
			//其他
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"other\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("other") + "\r\n");
			//建议和意见
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"suggestions\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("suggestions") + "\r\n");
			//经度
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"longitude\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("longitude") + "\r\n");
			//维度
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"latitude\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("latitude") + "\r\n");
			//店面地址
			sb.append("--" + BOUNDARY + "\r\n");
			sb.append("Content-Disposition: form-data; name=\"groupAddress\"" + "\r\n");
			sb.append("\r\n");
			sb.append(map.get("groupAddress"));
			
			byte[] before = sb.toString().getBytes("UTF-8");
			
			StringBuffer sb1 = new StringBuffer();
			StringBuffer sb2 = new StringBuffer();
			StringBuffer sb3 = new StringBuffer();
			for(String key : byteMap.keySet()){
				String fileName = map.get("managerNo") + System.currentTimeMillis()+key+".jpg";
				if(key.equals("image1")){
					file1 = SdCardPath.getSDPathFile(byteMap.get(key), fileName);
					sb1.append("\r\n--" + BOUNDARY + "\r\n");
					sb1.append("Content-Disposition: form-data; name=\"image1\"; filename=\"" + file1.getName() + "\"" + "\r\n");
					sb1.append("Content-Type: image/jpeg" + "\r\n");
					sb1.append("\r\n");
				}else if(key.equals("image2")){
					file2 = SdCardPath.getSDPathFile(byteMap.get(key), fileName);
					sb2.append("\r\n--" + BOUNDARY + "\r\n");
					sb2.append("Content-Disposition: form-data; name=\"image2\"; filename=\"" + file2.getName() + "\"" + "\r\n");
					sb2.append("Content-Type: image/jpeg" + "\r\n");
					sb2.append("\r\n");
				}else if(key.equals("image3")){
					file3 = SdCardPath.getSDPathFile(byteMap.get(key), fileName);
					sb3.append("\r\n--" + BOUNDARY + "\r\n");
					sb3.append("Content-Disposition: form-data; name=\"image3\"; filename=\"" + file3.getName() + "\"" + "\r\n");
					sb3.append("Content-Type: image/jpeg" + "\r\n");
					sb3.append("\r\n");
				}
				
			}
			byte[] before1 = sb1.toString().getBytes("UTF-8");
			byte[] before2 = sb2.toString().getBytes("UTF-8");
			byte[] before3 = sb3.toString().getBytes("UTF-8");
			
			byte[] after = ("\r\n--" + BOUNDARY + "--\r\n").getBytes("UTF-8");
	
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(60000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
			long length = before.length + after.length;
			if(file1 != null){
				length = before1.length + length + byteMap.get("image1").length;
			}
			if(file2 != null){
				length = before2.length + length + byteMap.get("image2").length;		
			}
			if(file3 != null){
				length = before3.length + length + byteMap.get("image3").length;
			}
			conn.setRequestProperty("Content-Length", String.valueOf(length));
			conn.setRequestProperty("HOST", AppConfig.HTTP_URL);
			conn.setDoOutput(true);
	
			out = conn.getOutputStream();
			out.write(before);
			
			for(String key : byteMap.keySet()){
				if(key.equals("image1")){
					out.write(before1);
					out.write(byteMap.get(key));
				}else if(key.equals("image2")){
					out.write(before2);
					out.write(byteMap.get(key));
				}else if(key.equals("image3")){
					out.write(before3);
					out.write(byteMap.get(key));
				}
			}
			out.write(after);
			if(conn.getResponseCode() == 200){
				String ss = conn.getResponseMessage();
				result.setStatus(NetInterfaceStatusDataStruct.STATUS_SUCCESS);
				result.setMessage(SdCardPath.convertStreamToString(conn.getInputStream()));
			}
		}catch(Exception e){
			result.setStatus(NetInterfaceStatusDataStruct.STATUS_FAILED);
			result.setMessage("当前请求网络数据异常，请查看网络状态或稍后在试！");
		}finally{
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Message msg = new Message();
			msg.what = msgWhat;
			msg.obj = result;
			if (handler != null) {
				handler.sendMessage(msg);
			}
		}
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
	
	
	private static final String TAG = "uploadFile";   
    private static final int TIME_OUT = 10*10000000; //超时时间   
    private static final String CHARSET = "utf-8"; //设置编码   
    public static final String SUCCESS="1"; public static final String FAILURE="0";  
     public static String uploadPic(String path,Map<String,String> map,Map<String,byte[]> byteMap,int msgWhat,String name,UploadPicListener listener) {  
         String BOUNDARY = "------------------------------";//
         String PREFIX = "--" , LINE_END = "\r\n";   
         String CONTENT_TYPE = "multipart/form-data"; //内容类型   
         File file=null;
         try {  
             URL url = new URL(path);   
             name=name+".jpg";
             file = SdCardPath.getSDPathFile(byteMap.get("image1"), name);
             //file=new File(absolutionPath()+"/"+name+".jpg");
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             conn.setReadTimeout(TIME_OUT); 
             conn.setConnectTimeout(TIME_OUT); 
             conn.setDoInput(true); //允许输入流  
             conn.setDoOutput(true); //允许输出流  
             conn.setUseCaches(false); //不允许使用缓存   
             conn.setRequestMethod("POST"); //请求方式   
             conn.setRequestProperty("Charset", CHARSET);   
             StringBuilder sbp = new StringBuilder();
 			//certNumber+"###"+flag+"###"+idCardscan
 			//"certNumber":证件号码，正面上传成功之后获取，上传背面图片时，该号码不能为空
 			//"flag":”1”代表正面，”2”代表背面
 			//“idCardscan”:代表是否使用身份证扫描器”N”代表否，”Y”代表是

             sbp.append("--" + BOUNDARY + "\r\n");
             sbp.append("Content-Disposition: form-data; name=\"certNumber\"" + "\r\n");
             sbp.append("\r\n");
             sbp.append(map.get("certNumber") + "\r\n");

             sbp.append("--" + BOUNDARY + "\r\n");
             sbp.append("Content-Disposition: form-data; name=\"flag\"" + "\r\n");
             sbp.append("\r\n");
             sbp.append(map.get("flag") + "\r\n");
 			
             sbp.append("--" + BOUNDARY + "\r\n");
             sbp.append("Content-Disposition: form-data; name=\"idCardscan\"" + "\r\n");
             sbp.append("\r\n");
 			 sbp.append(map.get("idCardscan") + "\r\n");
 			 
 			sbp.append("--" + BOUNDARY + "\r\n");
            sbp.append("Content-Disposition: form-data; name=\"loginNo\"" + "\r\n");
            sbp.append("\r\n");
			 sbp.append(map.get("login_no") + "\r\n");
			 
 			String uploadName=map.get("uploadName");
             //设置编码   
             conn.setRequestProperty("connection", "keep-alive");   
             conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);  
             if(file!=null) {   
            	
                 /** * 当文件不为空，把文件包装并且上传 */  
                 OutputStream outputSteam=conn.getOutputStream();   
                 
                 DataOutputStream dos = new DataOutputStream(outputSteam);   
                 //参数   
                 sbp.append(PREFIX);   
                 sbp.append(BOUNDARY); 
                 sbp.append(LINE_END);   
                 /**  
                 * 这里重点注意：  
                 * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件  
                 * filename是文件的名字，包含后缀名的 比如:abc.png  
                 */   
                 sbp.append("Content-Disposition: form-data; name=\""+uploadName+"\"; filename=\""+file.getName()+"\""+LINE_END);  
                 sbp.append("Content-Type: image/jpeg; charset="+CHARSET+LINE_END);   
                 sbp.append(LINE_END);   
                 dos.write(sbp.toString().getBytes());   
                 InputStream is = new FileInputStream(file);  
                 byte[] bytes = new byte[1024];   
                 int len = 0;   
                 while((len=is.read(bytes))!=-1)   
                 {   
                    dos.write(bytes, 0, len);   
                 }   
                 is.close();   
                 dos.write(LINE_END.getBytes());   
                 byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();   
                 dos.write(end_data);   
                 dos.flush();  
                 /**  
                 * 获取响应码 200=成功  
                 * 当响应成功，获取响应的流  
                 */   
                 //DataInputStream in=new DataInputStream(conn.getInputStream());
                 int res = conn.getResponseCode();   
                 Log.e(TAG, "response code:"+res);
                 if(res==200)   
                 {  
                	 DataInputStream in=new DataInputStream(conn.getInputStream());
                     String str="";
                     byte[] b = new byte[1024];  
                     int l = 0;   
                     while((l=in.read(b))!=-1)   
                     {   
                    	str+=new String(b, 0, l);
                     }   
                     listener.onUploadCallback(str);
                	 
                 }  
             }   
         } 
         catch (IOException e)   
         { e.printStackTrace();return FAILURE;    }   
         return "OK";   
     }   	
     public static String uploadPicFile(String path,Map<String,String> map,int msgWhat,File file,UploadPicListener listener) {  
    	 String BOUNDARY = "------------------------------";//
    	 String PREFIX = "--" , LINE_END = "\r\n";   
    	 String CONTENT_TYPE = "multipart/form-data"; //内容类型   
    	 try {  
    		 URL url = new URL(path);   
    		 //file=new File(absolutionPath()+"/"+name+".jpg");
    		 HttpURLConnection conn = (HttpURLConnection) url.openConnection();
    		 conn.setReadTimeout(TIME_OUT); 
    		 conn.setConnectTimeout(TIME_OUT); 
    		 conn.setDoInput(true); //允许输入流  
    		 conn.setDoOutput(true); //允许输出流  
    		 conn.setUseCaches(false); //不允许使用缓存   
    		 conn.setRequestMethod("POST"); //请求方式   
    		 conn.setRequestProperty("Charset", CHARSET);   
    		 StringBuilder sbp = new StringBuilder();
    		 //certNumber+"###"+flag+"###"+idCardscan
    		 //"certNumber":证件号码，正面上传成功之后获取，上传背面图片时，该号码不能为空
    		 //"flag":”1”代表正面，”2”代表背面
    		 //“idCardscan”:代表是否使用身份证扫描器”N”代表否，”Y”代表是
    		 
    		 sbp.append("--" + BOUNDARY + "\r\n");
    		 sbp.append("Content-Disposition: form-data; name=\"certNumber\"" + "\r\n");
    		 sbp.append("\r\n");
    		 sbp.append(map.get("certNumber") + "\r\n");
    		 
    		 sbp.append("--" + BOUNDARY + "\r\n");
    		 sbp.append("Content-Disposition: form-data; name=\"flag\"" + "\r\n");
    		 sbp.append("\r\n");
    		 sbp.append(map.get("flag") + "\r\n");
    		 
    		 sbp.append("--" + BOUNDARY + "\r\n");
    		 sbp.append("Content-Disposition: form-data; name=\"idCardscan\"" + "\r\n");
    		 sbp.append("\r\n");
    		 sbp.append(map.get("idCardscan") + "\r\n");
    		 
    		 sbp.append("--" + BOUNDARY + "\r\n");
    		 sbp.append("Content-Disposition: form-data; name=\"loginNo\"" + "\r\n");
    		 sbp.append("\r\n");
    		 sbp.append(map.get("login_no") + "\r\n");
    		 
    		 String uploadName=map.get("uploadName");
    		 //设置编码   
    		 conn.setRequestProperty("connection", "keep-alive");   
    		 conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);  
    		 if(file!=null) {   
    			 
    			 /** * 当文件不为空，把文件包装并且上传 */  
    			 OutputStream outputSteam=conn.getOutputStream();   
    			 
    			 DataOutputStream dos = new DataOutputStream(outputSteam);   
    			 //参数   
    			 sbp.append(PREFIX);   
    			 sbp.append(BOUNDARY); 
    			 sbp.append(LINE_END);   
    			 /**  
    			  * 这里重点注意：  
    			  * name里面的值为服务器端需要key 只有这个key 才可以得到对应的文件  
    			  * filename是文件的名字，包含后缀名的 比如:abc.png  
    			  */   
    			 sbp.append("Content-Disposition: form-data; name=\""+uploadName+"\"; filename=\""+file.getName()+"\""+LINE_END);  
    			 sbp.append("Content-Type: image/jpeg; charset="+CHARSET+LINE_END);   
    			 sbp.append(LINE_END);   
    			 dos.write(sbp.toString().getBytes());   
    			 InputStream is = new FileInputStream(file);  
    			 byte[] bytes = new byte[1024];   
    			 int len = 0;   
    			 while((len=is.read(bytes))!=-1)   
    			 {   
    				 dos.write(bytes, 0, len);   
    			 }   
    			 is.close();   
    			 dos.write(LINE_END.getBytes());   
    			 byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();   
    			 dos.write(end_data);   
    			 dos.flush();  
    			 /**  
    			  * 获取响应码 200=成功  
    			  * 当响应成功，获取响应的流  
    			  */   
    			 //DataInputStream in=new DataInputStream(conn.getInputStream());
    			 int res = conn.getResponseCode();   
    			 Log.e(TAG, "response code:"+res);
    			 if(res==200)   
    			 {  
    				 DataInputStream in=new DataInputStream(conn.getInputStream());
    				 String str="";
    				 byte[] b = new byte[1024];  
    				 int l = 0;   
    				 while((l=in.read(b))!=-1)   
    				 {   
    					 str+=new String(b, 0, l);
    				 }   
    				 listener.onUploadCallback(str);
    				 
    			 }  
    		 }   
    	 } 
    	 catch (IOException e)   
    	 { e.printStackTrace();return FAILURE;    }   
    	 return "OK";   
     }   	
}
