package com.sitech.prm.hn.unicomclient.activity;

import com.cbstest.unicomclient.R;
import com.tencent.mobileqq.openpay.api.IOpenApi;
import com.tencent.mobileqq.openpay.api.IOpenApiListener;
import com.tencent.mobileqq.openpay.api.OpenApiFactory;
import com.tencent.mobileqq.openpay.data.base.BaseResponse;
import com.tencent.mobileqq.openpay.data.pay.PayResponse;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

public class PayCallBack extends Activity implements IOpenApiListener {

	IOpenApi openApi;
	String appId = "100703379";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_paycallback);
		openApi = OpenApiFactory.getInstance(this, appId);
		openApi.handleIntent(getIntent(), this);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		openApi.handleIntent(intent, this);
	}

	public void onOpenResponse(BaseResponse response) {
		String title = "Callback from mqq";
		String message;

		if (response == null) {
			message = "response is null.";
			return;
		} else {
			if (response instanceof PayResponse) {
				PayResponse payResponse = (PayResponse) response;

				message = payResponse.retMsg;
			} else {
				message = "response is not PayResponse.";
			}
		}

		CustomDialog.Builder builder = new CustomDialog.Builder(this);
		builder.setMessage(message);
		builder.setTitle(title);
		builder.setNegativeButton("确定",
				new android.content.DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						finish();
					}
				});
		builder.create().show();
	}
}
