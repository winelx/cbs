package com.sitech.prm.hn.unicomclient.net;

import java.net.SocketTimeoutException;

import javax.ws.rs.core.MediaType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sitech.prm.hn.unicomclient.application.GlobalApplication;
import com.sitech.prm.hn.unicomclient.bean.NetInterfaceBean;

public class NetWorkInterfaceServ {
	public static final String SERV_IP = GlobalApplication.session.getServ_ip();
	public static final String SERV_PORT = GlobalApplication.session.getServ_port();
	public static final String SERV_NAME = "esbWS";
	public static final String SERV_REST = "rest";

	private final static int CONNECTTIMEOUT = 5 * 60 * 1000; // 超时时间为5分钟
	// private final static int CONNECTTIMEOUT = 2000; // 单机测试版超时时间为2秒

	private Context mContext;
	private ConnectivityManager connManager;

	public NetWorkInterfaceServ(Context mContext) {
		this.mContext = mContext;
	}

	public boolean checkMobileNetStatus() {
		boolean success = false;
		if (connManager == null) {
			connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if (State.CONNECTED == state) {
			success = true;
		}
		return success;
	}

	public boolean checkWifiNetStatus() {
		boolean success = false;
		if (connManager == null) {
			connManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		}
		State state = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if (State.CONNECTED == state) {
			success = true;
		}
		return success;
	}

	/***
	 * 调用网络接口现在使用的方法 --------------
	 * 
	 * @param netbean
	 */
	public synchronized void invokESBServer(NetInterfaceBean netbean) {
		Handler handler = netbean.getHandler();
		String wsServerName = netbean.getServerName();
		String inParamJson = netbean.getInParam();
		int handlerCode = netbean.getHandleCode();

		NetInterfaceBean outNetBean = netbean;

		String servPath = "http://" + SERV_IP + ":" + SERV_PORT + "/" + SERV_NAME + "/" + SERV_REST + "/" + wsServerName + "";
		Log.v("服务路径---->", servPath);
		Log.v("入参-------->", inParamJson);

		HttpClient httpClient = new DefaultHttpClient();
		// 设置超时时间为10秒
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONNECTTIMEOUT);
		httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, CONNECTTIMEOUT);

		try {
//			String inParamJsonDes = ThreeDesSecret.encrypt(inParamJson);
			// Log.v("加密入参-------->", inParamJsonDes);
			if (inParamJson == null) {
				outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
				outNetBean.setMessage("服务入参处理错误，请联系管理员。");
			} else {
				HttpPost httpPost = new HttpPost(servPath);
				// 构造最简单的字符串数据
				StringEntity reqEntity = new StringEntity(inParamJson, HTTP.UTF_8);
				// 设置类型
				reqEntity.setContentType("application/json;charset=\"UTF-8\"");
				// 设置请求的数据
				httpPost.setEntity(reqEntity);
				httpPost.setHeader("Accept", MediaType.APPLICATION_JSON);
				httpPost.setHeader("Content-Type", "application/json;charset=\"UTF-8\"");
				HttpResponse response = httpClient.execute(httpPost);
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					String outParamDes = EntityUtils.toString(entity, HTTP.UTF_8);
					// Log.v("加密出参-------->", outParamDes);

					if ("Unknown Server Error".equals(outParamDes)) {// 系统可能挂掉了
						outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
						outNetBean.setMessage("找不到后台服务，请联系管理员。");
					} else {
//						String outParam = ThreeDesSecret.decypt(outParamDes);

						if (outParamDes == null) {
							outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
							outNetBean.setMessage("服务参数解析错误，请联系管理员。");
						} else {
							Log.v("出参-------->", outParamDes);
							outNetBean.setStatus(NetInterfaceBean.STATUS_SUCCESS);
							outNetBean.setMessage("调用服务成功");
							outNetBean.setOutParam(outParamDes);
							/*if (isCheckSafeServer(wsServerName)) {
								try {
									// 进行参数处理
									JSONObject obj = new JSONObject(outParam).getJSONObject(netbean.getRootName()).getJSONObject("bondInfoResult");
									String bondIDIsSafe = obj.getString("bondIDIsSafe").trim();
									String bondIDIsFirstBundle = obj.getString("bondIDIsFirstBundle").trim();
									String bondID = obj.getString("bondID").trim();

									if ("true".equalsIgnoreCase(bondIDIsSafe)) {
										if ("true".equalsIgnoreCase(bondIDIsFirstBundle)) {
											// 第一次绑定
											JNIUtils.encode(bondID);
											// 成功后更新session中的bondID
											GlobalApplication.session.setBondID(bondID);
										}
									} else {
										outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
										outNetBean.setMessage("非法请求，请不要再次尝试！");
									}
								} catch (Exception e) {
									outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
									outNetBean.setMessage("取安全接入节点错误！");
								}
							}*/
						}
					}
				} else {
					outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
					outNetBean.setMessage("服务调用错误，请联系管理员。");
				}
			}

		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
			outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
			outNetBean.setMessage("服务参数解析错误，请联系管理员。");
		} catch (ConnectTimeoutException e) {
			e.printStackTrace();
			outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
			outNetBean.setMessage("连接超时，请检查网络设置。");
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
			outNetBean.setMessage("连接超时，请检查网络设置。");
		} catch (HttpHostConnectException e) {
			e.printStackTrace();
			outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
			outNetBean.setMessage("连接受限，请检查网络设置。");
		} catch (Exception e) {
			e.printStackTrace();
			if ("".equals(outNetBean.getMessage()) || null == outNetBean.getMessage()) {
				outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
				outNetBean.setMessage("未知系统错误，请联系管理员");
			}
		} finally {

			// 无内网环境时测试写死
			// outNetBean.setStatus(NetInterfaceBean.STATUS_SUCCESS);
			// outNetBean.setOutParam(getOutTempParam(wsServerName));
			// Log.v("retCode-->", outNetBean.getRetCode());
			if ("".equals(outNetBean.getMessage()) || null == outNetBean.getMessage()) {
				outNetBean.setMessage("未知系统错误，请联系管理员");
				outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
			} else {
				if (handler != null) {
					Message msg = new Message();
					msg.what = handlerCode;
					msg.obj = outNetBean;
					handler.sendMessage(msg);
				} else {
					outNetBean.setMessage("获得返回索引错误，请联系管理员");
					outNetBean.setStatus(NetInterfaceBean.STATUS_FAILED);
				}
			}
			httpClient.getConnectionManager().shutdown();
		}
	}

	/**
	 * 校验当前服务是否需要安全节点校验
	 * 写死服务名
	 * 
	 * -----不需要校验安全节点的服务-----
	 * 登陆之前的检查更新服务 sm017Qry_Rest
	 * 登陆获取流水的服务 sGetGraphRand_Rest
	 * 发送短信的服务 sRanDomPass_Rest
	 * 获取系统公告的服务 sIndexNotice_Rest
	 * 业务中打印电子免填单流水服务 sGetAccept_Rest
	 * 
	 * 大唐服务：实时数据加密 LTECard_EncAssemDynData
	 * 大唐服务：写卡结果回传 LTECard_WriteCardStatus
	 * 
	 * @param wsServerName
	 * @return
	 */
	private boolean isCheckSafeServer(String wsServerName) {

		boolean retB = true;
		String[] serverArr = new String[] { "LTECard_EncAssemDynData", "LTECard_WriteCardStatus", "sm017Qry_Rest", "sGetGraphRand_Rest", "sIndexNotice_Rest", "sRanDomPass_Rest",
				"sGetAccept_Rest", "getResInfoAndroidRest", "getTermInfoAndroidRest","chnrwd_rest","ChnScvWS_rest" };

		for (int i = 0; i < serverArr.length; i++) {
			if (wsServerName.equals(serverArr[i])) {// 找到有不需要校验的服务
				retB = false;
				break;
			}
		}
		return retB;
	}

	// 临时测试时候取出参，上线时删掉此方法
	private String getOutTempParam(String wsServerName) {
		// Log.v("--wsServerName--->", wsServerName);
		String tempOutParam = "";
		if ("sm017Qry_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"VN\":\"V1.3.6\",\"VP\":\"androidCrm_114122920682.apk\",\"N\":\"更新内容：<br/>1、修改主产品变更公务卡进入无法选资费问题；<br/>2、修改登陆取消工号短信验证；<br/>3、去掉1104开户业务入口；<br/>4、优化了系统安装文件下载和更新；<br/>5、增加同一工号多设备登陆的业务限制；\",\"F\":\"1\"}}";
		} else if ("sGetGraphRand_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"A\":\"114122920983\",\"P\":\"N4DX3e\"}}";
		} else if ("sRanDomPass_Rest".equals(wsServerName)) {
			tempOutParam = "\"R\":{\"C\":\"000000\",\"M\":\"操作成功\"}}";
		} else if ("sLoginCheckZD_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"N\":\"崔东瑞\",\"G\":\"0101001xp\",\"GROUP_ID\":\"10031\",\"LP\":\"EIGBDHBHPHHPMJCE\",\"O\":\"1104|1213|1219|1220|1234|1250|1270|1272|1500|1529|2266|4991|d004|e515|g073|1215|1302|1528|e610|5186|ga01|g794|2280|g303|i138|m001|m002|m066|\",\"FAV\":\"a047|a202|a242|a092|a009|a010|a017|a018|a020|a025|a041|a042|a045|a046|a200|a201|a206|a208|a212|a213|a062|a214|a217|a218|a226|a228|a250|a251|a271|a011|a019|a040|a043|a050|a204|a210|a216|a224|a234|a270|a1258|a291|a061|a070|a1256|a1257|a003|a007|a272|a273|a031|aaaa|a063|a001|a002|a004|a006|a008|a280|a005|a044|a222|a240|a904|a000|a274|a275|a276|a030|a032|a033|a034|a333|a013|a014|a220|a230|a232|a900|a090|a111|avbb|a281|a277|a123|a292|a777|a288|a306|a317|a290|a318|a321|a334|a335|a355|a357|a362|a363|a367|a385|a386|\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("sIndexNotice_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"SUCCESS!!!\",\"O_D_L\":\"4\",\"OUT_DATA\":[{\"N\":\"BOSS系统使用强调说明：\",\"D\":\"20111215221323\"},{\"N\":\"3、对于不按照省公司要求使用的人员，省公司将对该人员所在公司进行全省通报.\",\"D\":\"20071109101637\"},{\"N\":\"2、操作人在输入用户资料等信息时,不能使用如~类的特殊字符,此为系统使用的符号\",\"D\":\"20071109101624\"},{\"N\":\"1、必须使用IE浏览器，使用其他浏览器者产生的问题后果自负\",\"D\":\"20071109101613\"}]}}";
		} else if ("sCustOrderDraw_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"查询成功\",\"O_D_L\":\"0\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("sUserLoginIn_XML".equals(wsServerName)) {
			tempOutParam = "{\"ROOT\":{\"RETURN_CODE\":\"000000\",\"RETURN_MSG\":\"SUCCESS\",\"CN\":\"栾**\",\"RN\":\"正常\",\"IN\":\"身份证\",\"IC\":\"230881198612150810\",\"SM\":\"dn\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s1234CfmZD_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122741257\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s2266InitNew_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"O_D_L\":\"3\",\"OUT_DATA\":[{\"O\":\"20131209121212\",\"AN\":\"赠送话费\",\"RN\":\"TxIo0122872221\"},{\"O\":\"20131209121212\",\"AN\":\"赠送流量\",\"RN\":\"TxIo0122872243\"},{\"O\":\"20131209121212\",\"AN\":\"赠送短信\",\"RN\":\"赠品纸巾\"}],\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122741257\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s1213Cfm_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122741257\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("sUserInfoQry_XML".equals(wsServerName)) {
			tempOutParam = "{\"ROOT\":{\"RETURN_CODE\":\"000000\",\"RETURN_MSG\":\"\",\"OUT_DATA\":{\"BALANCE\":\"32.50\"},\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("sPointsInfoQry_XML".equals(wsServerName)) {
			tempOutParam = "{\"ROOT\":{\"RETURN_CODE\":\"000000\",\"RETURN_MSG\":\"操作成功\",\"OUT_DATA\":{\"SM_TYPE\":\"动感地带\",\"PHONE_NO\":\"13904512008\",\"POINTS\":\"1901\"},\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s1219Init_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"SUCCESS\",\"O_D_L\":\"7\",\"OUT_DATA\":[{\"A\":\"1006\",\"B\":\"国内直拨\"},{\"A\":\"1009\",\"B\":\"短消息\"},{\"A\":\"1026\",\"B\":\"全国漫游\"},{\"A\":\"1044\",\"B\":\"GPRS\"},{\"A\":\"1052\",\"B\":\"VPMN\"},{\"A\":\"1068\",\"B\":\"可视电话\"},{\"A\":\"1098\",\"B\":\"神州行来显\"}],\"O_D_L1\":\"2\",\"OUT_DATA1\":[{\"D\":\"1098\"},{\"D\":\"1086\"}],\"O_D_L2\":\"76\",\"OUT_DATA2\":[{\"E\":\"1005\",\"F\":\"国际长途\"},{\"E\":\"1007\",\"F\":\"语音信箱\"},{\"E\":\"1008\",\"F\":\"语音信箱\"},{\"E\":\"1009\",\"F\":\"短消息\"},{\"E\":\"1010\",\"F\":\"三方通话\"},{\"E\":\"1012\",\"F\":\"呼叫转移\"},{\"E\":\"1013\",\"F\":\"遇忙转移\"},{\"E\":\"1014\",\"F\":\"呼叫等待\"},{\"E\":\"1015\",\"F\":\"呼叫限制\"},{\"E\":\"1018\",\"F\":\"无应答转移\"},{\"E\":\"1023\",\"F\":\"传真\"},{\"E\":\"1024\",\"F\":\"数据通讯\"},{\"E\":\"1025\",\"F\":\"省内漫游\"},{\"E\":\"1026\",\"F\":\"全国漫游\"},{\"E\":\"1027\",\"F\":\"国际漫游\"},{\"E\":\"1030\",\"F\":\"来号显示\"},{\"E\":\"1031\",\"F\":\"来号显示\"},{\"E\":\"1032\",\"F\":\"来号显示\"},{\"E\":\"1033\",\"F\":\"手机炒股\"},{\"E\":\"1037\",\"F\":\"国际前转\"},{\"E\":\"1038\",\"F\":\"GPRS业务\"},{\"E\":\"1041\",\"F\":\"短信包月\"},{\"E\":\"1044\",\"F\":\"GPRS\"},{\"E\":\"1045\",\"F\":\"来显5元\"},{\"E\":\"1046\",\"F\":\"来电显示0元\"},{\"E\":\"1047\",\"F\":\"来显0元\"},{\"E\":\"1049\",\"F\":\"GPRS\"},{\"E\":\"1051\",\"F\":\"国际前转\"},{\"E\":\"1052\",\"F\":\"VPMN\"},{\"E\":\"1053\",\"F\":\"关闭语音\"},{\"E\":\"1055\",\"F\":\"来电显示5元\"},{\"E\":\"1058\",\"F\":\"5元来电\"},{\"E\":\"1059\",\"F\":\"5元来号显示\"},{\"E\":\"1062\",\"F\":\"国际前转(20)\"},{\"E\":\"1064\",\"F\":\"商旅专用10元来号显示\"},{\"E\":\"1066\",\"F\":\"商旅专用5元来电\"},{\"E\":\"1068\",\"F\":\"可视电话\"},{\"E\":\"1070\",\"F\":\"吉祥号月租\"},{\"E\":\"1071\",\"F\":\"乘风网内包月\"},{\"E\":\"1072\",\"F\":\"彩玲一年\"},{\"E\":\"1073\",\"F\":\"彩玲半年\"},{\"E\":\"1080\",\"F\":\"5元来号显示\"},{\"E\":\"1082\",\"F\":\"来号显示\"},{\"E\":\"1085\",\"F\":\"来电提醒\"},{\"E\":\"1091\",\"F\":\"小区月租\"},{\"E\":\"1092\",\"F\":\"3元来号显示\"},{\"E\":\"1093\",\"F\":\"3元短信\"},{\"E\":\"1096\",\"F\":\"5元短信包月\"},{\"E\":\"1097\",\"F\":\"月包\"},{\"E\":\"1098\",\"F\":\"神州行来显\"},{\"E\":\"1099\",\"F\":\"5元来号显示\"},{\"E\":\"2042\",\"F\":\"无漫游\"},{\"E\":\"2045\",\"F\":\"铁通e固话移动惠家\"},{\"E\":\"2046\",\"F\":\"关闭短信发送\"},{\"E\":\"2061\",\"F\":\"来号显示(隐藏)\"},{\"E\":\"2062\",\"F\":\"来号显示(隐藏)\"},{\"E\":\"2063\",\"F\":\"来号显示(隐藏)\"},{\"E\":\"2064\",\"F\":\"来显3元(隐藏)\"},{\"E\":\"2065\",\"F\":\"来显6元(隐藏)\"},{\"E\":\"2066\",\"F\":\"来显5元(隐藏)\"},{\"E\":\"2067\",\"F\":\"来电显示0元(隐藏)\"},{\"E\":\"2068\",\"F\":\"来显0元(隐藏)\"},{\"E\":\"2069\",\"F\":\"来电显示5元(隐藏)\"},{\"E\":\"2070\",\"F\":\"0元来电(隐藏)\"},{\"E\":\"2071\",\"F\":\"5元来电(隐藏)\"},{\"E\":\"2072\",\"F\":\"5元来号显示(隐藏)\"},{\"E\":\"2073\",\"F\":\"商旅专用10元来显隐藏\"},{\"E\":\"2074\",\"F\":\"商旅专用5元来电隐藏\"},{\"E\":\"2075\",\"F\":\"5元来号显示(隐藏)\"},{\"E\":\"2076\",\"F\":\"来号显示(隐藏)\"},{\"E\":\"2077\",\"F\":\"3元来号显示(隐藏)\"},{\"E\":\"2078\",\"F\":\"月包(隐藏)\"},{\"E\":\"2079\",\"F\":\"神州行来显(隐藏)\"},{\"E\":\"2080\",\"F\":\"5元来号显示(隐藏)\"},{\"E\":\"3000\",\"F\":\"物联网服务\"},{\"E\":\"3001\",\"F\":\"全球通温馨家庭服务\"}],\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s1219Cfm_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"调用成功\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("sMainCodeQry_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"O_D_L\":\"1\",\"A\":\"32749\",\"B\":\"动感地带短信套餐10元-0.25\",\"D\":\"动感地带\",\"F\":\"20101201 00:00:00\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("sMainModeQry_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"操作成功\",\"O_D_L\":\"30\",\"OUT_DATA\":[{\"A\":\"33188\",\"D\":\"动感地带网聊套餐1.0\",\"E\":\"下月生效\"},{\"A\":\"38731\",\"D\":\"18元动感地带上网套餐-网上售卡\",\"E\":\"下月生效\"},{\"A\":\"39906\",\"D\":\"动感地带幸福套餐15元\",\"E\":\"下月生效\"},{\"A\":\"39907\",\"D\":\"动感地带幸福套餐20元\",\"E\":\"下月生效\"},{\"A\":\"35335\",\"D\":\"动感地带短信套餐10元-0.20\",\"E\":\"下月生效\"},{\"A\":\"35345\",\"D\":\"动感地带校园套餐10元-0.10\",\"E\":\"下月生效\"},{\"A\":\"36068\",\"D\":\"动感地带网聊套餐-38元\",\"E\":\"下月生效\"},{\"A\":\"36069\",\"D\":\"动感地带网聊套餐-48元\",\"E\":\"下月生效\"},{\"A\":\"36070\",\"D\":\"动感地带网聊套餐-58元\",\"E\":\"下月生效\"},{\"A\":\"36071\",\"D\":\"动感地带网聊套餐-68元\",\"E\":\"下月生效\"},{\"A\":\"36072\",\"D\":\"动感地带网聊套餐-78元\",\"E\":\"下月生效\"},{\"A\":\"36799\",\"D\":\"动感地带流量套餐38\",\"E\":\"下月生效\"},{\"A\":\"19625\",\"D\":\"动感地带标准套餐\",\"E\":\"立即生效\"},{\"A\":\"36801\",\"D\":\"动感地带流量套餐58\",\"E\":\"下月生效\"},{\"A\":\"36802\",\"D\":\"动感地带流量套餐68\",\"E\":\"下月生效\"},{\"A\":\"36803\",\"D\":\"动感地带流量套餐78\",\"E\":\"下月生效\"},{\"A\":\"36804\",\"D\":\"动感地带流量套餐88\",\"E\":\"下月生效\"},{\"A\":\"36805\",\"D\":\"动感地带流量套餐128\",\"E\":\"下月生效\"},{\"A\":\"36806\",\"D\":\"动感地带流量套餐188\",\"E\":\"下月生效\"},{\"A\":\"36807\",\"D\":\"动感地带流量套餐288\",\"E\":\"下月生效\"},{\"A\":\"37042\",\"D\":\"动感地带流量套餐218\",\"E\":\"下月生效\"},{\"A\":\"37403\",\"D\":\"动感地带18元上网套餐\",\"E\":\"下月生效\"},{\"A\":\"37404\",\"D\":\"动感地带28元上网套餐\",\"E\":\"下月生效\"},{\"A\":\"37405\",\"D\":\"动感地带38元上网套餐\",\"E\":\"下月生效\"},{\"A\":\"37406\",\"D\":\"动感地带18元上网套餐（校园版）\",\"E\":\"下月生效\"},{\"A\":\"37407\",\"D\":\"动感地带28元上网套餐（校园版）\",\"E\":\"下月生效\"},{\"A\":\"37408\",\"D\":\"动感地带38元上网套餐（校园版）\",\"E\":\"下月生效\"},{\"A\":\"19919\",\"D\":\"动感地带标准卡\",\"E\":\"立即生效\"},{\"A\":\"19547\",\"D\":\"动感地带彩铃音乐套餐080512\",\"E\":\"下月生效\"},{\"A\":\"36800\",\"D\":\"动感地带流量套餐48\",\"E\":\"下月生效\"}],\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s5584QryMainProd_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"业务受理成功\",\"O_D_L\":\"14\",\"OUT_DATA\":[{\"A\":\"a4lP\",\"B\":\"呼兰\"},{\"A\":\"a4lQ\",\"B\":\"尚志\"},{\"A\":\"a4lR\",\"B\":\"双城\"},{\"A\":\"a4lS\",\"B\":\"肇东\"},{\"A\":\"a4lT\",\"B\":\"阿城\"},{\"A\":\"a4lU\",\"B\":\"巴彦\"},{\"A\":\"a4lV\",\"B\":\"宾县\"},{\"A\":\"a4lW\",\"B\":\"依兰\"},{\"A\":\"a4lX\",\"B\":\"方正\"},{\"A\":\"a4lY\",\"B\":\"延寿\"},{\"A\":\"a4lZ\",\"B\":\"木兰\"},{\"A\":\"a4la\",\"B\":\"通河\"},{\"A\":\"a4lb\",\"B\":\"平房\"},{\"A\":\"a4lc\",\"B\":\"五常\"}],\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s1270Must_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"\",\"O_D_L\":\"1\",\"OUT_DATA\":{\"A\":\"一费制\",\"D\":\"Y\",\"G\":\"30684\",\"J\":\"2\"},\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s1270Cfm_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"受理成功\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s1529_znzd_rest".equals(wsServerName)) {
			tempOutParam = "{\"ROOT\":{\"RETURN_CODE\":\"000000\",\"RETURN_MSG\":\"SUCCESS\",\"OUT_DATA\":[{\"A\":\"现金\",\"B\":\"61.97\",\"C\":\"61.97\",\"D\":\"20041004\",\"E\":\"20501231\",\"F\":\"0\"},{\"A\":\"空中充值\",\"B\":\"4.95\",\"C\":\"4.95\",\"D\":\"20081224\",\"E\":\"20500101\",\"F\":\"Y\"}],\"O_D_L\":\"2\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("s2266CfmNew_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"调用成功\",\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		} else if ("sProductInfoQry_Rest".equals(wsServerName)) {
			tempOutParam = "{\"R\":{\"C\":\"000000\",\"M\":\"调用成功！\",\"O_D_L\":\"5\",\"OUT_DATA\":[{\"A\":\"32386\",\"B\":\"1元长话包\",\"D\":\"40\",\"F\":\"20111101\",\"G\":\"20411101\"},{\"A\":\"30684\",\"B\":\"一费制\",\"D\":\"40\",\"F\":\"20081201\",\"G\":\"20500101\"},{\"A\":\"36550\",\"B\":\"网外呼叫\",\"D\":\"40\",\"F\":\"20120312\",\"G\":\"20500101\"},{\"A\":\"36303\",\"B\":\"30元网内基本通话费、漫游、长途免费\",\"D\":\"40\",\"F\":\"20120312\",\"G\":\"20500101\"},{\"A\":\"32749\",\"B\":\"动感地带短信套餐10元-0.25\",\"D\":\"10\",\"F\":\"20101201\",\"G\":\"20401201\"}],\"bondInfoResult\":{\"bondIDIsSafe\":\"true\",\"bondID\":\"114122920787\",\"bondIDIsFirstBundle\":\"false\"}}}";
		}
		return tempOutParam;
	}

	public boolean hasNetwork(Context context) {
		ConnectivityManager con = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo workinfo = con.getActiveNetworkInfo();
		if (workinfo == null || !workinfo.isAvailable()) {
			return false;
		}
		return true;
	}

}
