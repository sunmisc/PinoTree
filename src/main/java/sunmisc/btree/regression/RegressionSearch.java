package sunmisc.btree.regression;

import java.util.List;

public interface RegressionSearch<V extends Number> {

    RegressionSearch<V> add(int index, V value);

    RegressionSearch<V> remove(int index, V value);

    int search(List<V> values, V index);

    default RegressionSearch<V> addAll(List<V> values) {
        RegressionSearch<V> result = this;
        for (int i = 0; i < values.size(); i++) {
            result = this.add(i, values.get(i));
        }
        return result;
    }
}
