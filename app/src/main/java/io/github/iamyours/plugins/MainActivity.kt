package io.github.iamyours.plugins

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.github.iamyours.router.ARouter
import io.github.iamyours.router.annotation.Route
import kotlinx.android.synthetic.main.activity_main.*

@Route(path = "/app/main")
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv1.setOnClickListener {
            ARouter.getInstance().build("/news/news_list")
                    .withString("title", "from main")
                    .navigation(this, 1) { _, resultCode, data ->
                        Log.i("test", "resultCode:$resultCode,data:$data")
                        tv1.text = data.getStringExtra("name")
                    }
        }
    }

}
