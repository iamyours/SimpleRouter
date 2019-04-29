package io.github.iamyours.plugins;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import io.github.iamyours.router.ARouter;
import io.github.iamyours.router.Callback;

public class TestActivity extends AppCompatActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARouter.getInstance().build("/app/main")
                .navigation(this, new Callback() {
                    @Override
                    public void onActivityResult(int requestCode, int resultCode, Intent data) {

                    }
                });
    }
}
