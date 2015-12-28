![demo.gif](http://upload-images.jianshu.io/upload_images/1216032-9cc1f596c585d43d.gif?imageMogr2/auto-orient/strip)
注意：本项目还有一个小坑。第一次进去的时候有时候输入框没有得到焦点，没有弹出软键盘，所以不能更好的量取软键盘高度，给了一个默认值787 软键盘默认高度。所以可以忽略不计，一旦弹出了软键盘，这个高度就被记录下来了，存在本地，以便下一次用。
这几天没事，想到之前做im聊天的时候，表情输入和键盘之间的切换体验有些问题，看了微信的，觉得真好，就有了想描摹一下的心思，所有有了这个demo。站在巨人的肩膀上，我们才能走得更远。
一些配置,导入相关的库：
```
dependencies {

compile fileTree(dir: 'libs', include: ['*.jar'])

testCompile 'junit:junit:4.12'

compile project(':library')

compile 'com.android.support:appcompat-v7:23.1.1'

compile 'com.android.support:design:23.1.1'

compile 'com.jakewharton:butterknife:7.0.1'//butterknife注解框架

compile 'com.android.support:support-v4:23.1.1'

}
```
键盘弹出方案
```
android:windowSoftInputMode="stateVisible|adjustResize"
```
提一下emoji表情输入，其实很简单。
```
调用两个接口
EmojiconGridFragment.OnEmojiconClickedListener//点击表情接口
EmojiconsFragment.OnEmojiconBackspaceClickedListener//删除表情接口

然后实现一下方法，emoji就好了
@Override 
 public void onEmojiconBackspaceClicked(View v) {
 EmojiconsFragment.backspace(emojiEditTextView);
 } 
 
 
 @Override 
 public void onEmojiconClicked(Emojicon emojicon) {
 EmojiconsFragment.input(emojiEditTextView, emojicon);
 } 
```
在运用前，要知道这个公式：
KeyBoard_H = Screen_H - StatusBar_H - AppRect_H
软键盘高度 = 分辨率高 - 状态栏高 - 应用可视高
于是有了这个方法
```
 public static int getKeyboardHeight(Activity paramActivity) {
        int height = SystemUtils.getScreenHeight(paramActivity) - SystemUtils.getStatusBarHeight(paramActivity)
                - SystemUtils.getAppHeight(paramActivity);
        if (height == 0) {
            height = SharedPreferencesUtils.getIntShareData("KeyboardHeight", 787);//787为默认软键盘高度 基本差不离
        }else{
            SharedPreferencesUtils.putIntShareData("KeyboardHeight", height);
        }
        return height;
    }
```

主页布局文件
```
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:emojicon="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    app:layout_behavior="@string/appbar_scrolling_view_behavior"
    tools:context="com.wobiancao.keyboarddemo.MainActivity"
    tools:showIn="@layout/activity_main">

    <LinearLayout
        android:id="@+id/emojicons_container"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:orientation="vertical">

        <com.rockerhieu.emojicon.EmojiconEditText
            android:id="@+id/emojicons_edit"
            android:layout_width="match_parent"
            android:layout_height="0dp"
            android:layout_weight="1"
            android:gravity="left|top"
            android:padding="8dp"
            android:textSize="18sp"
            emojicon:emojiconSize="18sp" />

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginTop="10dp"
            android:background="?attr/colorPrimary"
            android:gravity="center">

            <ImageView
                android:id="@+id/emojicons_icon"
                android:layout_width="40dip"
                android:layout_height="40dip"
                android:padding="8dip"
                android:src="@mipmap/ic_emoticon" />
        </LinearLayout>
    </LinearLayout>

    <RelativeLayout
        android:id="@+id/emojicons_layout"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:visibility="gone"></RelativeLayout>
</LinearLayout>

```
剩下略提一下软键盘和输入框的切换
```
 @OnClick({R.id.emojicons_icon, R.id.emojicons_edit})
    void onClick(View view) {
        switch (view.getId()){
            case R.id.emojicons_icon://点击表情图标,如果表情显示，隐藏表情，打开软键盘。反之，显示表情，隐藏键盘
                if (emojiconsLayout.isShown()) {
                    hideEmotionView(true);
                } else {
                    showEmotionView(SystemUtils.isKeyBoardShow(this));
                }
                break;
            case R.id.emojicons_edit://点击输入框，打开软键盘，隐藏表情
                hideEmotionView(true);
                break;
            default:
                break;
        }

    }




/**
     * 隐藏emoji
     **/
    private void hideEmotionView(boolean showKeyBoard) {
        if (emojiconsLayout.isShown()) {
            if (showKeyBoard) {
                LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) emojiconsContainer.getLayoutParams();
                localLayoutParams.height = emojiconsLayout.getTop();
                localLayoutParams.weight = 0.0F;
                emojiconsLayout.setVisibility(View.GONE);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                SystemUtils.showKeyBoard(editEmojicon);
                editEmojicon.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        unlockContainerHeightDelayed();
                    }

                }, 200L);
            } else {
                emojiconsLayout.setVisibility(View.GONE);
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
                unlockContainerHeightDelayed();
            }
        }
    }

    private void showEmotionView(boolean showAnimation) {
        if (showAnimation) {
            transitioner.setDuration(200);
        } else {
            transitioner.setDuration(0);
        }
        emotionHeight = SystemUtils.getKeyboardHeight(this);
        SystemUtils.hideSoftInput(editEmojicon);
        emojiconsLayout.getLayoutParams().height = emotionHeight;
        emojiconsLayout.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //在5.0有navigationbar的手机，高度高了一个statusBar
        int lockHeight = SystemUtils.getAppContentHeight(this);
        lockContainerHeight(lockHeight);
    }
```
然后demo里面用到了开源emoji项目，贴上开源地址，表示感谢
 emoji表情开源：https://github.com/rockerhieu/emojicon
本demo开源地址 ：https://github.com/a12a15a05/KeyBoardDemo

demo apk地址：


![demov.png](http://upload-images.jianshu.io/upload_images/1216032-229bbebe3f45f596.png?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)

有bug或问题，欢迎探讨，谢谢
