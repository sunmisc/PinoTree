package sunmisc.btree.impl;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    public static <T> List<T> append(int idx, T value, List<T> list) {
        List<T> result = new ArrayList<>(list);
        result.add(idx, value);
        return result;
    }

    public static <T> List<T> set(int idx, T value, List<T> list) {
        List<T> result = new ArrayList<>(list);
        result.set(idx, value);
        return result;
    }

    public static <T> List<T> withoutIdx(int idx, List<T> list) {
        List<T> result = new ArrayList<>(list);
        if (result.isEmpty()) {
            return result;
        }
        result.remove(idx);
        return result;
    }

    public static <T> List<List<T>> splitAt(int idx, List<T> list) {
        List<T> left = List.copyOf(list.subList(0, idx));
        List<T> right = List.copyOf(list.subList(idx, list.size()));
        return List.of(left, right);
    }

    public static <V> List<V> head(List<V> list) {
        return list.isEmpty() ? List.of() : List.copyOf(list.subList(0, list.size() - 1));
    }
    public static <T> List<T> tail(List<T> list) {
        return list.isEmpty() ? List.of() : List.copyOf(list.subList(1, list.size()));
    }

    public static <T> List<T> unshift(T value, List<T> list) {
        List<T> result = new ArrayList<>(list.size() + 1);
        result.add(value);
        result.addAll(list);
        return result;
    }
}