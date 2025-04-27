package sunmisc.btree.impl;

import sunmisc.btree.api.Entry;
import sunmisc.btree.api.Node;
import sunmisc.btree.api.Split;
import sunmisc.btree.decode.Table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class InternalNode extends AbstractNode {
    private final List<Long> keys;

    public InternalNode(Table table, List<Long> keys, List<IndexedNode> children) {
        super(table, children);
        this.keys = keys;
    }

    @Override
    public int getMinChildren() {
        return Constants.INTERNAL_MIN_CHILDREN;
    }

    @Override
    public int getMaxChildren() {
        return Constants.INTERNAL_MAX_CHILDREN;
    }

    @Override
    public List<Long> keys() {
        return Collections.unmodifiableList(keys);
    }


    @Override
    public IndexedNode tail() {
        return createNewNode(Utils.tail(keys()), Utils.tail(children()));
    }

    @Override
    public void forEach(Consumer<Entry> consumer) {
        for (Node child : children()) {
            child.forEach(consumer);
        }
    }

    private IndexedNode head() {
        return createNewNode(Utils.head(keys()), Utils.head(children()));
    }

    private IndexedNode createNewNode(List<Long> keys, List<IndexedNode> children) {
        Node node = new InternalNode(table, keys, children);
        return new LazyNode(() -> node, table.nodes().alloc(node));
    }

    @Override
    public IndexedNode merge(Node other) {
        List<Long> toConcat = Utils.unshift(other.firstEntry().key(), other.keys());
        List<Long> newKeys = new ArrayList<>(keys);
        newKeys.addAll(toConcat);
        List<IndexedNode> newChildren = new ArrayList<>(children());
        newChildren.addAll(other.children());
        return createNewNode(newKeys, newChildren);
    }

    private IndexedNode rebalanceNode(int childIdx, IndexedNode child) {
        if (child.satisfiesMinChildren()) {
            return createNewNode(keys, withReplacedChildren(childIdx, List.of(child)));
        }
        boolean hasRightSibling = childIdx + 1 < children().size();
        boolean hasLeftSibling = childIdx > 0;
        // todo:
        IndexedNode right = hasRightSibling ? children().get(childIdx + 1) : null;
        IndexedNode left = hasLeftSibling ? children().get(childIdx - 1) : null;
        int minChildren = child.getMinChildren();

        if (hasRightSibling && (!hasLeftSibling || right.size() >= left.size())) {
            if (right.size() <= minChildren) {
                return withMergedChildren(childIdx, child, right);
            }
            List<IndexedNode> newNodes = child.stealFirstKeyFrom(right);
            return createUpdatedNode(childIdx, newNodes);
        } else if (hasLeftSibling) {
            if (left.size() <= minChildren) {
                return withMergedChildren(childIdx - 1, left, child);
            }
            List<IndexedNode> newNodes = left.giveLastKeyTo(child);
            return createUpdatedNode(childIdx - 1, newNodes);
        }
        return createNewNode(keys, withReplacedChildren(childIdx, List.of(child)));
    }

    private IndexedNode createUpdatedNode(int keyIdx, List<IndexedNode> newNodes) {
        IndexedNode left = newNodes.getFirst();
        IndexedNode right = newNodes.getLast();
        return createNewNode(
                Utils.set(keyIdx, right.firstEntry().key(), keys),
                withReplacedChildren(keyIdx, List.of(left, right))
        );
    }

    @Override
    public IndexedNode delete(long key) {
        int rawIndex = Collections.binarySearch(keys(), key);
        int index = rawIndex >= 0 ? rawIndex + 1 : -rawIndex - 1;

        IndexedNode origChild = children().get(index);
        IndexedNode child = origChild.delete(key);
        return rebalanceNode(index, child);
    }
    public IndexedNode withMergedChildren(int leftChildIdx,
                                          IndexedNode leftNode,
                                          IndexedNode rightNode) {
        IndexedNode merged = leftNode.merge(rightNode);
        List<Long> newKeys = Utils.withoutIdx(leftChildIdx, keys());
        if (leftChildIdx > 0) {
            newKeys = Utils.set(leftChildIdx - 1, merged.firstEntry().key(), newKeys);
        }
        List<IndexedNode> newChildren = new ArrayList<>(children());
        newChildren.remove(leftChildIdx);
        newChildren.set(leftChildIdx, merged);
        return createNewNode(newKeys, newChildren);
    }


    @Override
    public List<IndexedNode> stealFirstKeyFrom(Node right) {
        List<Long> newKeys = new ArrayList<>(keys());
        newKeys.add(right.firstEntry().key());
        List<IndexedNode> newChildren = new ArrayList<>(children());
        newChildren.add(right.children().getFirst());
        return List.of(createNewNode(newKeys, newChildren), right.tail());
    }

    @Override
    public List<IndexedNode> giveLastKeyTo(Node right) {
        IndexedNode stolenValue = children().getLast();
        List<Long> newSiblingKeys = Utils.unshift(
                right.firstEntry().key(),
                right.keys());
        List<IndexedNode> newSiblingChildren = Utils.unshift(
                stolenValue,
                right.children()
        );
        return List.of(this.head(), createNewNode(newSiblingKeys, newSiblingChildren));
    }

    public List<IndexedNode> withReplacedChildren(int idx, List<IndexedNode> newChildren) {
        List<IndexedNode> replaced = new ArrayList<>(children());
        for (int i = 0; i < newChildren.size(); i++) {
            replaced.set(idx + i, newChildren.get(i));
        }
        return replaced;
    }

    @Override
    public Entry firstEntry() {
        Node node = children().getFirst();
        return node.firstEntry();
    }

    private Split split(IndexedNode src) {
        int mid = src.keys().size() >>> 1;
        long midVal = src.keys().get(mid);
        IndexedNode left = createNewNode(
                src.keys().subList(0, mid),
                src.children().subList(0, mid + 1));
        IndexedNode right = createNewNode(
                src.keys().subList(mid + 1, src.keys().size()),
                src.children().subList(mid + 1, src.children().size())
        );
        return new Split.RebalancedSplit(midVal, left, right);
    }

    public IndexedNode withSplitChild(long idKey,
                                      IndexedNode splitChild,
                                      IndexedNode newChild) {
        int childIdx = Collections.binarySearch(keys(), idKey);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx;

        List<Long> newKeys = Utils.append(index, idKey, keys());
        List<IndexedNode> newChildren = Utils.append(index + 1, newChild, children());
        newChildren.set(index, splitChild);
        return createNewNode(newKeys, newChildren);
    }

    @Override
    public Split insert(long key, String value) {
        int childIdx = Collections.binarySearch(keys(), key);
        int index = childIdx < 0 ? -childIdx - 1 : childIdx + 1;

        IndexedNode child = children().get(index);
        Split result = child.insert(key, value);
        IndexedNode newChild = result.src();

        if (result.rebalanced()) {
            long medianKey = result.medianKey();
            IndexedNode splited = withSplitChild(medianKey, newChild, result.right());

            return splited.shouldSplit()
                    ? split(splited)
                    : new Split.UnarySplit(splited);
        }
        return new Split.UnarySplit(
                createNewNode(keys(), withReplacedChildren(index, List.of(newChild))
        ));
    }

    @Override
    public String search(long key) {
        int index = Collections.binarySearch(keys(), key);
        index = index >= 0 ? index + 1 : -index - 1;
        return children().get(index).search(key);
    }
}