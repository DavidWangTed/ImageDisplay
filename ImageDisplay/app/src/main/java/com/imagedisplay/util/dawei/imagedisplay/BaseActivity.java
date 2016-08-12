package com.imagedisplay.util.dawei.imagedisplay;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;


import java.util.List;


/**
 * Activity的基类
 * Created by wulianghuan on 2016/03/18.
 */
public abstract class BaseActivity extends ToolBarActivity implements View.OnClickListener, InterfaceBaseActivity, EasyPermissions.PermissionCallbacks {
    protected final String TAG = this.getClass().getSimpleName();

    public Context mContext;

    private LoadingLayout mLoadingLayout = null;

    private EmptyLayout mEmptyLayout;

    private long mLoadingStartTime = 0; // 加载动画最近一次开始的时间

    private long mMinLoadingShowTime = 800; // 加载动画至少要显示一秒

    public boolean mShouldNotInitSystemBar = false; // 该Activity不需要设置沉浸式模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            MetadataUtil.getInstance().onRestoreInstanceState(savedInstanceState);
        }
        super.onCreate(savedInstanceState);
        mContext = this;
//        initSystemBar();
        ActivityStack.addActivity(this);
        setRootView(); // 必须放在annotate之前调用
        ButterKnife.bind(this);
        initWidget();
        initData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        MetadataUtil.getInstance().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

//    private void initSystemBar() {
//        // 不需要设置沉浸式
//        if (mShouldNotInitSystemBar) {
//            return;
//        }
//        ImmerseUtil.initSystemBar(this);
//    }

    /**
     * 在{@link Activity#setContentView}之后调用
     *
     * @param activity
     *         要实现的沉浸式状态栏的Activity
     * @param titleViewGroup
     *         头部控件的ViewGroup,若为null,整个界面将和状态栏重叠
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void setImmerseLayout(Activity activity, View titleViewGroup) {
        if (activity == null)
            return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            if (titleViewGroup == null)
                return;
            // 设置头部控件ViewGroup的PaddingTop,防止界面与状态栏重叠
            int statusBarHeight = ImmerseUtil.getStatusBarHeight(activity);
            titleViewGroup.setPadding(0, statusBarHeight, 0, 0);
        }
    }

    /**
     * 初始化控件
     */
    @Override
    public void initWidget() {

    }

    /**
     * 初始化数据
     */
    @Override
    public void initData() {

    }

    /**
     * 控件点击事件
     *
     * @param v
     */
    @Override
    public void widgetClick(View v) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!EasyPermissions.hasPermissions(this, PermissionUtils.REQUIRED_PERMISSION)) {
            if (this instanceof WelcomeActivity) {
                return;
            }
            ActivityStack.popAllActivity();
            IntentBuilder.create(this).startActivity(WelcomeActivity.class);
        }
        MobclickAgent.onPageStart(TAG);
        MobclickAgent.onResume(mContext);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!AppUtil.isApplicationBrought(this)) {
            GetuiUtils.sendHeartbeat(this);
        }
    }

    @Override
    public void onClick(View view) {
        widgetClick(view);
        int i = view.getId();
        if (i == R.id.root) {
            reloadData();
        }
    }

    /**
     * 重新加载界面数据
     */
    protected void reloadData() {
        hideErrorView();
    }

    public void showMyToast(String msg) {
        ToastUtil.showInBottom(this, msg);
    }

    public void showMyToast(int stringID) {
        ToastUtil.showInCenter(this, stringID);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());
        if (!EasyPermissions.checkDeniedPermissionsNeverAskAgain(this,
                "需要同意这些权限才能使用开黑大师，打开设置页面去设置.",
                R.string.setting, R.string.cancel, perms)) {
            if (this instanceof WelcomeActivity) {
                finish();
            }
        }
    }

//    /**
//     * 执行所需权限任务
//     *
//     * @param taskCode
//     *         任务代号
//     * @param perms
//     *         执行任务所需的权限
//     */
//    public void executeTask(int taskCode, String... perms) {
//        if (EasyPermissions.hasPermissions(this, perms)) {
//            permissionsListener.onPermissionsGranted(taskCode);
//        } else {
//            EasyPermissions.requestPermissions(this, "必须同意这些权限才能执行此操作",
//                    taskCode, perms);
//        }
//    }


    /**
     * 处理请求权限结果
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * 加载动画界面
     */
    public void showLoadingView() {
        if (!initLoadingLayout()) {
            return;
        }
        mLoadingStartTime = System.currentTimeMillis();
        mLoadingLayout.setVisibility(View.VISIBLE);
    }

    /**
     * 显示业务主界面
     */
    public void hideLoadingView() {
        if (!initLoadingLayout()) {
            return;
        }
        long nowTime = System.currentTimeMillis();
        long timeInterval = nowTime - mLoadingStartTime;
        if (timeInterval >= mMinLoadingShowTime) {
            mLoadingLayout.setVisibility(View.GONE);
        } else {
            mLoadingLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mLoadingLayout.setVisibility(View.GONE);
                }
            }, (mMinLoadingShowTime - timeInterval));
        }
    }

    /**
     * 立即显示业务主界面
     */
    public void hideLoadingViewImmediate() {
        if (!initLoadingLayout()) {
            return;
        }
        mLoadingLayout.setVisibility(View.GONE);
    }

    /**
     * 初始化进度条
     */
    public boolean initLoadingLayout() {
        if (mLoadingLayout == null) {
            FrameLayout frameLayout = (FrameLayout) findViewById(android.R.id.content);
            if (frameLayout == null) {
                return false;
            }
            mLoadingLayout = new LoadingLayout(this);
            frameLayout.addView(mLoadingLayout);
        }
        return true;
    }

    /**
     * 请求失败，显示请求错误界面
     */
    private void showNetErrorView() {
        if (!initErrorLayout()) {
            return;
        }
        mEmptyLayout.showNetErrorView();
    }

    /**
     * 显示请求成功没有数据的界面
     */
    public void showErrorView() {
        if (!initErrorLayout()) {
            return;
        }
        if (!NetworkUtil.isNetworkConnected(this)) {
            // 网络未连接，导致的无数据
            showNetErrorView();
            return;
        }
        mEmptyLayout.showNoDataView();
    }

    /**
     * 设置隐藏emptyLayout
     */
    public void hideErrorView() {
        if (mEmptyLayout == null) {
            return;
        }
        mEmptyLayout.hideEmptyView();
    }

    /**
     * 初始化异常处理界面
     */
    public boolean initErrorLayout() {
        if (mEmptyLayout == null) {
            FrameLayout frameLayout = (FrameLayout) findViewById(android.R.id.content);
            if (frameLayout == null) {
                return false;
            }
//            int topMargin = ImmerseUtil.getStatusBarHeight(this) + getResources().getDimensionPixelOffset(R.dimen.actionbar_height);
            int topMargin = getResources().getDimensionPixelOffset(R.dimen.actionbar_height);
            mEmptyLayout = new EmptyLayout(this);
            mEmptyLayout.setOnLayoutClickListener(this);
            frameLayout.addView(mEmptyLayout);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mEmptyLayout.getLayoutParams();
            layoutParams.topMargin = topMargin;
            mEmptyLayout.setLayoutParams(layoutParams);
        }
        return true;
    }

    public void showSmallErrorView() {
        if (!initSmallErrorLayout()) {
            return;
        }
        if (!NetworkUtil.isNetworkConnected(this)) {
            // 网络未连接，导致的无数据
            showNetErrorView();
            return;
        }
        mEmptyLayout.showNoDataView();
    }

    /**
     * 非全屏显示的Frgment，不用控制margin
     * 初始化异常处理界面
     */
    public boolean initSmallErrorLayout() {
        if (mEmptyLayout == null) {
            FrameLayout frameLayout = (FrameLayout) findViewById(android.R.id.content);
            if (frameLayout == null) {
                return false;
            }
            int topMargin = ImmerseUtil.getStatusBarHeight(this);
            mEmptyLayout = new EmptyLayout(this);
            mEmptyLayout.setOnLayoutClickListener(this);
            frameLayout.addView(mEmptyLayout);
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) mEmptyLayout.getLayoutParams();
            layoutParams.topMargin = topMargin;
            mEmptyLayout.setLayoutParams(layoutParams);
        }
        return true;
    }

    public void setEmptyView(View emptyView) {
        if (!initErrorLayout()) {
            return;
        }
        mEmptyLayout.setContentView(emptyView);
    }

    public void switchNetErrorView() {
        mEmptyLayout.showNetError();
    }

    protected BaseActivity getInstance(boolean isShowLoading) {
        return isShowLoading ? this : null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(TAG);
        MobclickAgent.onPause(mContext);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消TAG对应的volley请求
        if (EasyPermissions.hasPermissions(this, PermissionUtils.REQUIRED_PERMISSION)) {
            CommonDataLoader.getInstance(MyApplication.getInstance().getApplicationContext()).cancelRequest(TAG);
        }
        ActivityStack.popActivity(this);
        ButterKnife.unbind(this);
    }
}