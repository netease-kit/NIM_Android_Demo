package com.netease.nim.uikit.common.util.collection;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class CollectionUtil {

    public interface ITransformer<T, S> {
        S transform(T t);
    }

    /**
     * 判断一个{@link Collection}是否为空
     *
     * @param collection 被判断对象
     * @return null或者不包含元素：true；其他：false
     */
    public static <T> boolean isEmpty(Collection<T> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * 返回一个能代表这个{@link Collection}的字符串
     */
    public static <T> String toString(Collection<T> collection) {
        return toString(collection, ", ");
    }


    /**
     * 返回一个能代表这个{@link Collection}的字符串
     */
    public static <T> String toString(Collection<T> collection, String separator) {
        return toString(collection, separator, String::valueOf);
    }


    /**
     * 返回一个能代表这个{@link Collection}的字符串
     */
    public static <T> String toString(Collection<T> collection, String separator, ITransformer<T, String> itemToString) {
        return toString(collection, separator, "", "", itemToString);
    }


    /**
     * 返回一个能代表这个{@link Collection}的字符串
     */
    public static <T> String toString(Collection<T> collection, String separator, String preStr, String sufStr, ITransformer<T, String> itemToString) {
        StringBuilder sb = new StringBuilder();
        if (isEmpty(collection)) {
            return "";
        }
        for (T t : collection) {
            sb.append(separator).append(itemToString.transform(t));
        }
        return preStr + sb.substring(separator.length()) + sufStr;
    }

    /**
     * 创建一个只有一个元素的ArrayList
     *
     * @param t 元素
     */
    public static <T> ArrayList<T> createSingleArrayList(@Nullable T t) {
        ArrayList<T> list = new ArrayList<>(1);
        list.add(t);
        return list;
    }

    public static HashSet<String> createStringSetFromJSONArray(@Nullable JSONArray array) {
        int len;
        if (array == null || (len = array.length()) == 0) {
            return new HashSet<>(0);
        }
        HashSet<String> set = new HashSet<>(len);
        String elem;
        for (int i = 0; i < len; ++i) {
            try {
                elem = array.getString(i);
            } catch (JSONException e) {
                e.printStackTrace();
                continue;
            }
            set.add(elem);
        }
        return set;
    }

    /**
     * 提取元素，组成新的Collection
     *
     * @param collection 被提取的Collection
     * @param extractor  提取回调
     * @return 提取出的元素组成的列表
     */
    public static <T, S> ArrayList<S> extractElements(Collection<T> collection, ITransformer<T, S> extractor) {
        if (isEmpty(collection) || extractor == null) {
            return null;
        }

        ArrayList<S> sList = new ArrayList<>(collection.size());
        for (T t : collection) {
            sList.add(extractor.transform(t));
        }
        return sList;
    }

    /**
     * 遍历每个元素，分别执行extractor中的方法
     *
     * @param collection 被遍历的Collection
     * @param extractor  执行器。返回值：
     *                   {@link Boolean#TRUE}: 继续遍历
     *                   {@link Boolean#FALSE}: 终止遍历
     *                   null: 视为{@link Boolean#TRUE}
     * @param <T>        {@link Collection}中元素的类型
     */
    public static <T> void forEach(Collection<T> collection, ITransformer<T, Boolean> extractor) {
        if (isEmpty(collection) || extractor == null) {
            return;
        }
        for (T t : collection) {
            if (Boolean.FALSE.equals(extractor.transform(t))) {
                return;
            }
        }
    }


    /**
     * 顺序对比，选择具有最值的元素并返回其index
     *
     * @param list       待对比序列
     * @param comparator 比较器，大于等于0选择前者，小于0选择后者
     * @return 被选中元素的index，如果list为空，返回-1
     */
    public static <T> int indexOfMost(List<T> list, Comparator<T> comparator) {
        if (isEmpty(list) || comparator == null) {
            return -1;
        }

        Iterator<T> iterator = list.iterator();
        int mostIndex = 0;
        T mostT = iterator.next();
        int index = 1;
        T item;
        while (iterator.hasNext()) {
            item = iterator.next();
            if (comparator.compare(mostT, item) < 0) {
                mostT = item;
                mostIndex = index;
            }
            ++index;
        }
        return mostIndex;
    }

    /**
     * 查找Collection中第一个符合条件的元素
     *
     * @param collection 待判断Collection
     * @param filter     判断依据，结果若为null，视为false
     * @return 第一个符合条件的元素，如果没有，返回null
     */
    public static <T> T firstElement(Collection<T> collection, ITransformer<T, Boolean> filter) {
        if (isEmpty(collection) || filter == null) {
            return null;
        }

        for (T t : collection) {
            if (Boolean.TRUE.equals(filter.transform(t))) {
                return t;
            }
        }
        return null;
    }

    /**
     * 查找Collection中符合条件的所有元素
     *
     * @param collection 待判断Collection
     * @param filter     判断依据，结果若为null，视为false
     * @return 所有符合条件的元素组成的列表，如果没有，返回空列表
     */
    public static <T> List<T> findAll(Collection<T> collection, ITransformer<T, Boolean> filter) {
        if (isEmpty(collection) || filter == null) {
            return null;
        }
        ArrayList<T> result = new ArrayList<>();

        for (T t : collection) {
            if (Boolean.TRUE.equals(filter.transform(t))) {
                result.add(t);
            }
        }
        return result;
    }

    /**
     * 查找Collection中是否包含符合条件的元素
     *
     * @param collection 待判断Collection
     * @param filter     判断依据，结果若为null，视为false
     * @return 是否包含
     */
    public static <T> boolean contains(Collection<T> collection, ITransformer<T, Boolean> filter) {
        return firstElement(collection, filter) != null;
    }

    /**
     * 创建一个ArrayList，并根据元素生成器的规则填入元素
     *
     * @param size 元素个数
     * @param transformer 元素生成器
     */
    public static <T> ArrayList<T> createArrayList(int size, ITransformer<Integer, T> transformer) {
        size = Math.max(0, size);
        if (transformer == null) {
            return new ArrayList<>(size);
        }
        ArrayList<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            list.add(transformer.transform(i));
        }
        return list;
    }

    /**
     * 移除所有满足条件的元素
     *
     * @param collection 待判断Collection
     * @param filter     判断依据，如果返回true，则删除；否则不删除
     */
    public static <T> void removeIf(Collection<T> collection, ITransformer<T, Boolean> filter) {
        if (isEmpty(collection) || filter == null) {
            return;
        }
        Iterator<T> it = collection.iterator();
        T t;
        while (it.hasNext()) {
            t = it.next();
            if (filter.transform(t)) {
                it.remove();
            }
        }
    }
}
