package com.sitech.prm.hn.unicomclient.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 
 * @author 
 * 
 */
public class StringUtil {

	private static String rootName = "R";
	private static String retCode = "C";
	private static String retMsg = "M";

	public final static int ANNODAY = 30; // 多少天算最新公告

	/**
	 * TextView 显示文字换行不确定，返回带固定换行的字符串，就是每个n个字符加入\n
	 * @param str
	 * @return
	 */
	public StringUtil() {
	}

	public StringUtil(String rootname, String retcode, String retmsg) {
		StringUtil.rootName = rootname;
		StringUtil.retCode = retcode;
		StringUtil.retMsg = retmsg;
	}

	/***
	 *  HashMap 转json串，外边固定一个R格式为{R:{K:Y}}
	 */

	public String hashMapToJson(Map<String, String> inParam) {
		JSONObject rjson = new JSONObject();
		try {
			JSONObject json = new JSONObject();
			for (String key : inParam.keySet()) {
				String val = inParam.get(key);
				json.put(key, val);
			}
			rjson.put(rootName, json);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return rjson.toString();
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> JsonTohashMap(String inParam) {
		Map<String, String> retMap = new HashMap<String, String>();
		try {
			JSONObject json = new JSONObject(inParam).getJSONObject(rootName);
			Iterator<String> keyIter = json.keys();
			String key = "";
			String value = "";
			while (keyIter.hasNext()) {
				key = keyIter.next();
				value = String.valueOf(json.get(key));
				if (value.startsWith("[") && value.endsWith("]")) {
					// 这是个对象，再进行处理
				} else {
					retMap.put(key, value);
				}
			}
		} catch (JSONException e) {
			retMap.put(retCode, "1");
			retMap.put(retMsg, "解析出参出错");
			e.printStackTrace();
		}
		return retMap;
	}

	@SuppressWarnings("unchecked")
	public Map<String, Object> JsonTohashObjMap(String inParam) {
		Map<String, Object> retMap = new HashMap<String, Object>();
		try {
			JSONObject json = new JSONObject(inParam).getJSONObject(rootName);
			Iterator<String> keyIter = json.keys();
			String key = "";
			String value = "";
			while (keyIter.hasNext()) {
				key = keyIter.next();
				value = String.valueOf(json.get(key));
				if (value.startsWith("[") && value.endsWith("]")) {
					// 这是个对象，再进行处理
					JSONArray jsonArray = json.getJSONArray("LIST");
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject item = jsonArray.getJSONObject(i); // 锟矫碉拷每锟斤拷锟斤拷锟斤拷
						item.keys();
					}
				} else {
					retMap.put(key, value);
				}
			}
		} catch (JSONException e) {
			retMap.put(retCode, "1");
			retMap.put(retMsg, "解析出参出错");
			e.printStackTrace();
		}
		return retMap;
	}

	/**
	 * 格式化日期格式字符串
	 * 
	 * @param dataStr
	 * @return
	 */
	public String formatDataStr(String dataStr, String datatype) {

		SimpleDateFormat formatDate = new SimpleDateFormat(datatype);
		SimpleDateFormat formatDatep = new SimpleDateFormat("yyyy年MM月dd日");

		String retVal = "";
		try {
			Date time = formatDate.parse(dataStr.substring(0, 8));
			retVal = formatDatep.format(time);
		} catch (Exception e) {
			retVal = "yyyy年MM月dd日";
			e.printStackTrace();
		}
		return retVal;
	}

	public String formatDataStr(String dataStr) {
		return formatDataStr(dataStr, "yyyyMMdd");
	}

	public Object getOutParamByKey(String jsonStr, String key, String mark) {
		String retObj = null;
		try {
			String keyArr[] = key.split(mark);
			JSONObject json = new JSONObject(jsonStr);
			for (int i = 0; i < keyArr.length; i++) {
				json.get(keyArr[i]);
			}
		} catch (JSONException e) {
			retObj = null;
			e.printStackTrace();
		}

		return retObj;
	}

	/**
	 * 根据key取得具体json值
	 * 
	 * @param outParam
	 * @param mark
	 *            length =3 0 1 2
	 * @param key
	 *            ROOT#USER#NAME
	 * @return
	 */
	public String getJsonStrVal(String outParam, String mark, String key) {
		String retVal = "";
		try {
			String keyArr[] = key.split(mark);
			if (keyArr.length == 0) {
				retVal = "";
			} else if (keyArr.length == 1) {
				JSONObject json = new JSONObject(outParam);
				retVal = json.getString(keyArr[0]);
			} else {
				JSONObject json = new JSONObject(outParam);
				for (int i = 0; i < keyArr.length; i++) {
					if (i == keyArr.length - 1) {
						retVal = json.getString(keyArr[i]);
					} else {
						json = json.getJSONObject(keyArr[i]);
					}
				}
			}

		} catch (JSONException e) {
			retVal = "";
			e.printStackTrace();
		}
		return retVal;
	}

	/**
	 * 取得出参json中的数组
	 * 
	 * @param outParam
	 *            0 1 2 R#LIST#D {R: {N:aaa, LIST: [ {D:ddd,N:nnn},
	 *            {D:ddd,N:nnn}, {D:ddd,N:nnn} ] } }
	 * @param mark
	 * @param key
	 * @return
	 */
	public ArrayList<String> getJsonStrListVal(String outParam, String mark, String key) {
		ArrayList<String> retVal = new ArrayList<String>();
		try {
			retVal = new ArrayList<String>();
			String keyArr[] = key.split(mark);
			if (keyArr.length < 2) {// 取数组最少有2个节点名称
				retVal = null;
			} else {
				String listKey = keyArr[keyArr.length - 1];
				JSONObject json = new JSONObject(outParam);
				// 循环到前一位就可以了
				for (int i = 0; i < keyArr.length - 1; i++) {
					if (i == keyArr.length - 2) {// 取数组那个节点
						JSONArray tempArr = json.getJSONArray(keyArr[i]);
						for (int j = 0; j < tempArr.length(); j++) {
							retVal.add(tempArr.getJSONObject(j).getString(listKey));
						}
					} else {
						json = json.getJSONObject(keyArr[i]);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return retVal;
	}

	/**
	 * 取对应节点的list
	 * 
	 * @param outParam
	 * @param mark
	 * @param key
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap<String, String>> getJsonStrListMap(String outParam, String mark, String key, String lengKey) {
		ArrayList<HashMap<String, String>> retVal = new ArrayList<HashMap<String, String>>();
		try {
			String keyArr[] = key.split(mark);
			if (keyArr.length < 2) {
				// 取数组最少有2个节点名称
			} else {

				JSONObject json = new JSONObject(outParam);//
				String arrLen = "2"; // 默认为2多行形式
				String nodeKey = keyArr[keyArr.length - 1]; // 要取的节点名称

				// 循环到前一位就可以了，找到要取数组的父节点 length = 4 0 R@ T@ OUT_DATA
				// 0 <2 2 1
				for (int i = 0; i < keyArr.length - 1; i++) {
					json = json.getJSONObject(keyArr[i]);
				}
				try {
					// 取OUT_DATA数组长度，如果长度大于1则按照数组方式，否则为普通json
					arrLen = json.getString(lengKey);
				} catch (Exception e) {
				}
				// 找到json节点、要取的是数组还是普通json标志后单独处理
				if ("0".equals(arrLen)) {
					// 返回的数组长度为0，不拆json
				} else if ("1".equals(arrLen)) {
					// 返回数组的位置只有1行，拼成length为1的数组
					JSONObject tempJsonObj = json.getJSONObject(nodeKey);
					Iterator itKets = tempJsonObj.keys();
					HashMap<String, String> tMap = new HashMap<String, String>();
					for (Iterator iterator = itKets; iterator.hasNext();) {
						String keyn = (String) itKets.next();
						String value = tempJsonObj.getString(keyn);
						tMap.put(keyn, value);
					}
					retVal.add(tMap);
				} else {
					// 返回的为数组
					JSONArray tempJSONArr = json.getJSONArray(nodeKey);
					for (int j = 0; j < tempJSONArr.length(); j++) {//  循环数组
						JSONObject tempJsonObj = tempJSONArr.getJSONObject(j);
						Iterator itKets = tempJsonObj.keys();
						HashMap<String, String> tMap = new HashMap<String, String>();
						for (Iterator iterator = itKets; iterator.hasNext();) {

							String keyn = (String) itKets.next();
							String value = tempJsonObj.getString(keyn);
							tMap.put(keyn, value);
						}
						retVal.add(tMap);
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return retVal;
	}

	/**
	 * 取对应节点的list的重构方法，去掉O_D_L的判断
	 * ,取节点下的值，若是[开始 ]结束就是数组按照数组方式返回，否则直接取单行数组
	 * @param outParam
	 * @param mark
	 * @param key
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList<HashMap<String, String>> getJsonStrListMap(String outParam, String mark, String key) {
		ArrayList<HashMap<String, String>> retVal = new ArrayList<HashMap<String, String>>();
		try {
			String keyArr[] = key.split(mark);
			if (keyArr.length < 2) {
				// 取数组最少有2个节点名称
			} else {
				String nodeKey = keyArr[keyArr.length - 1]; // 要取的节点名称
				JSONObject json = new JSONObject(outParam);//
				// 循环取得json的节点
				for (int i = 0; i < keyArr.length - 1; i++) {
					json = json.getJSONObject(keyArr[i]);
				}
				String getJsonStr = json.toString();
				if (getJsonStr.startsWith("[") && getJsonStr.endsWith("]")) {
					// 返回数组的位置只有1行，拼成length为1的数组
					JSONObject tempJsonObj = json.getJSONObject(nodeKey);
					Iterator itKets = tempJsonObj.keys();
					HashMap<String, String> tMap = new HashMap<String, String>();
					for (Iterator iterator = itKets; iterator.hasNext();) {
						String keyn = (String) itKets.next();
						String value = tempJsonObj.getString(keyn);
						tMap.put(keyn, value);
					}
					retVal.add(tMap);
				} else {
					// 返回的为数组
					JSONArray tempJSONArr = json.getJSONArray(nodeKey);
					for (int j = 0; j < tempJSONArr.length(); j++) {// 循环数组
						JSONObject tempJsonObj = tempJSONArr.getJSONObject(j);
						Iterator itKets = tempJsonObj.keys();
						HashMap<String, String> tMap = new HashMap<String, String>();
						for (Iterator iterator = itKets; iterator.hasNext();) {

							String keyn = (String) itKets.next();
							String value = tempJsonObj.getString(keyn);
							tMap.put(keyn, value);
						}
						retVal.add(tMap);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return retVal;
	}

	/**
	 *  与当前日期比较，返回相差的天数
	 * 
	 * @param annoDateStr
	 * @return
	 */
	public long compDate(String annoDateStr, String dataFormat) {
		long val = 0;
		Date currentDate = new Date();
		SimpleDateFormat formatDatep = new SimpleDateFormat(dataFormat);
		try {
			Date annoDate = formatDatep.parse(annoDateStr);
			val = (currentDate.getTime() - annoDate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return val;
	}

	/**
	 *  与当前日期比较，返回相差的天数
	 * 
	 * @param annoDateStr
	 * @return
	 */
	public long compDate(String annoDateStr) {
		// 默锟斤拷为 yyyyMMdd
		return compDate(annoDateStr, "yyyyMMdd");
	}

	/**
	 * 从list中某个key的数组
	 * 
	 * @param key
	 * @return
	 */
	public String[] getArrByRultList(ArrayList<HashMap<String, String>> resultList_gen_Map, String key) {
		String retArr[] = new String[resultList_gen_Map.size()];
		try {
			int i = 0;
			for (HashMap<String, String> map : resultList_gen_Map) {
				retArr[i++] = map.get(key);
			}
		} catch (Exception e) {

		}
		return retArr;
	}

	/**
	 *从List Map中取出map对应key的数组
	 */

	public String[] getListMapToArr(ArrayList<HashMap<String, String>> list, String key) {
		String retArr[] = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			retArr[i] = list.get(i).get(key);
		}
		return retArr;
	}

	/**
	 * 判断字符串是否为相同数字组成，如果是返回true 否则返回false
	 * 
	 * @param numStr
	 * @return
	 */
	public static boolean isSameNumber(String numStr) {
		boolean retB = false;
		// 先判断字符串是否为数字组成的
		if ("".equals(numStr)) {// 空字符串
			retB = false;
		} else if (!ValidateUtil.checkedNumber(numStr)) {// 非数字
			retB = false;
		} else {
			String oneChar = numStr.substring(0, 1);//  取第一个字符
			/*
			 * 以第一个字符分割，如果长度为0肯定是相同的字符串，否则肯定不同
			 * 这种算法不用二次循环
			 */
			if (numStr.split(oneChar).length == 0) {
				retB = true;
			} else {
				retB = false;
			}
		}
		return retB;
	}

	/**
	 * 判断字符串是否为连续数字
	 * 
	 * @param numStr
	 * @return
	 */
	public static boolean isSerialNumber(String numStr) {
		boolean retB = false;
		// 先判断字符串是否为数字组成的
		if ("".equals(numStr)) {// 空字符串
			retB = false;
		} else if (!ValidateUtil.checkedNumber(numStr)) {// 非数字
			retB = false;
		} else {
			retB = true;

			String oneChar = numStr.substring(0, 1);// 取第一个字符
			int oneNum = Integer.parseInt(oneChar);
			for (int i = 1; i < numStr.length(); i++) {
				oneNum = oneNum + 1;
				int nextNum = Integer.parseInt(numStr.substring(i, i + 1));
				if (oneNum != nextNum) {// 只要有1个不与下一个数字不相同就不是连续
					retB = false;
					break;
				}
			}
		}
		return retB;
	}

	/**
	 * 从 list<map<String,String>> 中取得 map中相应key的数组
	 * 
	 * @param result
	 * @param key
	 * @return
	 */
	public List<String> getListByKey(ArrayList<HashMap<String, String>> result, String key) {
		List<String> retList = new ArrayList<String>();
		for (HashMap<String, String> map : result) {
			retList.add(map.get(key));
		}
		return retList;
	}

	/**
	 * 多层hashmap入参，转化为多层json入参
	 * 只有一个根节点
	 * 
	 * @param inParam
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public JSONObject hashMapToFixJson(Map<String, Object> inParam) {
		JSONObject rjson = new JSONObject();
		try {
			for (String key : inParam.keySet()) {
				Object val = inParam.get(key);
				if (val instanceof String) {
					rjson.put(key, val);
				} else {
					rjson.put(key, hashMapToFixJson((Map<String, Object>) val));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return rjson;
	}
	
	/**
	 * 字符串转化为json对象
	 * 
	 * @param jsonstr
	 * @return
	 */
	public static JSONObject getJSONObject_F_Str(String jsonstr) {
		JSONObject jsonObject = new JSONObject();//空json对象
		try {
			jsonObject = new JSONObject(jsonstr);
		} catch (JSONException e) {
		}
		return jsonObject;
	}

	/**
	 * 字符串转化为json数组对象
	 * 
	 * @param jsonstr
	 * @return
	 */
	public static JSONArray getJSONArray_F_Str(String jsonstr) {
		JSONArray jsonArray = new JSONArray();// 空json对象
		try {
			jsonArray = new JSONArray(jsonstr);
		} catch (JSONException e) {
		}
		return jsonArray;
	}

	/**
	 * 从json字符串中获取key对应的字符串类型值
	 * 
	 * @param jsonstr
	 * @param key
	 * @return
	 */
	public static String getStr_F_JsonStr(String jsonstr, String key) {
		JSONObject jsonObject = getJSONObject_F_Str(jsonstr);
		String value = "";
		try {
			value = jsonObject.getString(key);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return value;
	}

	/**
	 * 判断字符串是否为json数组
	 * 
	 * @param name_i_str
	 * @return
	 */
	private static boolean has_JsonArrObj(String jsonstr) {

		JSONArray jsonArray = getJSONArray_F_Str(jsonstr);
		return jsonArray.length() == 0 ? false : true;
	}

	/**
	 * 判断字符串是否为json对象 长度是0说明 不是json对象，否则还是json对象
	 * 
	 * @param jsonstr
	 * @return
	 */
	private static boolean has_JsonObj(String jsonstr) {

		JSONObject jsonObject = getJSONObject_F_Str(jsonstr);
		return jsonObject.length() == 0 ? false : true;
	}

	/**
	 * 根据key获取list
	 * 
	 * 两种情况 {key:value ,[]}
	 * 
	 * @param outWeatherJson
	 * @param key results->weather_data
	 * @return 根据入参格式返回可能为 list或map
	 * 
	 * 
	 */
	public static HashMap<String, Object> getAllObj_F_JsonStr(String jsonstr) {
		JSONObject jsonObject = getJSONObject_F_Str(jsonstr);
		HashMap<String, Object> ret_map = new HashMap<String, Object>();
		try {
			JSONArray names = jsonObject.names();
			for (int i = 0; i < names.length(); i++) {
				String name_i = (String) names.get(i);
				String name_i_str = jsonObject.getString(name_i);

				// System.out.println("name_i = [" + name_i + "], name_i_str=[" + name_i_str + "]   是否为json对象[" + has_JsonObj(name_i_str) + "]    是否为数组[" + has_JsonArrObj(name_i_str) + "]");
				if (has_JsonArrObj(name_i_str)) {// 是json数组
					List<HashMap<String, Object>> ret_list = new ArrayList<HashMap<String, Object>>();

					JSONArray jsonArray = getJSONArray_F_Str(name_i_str);
					for (int j = 0; j < jsonArray.length(); j++) {
						String name_j_str = jsonArray.getString(j);
						ret_list.add(getAllObj_F_JsonStr(name_j_str));
					}
					ret_map.put(name_i, ret_list);// 递归调用取下面的节点
				} else if (has_JsonObj(name_i_str)) {// 普通json对象
					ret_map.put(name_i, getAllObj_F_JsonStr(name_i_str));// 递归调用取下面的节点
				} else {// 单纯字符串
					ret_map.put(name_i, name_i_str);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret_map;
	}
	
	public static String stringDateFormat(String strDateType,String newDateType,String str){
		SimpleDateFormat sd = new SimpleDateFormat(strDateType);
		Date time = null;
		try {
			 time = sd.parse(str);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new SimpleDateFormat(newDateType).format(time);
	}

	public static void main(String[] args) {
//		String test_json_obj = "{\"name\":\"value\",\"name1\":\"value\",\"arr\":[{\"at\":\"value3\"},{\"at\":\"value2\"},{\"at\":\"value3\"}]}";
//		String aa = "{\"R\":{\"RETURN_CODE\":\"0\",\"RETURN_MSG\":\"成功\",\"DETAIL_MSG\":\"营销方式查询\",\"OUT_DATA\":{\"ACTION\":{\"ACTION_ID\":\"201404035000044155\",\"ACTION_NAME\":\"新智能终端支撑业务测试活动\",\"ACTION_DESC\":\"\",\"ACTION_DATE\":\"2013-11-23至2099-12-31\",\"MKT_DICTION\":\"智能终端测试\",\"PROVIDE_TYPE\":\"1\"},\"MEANS\":{\"MEAN\":[{\"MEANS_ID\":\"201405045000045737\",\"MEANS_NAME\":\"附加资费全选与抵消积分\",\"BUSI_INFO\":{\"TOTAL_FEE\":\"100\"},\"H01\":{\"PAY_TYPE\":\"0\",\"PAY_MONEY\":\"100\"},\"H11\":{\"ADD_FEE_LIST\":{\"ADD_FEE_INFO\":[{\"ADD_FEE_TYPE\":\"0\",\"ADD_FEE_VALID_FLAG\":\"0\"},{\"ADD_FEE_CODE\":\"12036\",\"ADD_FEE_NAME\":\"10元短信包月\",\"ADD_FEE_SCORE\":\"360\",\"ADD_FEE_OFFSET_TYPE\":\"6\"},{\"ADD_FEE_CODE\":\"12139\",\"ADD_FEE_NAME\":\"20元套餐\",\"ADD_FEE_SCORE\":\"360\",\"ADD_FEE_OFFSET_TYPE\":\"6\"}]}},\"H14\":{\"IS_SCORE\":\"2\"}},{\"MEANS_ID\":\"201405045000045740\",\"MEANS_NAME\":\"附加资费多选一与抵消积分\",\"BUSI_INFO\":{\"TOTAL_FEE\":\"100\"},\"H01\":{\"PAY_TYPE\":\"0\",\"PAY_MONEY\":\"100\"},\"H11\":{\"ADD_FEE_LIST\":{\"ADD_FEE_INFO\":[{\"ADD_FEE_TYPE\":\"1\",\"ADD_FEE_VALID_FLAG\":\"0\"},{\"ADD_FEE_CODE\":\"12036\",\"ADD_FEE_NAME\":\"10元短信包月\",\"ADD_FEE_SCORE\":\"360\",\"ADD_FEE_OFFSET_TYPE\":\"6\"},{\"ADD_FEE_CODE\":\"12139\",\"ADD_FEE_NAME\":\"20元套餐\",\"ADD_FEE_SCORE\":\"360\",\"ADD_FEE_OFFSET_TYPE\":\"6\"}]}},\"H14\":{\"IS_SCORE\":\"2\"}},{\"MEANS_ID\":\"201405045000045743\",\"MEANS_NAME\":\"终端资费分离 \",\"BUSI_INFO\":{\"TOTAL_FEE\":\"0\"},\"H05\":{\"PAY_TYPE_LIST\":{\"PAY_TYPE_INFO\":[{\"RESOURCE_FAVORS_RATE\":\"30\",\"RES_CONTRACT_TYPE\":\"0\"},{\"RES_BRAND_C\":\"联想\",\"RES_MODEL_C\":\"A398t\",\"RES_COST_PRICE_C\":\"590\",\"RES_CONTRACT_PRICE\":\"1200.00\",\"RES_RES_CODE_C\":\"20007541\",\"RES_BRAND_CODE_C\":\"10010\",\"TAX_PERCENT\":\"0.17\"}]}}}]}},\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114133894711\",\"bondIDIsFirstBundle\":\"false\"}},\"tt\":\"TT\"}";
//		System.out.println("------getMapObj(test_json)-----" + getAllObj_F_JsonStr(aa));
//		// System.out.println("------has_JsonObj(test_json)-----" + has_JsonObj(test_json_obj));
//
//		HashMap<String, Object> at = getAllObj_F_JsonStr(aa);
//		HashMap<String, Object> bt = (HashMap<String, Object>) at.get("R");
//		HashMap<String, Object> ct = (HashMap<String, Object>) bt.get("OUT_DATA");
//		HashMap<String, Object> dt = (HashMap<String, Object>) ct.get("MEANS");
//		List<HashMap<String, Object>>et = (List<HashMap<String, Object>>) dt.get("MEAN");
//		
//		HashMap<String, Object> ft = et.get(0);
//		System.out.println(ft.get("MEANS_NAME"));
		
		System.out.println(new StringUtil().formatDataStr("2005-01", "yyyyMM"));
	}
}
