

# Android框架，集成大多数可能会有用的功能。用于快速搭建Android项目



## Lib引入方式（一），复制代码

1. **新建项目**

2. **在项目的根目录下创建config.gradle文件，文件里的内容见最后**

3. **在项目的根目录下创建baselib目录，将所有代码复制到baselib目录**

4. **在新建项目根目录的`build.gradle`的第一行加入** 

   > apply from: "config.gradle"

   并添加`jitpack`仓库地址

   > maven { url 'https://jitpack.io' }

5. **修改app下的build.gradle，添加如下代码**

   **5.1 添加java编译版本**

```groovy
compileOptions {
    sourceCompatibility = '1.8'
    targetCompatibility = '1.8'
}
```

​		**5.2 将默认的`defaultConfig`替换成下面**

```groovy
defaultConfig {
    applicationId "your package name"
    minSdkVersion rootProject.ext.android["minSdkVersion"]
    targetSdkVersion rootProject.ext.android["targetSdkVersion"]
    versionCode rootProject.ext.android["appVersionCode"]
    versionName rootProject.ext.android["appVersionName"]
}
```

​		**5.3 同时将默认的dependencies替换成如下**

```
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar'])
    implementation project(path: ':baselib')

    // annotationProcessor 在每个module中都必须配置才起作用
    annotationProcessor rootProject.ext.dependencies["butterknife-compiler"]
    annotationProcessor rootProject.ext.dependencies["glide-compiler"]
}
```

6. **修改app的主题样式**

```
<style name="AppTheme" parent="BaseAppTheme">
```

**ps:如果报如下错误，肯定是忘记了上面的第4步**

`ERROR: Cannot get property 'version' on extra properties extension as it does not exist`



7. **将Application继承至BaseApplication**



## Lib引入方式（二），gradle引入

与上面第一种复制lib引入方式相对有两点不同

1. 不需要第3步的 **在app同目录下新建baselib目录，将所有代码复制到baselib目录**

2. 将5.3中的

   ```groovy
   implementation project(path: ':baselib')
   ```

   替换成

   ```groovy
   implementation 'com.github.duxl:baselib:v1.0_beta'
   ```

   



## BaseActivity API

### 页面基础通用

``` java
// 隐藏状态栏，默认显示
public void hideStateBar()
```

``` java
// 显示状态栏，默认显示
public void showStateBar()
```

```java
// 设置状态栏颜色
public void setStateBarColor(int color)
```

```java
// 设置状态栏背景
public void setStateBarResource(int resId)
```

*ActionBar与StateBar有如上相似的api，这里就不再累述*

```java
// 设置标题
public void setTitle(int titleId)
public void setTitle(CharSequence title)
```

```java
// 返回按钮点击事件，可重写，默认是关闭Activity
protected void onClickActionBack(View v)
```

```java
// 关闭按钮点击事件，可重写，默认是关闭Activity。通常WebView页面有关闭按钮。
// 默认隐藏，需要显示请调用getActionBarView().getIvClose().setVisibility(View.VISIBLE);
protected void onClickActionClose(View v)
```

```java
// 设置右边文字点击事件
// 默认隐藏，需要显示请调用getActionBarView().getTvRight().setVisibility(View.VISIBLE);
protected void onClickActionTvRight(View v)
```

```java
// 设置右边图标点击事件
// 默认隐藏，需要显示请调用getActionBarView().getTvRight().setVisibility(View.VISIBLE);
protected void onClickActionIvRight(View v)
```

```java
// 重写此方法可单独修改页面的StatusView，全局配置可在app中重写global_status_view_config.xml
protected IStatusView initStatusView()
```

```java
// 设置状态栏字体和图标模式：深色和浅色
public void setStateBarDarkMode()
public void setStateBarLightMode()
```

### 页面刷新相关

```java
// 设置页面加载更多和下拉刷新是否可用
public void setEnableLoadMore(boolean enabled)
public void setEnableRefresh(boolean enabled)
```

```java
// 完成刷新/加载更多
public RefreshLayout finishRefresh()
public RefreshLayout finishLoadMore()
```

```java
// 完成加载并标记没有更多数据
public RefreshLayout finishLoadMoreWithNoMoreData()
```

```java
// 恢复没有更多数据的原始状态
public RefreshLayout resetNoMoreData()
```

```java
// 设置刷新、加载更多、点击重试 等监听
public void setOnLoadListener(OnLoadListener listener)
```



### http接口请求使用步骤

1. 重写Application的getGlobalHttpConfig()，配置全局的baseUrl、log处理器、网络监测处理

2. 定义interface接口，用于描述接口地址和参数信息，示例如下

   ```java
   @GET("app/mock/258579/test_list_data")
   Observable<Root<List<String>>> getList(
           @Query("pageNum") int pageNum,
           @Query("pageSize") int pageSize
   );
   ```

3. 使用RetrofitManager发起请求，示例如下

   ```java
   private void loadData() {
           RetrofitManager
                   .getInstance()
                   .create(HttpService.class)
                   .getList(pageNum, 20)
                   .compose(new LifecycleTransformer<Root<List<String>>>(this))
                   // 如果刷新和状态view都是页面，这里可以传Activity.this作为参数，第一个参数adapter是必须的，后面两个是可选的
                   // 普通接口可以用 BaseHttpObserver
                   //.subscribe(new RVHttpObserver<Root<List<String>>, String>(mAdapter, this, this) {
                   .subscribe(new RVHttpObserver<Root<List<String>>, String>(mAdapter, mSmartRecyclerView, mSmartRecyclerView) {
                       @Override
                       public List<String> getListData(Root<List<String>> root) {
                           return root.data;
                       }
   
                       @Override
                       public boolean isFirstPage() {
                           return pageNum == 1;
                       }
   
                       @Override
                       public boolean hasMoreData(Root<List<String>> root) {
                           return pageNum < 3;
                       }
   
                       @Override
                       public void onError(int code, String msg, Root<List<String>> root) {
                           if(code == HttpExceptionReasons.CONNECT_NO.getCode()) {
                               // 这里可以特殊处理，比如弹框去设置打开wifi等
                           } else {
                               super.onError(code, msg, root);
                           }
                       }
                   });
       }
   ```

4. 





## BaseFragment API

BaseActivity 的 Api 在 BaseFragment中都有对应的，这里不再累述



## config.gradle文件内容如下

```groovy
ext {

    android = [
            compileSdkVersion: 29,
            minSdkVersion    : 19,
            targetSdkVersion : 29,
            appVersionCode   : 1,
            appVersionName   : "1.0.0"
    ]

    version = [
            androidxVersion      : "1.3.0-alpha01",
            recyclerview         : "1.2.0-alpha05",
            constraintlayout     : "2.0.0-rc1",
            butterknifeSdkVersion: "10.2.3",
            smartrefresh         : "2.0.1",
            rxjava               : "3.0.5",
            rxandroid            : "3.0.0",
            rxlifecycle          : "4.0.0",
            retrofitSdkVersion   : "2.9.0",
            glide                : "4.11.0",
    ]

    dependencies = [
            "appcompat"                           : "androidx.appcompat:appcompat:${version["androidxVersion"]}",
            "constraintlayout"                    : "androidx.constraintlayout:constraintlayout:${version["constraintlayout"]}",
            "material"                              : "com.google.android.material:material:${version["androidxVersion"]}",
            "recyclerview"                        : "androidx.recyclerview:recyclerview:${version["recyclerview"]}",
            "baseviewadapter"                     : "com.github.CymChad:BaseRecyclerViewAdapterHelper:3.0.4",
            "flexbox"                             : "com.google.android:flexbox:1.0.0",
            "butterknife"                         : "com.jakewharton:butterknife:${version["butterknifeSdkVersion"]}",
            "butterknife-compiler"                : "com.jakewharton:butterknife-compiler:${version["butterknifeSdkVersion"]}",
            // 下拉刷新
            "smart-refreshrefresh-kernel"         : "com.scwang.smart:refresh-layout-kernel:${version["smartrefresh"]}", // 核心必须依赖
            "smart-refreshrefresh-header-classics": "com.scwang.smart:refresh-header-classics:${version["smartrefresh"]}", // 经典刷新头
            // rx
            "rxjava"                              : "io.reactivex.rxjava3:rxjava:${version["rxjava"]}",
            "rxandroid"                           : "io.reactivex.rxjava3:rxandroid:${version["rxandroid"]}",
            "rxlifecycle"                         : "com.trello.rxlifecycle4:rxlifecycle:${version["rxlifecycle"]}",
            "rxlifecycle-android"                 : "com.trello.rxlifecycle4:rxlifecycle-android:${version["rxlifecycle"]}",
            "rxlifecycle-components"              : "com.trello.rxlifecycle4:rxlifecycle-components:${version["rxlifecycle"]}",
            // Retrofit
            "retrofit"                            : "com.squareup.retrofit2:retrofit:${version["retrofitSdkVersion"]}",
            "retrofit-converter-gson"             : "com.squareup.retrofit2:converter-gson:${version["retrofitSdkVersion"]}",
            "retrofit-adapter-rxjava3"            : "com.squareup.retrofit2:adapter-rxjava3:${version["retrofitSdkVersion"]}",
            "retrofit-url-manager"                : "me.jessyan:retrofit-url-manager:1.4.0",
            // RxPermissions
            "rxpermissions"                       : "com.github.tbruyelle:rxpermissions:0.12",
            // 图片选择框架
            "matisse"                             : "com.zhihu.android:matisse:0.5.3-beta3",
            // glide
            "glide"                               : "com.github.bumptech.glide:glide:${version["glide"]}",
            "glide-compiler"                      : "com.github.bumptech.glide:compiler:${version["glide"]}",
    ]
}
```

