package com.netease.yunxin.nertc.nertcvideocall.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by luc on 3/25/21.
 */
public class ParameterMap {
    private final String name;
    private final Map<String, Object> parameterMap = new HashMap<>();

    public ParameterMap(String name) {
        this.name = name;
    }

    public ParameterMap append(String key, Object value) {
        parameterMap.put(key, value);
        return this;
    }

    public ParameterMap append(Map<String, ?> map) {
        parameterMap.putAll(map);
        return this;
    }

    public ParameterMap remove(String key) {
        parameterMap.remove(key);
        return this;
    }

    public String toValue() {
        return name + ":" + parameterMap.toString();
    }
}
