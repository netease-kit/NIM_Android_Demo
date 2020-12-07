package com.netease.nim.uikit.business.session.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.netease.nim.uikit.R;
import com.netease.nim.uikit.api.NimUIKit;
import com.netease.nim.uikit.api.model.CreateMessageCallback;
import com.netease.nim.uikit.business.contact.selector.activity.ContactSelectActivity;
import com.netease.nim.uikit.business.session.constant.Extras;
import com.netease.nim.uikit.business.session.helper.MessageHelper;
import com.netease.nim.uikit.business.session.module.Container;
import com.netease.nim.uikit.business.session.module.ModuleProxy;
import com.netease.nim.uikit.business.session.module.MultiRetweetMsgCreatorFactory;
import com.netease.nim.uikit.business.session.module.list.MessageListPanelEx;
import com.netease.nim.uikit.business.session.module.list.MsgAdapter;
import com.netease.nim.uikit.common.ToastHelper;
import com.netease.nim.uikit.common.activity.UI;
import com.netease.nim.uikit.common.ui.dialog.CustomAlertDialog;
import com.netease.nim.uikit.common.util.log.sdk.wrapper.NimLog;
import com.netease.nim.uikit.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.MsgService;
import com.netease.nimlib.sdk.msg.constant.SessionTypeEnum;
import com.netease.nimlib.sdk.msg.model.IMMessage;
import com.netease.nimlib.sdk.msg.model.QueryDirectionEnum;

import java.util.List;

import static com.netease.nimlib.sdk.msg.constant.SessionTypeEnum.P2P;
import static com.netease.nimlib.sdk.msg.constant.SessionTypeEnum.Team;

public class MsgSelectActivity extends UI implements ModuleProxy {
    // 用到的EXTRA key
    // Extras.EXTRA_FROM: 被长按的消息在消息列表中所在的index
    // Extras.EXTRA_START: 第一条消息
    // Extras.EXTRA_AMOUNT: 消息总数

    private static final String TAG = "MsgSelectActivity";
    /** 可合并发送的最小消息条数 */
    private static final int MIN_MSG_COUNT = 1;

    /**
     * 开启MsgSelectActivity，并需要处理Result
     *
     * @param reqCode          请求码
     * @param activity         触发开启操作的Activity
     * @param startMessage     进入过滤列表的最早消息
     * @param msgAmount        进入过滤列表总消息数目
     * @param selectedPosition 被长按的消息在过滤列表中所在的index
     */
    public static void startForResult(int reqCode, Activity activity, IMMessage startMessage, int msgAmount, int selectedPosition) {
        Intent intent = new Intent();
        if (startMessage != null) {
            intent.putExtra(Extras.EXTRA_FROM, selectedPosition);
            intent.putExtra(Extras.EXTRA_START, startMessage);
            intent.putExtra(Extras.EXTRA_AMOUNT, msgAmount);
        }

        intent.setClass(activity, MsgSelectActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        activity.startActivityForResult(intent, reqCode);
    }


    /** 发送按钮 */
    private TextView mSendTV;

    /** 返回按钮 */
    private TextView mBackTV;

    /** 单向删除按钮 */
    private TextView mDeleteSelfTV;

    /** 可选消息的列表 */
    private RecyclerView mMsgSelectorRV;

    private TextView mSessionNameTV;

    /** 在线状态，在P2P会话中显示 */
    private TextView mOnlineStateTV;

    /** Intent传入的参数: 最早一条消息 */
    private IMMessage mExtraStart;

    /** Intent传入的参数: 过滤前消息总数 */
    private int mExtraMsgAmount;

    /** Intent传入的参数: 被长按消息在过滤前的位置 */
    private int mExtraSelectedPosition;

    /** 消息列表的适配器，列表的源数据通过adapter.getData获取 */
    private MsgAdapter mMsgAdapter;

    /** RecyclerView的初始位置，即被长按的消息的位置 */
    private int mSelectedPosition = 0;

    /** 转发内容所处会话的类型 */
    private SessionTypeEnum mSessionType;

    /** 会话ID */
    private String mSessionID;

    /** 选中的会话的个数 */
    private int mCheckedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nim_msg_select);
        getExtras();
        initViews();
        //获取mItems，调整mSelectedPosition
        queryCheckableMsgList(mExtraStart, mExtraMsgAmount, new QueryMessageListCallback());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case MessageListPanelEx.REQUEST_CODE_FORWARD_PERSON:
            case MessageListPanelEx.REQUEST_CODE_FORWARD_TEAM:
                onSelectSessionResult(requestCode, resultCode, data);
                break;
            default:
                break;
        }
    }

    /**
     * 选择合并发送目标结束的回调
     */
    private void onSelectSessionResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != Activity.RESULT_OK || data == null) {
            return;
        }
        //用于确认发送的会话框
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.confirm_forwarded)
                .setMessage(getString(R.string.confirm_forwarded_to) + data.getStringArrayListExtra(Extras.RESULT_NAME).get(0) + "?")
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

                    private void sendMsg(SessionTypeEnum sessionType, IMMessage packedMsg) {
                        data.putExtra(Extras.EXTRA_DATA, packedMsg);
                        data.putExtra(Extras.EXTRA_TYPE, sessionType.getValue());
                        setResult(Activity.RESULT_OK, data);
                        finish();
                    }

                    class P2PCallback extends CreateMessageCallbackImpl {

                        @Override
                        public void onFinished(IMMessage multiRetweetMsg) {
                            sendMsg(P2P, multiRetweetMsg);
                        }
                    }

                    class TeamCallback extends CreateMessageCallbackImpl {

                        @Override
                        public void onFinished(IMMessage multiRetweetMsg) {
                            sendMsg(Team, multiRetweetMsg);
                        }
                    }

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //获取被勾选的消息
                        List<IMMessage> checked = MessageHelper.getInstance().getCheckedItems(mMsgAdapter.getData());
                        switch (requestCode) {
                            //转发给个人
                            case MessageListPanelEx.REQUEST_CODE_FORWARD_PERSON:
                                MultiRetweetMsgCreatorFactory.createMsg(checked, true, new P2PCallback());
                                break;
                            //转发给群组
                            case MessageListPanelEx.REQUEST_CODE_FORWARD_TEAM:
                                MultiRetweetMsgCreatorFactory.createMsg(checked, true, new TeamCallback());
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                })
                .setOnCancelListener(dialog -> {
                    setResult(Activity.RESULT_CANCELED);
                    finish();
                });
        dialogBuilder.create().show();
    }

    private void getExtras() {
        final Bundle arguments = getIntent().getExtras();
        if (arguments == null) {
            return;
        }
        //被长按的消息的位置
        mExtraSelectedPosition = arguments.getInt(Extras.EXTRA_FROM);

        //最早一条消息
        mExtraStart = (IMMessage) arguments.getSerializable(Extras.EXTRA_START);
        if (mExtraStart != null) {
            //获取会话类型
            mSessionType = mExtraStart.getSessionType();
            //获取会话ID
            mSessionID = mExtraStart.getSessionId();
        }

        // 进入过滤的消息的总数
        mExtraMsgAmount = arguments.getInt(Extras.EXTRA_AMOUNT);
    }

    private void initViews() {
        mSendTV = findViewById(R.id.tv_send);
        //点击发送按钮，弹出选择类型(P2P/Team)的会话框
        mSendTV.setOnClickListener((v) -> {
            if (mCheckedCount < MIN_MSG_COUNT) {
                Toast.makeText(getApplicationContext(), "请选择不少于" + MIN_MSG_COUNT + "条要合并转发的消息", Toast.LENGTH_SHORT).show();
                return;
            }
            showTransFormTypeDialog();
        });

        mDeleteSelfTV = findView(R.id.tv_delete_self);
        mDeleteSelfTV.setOnClickListener(v -> deleteSelf());

        mBackTV = findViewById(R.id.txt_back);
        mBackTV.setOnClickListener((v -> this.finish()));

        initSessionNameAndState();
        initMsgSelector();
    }

    /**
     * 展示选择转发类型的会话框
     */
    private void showTransFormTypeDialog() {
        CustomAlertDialog alertDialog = new CustomAlertDialog(this);
        alertDialog.setCancelable(true);
        alertDialog.setCanceledOnTouchOutside(true);
        addForwardToPersonItem(alertDialog);
        addForwardToTeamItem(alertDialog);
        alertDialog.show();
    }

    /**
     * 单向删除
     */
    private void deleteSelf() {
        List<IMMessage> checked = MessageHelper.getInstance().getCheckedItems(mMsgAdapter.getData());
        NIMClient.getService(MsgService.class).deleteMsgSelf(checked, "").setCallback(new RequestCallback<Long>() {
            @Override
            public void onSuccess(Long param) {
                ToastHelper.showToast(MsgSelectActivity.this, "单向删除成功 " + param);
                mMsgAdapter.deleteItems(checked, true);
                finish();
            }

            @Override
            public void onFailed(int code) {
                ToastHelper.showToast(MsgSelectActivity.this, "单向删除失败 code=" + code);
            }

            @Override
            public void onException(Throwable exception) {
                ToastHelper.showToast(MsgSelectActivity.this, "单向删除错误 msg=" + exception.getMessage());
                exception.printStackTrace();
            }
        });
    }

    /**
     * 添加转发到个人的项
     *
     * @param alertDialog 所在会话框
     */
    private void addForwardToPersonItem(CustomAlertDialog alertDialog) {
        alertDialog.addItem(getString(R.string.forward_to_person), () -> {
            ContactSelectActivity.Option option = new ContactSelectActivity.Option();
            option.title = "个人";
            option.type = ContactSelectActivity.ContactSelectType.BUDDY;
            option.multi = false;
            option.maxSelectNum = 1;
            NimUIKit.startContactSelector(MsgSelectActivity.this, option, MessageListPanelEx.REQUEST_CODE_FORWARD_PERSON);
        });
    }

    /**
     * 添加转发到群组的项
     *
     * @param alertDialog 所在会话框
     */
    private void addForwardToTeamItem(CustomAlertDialog alertDialog) {
        alertDialog.addItem(getString(R.string.forward_to_team), () -> {
            ContactSelectActivity.Option option = new ContactSelectActivity.Option();
            option.title = "群组";
            option.type = ContactSelectActivity.ContactSelectType.TEAM;
            option.multi = false;
            option.maxSelectNum = 1;
            NimUIKit.startContactSelector(MsgSelectActivity.this, option, MessageListPanelEx.REQUEST_CODE_FORWARD_TEAM);
        });
    }

    /**
     * 初始化选择消息的部分
     */
    private void initMsgSelector() {
        //初始化消息列表
        mMsgSelectorRV = findViewById(R.id.rv_msg_selector);
        mMsgSelectorRV.setLayoutManager(new LinearLayoutManager(this));
        mMsgSelectorRV.requestDisallowInterceptTouchEvent(true);
        mMsgSelectorRV.setOverScrollMode(View.OVER_SCROLL_NEVER);
        Container container = new Container(this, NimUIKit.getAccount(), mSessionType, this);

        mMsgAdapter = new MsgAdapter(mMsgSelectorRV, null, container);
        mMsgAdapter.setEventListener(new MsgAdapter.BaseViewHolderEventListener() {

            @Override
            public void onCheckStateChanged(int index, Boolean newState) {
                List<IMMessage> items = mMsgAdapter.getData();
                if (items == null || items.isEmpty() || index < 0) {
                    return;
                }
                //更新数据源
                final IMMessage msg = items.get(index);
                if (msg.isChecked() == newState) {
                    return;
                }
                msg.setChecked(newState);
                //更新界面
                mMsgAdapter.notifyItemChanged(index);
                //跟踪选中数目
                mCheckedCount += Boolean.TRUE.equals(newState) ? 1 : -1;
            }
        });
        mMsgSelectorRV.setAdapter(mMsgAdapter);
    }

    /**
     * 初始化聊天名称和状态信息。如果是P2P才有状态信息
     */
    private void initSessionNameAndState() {
        mSessionNameTV = findViewById(R.id.tv_session_name);
        mOnlineStateTV = findViewById(R.id.tv_online_state);
        //将会话ID作为默认值
        String name = MessageHelper.getInstance().getStoredNameFromSessionId(mSessionID, mSessionType);
        mSessionNameTV.setText(name == null ? mSessionID : name);
        if (mSessionType == P2P) {
            //获取在线状态
            String onlineState = NimUIKitImpl.getOnlineStateContentProvider().getSimpleDisplay(mSessionID);
            //设置状态内容
            mOnlineStateTV.setText(onlineState == null ? "" : onlineState);
            //设置状态可见
            mOnlineStateTV.setVisibility(View.VISIBLE);
        } else {
            //设置状态不可见
            mOnlineStateTV.setVisibility(View.GONE);
        }
    }

    /**
     * 从本地数据库获取消息列表，并在过滤后传递给mItems，同时更新mSelectedPosition的值
     *
     * @param startMsg  最早的消息
     * @param msgAmount 过滤前消息总数
     */
    private void queryCheckableMsgList(IMMessage startMsg, int msgAmount, RequestCallback<List<IMMessage>> queryMsgListCallback) {
        IMMessage anchor = MessageBuilder.createEmptyMessage(startMsg.getSessionId(), startMsg.getSessionType(), startMsg.getTime());
        NIMClient.getService(MsgService.class).queryMessageListEx(anchor, QueryDirectionEnum.QUERY_NEW, msgAmount, true).setCallback(queryMsgListCallback);
    }

    @Override
    public boolean sendMessage(IMMessage msg) {
        return false;
    }

    @Override
    public void onInputPanelExpand() {
    }

    @Override
    public void shouldCollapseInputPanel() {
    }

    @Override
    public boolean isLongClickEnabled() {
        return false;
    }

    @Override
    public void onItemFooterClick(IMMessage message) {
    }

    @Override
    public void onReplyMessage(IMMessage replyMsg) {

    }

    public static class CreateMessageCallbackImpl implements CreateMessageCallback {
        @Override
        public void onFinished(IMMessage message) {
        }

        @Override
        public void onFailed(int code) {
            NimLog.d(TAG, "创建消息失败, code=" + code);
        }

        @Override
        public void onException(Throwable exception) {
            NimLog.d(TAG, "创建消息异常, e=" + exception.getMessage());
        }
    }

    private class QueryMessageListCallback implements RequestCallback<List<IMMessage>> {
        @Override
        public void onSuccess(List<IMMessage> param) {
            int size = param.size();
            List<IMMessage> items = mMsgAdapter.getData();
            mSelectedPosition = -1;
            for (int i = 0; i < size; ++i) {
                IMMessage imMessage = param.get(i);

                if (i == mExtraSelectedPosition) {
                    mSelectedPosition = items.size();
                }

                // 消息过滤。不是能合并转发的消息，不添加进数据源，进入下一次循环
                if (!MessageHelper.getInstance().isAvailableInMultiRetweet(imMessage)) {
                    continue;
                }
                imMessage.setChecked(false);
                items.add(imMessage);
            }
            if (mSelectedPosition == -1) {
                mSelectedPosition = items.size() - 1;
            }
            if (mMsgAdapter != null) {
                runOnUiThread(() -> {
                    mMsgAdapter.notifyDataSetChanged();
                    LinearLayoutManager manager = (LinearLayoutManager) mMsgSelectorRV.getLayoutManager();
                    if (manager == null) {
                        mMsgSelectorRV.scrollToPosition(mSelectedPosition);
                        return;
                    }
                    manager.scrollToPosition(Math.max(0, mSelectedPosition));
                });
            }
        }

        @Override
        public void onFailed(int code) {
        }

        @Override
        public void onException(Throwable exception) {
        }
    }

}
