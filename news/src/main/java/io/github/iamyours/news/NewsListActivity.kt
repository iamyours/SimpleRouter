package io.github.iamyours.news

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import io.github.iamyours.router.annotation.Route
import kotlinx.android.synthetic.main.activity_news_list.*

@Route(path = "/news/news_list")
class NewsListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_list)
        val title = intent.getStringExtra("title")
        tv_test.text = title
        tv_test.setOnClickListener {
            val intent = Intent()
            intent.putExtra("name","张三")
            setResult(1000,intent)
            finish()
        }
    }
}