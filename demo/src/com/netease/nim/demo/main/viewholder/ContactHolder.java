package com.netease.nim.demo.main.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.netease.nim.demo.R;
import com.netease.nim.demo.contact.activity.UserProfileActivity;
import com.netease.nim.demo.contact.core.item.ContactItem;
import com.netease.nim.demo.contact.core.model.ContactDataAdapter;
import com.netease.nim.demo.contact.core.query.TextQuery;
import com.netease.nim.demo.contact.core.viewholder.AbsContactViewHolder;
import com.netease.nim.demo.contact.model.IContact;
import com.netease.nim.demo.contact.query.ContactSearch;
import com.netease.nim.demo.contact.query.ContactSearch.HitInfo;
import com.netease.nim.uikit.common.ui.imageview.HeadImageView;

public class ContactHolder extends AbsContactViewHolder<ContactItem> {
	
	protected HeadImageView head;

	protected TextView name;

	protected TextView desc;

	protected RelativeLayout headLayout;

	@Override
	public void refresh(ContactDataAdapter adapter, int position, ContactItem item) {
		final IContact contact = item.getContact();

		TextQuery query = adapter.getQuery();
		HitInfo hitInfo = query != null ? ContactSearch.hitInfo(contact, query) : null;
        if(contact.getContactType() == IContact.Type.Buddy) {
            head.loadBuddyAvatar(contact.getContactId());
        } else {
            head.setImageResource(R.drawable.avatar_group);
        }

		name.setText(contact.getDisplayName());

		if (hitInfo != null && !hitInfo.text.equals(contact.getDisplayName())) {
			desc.setVisibility(View.VISIBLE);
		} else {
			desc.setVisibility(View.GONE);
		}
		headLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (contact.getContactType() == IContact.Type.Buddy) {
					UserProfileActivity.start(context, contact.getContactId());
				}
			}
		});
	}

	@Override
	public View inflate(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.contacts_item, null);

		headLayout = (RelativeLayout) view.findViewById(R.id.head_layout);
		head = (HeadImageView) view.findViewById(R.id.contacts_item_head);
		name = (TextView) view.findViewById(R.id.contacts_item_name);
		desc = (TextView) view.findViewById(R.id.contacts_item_desc);

		return view;
	}
}
