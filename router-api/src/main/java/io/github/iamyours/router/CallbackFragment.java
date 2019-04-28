package io.github.iamyours.router;

import android.app.Fragment;
import android.content.Intent;

public class CallbackFragment extends Fragment {
    private Callback callback;

    public CallbackFragment setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, data);
        }
    }

}
