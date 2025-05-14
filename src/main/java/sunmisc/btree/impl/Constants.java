package sunmisc.btree.impl;

public final class Constants {
    public static final int ORDER = 64; // 64

    public static final int LEAF_MIN_CHILDREN = Math.ceilDiv(ORDER, 2) - 1;
    public static final int LEAF_MAX_CHILDREN = ORDER - 1;
    public static final int INTERNAL_MIN_CHILDREN = Math.ceilDiv(ORDER, 2);
    public static final int INTERNAL_MAX_CHILDREN = ORDER;
}