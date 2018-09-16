package cn.gavinliu.notificationbox.ui.detail;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.gavinliu.notificationbox.R;
import cn.gavinliu.notificationbox.model.NotificationInfo;
import cn.gavinliu.notificationbox.utils.CommonUtils;
import cn.gavinliu.notificationbox.widget.BaseListFragment;
import cn.gavinliu.notificationbox.widget.BaseViewHolder;

/**
 * Created by Gavin on 2016/10/11.
 */

public class DetailFragment extends BaseListFragment implements DetailContract.View {

    DetailContract.Presenter mPresenter;

    private String mAppName;
    private String mPackageName;

    public static DetailFragment newInstance(String appName, String packageName) {
        if (appName == null || packageName == null) {
            throw new IllegalArgumentException("appName and packageName can not is null");
        }

        Bundle args = new Bundle();
        args.putString("appName", appName);
        args.putString("packageName", packageName);

        DetailFragment fragment = new DetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mPresenter != null) mPresenter.startLoad(mPackageName);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) mPresenter.unsubscribe();
    }

    @Override
    public void getArguments(Bundle bundle) {
        if (bundle != null) {
            mAppName = bundle.getString("appName");
            mPackageName = bundle.getString("packageName");
        }
    }

    @Override
    public void setupActionBar(ActionBar actionBar) {
        actionBar.setTitle(mAppName);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void showProgress(boolean isShown) {
        if (isShown) {
            showProgressView();
        } else {
            hideProgressView();
        }
    }

    @Override
    public void showEmpty() {
        hideProgressView();
        showEmptyView();
        setEmptyText(getText(R.string.empty_detail));
    }

    @Override
    public void showNotifications(List<NotificationInfo> notifications) {
        mRecyclerView.setAdapter(new Adapter(getContext(), notifications));
    }

    @Override
    public void setPresenter(DetailContract.Presenter presenter) {
        mPresenter = presenter;
    }


    private static class Adapter extends RecyclerView.Adapter<ViewHolder> {

        Context context;
        List<NotificationInfo> notificationList;

        public Adapter(Context context, List<NotificationInfo> notificationList) {
            this.context = context;
            this.notificationList = notificationList;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_detail2, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            NotificationInfo notification = notificationList.get(position);
            holder.title.setText(notification.getTitle());
            holder.text.setText(notification.getText());
            holder.time.setText(CommonUtils.getTimeStr(notification.getTime()));
            int flag=notification.getFlag();
            switch (flag){
                // 完全未匹配，白色，包名匹配但是放行，绿色  包名匹配命中 黄色（淡淡）   异常（红色
                case 1:
                    holder.bg.setBackgroundColor(Color.parseColor("#FFddffdd"));
                    break;
                case 2:
                    holder.bg.setBackgroundColor(Color.parseColor("#FFF9FFC2"));
                    break;
                case 3:
                    holder.bg.setBackgroundColor(Color.parseColor("#FFFFF0C2"));
                    break;
                case -1:
                    holder.bg.setBackgroundColor(Color.parseColor("#FFffcccc"));
                    break;
                case 0:
                    holder.bg.setBackgroundColor(Color.parseColor("#ffffffff"));
            }
        }

        @Override
        public int getItemCount() {
            return notificationList != null ? notificationList.size() : 0;
        }
    }


    private static class ViewHolder extends BaseViewHolder {

        TextView title;
        TextView text;
        TextView time;
        View bg;

        public ViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            text = (TextView) itemView.findViewById(R.id.text);
            time = (TextView) itemView.findViewById(R.id.time);
            bg=(View)itemView.findViewById(R.id.ViewDataItem);
        }
    }

}
