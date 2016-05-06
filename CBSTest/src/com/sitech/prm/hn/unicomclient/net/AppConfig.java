package com.sitech.prm.hn.unicomclient.net;

public class AppConfig {

	// public static final String BASE_URL =
	// "http://172.21.134.122:18888/PHONE_WGYX_SERVER/";
	// public static final String BASE_URL = "http://172.21.134.122/";
	// public static final String HTTP_URL = "172.21.134.122";
	// public static final String BASE_URL = "http://192.168.1.110/";
	// public static final String HTTP_URL = "172.19.195.246:80";
	// public static final String BASE_URL = "http://apph5.170.com/";
	public static final String BASE_URL = "http://cbsapp.170.com/";
	public static final String HTTP_URL = "119.39.227.91:9901";
	public static final String HTTP_URL_TY = "172.17.10.15:8980";
	// 登录页面入口
	public static final String LOGIN_HTML = BASE_URL
			+ "html5/pub-page/welcome.html";
	public static final String FIRST_PAGE = "/test1.html";
	public static final String LOGIN_HTML_MENU4 = BASE_URL
			+ "html5/pub-page/erji_menu4.html";

	// 巡店日志 获取信息
	public static final String RPT_GROUPVISIT = BASE_URL + "rpt_groupVisit.do";
	// 巡店日志 获取信息
	public static final String RPT_GROUPVISIT_UPLOADFILE = BASE_URL
			+ "rpt_groupVisit.do?operate=upLoadFile";

	// 基站列表
	public static final String JIZHAN_LIST = BASE_URL
			+ "html5/pub-page/jizhan_list.html";
	public static final String MORE_JIZHAN_QUERY = BASE_URL
			+ "html5/pub-page/more_jizhan_query_map.html";
	public static final String RPT_STATIONINFOSHOW = BASE_URL
			+ "rpt_stationinfoshow.do";
	public static final String JIZHAN_DETAIL_MAP = BASE_URL
			+ "html5/pub-page/jizhan_detail_map.html";

	// 基站列表
	public static final String QUDAO_LIST = BASE_URL
			+ "html5/pub-page/qudao_list.html";
	public static final String MORE_QUDAO_QUERY_MAP = BASE_URL
			+ "html5/pub-page/qudao_more_query_map.html";
	public static final String RPT_GROUPINFOSHOW = BASE_URL
			+ "rpt_groupinfoshow.do";
	public static final String QUDAO_DETAIL_MAP = BASE_URL
			+ "html5/pub-page/qudao_detail_map.html";

	// 激活上传借口
	public static final String TY_ACT_UPLOAD = BASE_URL
			+ "app/business/s9p31_ajax_uploadcheck.jspa";
	// demo
	// public static final String LOGIN_HTML = BASE_URL + "demo/index.html";
}
