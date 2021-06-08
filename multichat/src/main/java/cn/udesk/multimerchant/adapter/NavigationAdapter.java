package cn.udesk.multimerchant.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import cn.udesk.multimerchant.R;
import cn.udesk.multimerchant.UdeskMultimerchantSDKManager;
import cn.udesk.multimerchant.model.UdeskMultimerchantNavigationMode;

/**
 * Created by user on 2018/3/28.
 */

public class NavigationAdapter extends RecyclerView.Adapter<NavigationAdapter.NavigationViewHolder> implements View.OnClickListener {

    private Context mContext;
    List<UdeskMultimerchantNavigationMode> navigationModes = new ArrayList<UdeskMultimerchantNavigationMode>();
    private OnRecyclerViewItemClickListener mOnItemClickListener = null;

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(View view, UdeskMultimerchantNavigationMode data);
    }

    public void setOnItemClickListener(OnRecyclerViewItemClickListener listener) {
        this.mOnItemClickListener = listener;
    }

    public NavigationAdapter(Context context) {
        this.mContext = context;
        if (UdeskMultimerchantSDKManager.getInstance().getNavigationModes() != null){
            navigationModes = UdeskMultimerchantSDKManager.getInstance().getNavigationModes() ;
        }

    }

    @Override
    public void onClick(View view) {
        if (mOnItemClickListener != null) {
            //注意这里使用getTag方法获取数据
            mOnItemClickListener.onItemClick(view, (UdeskMultimerchantNavigationMode) view.getTag());
        }
    }

    @Override
    public NavigationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(mContext).inflate(R.layout.udesk_multimerchant_text_view, parent, false);
        view.setOnClickListener(this);
        return new NavigationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NavigationViewHolder holder, int position) {
        UdeskMultimerchantNavigationMode navigationMode = navigationModes.get(position);
        if (navigationMode != null) {
            holder.itemView.setTag(navigationMode);
            holder.name.setText(navigationMode.getName());
        }
    }

    @Override
    public int getItemCount() {
        return navigationModes.size();
    }

    public static class NavigationViewHolder extends RecyclerView.ViewHolder {

        private TextView name;

        public NavigationViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.item_name);
        }

    }
}
