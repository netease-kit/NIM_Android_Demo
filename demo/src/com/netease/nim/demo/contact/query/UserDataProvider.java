package com.netease.nim.demo.contact.query;

import com.netease.nim.demo.contact.core.item.AbsContactItem;
import com.netease.nim.demo.contact.core.item.ContactItem;
import com.netease.nim.demo.contact.core.item.ItemTypes;
import com.netease.nim.demo.contact.core.query.TextQuery;
import com.netease.nim.demo.contact.cache.ContactDataCache;
import com.netease.nim.demo.contact.model.User;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class UserDataProvider {
    public static final List<AbsContactItem> provide(TextQuery query) {
        List<User> sources = query(query);
        List<AbsContactItem> items = new ArrayList<>(sources.size());
        for (User b : sources) {
            items.add(new ContactItem(b, ItemTypes.BUDDY));
        }

        return items;
    }

    private static final List<User> query(TextQuery query) {
        if (query != null) {
            List<User> buddies = ContactDataCache.getInstance().getUsersOfMyFriendFromCache();
            for (Iterator<User> iter = buddies.iterator(); iter.hasNext(); ) {
                if (!ContactSearch.hitBuddy(iter.next(), query)) {
                    iter.remove();
                }
            }
            return buddies;
        } else {
            return ContactDataCache.getInstance().getUsersOfMyFriendFromCache();
        }
    }
}