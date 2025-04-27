package sunmisc.btree.utils;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public final class ConcurrentLazy<V> implements Supplier<V> {
    private final AtomicReference<Supplier<V>> outcome;

    public ConcurrentLazy(final Supplier<V> scalar) {
        final class Sync extends ReentrantLock implements Supplier<V> {
            @Override
            public V get() {
                this.lock();
                try {
                    if (ConcurrentLazy.this.outcome.getPlain() != this) {
                        return ConcurrentLazy.this.get();
                    }
                    final V val = scalar.get();
                    ConcurrentLazy.this.outcome.set(new Supplier<>() {
                        @Override
                        public V get() {
                            return val;
                        }

                        @Override
                        public String toString() {
                            return Objects.toString(val);
                        }
                    });
                    return val;
                } finally {
                    this.unlock();
                }
            }

            @Override
            public String toString() {
                return "uninitialized";
            }
        }
        this.outcome = new AtomicReference<>(new Sync());
    }


    @Override
    public V get() {
        return this.outcome.get().get();
    }

    @Override
    public String toString() {
        return this.outcome.get().toString();
    }
}