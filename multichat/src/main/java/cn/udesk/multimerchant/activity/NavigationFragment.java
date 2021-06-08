package cn.udesk.multimerchant.activity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import cn.udesk.multimerchant.R;
import cn.udesk.multimerchant.UdeskMultimerchantSDKManager;
import cn.udesk.multimerchant.adapter.NavigationAdapter;
import cn.udesk.multimerchant.model.UdeskMultimerchantNavigationMode;

/**
 * Created by user on 2018/3/28.
 */

public class NavigationFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private NavigationAdapter navigationAdapter;

    UdeskChatActivity activity;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = null;
        try {
            activity = (UdeskChatActivity) NavigationFragment.this.getActivity();
            rootView = inflater.inflate(R.layout.udesk_multimerchant_navigatiion_fragment,
                    container, false);
            mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rv_navigation_list);
            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mRecyclerView.setLayoutManager(layoutManager);
            mRecyclerView.setItemAnimator(new DefaultItemAnimator());

            navigationAdapter = new NavigationAdapter(getContext());
            mRecyclerView.setAdapter(navigationAdapter);
            navigationAdapter.setOnItemClickListener(new NavigationAdapter.OnRecyclerViewItemClickListener() {
                @Override
                public void onItemClick(View view, UdeskMultimerchantNavigationMode data) {
                    if (UdeskMultimerchantSDKManager.getInstance().getNavigationItemClickCallBack()!= null && activity != null
                            && activity.getPresenter() != null) {
                        UdeskMultimerchantSDKManager.getInstance().getNavigationItemClickCallBack().callBack(activity.getApplicationContext(), activity.getPresenter(), data);
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rootView;
    }
}
