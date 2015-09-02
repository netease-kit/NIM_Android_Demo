package com.netease.nim.demo.contact.activity;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;

import java.util.List;

/**
 * Created by huangjun on 2015/8/12.
 */
public class BlackListAdapter extends RecyclerView.Adapter<BlackListAdapter.ViewHolder> {

    public interface ViewListener {
        void onItemClick(int position);

        void onRemove(User user);
    }

    private List<User> data;

    private ViewListener viewListener;

    public BlackListAdapter(List<User> data, ViewListener viewListener) {
        this.data = data;
        this.viewListener = viewListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.black_list_item, null);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        view.setLayoutParams(lp);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (position >= 0 || position < data.size()) {
            holder.refresh(data.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    /**
     * ViewHolder
     */
    public class ViewHolder extends RecyclerView.ViewHolder {
        private HeadImageView headImageView;
        private TextView accountText;
        private Button removeBtn;

        private User user;

        public ViewHolder(View itemView) {
            super(itemView);

            headImageView = (HeadImageView) itemView.findViewById(R.id.head_image);
            accountText = (TextView) itemView.findViewById(R.id.account);
            removeBtn = (Button) itemView.findViewById(R.id.remove);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewListener != null) {
                        viewListener.onItemClick(getLayoutPosition());
                    }
                }
            });

            removeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (viewListener != null) {
                        viewListener.onRemove(user);
                    }
                }
            });
        }

        public void refresh(User user) {
            if (user == null) {
                return;
            }

            this.user = user;

            accountText.setText(user.getAccount());
            headImageView.loadBuddyAvatar(user.getAccount());
        }
    }
}
