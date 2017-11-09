package cn.udesk.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import cn.udesk.R;


/**
 * author : ${揭军平}
 * time   : 2017/09/17
 * desc   :
 * version: 1.0
 */
public class UdeskLodingDialog extends Dialog {

    private int resId;
    Context context;
    private TextView tv_login_title;
    private ProgressBar progressBar;
    private String titleString;

    public UdeskLodingDialog(Context context, int theme, int resId, String title) {
        super(context, theme);
        this.context = context;
        this.resId = resId;
        this.titleString = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(resId);
        tv_login_title = (TextView) findViewById(R.id.udesk_content_logining);
        progressBar = (ProgressBar) findViewById(R.id.udesk_progress);
        tv_login_title.setText(titleString);
    }

}
