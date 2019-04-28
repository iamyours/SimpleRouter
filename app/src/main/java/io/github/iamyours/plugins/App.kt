package io.github.iamyours.plugins

import android.app.Application
import io.github.iamyours.router.ARouter

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ARouter.getInstance().init()
    }
}