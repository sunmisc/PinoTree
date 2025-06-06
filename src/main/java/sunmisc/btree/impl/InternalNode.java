package sunmisc.btree.impl;

import sunmisc.btree.api.*;

import java.util.*;
import java.util.stream.StreamSupport;

public final class InternalNode extends AbstractNode {
    private final List<Long> keys;

    public InternalNode(final Table table,
                        final List<Long> keys,
                        final List<IndexedNode> children) {
        super(table, children);
        this.keys = keys;
    }

    @Override
    public int minChildren() {
        return INTERNAL_MIN_CHILDREN;
    }

    @Override
    public int maxChildren() {
        return INTERNAL_MAX_CHILDREN;
    }

    @Override
    public List<Long> keys() {
        return Collections.unmodifiableList(this.keys);
    }

    @Override
    public IndexedNode withoutFirst() {
        return this.createNewNode(Utils.withoutFirst(this.keys()), Utils.withoutFirst(this.children()));
    }

    private IndexedNode withoutLast() {
        return this.createNewNode(Utils.withoutLast(this.keys()), Utils.withoutLast(this.children()));
    }

    @Override
    public Iterator<Entry> iterator() {
        return this.children().stream()
                .flatMap(child -> StreamSupport.stream(child.spliterator(), false))
                .iterator();
    }


    private IndexedNode createNewNode(final List<Long> keys, final List<IndexedNode> children) {
        final Node node = new InternalNode(this.table, keys, children);
        return new FwdNode(node, this.table.nodes().put(node));
    }

    @Override
    public IndexedNode merge(final Node other) {
        final List<Long> toConcat = Utils.unshift(
                other.firstEntry().orElseThrow().key(),
                other.keys()
        );
        final List<Long> newKeys = new ArrayList<>(this.keys);
        newKeys.addAll(toConcat);
        final List<IndexedNode> newChildren = new ArrayList<>(this.children());
        newChildren.addAll(other.children());
        return this.createNewNode(newKeys, newChildren);
    }

    private IndexedNode rebalanceNode(final int childIdx, final IndexedNode child) {
        if (child.satisfiesMinChildren()) {
            return this.createNewNode(this.keys, this.withReplacedChildren(childIdx, List.of(child)));
        }
        final boolean hasRightSibling = childIdx + 1 < this.children().size();
        final boolean hasLeftSibling = childIdx > 0;
        // todo:
        final IndexedNode right = hasRightSibling ? this.children().get(childIdx + 1) : null;
        final IndexedNode left = hasLeftSibling ? this.children().get(childIdx - 1) : null;
        final int minChildren = child.minChildren();

        if (hasRightSibling && (!hasLeftSibling || right.size() >= left.size())) {
            if (right.size() <= minChildren) {
                return this.withMergedChildren(childIdx, child, right);
            }
            final List<IndexedNode> newNodes = child.stealFirstKeyFrom(right);
            return this.createUpdatedNode(childIdx, newNodes);
        } else if (hasLeftSibling) {
            if (left.size() <= minChildren) {
                return this.withMergedChildren(childIdx - 1, left, child);
            }
            final List<IndexedNode> newNodes = left.giveLastKeyTo(child);
            return this.createUpdatedNode(childIdx - 1, newNodes);
        }
        return this.createNewNode(this.keys, this.withReplacedChildren(childIdx, List.of(child)));
    }

    private IndexedNode createUpdatedNode(final int keyIdx, final List<IndexedNode> newNodes) {
        final IndexedNode left = newNodes.getFirst();
        final IndexedNode right = newNodes.getLast();
        return this.createNewNode(
                Utils.set(keyIdx, right.firstEntry().orElseThrow().key(), this.keys),
                this.withReplacedChildren(keyIdx, List.of(left, right))
        );
    }

    @Override
    public IndexedNode delete(final long key) {
        final int rawIndex = Collections.binarySearch(this.keys(), key);
        final int index = rawIndex >= 0 ? rawIndex + 1 : -rawIndex - 1;
        final IndexedNode child = this.children().get(index);
        return this.rebalanceNode(index, child.delete(key));
    }

    @Override
    public IndexedNode delete(final long key, final String value) {
        final int rawIndex = Collections.binarySearch(this.keys(), key);
        final int index = rawIndex >= 0 ? rawIndex + 1 : -rawIndex - 1;
        final IndexedNode child = this.children().get(index);
        return this.rebalanceNode(index, child.delete(key, value));
    }

    private IndexedNode withMergedChildren(final int leftChildIdx,
                                          final IndexedNode leftNode,
                                          final IndexedNode rightNode) {
        final IndexedNode merged = leftNode.merge(rightNode);
        List<Long> newKeys = Utils.withoutIdx(leftChildIdx, this.keys());
        if (leftChildIdx > 0) {
            newKeys = Utils.set(
                    leftChildIdx - 1,
                    merged.firstEntry().orElseThrow().key(),
                    newKeys
            );
        }
        final List<IndexedNode> newChildren = new ArrayList<>(this.children());
        newChildren.remove(leftChildIdx);
        newChildren.set(leftChildIdx, merged);
        return this.createNewNode(newKeys, newChildren);
    }


    @Override
    public List<IndexedNode> stealFirstKeyFrom(final Node right) {
        final List<Long> newKeys = new ArrayList<>(this.keys());
        newKeys.add(right.firstEntry().orElseThrow().key());
        final List<IndexedNode> newChildren = new ArrayList<>(this.children());
        newChildren.add(right.children().getFirst());
        return List.of(this.createNewNode(newKeys, newChildren), right.withoutFirst());
    }

    @Override
    public List<IndexedNode> giveLastKeyTo(final Node right) {
        final IndexedNode stolenValue = this.children().getLast();
        final List<Long> newSiblingKeys = Utils.unshift(
                right.firstEntry().orElseThrow().key(),
                right.keys());
        final List<IndexedNode> newSiblingChildren = Utils.unshift(
                stolenValue,
                right.children()
        );
        return List.of(this.withoutLast(), this.createNewNode(newSiblingKeys, newSiblingChildren));
    }

    public List<IndexedNode> withReplacedChildren(final int idx, final List<IndexedNode> newChildren) {
        final List<IndexedNode> replaced = new ArrayList<>(this.children());
        for (int i = 0; i < newChildren.size(); i++) {
            replaced.set(idx + i, newChildren.get(i));
        }
        return replaced;
    }

    @Override
    public Optional<Entry> firstEntry() {
        return this.children().isEmpty() ? Optional.empty() : this.children().getFirst().firstEntry();
    }
    @Override
    public Optional<Entry> lastEntry() {
        return this.children().isEmpty() ? Optional.empty() : this.children().getLast().lastEntry();
    }

    private Split split(final IndexedNode src) {
        final int mid = src.keys().size() >>> 1;
        final long midVal = src.keys().get(mid);
        final IndexedNode left = this.createNewNode(
                src.keys().subList(0, mid),
                src.children().subList(0, mid + 1));
        final IndexedNode right = this.createNewNode(
                src.keys().subList(mid + 1, src.keys().size()),
                src.children().subList(mid + 1, src.children().size())
        );
        return new Split.RebalancedSplit(midVal, left, right);
    }

    public IndexedNode withSplitChild(final long idKey,
                                      final IndexedNode splitChild,
                                      final IndexedNode newChild) {
        final int childIdx = Collections.binarySearch(this.keys(), idKey);
        final int index = childIdx < 0 ? -childIdx - 1 : childIdx;

        final List<Long> newKeys = Utils.append(index, idKey, this.keys());
        final List<IndexedNode> newChildren = Utils.append(index + 1, newChild, this.children());
        newChildren.set(index, splitChild);
        return this.createNewNode(newKeys, newChildren);
    }

    @Override
    public Split insert(final long key, final String value) {
        final int childIdx = Collections.binarySearch(this.keys(), key);
        final int index = childIdx < 0 ? -childIdx - 1 : childIdx + 1;

        final IndexedNode child = this.children().get(index);
        final Split result = child.insert(key, value);
        final IndexedNode newChild = result.src();

        if (result.rebalanced()) {
            final long medianKey = result.medianKey();
            final IndexedNode splited = this.withSplitChild(medianKey, newChild, result.right());

            return splited.shouldSplit()
                    ? this.split(splited)
                    : new Split.UnarySplit(splited);
        }
        return new Split.UnarySplit(
                this.createNewNode(this.keys(), this.withReplacedChildren(index, List.of(newChild))
        ));
    }

    @Override
    public Optional<String> search(final long key) {
        int index = Collections.binarySearch(this.keys(), key);
        index = index >= 0 ? index + 1 : -index - 1;
        return this.children().get(index).search(key);
    }

    @Override
    public SequencedMap<Long, String> rangeSearch(final long minKey, final long maxKey) {
        final SequencedMap<Long, String> result = new LinkedHashMap<>();
        int startIdx = Collections.binarySearch(this.keys, minKey);
        startIdx = startIdx >= 0 ? startIdx : -startIdx - 1;
        for (int i = startIdx; i < this.children().size(); i++) {
            if (i == 0 || this.keys.get(i - 1) <= maxKey) {
                result.putAll(this.children().get(i).rangeSearch(minKey, maxKey));
            } else {
                break;
            }
        }
        return result;
    }
}