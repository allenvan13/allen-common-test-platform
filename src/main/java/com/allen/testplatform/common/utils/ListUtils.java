package com.allen.testplatform.common.utils;

import cn.nhdc.common.util.CollectionUtils;

import java.util.*;

/**
 * @author Fan QingChuan
 * @since 2022/3/11 13:47
 */
public class ListUtils {
    public static <T> List<List<T>> splitList(List<T> list, int len) {
        if (list == null || list.size() == 0 || len < 1) {
            return null;
        }

        List<List<T>> result = new ArrayList<>();

        int size = list.size();
        int count = (size + len - 1) / len;

        for (int i = 0; i < count; i++) {
            List<T> subList = list.subList(i * len, ((i + 1) * len > size ? size : len * (i + 1)));
            result.add(subList);
        }
        return result;
    }

    //获取2个list中均存在的元素，并去重
    public static <T> List<T> getDuplicateV2(List<T> list1, List<T> list2) {

        List<T> duplicate = new ArrayList<>();
        List<T> maxList = list1;
        List<T> minList = list2;

        if(list2.size()>list1.size()) {
            maxList = list2;
            minList = list1;
        }

        Map<T,Integer> map = new HashMap<T,Integer>(maxList.size());

        for (T t : maxList) {
            map.put(t, 1);
        }

        for (T t : minList) {
            if(map.get(t) != null) {
                map.put( t, 2);
            }
        }

        for(Map.Entry<T, Integer> entry : map.entrySet()) {
            if(entry.getValue() != 1) {
                duplicate.add(entry.getKey());
            }
        }

        return duplicate;
    }

    //以sourceList为基础(业务需求不能去重) 获取与matchList不一样的
    public static <T> List<T> getDiffByMatch(List<T> sourceList, List<T> matchList) {
        List<T> resultList = new ArrayList<>();
        for (T t : sourceList) {
            if ( !matchList.contains(t)) {
                resultList.add(t);
            }
        }
        return resultList;
    }

    public static <T> List<T> distinctListBySet(List<T> list){
        List<T> result = new ArrayList<>();
        Set<T> set = new HashSet<>();
        set.addAll(list);
        result.addAll(set);
        return result;
    }

    //合并2个list中的元素,并去重
    public static <T> List<T> mergeListDistinct(List<T> list1, List<T> list2) {

        List<T> result = new ArrayList<>();
        List<T> maxList = list1;
        List<T> minList = list2;

        if(list2.size() > list1.size()) {
            maxList = list2;
            minList = list1;
        }

        Map<T,Integer> map = new HashMap<T,Integer>(maxList.size());

        for (T t : maxList) {
            map.put(t, 1);
        }

        for (T t : minList) {
            if(map.get(t) != null) {
                map.put( t, 2);
            }else {
                map.put(t,3);
            }
        }

        for(Map.Entry<T, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 2 || entry.getValue() == 1 || entry.getValue() == 3) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static String[] toArray(List<String> list) {

        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        String[] array = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
