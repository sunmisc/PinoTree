package sunmisc.btree.server;

public interface Exit {

    Exit NEVER = () -> false;

    boolean ready();

    final class Or implements Exit {
        private final Exit left, right;

        public Or(final Exit lft, final Exit rht) {
            this.left = lft;
            this.right = rht;
        }

        @Override
        public boolean ready() {
            return this.left.ready() || this.right.ready();
        }
    }

    final class And implements Exit {
        private final Exit left, right;

        public And(final Exit lft, final Exit rht) {
            this.left = lft;
            this.right = rht;
        }

        @Override
        public boolean ready() {
            return this.left.ready() && this.right.ready();
        }
    }

    final class Not implements Exit {
        private final Exit origin;

        public Not(final Exit exit) {
            this.origin = exit;
        }

        @Override
        public boolean ready() {
            return !this.origin.ready();
        }
    }

}
