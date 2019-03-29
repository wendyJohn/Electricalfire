package com.sanleng.electricalfire.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.sanleng.electricalfire.MyApplication;
import com.sanleng.electricalfire.R;
import com.sanleng.electricalfire.util.MessageEvent;

import org.greenrobot.eventbus.EventBus;


public class FireTipsDialog extends Dialog implements View.OnClickListener {

    private Context context;
    private TextView notice;
    private TextView cancle;
    private TextView message;
    private String str_test;

    public FireTipsDialog(Context context, String str_test) {
        super(context);
        this.context = context;
        this.str_test = str_test;
    }

    public FireTipsDialog(Context context, int theme) {
        super(context, theme);
        this.context = context;

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        this.setContentView(R.layout.firetipsdialog);
        this.setCancelable(false);// 设置点击屏幕Dialog不消失
        notice = findViewById(R.id.notice);
        cancle = findViewById(R.id.cancle);
        message = findViewById(R.id.message);
        message.setText(str_test);
        notice.setOnClickListener(this);
        cancle.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.notice:
//				MessageEvent messageEvent = new MessageEvent(MyApplication.MESSRFireTips);
//				EventBus.getDefault().post(messageEvent);
                dismiss();
                break;
            case R.id.cancle:
                dismiss();
                break;
            default:
                break;
        }
    }

}
