# SimpleRouter
a router plugin for android

# Feature
- simple config,easy to use
- without reflection,batter performance
- simple callback without onActivityResult in activity

# Lastest version
module|router-api|router-plugin
---|---|---
version|[ ![Download](https://api.bintray.com/packages/iamyours/maven/router-api/images/download.svg?version=1.0.1) ](https://bintray.com/iamyours/maven/router-api/1.0.1/link)|[ ![Download](https://api.bintray.com/packages/iamyours/maven/router-plugin/images/download.svg?version=1.0.1) ](https://bintray.com/iamyours/maven/router-plugin/1.0.1/link)

# Quick start
### Configuration
add classpath in yours project build.gradle
``` gradle
classpath 'io.github.iamyours:router-plugin:1.0.1'
```
apply plugin in app module
``` gradle
apply plugin: 'RouterPlugin'
```
add dependency in the module that will use @Route annotation,you can add it in common module.
``` gradle
api 'io.github.iamyours:router-api:1.0.1'
```

### add route annotation
``` kotlin
@Route(path = "/test/activity")
class YourActivity : AppCompatActivity(){
    ...
}
```
### jump with router
``` kotlin
ARouter.getInstance()
       .build("/test/test")
       .navigation(this) { _, resultCode, data ->
            val title = data?.getStringExtra("title")//data can be null,so mark it with ?
       }

```
### for java
``` java
ARouter.getInstance()
        .build("/test/test")
        .navigation(this, new Callback() {
            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {

            }
        });
```
# If Proguard is turn on
add rules in proguard-rules.pro
```
-keep public class io.github.iamyours.router.**{*;}
```





