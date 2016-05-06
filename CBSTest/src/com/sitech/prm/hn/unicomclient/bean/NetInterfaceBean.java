package com.sitech.prm.hn.unicomclient.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;

import com.alibaba.fastjson.JSON;
import com.sitech.prm.hn.unicomclient.application.GlobalApplication;
import com.sitech.prm.hn.unicomclient.common.StringUtil;

/***
 * 网络通讯接口
 * 
 * @author hejw
 * 
 */
public class NetInterfaceBean {
	public final static String STATUS_SUCCESS = "1";
	public final static String STATUS_FAILED = "2";

	private Map<String, String> inParam; // 入参json
	private Map<String, Object> inParam_obj; // 多层入参json
	private Map<String, String> outParamtoMap; // 出参转为map
	private String outParam; // 出参json
	private String serverName; // 调用服务名称
	private String status; // 通讯状态
	private String message;
	private int handleCode; //
	private Handler handler;

	private String rootName = "R";
	private String retCodeKey = "C";
	private String retMsgKey = "M";
	
	private String retCodeKey_xl = "RETURN_CODE";
	private String retMsgKey_xl = "RETURN_MSG";
	
	private ArrayList<HashMap<String, String>> resultList_gen_Map;

	public final static String MARK = "#";

	private int initType;// 入参初始化方式 0:原有方式Map<String, String> 1:Map<?, ?>
	public final static int INIT_SIMPLE = 0;
	public final static int INIT_COMMON = 1;

	public String getRootName() {
		return rootName;
	}

	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	public String getRetCodeKey() {
		return retCodeKey;
	}

	public void setRetCodeKey(String retCodeKey) {
		this.retCodeKey = retCodeKey;
	}

	public String getRetMsgKey() {
		return retMsgKey;
	}

	public void setRetMsgKey(String retMsgKey) {
		this.retMsgKey = retMsgKey;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Handler getHandler() {
		return handler;
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	public int getHandleCode() {
		return handleCode;
	}

	public void setHandleCode(int handleCode) {
		this.handleCode = handleCode;
	}

	public String getInParam() {
		String retJson = "";
		switch (this.initType) {
		case INIT_SIMPLE:
			retJson = new StringUtil(rootName, retCodeKey, retMsgKey).hashMapToJson(inParam);
			break;
		case INIT_COMMON:
			// retJson = JSON.toJSONString(inParam);
			retJson = new StringUtil().hashMapToFixJson(inParam_obj).toString();
			break;
		default:
			break;
		}
		return retJson;
	}

	public void setFixJsonParam(Map<String, Object> inParam_obj) {
		this.inParam_obj = inParam_obj;
		this.initType = INIT_COMMON;
	}

	public void setInParam(Map<String, String> inParam) {
		// P_I_I 公共校验IMEI入参key
		// P_I_A 公共校验流水入参key 新的作为公共手机唯一绑定ID校验Key
		// P_I_B_ID 公共手机唯一绑定ID校验Key
//		inParam.put("P_I_I", GlobalApplication.session.getImei());
//		inParam.put("P_I_A", GlobalApplication.session.getBondID());
//		// inParam.put("P_I_B_ID", GlobalApplication.session.getBondID());
//		this.inParam = inParam;
//		this.initType = INIT_SIMPLE;
	}

	public Map<String, String> getOutParam() {
		return outParamtoMap;
	}

	public void setOutParam(String outParam) {
		this.outParamtoMap = new StringUtil(rootName, retCodeKey, retMsgKey).JsonTohashMap(outParam);
		this.outParam = outParam;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * 根据具体节点取值
	 * 
	 * @return
	 */

	public String getResultByKey(String key) {
		String retVal = new StringUtil().getJsonStrVal(this.outParam, NetInterfaceBean.MARK, key);
		return retVal;
	}

	/**
	 * 方法名 getListResultByKey
	 * 
	 * 取json中的数组
	 * 
	 * @param key
	 *            到list
	 * @param listName
	 *            list中的key值
	 * @return
	 * 
	 *         ××××××××××××××××××××××已废弃的方法×××××××××××××××××××××××××××××××××××××
	 *         ×××××××××××××××××××××××××××××
	 *         ×××××××××××××代替方法参考 1270 1104 获取 offerNameList offerIdList
	 *         offervTypeList×××××××××××××××××××××
	 *         ×××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××××
	 *         ××××××××××××××××××××××××××××××××××
	 */
	public ArrayList<String> getListResultByKey(String key) {
		ArrayList<String> retVal = new StringUtil().getJsonStrListVal(this.outParam, NetInterfaceBean.MARK, key);
		return retVal;
	}

	public ArrayList<HashMap<String, String>> getListMapByStr(String Str, String lengKey) {
		// 默认不写参数长度为O_D_L
		return getListMapByKey(rootName + NetInterfaceBean.MARK + Str, lengKey);
	}

	public ArrayList<HashMap<String, String>> getListMapByStr(String Str) {
		// 默认不写参数长度为O_D_L
		return getListMapByKey(rootName + NetInterfaceBean.MARK + Str);
	}

	public ArrayList<HashMap<String, String>> getListMapByKey(String key) {
		// 默认不写参数长度为O_D_L
		return getListMapByKey(key, "O_D_L");
	}

	public ArrayList<HashMap<String, String>> getListMapByKey(String key, String lengKey) {
		ArrayList<HashMap<String, String>> retVal = new StringUtil().getJsonStrListMap(this.outParam, NetInterfaceBean.MARK, key, lengKey);
		this.resultList_gen_Map = retVal;
		return retVal;
	}

	// 规定key为C的是retCode
	public String getRetCode() {
		return getResultValue(retCodeKey);
	}

	// 规定key为M的是retMsg
	public String getRetMsg() {
		// return outParamtoMap.get(retMsgKey);
		return getResultValue(retMsgKey);
	}
	
	// 规定key为RETURN_CODE的是retCode_xl
	public String getRetCode_xl() {
		return getResultValue(retCodeKey_xl);
	}

	// 规定key为RETURN_MSG的是retMsg_xl
	public String getRetMsg_xl() {
		// return outParamtoMap.get(retMsgKey);
		return getResultValue(retMsgKey_xl);
	}

	/**
	 * 从list中某个key的数组
	 * 
	 * @param key
	 * @return
	 */
	public String[] getArrByRultList(String key) {
		String retArr[] = new String[] {};
		retArr = new StringUtil().getArrByRultList(resultList_gen_Map, key);
		return retArr;
	}

	/**
	 * 取得节点参数，取代之前的笨重写法
	 */

	public String getResultValue(String key) {
		String retVal = getResultByKey(rootName + MARK + key);
		return retVal;
	}

	@SuppressWarnings("rawtypes")
	public Map getOutParamMap() {
		return JSON.parseObject(outParam);
	}

	public HashMap<String, Object> getAllObj() {
		return StringUtil.getAllObj_F_JsonStr(this.outParam);
	}
}
