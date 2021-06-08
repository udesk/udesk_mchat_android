package cn.udesk.multimerchant.demo;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 自定义customer
 */
public class UdeskCustomDialog extends Dialog {

    private EditText editText;
    private TextView okTextView, cancleTextView;
    private TextView title;

    public UdeskCustomDialog(Context context) {
        super(context, R.style.add_dialog);
        setCustomDialog();
    }

    private void setCustomDialog() {
        View mView = LayoutInflater.from(getContext()).inflate(R.layout.udesk_dialog_normal_layout, null);
        title = (TextView) mView.findViewById(R.id.udeskdemo_customer_title);
        editText = (EditText) mView.findViewById(R.id.udeskdemo_custome_edit);
        okTextView = (TextView) mView.findViewById(R.id.udeskdemo_ok);
        cancleTextView = (TextView) mView.findViewById(R.id.udeskdemo_cancle);
        super.setContentView(mView);
    }

    public View getEditText() {
        editText.setVisibility(View.VISIBLE);
        return editText;
    }

    //设置确定监听事件
    public void setOkTextViewOnclick(View.OnClickListener listener) {
        okTextView.setOnClickListener(listener);
    }

    //设置取消监听事件
    public void setCancleTextViewOnclick(View.OnClickListener listener) {
        cancleTextView.setOnClickListener(listener);
    }

    public void setDialogTitle(String name) {
        if (title != null) {
            title.setText(name);
        }
    }


}
