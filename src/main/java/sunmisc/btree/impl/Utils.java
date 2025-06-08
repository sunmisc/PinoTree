package sunmisc.btree.impl;

import java.util.ArrayList;
import java.util.List;

public final class Utils {

    public static <T> List<T> append(final int idx, final T value, final List<T> list) {
        final List<T> result = new ArrayList<>(list);
        result.add(idx, value);
        return result;
    }

    public static <T> List<T> set(final int idx, final T value, final List<T> list) {
        final List<T> result = new ArrayList<>(list);
        result.set(idx, value);
        return result;
    }

    public static <T> List<T> withoutIdx(final int idx, final List<T> list) {
        if (list.isEmpty()) {
            return list;
        }
        final List<T> result = new ArrayList<>(list);
        result.remove(idx);
        return result;
    }

    public static <T> List<List<T>> splitAt(final int idx, final List<T> list) {
        final List<T> left = list.subList(0, idx);
        final List<T> right = list.subList(idx, list.size());
        return List.of(left, right);
    }

    public static <V> List<V> withoutLast(final List<V> list) {
        return list.isEmpty() ? List.of() : List.copyOf(list.subList(0, list.size() - 1));
    }

    public static <T> List<T> withoutFirst(final List<T> list) {
        return list.isEmpty() ? List.of() : List.copyOf(list.subList(1, list.size()));
    }

    public static <T> List<T> unshift(final T value, final List<T> list) {
        final List<T> result = new ArrayList<>(list.size() + 1);
        result.add(value);
        result.addAll(list);
        return result;
    }
}