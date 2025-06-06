package sunmisc.btree.api;

public interface Split {

    long medianKey();

    boolean rebalanced();

    IndexedNode src();

    IndexedNode right();

    final class RebalancedSplit implements Split {
        private final long median;
        private final IndexedNode left, right;

        public RebalancedSplit(final long median, final IndexedNode left, final IndexedNode right) {
            this.median = median;
            this.left = left;
            this.right = right;
        }

        @Override
        public long medianKey() {
            return this.median;
        }

        @Override
        public boolean rebalanced() {
            return true;
        }

        @Override
        public IndexedNode right() {
            return this.right;
        }

        @Override
        public IndexedNode src() {
            return this.left;
        }
    }

    final class UnarySplit implements Split {
        private final IndexedNode origin;

        public UnarySplit(final IndexedNode origin) {
            this.origin = origin;
        }

        @Override
        public long medianKey() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean rebalanced() {
            return false;
        }

        @Override
        public IndexedNode right() {
            throw new UnsupportedOperationException();
        }

        @Override
        public IndexedNode src() {
            return this.origin;
        }
    }
}
