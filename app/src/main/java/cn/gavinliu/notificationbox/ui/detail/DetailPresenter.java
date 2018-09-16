package cn.gavinliu.notificationbox.ui.detail;

import java.util.List;

import cn.gavinliu.notificationbox.model.NotificationInfo;
import cn.gavinliu.notificationbox.utils.DbUtils;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by Gavin on 16-10-17.
 */

public class DetailPresenter implements DetailContract.Presenter {

    DetailContract.View mView;

    private CompositeSubscription mSubscriptions;
    private String query1;
    private String query2;

    public DetailPresenter(DetailContract.View view) {
        mView = view;

        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    public DetailPresenter(DetailContract.View view,String query1,String query2) {
        mView = view;
        this.query1=query1;
        this.query2=query2;
        mSubscriptions = new CompositeSubscription();
        mView.setPresenter(this);
    }

    @Override
    public void start() {

    }

   @Override
/*
    public void startLoad(String packageName) {
        mView.showProgress(false);

        List<NotificationInfo> infos = DbUtils.getNotification(packageName);

        if (infos == null || infos.size() == 0) {
            mView.showEmpty();
        } else {
            mView.showNotifications(infos);
        }
    }

*/

    public void startLoad(String packageName) {
        mView.showProgress(false);

        List<NotificationInfo> infos = DbUtils.getNotification(packageName,query1,query2);

        if (infos == null || infos.size() == 0) {
            mView.showEmpty();
        } else {
            mView.showNotifications(infos);
        }
    }

    @Override
    public void subscribe() {

    }

    @Override
    public void unsubscribe() {
        mSubscriptions.unsubscribe();
    }
}
