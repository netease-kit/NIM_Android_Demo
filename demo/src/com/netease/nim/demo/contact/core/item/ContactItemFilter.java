package com.netease.nim.demo.contact.core.item;

import com.netease.nim.demo.contact.core.item.AbsContactItem;

import java.io.Serializable;

public interface ContactItemFilter extends Serializable {
	boolean filter(AbsContactItem item);
}
