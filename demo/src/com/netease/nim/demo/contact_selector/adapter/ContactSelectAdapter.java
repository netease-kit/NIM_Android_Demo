package com.netease.nim.demo.contact_selector.adapter;

import android.content.Context;

import com.netease.nim.demo.contact.core.item.ContactItem;
import com.netease.nim.demo.contact.core.item.ItemTypes;
import com.netease.nim.demo.contact.core.model.ContactDataAdapter;
import com.netease.nim.demo.contact.core.model.ContactGroupStrategy;
import com.netease.nim.demo.contact.core.item.AbsContactItem;
import com.netease.nim.demo.contact.core.query.IContactDataProvider;
import com.netease.nim.demo.contact.cache.ContactDataCache;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.demo.contact.model.IContact;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ContactSelectAdapter extends ContactDataAdapter {
    private HashSet<String> selects = new HashSet<String>();

    public ContactSelectAdapter(Context context,
                                ContactGroupStrategy groupStrategy, IContactDataProvider dataProvider) {
        super(context, groupStrategy, dataProvider);
    }

    public final void setAlreadySelectedUids(List<String> uids) {
        selects.addAll(uids);
    }

    public final List<ContactItem> getSelectedItem() {
        if (selects.isEmpty()) {
            return null;
        }

        List<ContactItem> res = new ArrayList<>();
        for (String uid : selects) {

            User user = ContactDataCache.getInstance().getUser(uid);
            if (user != null) {
                res.add(new ContactItem(user, ItemTypes.BUDDY));
            }
        }

        return res;
    }

    public final void selectItem(int position) {
        AbsContactItem item = (AbsContactItem) getItem(position);
        if (item != null && item instanceof ContactItem) {
            selects.add(((ContactItem) item).getContact().getContactId());
        }
        notifyDataSetChanged();
    }

    public final boolean isSelected(int position) {
        AbsContactItem item = (AbsContactItem) getItem(position);
        if (item != null && item instanceof ContactItem) {
            return selects.contains(((ContactItem) item).getContact().getContactId());
        }
        return false;
    }

    public final void cancelItem(int position) {
        AbsContactItem item = (AbsContactItem) getItem(position);
        if (item != null && item instanceof ContactItem) {
            selects.remove(((ContactItem) item).getContact().getContactId());
        }
        notifyDataSetChanged();
    }

    public final void cancelItem(IContact iContact) {
        selects.remove(iContact.getContactId());
    }
}
