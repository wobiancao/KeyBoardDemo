package com.wobiancao.keyboarddemo;

import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.rockerhieu.emojicon.EmojiconEditText;
import com.rockerhieu.emojicon.EmojiconGridFragment;
import com.rockerhieu.emojicon.EmojiconsFragment;
import com.rockerhieu.emojicon.emoji.Emojicon;
import com.wobiancao.keyboarddemo.utils.SystemBarUtils;
import com.wobiancao.keyboarddemo.utils.SystemUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements EmojiconGridFragment.OnEmojiconClickedListener, EmojiconsFragment.OnEmojiconBackspaceClickedListener {
    @Bind(R.id.emojicons_edit)
    EmojiconEditText editEmojicon;
    @Bind(R.id.emojicons_container)
    LinearLayout emojiconsContainer;
    @Bind(R.id.emojicons_layout)
    RelativeLayout emojiconsLayout;
    @Bind(R.id.emojicons_icon)
    ImageView emojiconsIcon;
    @Bind(R.id.toolbar)
    Toolbar toolbar;
    @Bind(R.id.content_lay)
    CoordinatorLayout contentLay;
    @OnClick({R.id.emojicons_icon, R.id.emojicons_edit})
    void onClick(View view) {
        switch (view.getId()){
            case R.id.emojicons_icon:
                if (emojiconsLayout.isShown()) {
                    hideEmotionView(true);
                } else {
                    showEmotionView(SystemUtils.isKeyBoardShow(this));
                }
                break;
            case R.id.emojicons_edit:
                hideEmotionView(true);
                break;
            default:
                break;
        }

    }
    private final LayoutTransition transitioner = new LayoutTransition();//键盘和表情切换
    private int emotionHeight;
    private EmojiconsFragment emojiconsFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        /**上下平移动画**/
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "translationY",
                SystemUtils.getScreenHeight(this), emotionHeight).
                setDuration(transitioner.getDuration(LayoutTransition.APPEARING));
        transitioner.setAnimator(LayoutTransition.APPEARING, animIn);
        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "translationY",
                emotionHeight,
                SystemUtils.getScreenHeight(this)).
                setDuration(transitioner.getDuration(LayoutTransition.DISAPPEARING));
        transitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        contentLay.setLayoutTransition(transitioner);
        /**安全判断 有些情况会出现异常**/
        if (savedInstanceState == null) {
            emojiconsFragment = EmojiconsFragment.newInstance(false);
            getSupportFragmentManager().beginTransaction().add(R.id.emojicons_layout, emojiconsFragment, "EmotionFragemnt").commit();
        }else {
            emojiconsFragment = (EmojiconsFragment) getSupportFragmentManager().findFragmentByTag("EmotionFragemnt");
        }
        /**先弹出软键盘**/
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        SystemUtils.showKeyBoard(editEmojicon);
        editEmojicon.postDelayed(new Runnable() {

            @Override
            public void run() {
                unlockContainerHeightDelayed();
            }

        }, 200L);

    }

    @Override
    public void onEmojiconBackspaceClicked(View v) {
        EmojiconsFragment.backspace(editEmojicon);
    }

    @Override
    public void onEmojiconClicked(Emojicon emojicon) {
        EmojiconsFragment.input(editEmojicon, emojicon);
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

        int statusBarHeight = SystemBarUtils.getStatusBarHeight(this);
        emotionHeight = SystemUtils.getKeyboardHeight(this);

        SystemUtils.hideSoftInput(editEmojicon);
        emojiconsLayout.getLayoutParams().height = emotionHeight;
        emojiconsLayout.setVisibility(View.VISIBLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //在5.0有navigationbar的手机，高度高了一个statusBar
        int lockHeight = SystemUtils.getAppContentHeight(this);
//            lockHeight = lockHeight - statusBarHeight;
        lockContainerHeight(lockHeight);
    }

    private void lockContainerHeight(int paramInt) {
        LinearLayout.LayoutParams localLayoutParams = (LinearLayout.LayoutParams) emojiconsContainer.getLayoutParams();
        localLayoutParams.height = paramInt;
        localLayoutParams.weight = 0.0F;
    }

    public void unlockContainerHeightDelayed() {
        ((LinearLayout.LayoutParams) emojiconsContainer.getLayoutParams()).weight = 1.0F;
    }

}
