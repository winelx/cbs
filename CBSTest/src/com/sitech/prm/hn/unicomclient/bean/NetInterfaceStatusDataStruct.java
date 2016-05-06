
package com.sitech.prm.hn.unicomclient.bean;

public class NetInterfaceStatusDataStruct {
	public static final String STATUS_SUCCESS = "1";
	public static final String STATUS_FAILED = "2";
	
	private String status;
	private String message;
	
	private Object obj;
	
	public NetInterfaceStatusDataStruct() {
	}
	public NetInterfaceStatusDataStruct(String status,String message) {
		setStatus(status);
		setMessage(message);
	}
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Object getObj() {
		return obj;
	}
	public void setObj(Object obj) {
		this.obj = obj;
	}
	
	
}
