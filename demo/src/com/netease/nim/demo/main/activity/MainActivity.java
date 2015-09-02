package com.netease.nim.demo.main.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.avchat.AVChatProfile;
import com.netease.nim.demo.avchat.activity.AVChatActivity;
import com.netease.nim.demo.contact.activity.AddFriendActivity;
import com.netease.nim.demo.contact.cache.ContactDataCache;
import com.netease.nim.demo.contact.protocol.ContactHttpClient;
import com.netease.nim.demo.database.DatabaseManager;
import com.netease.nim.demo.login.LoginActivity;
import com.netease.nim.demo.main.fragment.HomeFragment;
import com.netease.nim.demo.main.model.Extras;
import com.netease.nim.demo.session.SessionHelper;
import com.netease.nim.demo.team.activity.AdvancedTeamCreateActivity;
import com.netease.nim.demo.team.activity.AdvancedTeamSearchActivity;
import com.netease.nim.demo.team.activity.NormalTeamInfoActivity;
import com.netease.nim.uikit.common.activity.TActionBarActivity;
import com.netease.nim.uikit.common.cache.BitmapCache;
import com.netease.nim.uikit.session.emoji.StickerManager;
import com.netease.nim.uikit.team.TeamDataCache;
import com.netease.nimlib.sdk.NimIntent;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * 主界面
 * <p/>
 * Created by huangjun on 2015/3/25.
 */
public class MainActivity extends TActionBarActivity {

    private static final String EXTRA_APP_QUIT = "APP_QUIT";
    private final String TAG = "MainActivity";

    private HomeFragment mainFragment;

    public static void start(Context context) {
        start(context, null);
    }

    public static void start(Context context, Intent extras) {
        Intent intent = new Intent();
        intent.setClass(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if (extras != null) {
            intent.putExtras(extras);
        }
        context.startActivity(intent);
    }

    // 注销
    public static void logout(Context context, boolean quit) {
        Intent extra = new Intent();
        extra.putExtra(EXTRA_APP_QUIT, quit);
        start(context, extra);
    }

    @Override
    protected boolean displayHomeAsUpEnabled() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_tab);
        setTitle(R.string.app_name);
        onParseIntent();

        // 准备必要的数据
        prepareData();

        // observers
        ContactDataCache.getInstance().init();
        TeamDataCache.getInstance().init();

        // 加载主页面
        new Handler(MainActivity.this.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                showMainFragment();
            }
        }, 100);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        onParseIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // observers
        TeamDataCache.getInstance().release();
    }

    @Override
    public void onBackPressed() {
        if (mainFragment != null) {
            if (mainFragment.onBackPressed()) {
                return;
            } else {
                moveTaskToBack(true);
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.clear();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                break;
            case R.id.create_normal_team:
                NormalTeamInfoActivity.startForCreateNormalTeam(MainActivity.this);
                break;
            case R.id.create_regular_team:
                startActivity(new Intent(MainActivity.this, AdvancedTeamCreateActivity.class));
                break;
            case R.id.search_advanced_team:
                AdvancedTeamSearchActivity.start(MainActivity.this);
                break;
            case R.id.add_buddy:
                AddFriendActivity.start(MainActivity.this);
                break;
            case R.id.search_btn:
                GlobalSearchActivity.start(MainActivity.this);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onParseIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(NimIntent.EXTRA_NOTIFY_CONTENT)) {
            IMMessage message = (IMMessage) getIntent().getSerializableExtra(NimIntent.EXTRA_NOTIFY_CONTENT);
            switch (message.getSessionType()) {
                case P2P:
                    SessionHelper.startP2PSession(this, message.getSessionId());
                    break;
                case Team:
                    SessionHelper.startTeamSession(this, message.getSessionId());
                    break;
                default:
                    break;
            }
        } else if (intent.hasExtra(EXTRA_APP_QUIT)) {
            onLogout();
            return;
        } else if (intent.hasExtra(AVChatActivity.INTENT_ACTION_AVCHAT)) {
            if (AVChatProfile.getInstance().isAVChatting()) {
                Intent localIntent = new Intent();
                localIntent.setClass(this, AVChatActivity.class);
                startActivity(localIntent);
            }
        } else if (intent.hasExtra(Extras.EXTRA_JUMP_P2P)) {
            Intent data = intent.getParcelableExtra(Extras.EXTRA_DATA);
            String account = data.getStringExtra(Extras.EXTRA_ACCOUNT);
            if (!TextUtils.isEmpty(account)) {
                SessionHelper.startP2PSession(this, account);
            }
        }
    }

    private void showMainFragment() {
        if (mainFragment == null) {
            mainFragment = new HomeFragment();
            switchFragmentContent(mainFragment);
        }
    }

    private void prepareData() {
        prepareCacheData();
        prepareRemoteData();
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                prepareLocalData();
            }
        }, 2000);
    }

    private void prepareCacheData() {
        ContactDataCache.getInstance().initUserCache(null);
        TeamDataCache.getInstance().initTeamCache();
    }

    private void prepareRemoteData() {
        ContactDataCache.getInstance().clearFriendCache();
        ContactDataCache.getInstance().clearUserCache();
        ContactDataCache.getInstance().getUsersOfMyFriend(null);
    }

    private void prepareLocalData() {
        StickerManager.getInstance().init(); // 加载本地贴图基本数据
        BitmapCache.getInstance().init();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == NormalTeamInfoActivity.REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            String result = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_REASON);
            if (result == null) {
                return;
            }
            if (result.equals(NormalTeamInfoActivity.RESULT_EXTRA_REASON_CREATE)) {
                String tid = data.getStringExtra(NormalTeamInfoActivity.RESULT_EXTRA_DATA);
                if (TextUtils.isEmpty(tid)) {
                    return;
                }

                SessionHelper.startTeamSession(MainActivity.this, tid); // 进入创建的群
            }
        }
    }

    // 注销
    private void onLogout() {
        DatabaseManager.getInstance().close();
        ContactDataCache.getInstance().clearFriendCache();
        ContactDataCache.getInstance().clearUserCache();
        TeamDataCache.getInstance().clearTeamCache();
        ContactHttpClient.getInstance().resetToken();
        DemoCache.clear();
        BitmapCache.getInstance().clearCache();
        LoginActivity.start(this);
        finish();
    }
}
