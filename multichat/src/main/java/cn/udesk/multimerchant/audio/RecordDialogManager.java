package cn.udesk.multimerchant.audio;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import cn.udesk.multimerchant.R;


public class RecordDialogManager {

    private Context mContext;
    private Dialog mDialog;
    private LayoutInflater mInflater;
    private ImageView mIvRecord;
    private ImageView mIvVoiceLevel;
    private TextView mTvTip;

    public RecordDialogManager(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    public void showDialogRecord() {
        try {
            View view = mInflater.inflate(R.layout.udesk_multimerchant_dialog_audio_record_button, null);
            mDialog = new Dialog(mContext, R.style.udesk_multimerchant_theme_Audio_Record_Button);
            mDialog.setContentView(view);
            mIvRecord = (ImageView) mDialog.findViewById(R.id.iv_record);
            mIvVoiceLevel = (ImageView) mDialog.findViewById(R.id.iv_voice_level);
            mTvTip = (TextView) mDialog.findViewById(R.id.tv_dialog_tip);
            mDialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showRecording() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mIvRecord.setImageResource(R.drawable.udesk_multimerchant_recorder);
                mIvVoiceLevel.setVisibility(View.VISIBLE);
                mTvTip.setText(mContext.getString(R.string.udesk_multimerchant_move_up_cancel));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialogToShort() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mIvRecord.setImageResource(R.drawable.udesk_multimerchant_voice_to_short);
                mIvVoiceLevel.setVisibility(View.GONE);
                mTvTip.setText(mContext.getString(R.string.udesk_multimerchant_record_to_short));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showDialogWantCancel() {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                mIvRecord.setImageResource(R.drawable.udesk_multimerchant_video_cancle);
                mIvVoiceLevel.setVisibility(View.GONE);
                mTvTip.setText(mContext.getString(R.string.udesk_multimerchant_release_cancel));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据音量大小更新 音量图标高度
     *
     * @param level
     */
    public void updateVoiceLevel(int level) {
        try {
            if (mDialog != null && mDialog.isShowing()) {
                int resId = mContext.getResources().getIdentifier("udeskv_" + level,
                        "drawable", mContext.getPackageName());
                mIvVoiceLevel.setImageResource(resId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dismissDialog() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}
