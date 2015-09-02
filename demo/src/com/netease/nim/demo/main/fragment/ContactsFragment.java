package com.netease.nim.demo.main.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.netease.nim.demo.DemoCache;
import com.netease.nim.demo.R;
import com.netease.nim.demo.common.ui.liv.LetterIndexView;
import com.netease.nim.demo.common.ui.liv.LivIndex;
import com.netease.nim.demo.common.ui.ptr.PullToRefreshBase;
import com.netease.nim.demo.common.ui.ptr.PullToRefreshListView;
import com.netease.nim.demo.contact.activity.BlackListActivity;
import com.netease.nim.demo.contact.cache.ContactDataCache;
import com.netease.nim.demo.contact.core.item.AbsContactItem;
import com.netease.nim.demo.contact.core.item.ContactItem;
import com.netease.nim.demo.contact.core.item.ItemTypes;
import com.netease.nim.demo.contact.core.model.ContactDataAdapter;
import com.netease.nim.demo.contact.core.model.ContactGroupStrategy;
import com.netease.nim.demo.contact.core.query.IContactDataProvider;
import com.netease.nim.demo.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.demo.contact.core.viewholder.LabelHolder;
import com.netease.nim.demo.contact.model.IContact;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.demo.contact.protocol.IContactHttpCallback;
import com.netease.nim.demo.contact.query.ContactDataProvider;
import com.netease.nim.demo.main.activity.SystemMessageActivity;
import com.netease.nim.demo.main.activity.TeamListActivity;
import com.netease.nim.demo.main.helper.SystemMessageUnreadManager;
import com.netease.nim.demo.main.model.MainTab;
import com.netease.nim.demo.main.reminder.ReminderId;
import com.netease.nim.demo.main.reminder.ReminderItem;
import com.netease.nim.demo.main.reminder.ReminderManager;
import com.netease.nim.demo.main.viewholder.ContactHolder;
import com.netease.nim.demo.session.SessionHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * 通讯录列表
 * <p/>
 * Created by huangjun on 2015/2/10.
 */
public class ContactsFragment extends MainTabFragment {

    private ContactDataAdapter adapter;

    private PullToRefreshListView lvContacts;

    private TextView countView;

    private LivIndex litterIdx;

    private View lodingFrame;

    public ContactsFragment() {
        this.setContainerId(MainTab.CONTACT.fragmentId);
    }

    private static final class ContactsGroupStrategy extends ContactGroupStrategy {
        public ContactsGroupStrategy() {
            add(ContactGroupStrategy.GROUP_NULL, -1, "");
            addABC(0);
        }
    }

    public static final class FuncItem extends AbsContactItem {
        static final FuncItem VERIFY = new FuncItem();
        static final FuncItem NORMAL_TEAM = new FuncItem();
        static final FuncItem ADVANCED_TEAM = new FuncItem();
        static final FuncItem BLACK_LIST = new FuncItem();
        static final FuncItem MY_COMPUTER = new FuncItem();

        @Override
        public int getItemType() {
            return ItemTypes.FUNC;
        }

        @Override
        public String belongsGroup() {
            return null;
        }

        public static final class FuncViewHolder extends AbsContactViewHolder<FuncItem> {
            private ImageView image;
            private TextView funcname;
            private TextView unreadNum;

            @Override
            public View inflate(LayoutInflater inflater) {
                View view = inflater.inflate(R.layout.func_contacts_item, null);
                this.image = (ImageView) view.findViewById(R.id.imgHead);
                this.funcname = (TextView) view.findViewById(R.id.lblfuncname);
                this.unreadNum = (TextView) view.findViewById(R.id.tab_new_msg_label);
                return view;
            }

            @Override
            public void refresh(ContactDataAdapter contactAdapter, int position, FuncItem item) {
                if (item == VERIFY) {
                    funcname.setText("验证提醒");
                    image.setImageResource(R.drawable.icon_verify_remind);
                    image.setScaleType(ScaleType.FIT_XY);
                    int unreadCount = SystemMessageUnreadManager.getInstance().getSysMsgUnreadCount();
                    updateUnreadNum(unreadCount);

                    ReminderManager.getInstance().registerUnreadNumChangedCallback(new ReminderManager.UnreadNumChangedCallback() {
                        @Override
                        public void onUnreadNumChanged(ReminderItem item) {
                            if (item.getId() != ReminderId.CONTACT) {
                                return;
                            }

                            updateUnreadNum(item.getUnread());
                        }
                    });
                } else if (item == NORMAL_TEAM) {
                    funcname.setText("普通群");
                    image.setImageResource(R.drawable.ic_secretary);
                } else if (item == ADVANCED_TEAM) {
                    funcname.setText("高级群");
                    image.setImageResource(R.drawable.ic_advanced_team);
                } else if (item == BLACK_LIST) {
                    funcname.setText("黑名单");
                    image.setImageResource(R.drawable.ic_black_list);
                } else if (item == MY_COMPUTER) {
                    funcname.setText("我的电脑");
                    image.setImageResource(R.drawable.ic_my_computer);
                }

                if (item != VERIFY) {
                    image.setScaleType(ScaleType.FIT_XY);
                    unreadNum.setVisibility(View.GONE);
                }
            }

            private void updateUnreadNum(int unreadCount) {
                // 2.*版本viewholder复用问题
                if (unreadCount > 0 && funcname.getText().toString().equals("验证提醒")) {
                    unreadNum.setVisibility(View.VISIBLE);
                    unreadNum.setText("" + unreadCount);
                } else {
                    unreadNum.setVisibility(View.GONE);
                }
            }
        }

        static List<AbsContactItem> provide() {
            List<AbsContactItem> items = new ArrayList<AbsContactItem>();
            items.add(VERIFY);
            items.add(NORMAL_TEAM);
            items.add(ADVANCED_TEAM);
            items.add(BLACK_LIST);
            items.add(MY_COMPUTER);

            return items;
        }

        static void handle(Context context, AbsContactItem item) {
            if (item == VERIFY) {
                SystemMessageActivity.start(context);
            } else if (item == NORMAL_TEAM) {
                TeamListActivity.start(context, ItemTypes.TEAMS.NORMAL_TEAM);
            } else if (item == ADVANCED_TEAM) {
                TeamListActivity.start(context, ItemTypes.TEAMS.ADVANCED_TEAM);
            } else if (item == MY_COMPUTER) {
                SessionHelper.startP2PSession(context, DemoCache.getAccount());
            } else if (item == BLACK_LIST) {
                BlackListActivity.start(context);
            }
        }
    }

    private void initAdapter() {
        IContactDataProvider dataProvider = new ContactDataProvider(ItemTypes.BUDDY);

        adapter = new ContactDataAdapter(getActivity(), new ContactsGroupStrategy(), dataProvider) {
            @Override
            protected List<AbsContactItem> onNonDataItems() {
                return FuncItem.provide();
            }

            @Override
            protected void onPreReady() {
                lodingFrame.setVisibility(View.VISIBLE);
            }

            @Override
            protected void onPostLoad(boolean empty, String queryText, boolean all) {
                lodingFrame.setVisibility(View.GONE);
                int buddyCount = ContactDataCache.getInstance().getMyFriendCounts();
                countView.setText("共有好友" + buddyCount + "名");
            }
        };

        adapter.addViewHolder(ItemTypes.LABEL, LabelHolder.class);
        adapter.addViewHolder(ItemTypes.FUNC, FuncItem.FuncViewHolder.class);
        adapter.addViewHolder(ItemTypes.BUDDY, ContactHolder.class);
    }

    @Override
    protected void onInit() {
        findViews();
        initPullToRefreshListView();
        buildLitterIdx(getView());
        litterIdx.show();

        // 加载本地数据
        reloadChange(true);

        // 向服务器请求数据
        requestUserData();
        registerObserver(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!inited()) {
            return;
        }

        this.reloadChange(false);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        registerObserver(false);
    }

    private void findViews() {
        View ctCountView = View.inflate(getView().getContext(), R.layout.contacts_count_item, null);
        countView = (TextView) ctCountView.findViewById(R.id.contactCountText);
        lodingFrame = getView().findViewById(R.id.contactLoadingFrame);
        initAdapter();
        lvContacts = (PullToRefreshListView) getView().findViewById(R.id.lvContacts);
        ctCountView.setClickable(false);
        lvContacts.getRefreshableView().addFooterView(ctCountView);
        lvContacts.setAdapter(adapter);

        ContactItemClickListener listener = new ContactItemClickListener();
        lvContacts.setOnItemClickListener(listener);
        lvContacts.setOnItemLongClickListener(listener);
    }

    private void registerObserver(boolean register) {
        ContactDataCache.getInstance().registerFriendDataChangedObserver(friendDataChangedObserver, register);
    }

    ContactDataCache.FriendDataChangedObserver friendDataChangedObserver = new ContactDataCache.FriendDataChangedObserver() {
        @Override
        public void onAddFriend(String account) {
            refreshListView();
        }

        @Override
        public void onDeleteFriend(String account) {
            refreshListView();
        }

        @Override
        public void onUpdateFriend(String account) {
            refreshListView();
        }

        @Override
        public void onAddUserToBlackList(String account) {
            refreshListView();
        }

        @Override
        public void onRemoveUserFromBlackList(String account) {
            refreshListView();
        }
    };

    private void buildLitterIdx(View view) {
        LetterIndexView livIndex = (LetterIndexView) view
                .findViewById(R.id.livIndex);
        livIndex.setNormalColor(getResources().getColor(
                R.color.contacts_letters_color));
        ImageView imgBackLetter = (ImageView) view
                .findViewById(R.id.imgBackLetter);
        TextView litterHit = (TextView) view.findViewById(R.id.lblLetterHit);
        litterIdx = adapter.createLivIndex(lvContacts, livIndex, litterHit,
                imgBackLetter);
    }

    private void initPullToRefreshListView() {
        lvContacts.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener2<ListView>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                requestUserData();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {

            }
        });
    }

    private void requestUserData() {
        ContactDataCache.getInstance().getUsersOfMyFriend(
                new IContactHttpCallback<List<User>>() {
                    @Override
                    public void onSuccess(List<User> users) {
                        if (!users.isEmpty()) {
                            refreshListView();
                        }

                        lvContacts.onRefreshComplete();
                    }

                    @Override
                    public void onFailed(int code, String errorMsg) {
                        if (code == 400 || code == 401) {
                            if (getActivity() == null) {
                                return;
                            }

                            Toast.makeText(getActivity(), "access_token无效,请重试。code=" + code,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (getActivity() == null) {
                                return;
                            }

                            Toast.makeText(getActivity(), "request failed, code=" + code + ", errorMsg=" + errorMsg,
                                    Toast.LENGTH_SHORT).show();
                        }

                        lvContacts.onRefreshComplete();
                    }
                }

        );
    }

    private void refreshListView() {
        reloadChange(true);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * 加载本地数据（已从服务器下载到本地），切换到当前tab时触发
     */
    private void reloadChange(boolean rebuild) {
        if (!inited()) {
            return;
        }
        if (adapter == null) {
            if (this.getActivity() == null) {
                return;
            }
            initAdapter();
        }

        adapter.load(rebuild);
    }

    private final class ContactItemClickListener implements OnItemClickListener, OnItemLongClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            AbsContactItem item = (AbsContactItem) adapter.getItem(position - 1);

            if (item == null) {
                return;
            }

            int type = item.getItemType();

            if (type == ItemTypes.LABEL) {
                return;
            }

            if (type == ItemTypes.FUNC) {
                FuncItem.handle(getActivity(), item);
                return;
            }

            if (type == ItemTypes.BUDDY) {
                IContact contact = ((ContactItem) item).getContact();
                SessionHelper.startP2PSession(getActivity(), contact.getContactId());
            }
        }

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            return true;
        }
    }

    @Override
    public void onCurrentScrolled() {
    }

    @Override
    public void onCurrentTabClicked() {
        if (lvContacts != null && isCurrent()) {
            int top = lvContacts.getRefreshableView().getFirstVisiblePosition();
            int bottom = lvContacts.getRefreshableView().getLastVisiblePosition();
            if (top >= (bottom - top)) {
                lvContacts.getRefreshableView().setSelection(bottom - top);
                lvContacts.getRefreshableView().smoothScrollToPosition(0);
            } else {
                lvContacts.getRefreshableView().smoothScrollToPosition(0);
            }
        }
    }
}
