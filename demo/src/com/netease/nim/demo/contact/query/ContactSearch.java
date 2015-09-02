package com.netease.nim.demo.contact.query;

import com.netease.nim.uikit.team.TeamDataCache;
import com.netease.nim.demo.contact.core.query.TextQuery;
import com.netease.nim.demo.contact.core.query.TextSearcher;
import com.netease.nim.demo.contact.model.User;
import com.netease.nim.demo.contact.model.IContact;
import com.netease.nim.demo.contact.model.TeamContact;
import com.netease.nim.demo.contact.query.ContactSearch.HitInfo.Type;
import com.netease.nimlib.sdk.team.model.Team;
import com.netease.nimlib.sdk.team.model.TeamMember;

public class ContactSearch {
    public static final boolean hitBuddy(User contact, TextQuery query) {
        String name = contact.getName();

        return TextSearcher.contains(query.t9, name, query.text);
    }

    public static final boolean hitTeam(Team contact, TextQuery query) {
        String name = contact.getName();
        String id = contact.getId();

        return TextSearcher.contains(query.t9, name, query.text) || TextSearcher.contains(query.t9, id, query.text);
    }

    public static final boolean hitTeamMember(TeamMember teamMember, TextQuery query) {
        String name = TeamDataCache.getInstance().getTeamMemberDisplayName(teamMember.getTid(), teamMember.getAccount());

        return TextSearcher.contains(query.t9, name, query.text);
    }

    public static final class HitInfo {
        public enum Type {
            Account, Name,
        }

        public final Type type;

        public final String text;

        public final int[] range;

        public HitInfo(Type type, String text, int[] range) {
            this.type = type;
            this.text = text;
            this.range = range;
        }
    }

    public static final HitInfo hitInfoBuddy(User contact, TextQuery query) {
        String account = contact.getAccount();
        String name = contact.getName();

        int[] range = TextSearcher.indexOf(query.t9, name, query.text);

        if (range != null) {
            return new HitInfo(Type.Name, name, range);
        }

        range = TextSearcher.indexOf(query.t9, account, query.text);

        if (range != null) {
            return new HitInfo(Type.Account, account, range);
        }

        return null;
    }

    public static final HitInfo hitInfoTeamContact(TeamContact contact, TextQuery query) {
        String name = contact.getDisplayName();

        int[] range = TextSearcher.indexOf(query.t9, name, query.text);

        if (range != null) {
            return new HitInfo(Type.Name, name, range);
        }

        return null;
    }

    public static final HitInfo hitInfoContact(IContact contact, TextQuery query) {
        String name = contact.getDisplayName();

        int[] range = TextSearcher.indexOf(query.t9, name, query.text);

        if (range != null) {
            return new HitInfo(Type.Name, name, range);
        }

        return null;
    }

    public static final HitInfo hitInfo(IContact contact, TextQuery query) {
        if (contact instanceof User) {
            return hitInfoBuddy((User) contact, query);
        }

        if (contact instanceof TeamContact) {
            return hitInfoTeamContact((TeamContact) contact, query);
        }

        return hitInfoContact(contact, query);
    }
}