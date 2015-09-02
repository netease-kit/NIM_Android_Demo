package com.netease.nim.demo.contact.core.viewholder;

import com.netease.nim.demo.contact.core.item.LabelItem;
import com.netease.nim.demo.contact.core.model.ContactDataAdapter;
import com.netease.nim.demo.R;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class LabelHolder extends AbsContactViewHolder<LabelItem> {

	private TextView name;

	@Override
	public void refresh(ContactDataAdapter contactAdapter, int position, LabelItem item) {
		this.name.setText(item.getText());
	}

	@Override
	public View inflate(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.contacts_abc_item, null);
		this.name = (TextView) view.findViewById(R.id.lblNickname);
		return view;
	}

}
