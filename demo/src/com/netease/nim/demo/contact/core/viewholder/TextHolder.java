package com.netease.nim.demo.contact.core.viewholder;

import com.netease.nim.demo.contact.core.item.TextItem;
import com.netease.nim.demo.contact.core.model.ContactDataAdapter;
import com.netease.nim.demo.R;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class TextHolder extends AbsContactViewHolder<TextItem> {
	private TextView textView;

	public void refresh(ContactDataAdapter contactAdapter, int position, TextItem item) {
		textView.setText(item.getText());
	};

	@Override
	public View inflate(LayoutInflater inflater) {
		View view = inflater.inflate(R.layout.t_text_item, null);
		textView = (TextView) view.findViewById(R.id.text);
		return view;
	}
}
