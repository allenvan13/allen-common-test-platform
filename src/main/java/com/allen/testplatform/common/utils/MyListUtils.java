package com.allen.testplatform.common.utils;

import java.util.*;

/**
 * @author Fan QingChuan
 * @since 2022/3/11 13:47
 */

public class MyListUtils {
    //获取2个list中均存在的元素，并去重
    public static <T> List<T> getDuplicateV2(List<T> list1, List<T> list2) {

        List<T> duplicate = new ArrayList<>();
        List<T> maxList = list1;
        List<T> minList = list2;

        if(list2.size()>list1.size()) {
            maxList = list2;
            minList = list1;
        }

        Map<T,Integer> map = new HashMap<>(maxList.size());

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
        Set<T> set = new HashSet<>(list);
        List<T> result = new ArrayList<>(set);
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

        Map<T,Integer> map = new HashMap<>(maxList.size());

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
}
