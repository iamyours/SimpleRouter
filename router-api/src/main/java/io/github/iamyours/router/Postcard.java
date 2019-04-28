package io.github.iamyours.router;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

public class Postcard {
    private String activityName;
    private Bundle mBundle;

    public Postcard(String activityName) {
        this.activityName = activityName;
        mBundle = new Bundle();
    }

    public Postcard withString(String key, String value) {
        mBundle.putString(key, value);
        return this;
    }

    public Postcard withInt(String key, int value) {
        mBundle.putInt(key, value);
        return this;
    }

    public Postcard withFloat(String key, float value) {
        mBundle.putFloat(key, value);
        return this;
    }

    public Postcard withLong(String key, long value) {
        mBundle.putLong(key, value);
        return this;
    }

    public Postcard withParcelable(String key, Parcelable value) {
        mBundle.putParcelable(key, value);
        return this;
    }

    public Postcard with(Bundle bundle) {
        if (null != bundle) {
            mBundle = bundle;
        }
        return this;
    }

    public void navigation(Context context) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context.getPackageName(), activityName));
        intent.putExtras(mBundle);
        context.startActivity(intent);
    }

    public void navigation(Activity context, int requestCode) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(context.getPackageName(), activityName));
        intent.putExtras(mBundle);
        context.startActivityForResult(intent, requestCode);
    }

    public void navigation(Context context, int requestCode, Callback callback) {
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(context.getPackageName(), activityName));
            intent.putExtras(mBundle);
            getFragment(activity)
                    .setCallback(callback)
                    .startActivityForResult(intent, requestCode);
        }
    }

    public void navigation(Context context, Callback callback) {
        navigation(context, 10, callback);
    }

    private static final String TAG = "io.github.iamyours.router.Postcard";

    private CallbackFragment getFragment(Activity activity) {
        CallbackFragment fragment = (CallbackFragment) activity.getFragmentManager().findFragmentByTag(TAG);
        if (fragment == null) {
            fragment = new CallbackFragment();
            FragmentManager fm = activity.getFragmentManager();
            fm.beginTransaction().add(fragment, TAG)
                    .commitAllowingStateLoss();
            fm.executePendingTransactions();
        }
        return fragment;
    }


}
