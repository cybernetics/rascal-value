/*******************************************************************************
 * Copyright (c) 2013-2014 CWI
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *
 *   * Michael Steindorfer - Michael.Steindorfer@cwi.nl - CWI  
 *******************************************************************************/
package org.eclipse.imp.pdb.facts.util;

import java.text.DecimalFormat;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings("rawtypes")
public class TrieSet_BleedingEdge<K> implements ImmutableSet<K> {

	@SuppressWarnings("unchecked")
	private static final TrieSet_BleedingEdge EMPTY_SET = new TrieSet_BleedingEdge(
					CompactSetNode.EMPTY_NODE, 0, 0);

	private static final boolean DEBUG = false;

	private final AbstractSetNode<K> rootNode;
	private final int hashCode;
	private final int cachedSize;

	TrieSet_BleedingEdge(AbstractSetNode<K> rootNode, int hashCode, int cachedSize) {
		this.rootNode = rootNode;
		this.hashCode = hashCode;
		this.cachedSize = cachedSize;
		if (DEBUG) {
			assert checkHashCodeAndSize(hashCode, cachedSize);
		}
	}

	@SuppressWarnings("unchecked")
	public static final <K> ImmutableSet<K> of() {
		return TrieSet_BleedingEdge.EMPTY_SET;
	}

	@SuppressWarnings("unchecked")
	public static final <K> ImmutableSet<K> of(K... keys) {
		ImmutableSet<K> result = TrieSet_BleedingEdge.EMPTY_SET;

		for (final K key : keys) {
			result = result.__insert(key);
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public static final <K> TransientSet<K> transientOf() {
		return TrieSet_BleedingEdge.EMPTY_SET.asTransient();
	}

	@SuppressWarnings("unchecked")
	public static final <K> TransientSet<K> transientOf(K... keys) {
		final TransientSet<K> result = TrieSet_BleedingEdge.EMPTY_SET.asTransient();

		for (final K key : keys) {
			result.__insert(key);
		}

		return result;
	}

	private boolean checkHashCodeAndSize(final int targetHash, final int targetSize) {
		int hash = 0;
		int size = 0;

		for (Iterator<K> it = keyIterator(); it.hasNext();) {
			final K key = it.next();

			hash += key.hashCode();
			size += 1;
		}

		return hash == targetHash && size == targetSize;
	}

	private static int improve(final int hash) {
		return hash; // return idendity
	}

	@Override
	public TrieSet_BleedingEdge<K> __insert(final K key) {
		final int keyHash = key.hashCode();
		final Result<K> details = Result.unchanged();

		final CompactSetNode<K> newRootNode = rootNode.updated(null, key, improve(keyHash), 0,
						details);

		if (details.isModified()) {

			return new TrieSet_BleedingEdge<K>(newRootNode, hashCode + keyHash, cachedSize + 1);

		}

		return this;
	}

	@Override
	public TrieSet_BleedingEdge<K> __insertEquivalent(final K key, final Comparator<Object> cmp) {
		final int keyHash = key.hashCode();
		final Result<K> details = Result.unchanged();

		final CompactSetNode<K> newRootNode = rootNode.updated(null, key, improve(keyHash), 0,
						details, cmp);

		if (details.isModified()) {

			return new TrieSet_BleedingEdge<K>(newRootNode, hashCode + keyHash, cachedSize + 1);

		}

		return this;
	}

	@Override
	public ImmutableSet<K> __remove(final K key) {
		final int keyHash = key.hashCode();
		final Result<K> details = Result.unchanged();

		final CompactSetNode<K> newRootNode = rootNode.removed(null, key, improve(keyHash), 0,
						details);

		if (details.isModified()) {

			return new TrieSet_BleedingEdge<K>(newRootNode, hashCode - keyHash, cachedSize - 1);

		}

		return this;
	}

	@Override
	public ImmutableSet<K> __removeEquivalent(final K key, final Comparator<Object> cmp) {
		final int keyHash = key.hashCode();
		final Result<K> details = Result.unchanged();

		final CompactSetNode<K> newRootNode = rootNode.removed(null, key, improve(keyHash), 0,
						details, cmp);

		if (details.isModified()) {

			return new TrieSet_BleedingEdge<K>(newRootNode, hashCode - keyHash, cachedSize - 1);

		}

		return this;
	}

	@Override
	public boolean contains(final java.lang.Object o) {
		try {
			@SuppressWarnings("unchecked")
			final K key = (K) o;
			return rootNode.containsKey(key, improve(key.hashCode()), 0);
		} catch (ClassCastException unused) {
			return false;
		}
	}

	@Override
	public boolean containsEquivalent(final java.lang.Object o, final Comparator<Object> cmp) {
		try {
			@SuppressWarnings("unchecked")
			final K key = (K) o;
			return rootNode.containsKey(key, improve(key.hashCode()), 0, cmp);
		} catch (ClassCastException unused) {
			return false;
		}
	}

	@Override
	public K get(final java.lang.Object o) {
		try {
			@SuppressWarnings("unchecked")
			final K key = (K) o;
			final Optional<K> result = rootNode.findByKey(key, improve(key.hashCode()), 0);

			if (result.isPresent()) {
				return result.get();
			} else {
				return null;
			}
		} catch (ClassCastException unused) {
			return null;
		}
	}

	@Override
	public K getEquivalent(final java.lang.Object o, final Comparator<Object> cmp) {
		try {
			@SuppressWarnings("unchecked")
			final K key = (K) o;
			final Optional<K> result = rootNode.findByKey(key, improve(key.hashCode()), 0, cmp);

			if (result.isPresent()) {
				return result.get();
			} else {
				return null;
			}
		} catch (ClassCastException unused) {
			return null;
		}
	}

	@Override
	public ImmutableSet<K> __insertAll(final ImmutableSet<? extends K> set) {
		TransientSet<K> tmp = asTransient();
		tmp.__insertAll(set);
		return tmp.freeze();
	}

	@Override
	public ImmutableSet<K> __insertAllEquivalent(final ImmutableSet<? extends K> set,
					final Comparator<Object> cmp) {
		TransientSet<K> tmp = asTransient();
		tmp.__insertAllEquivalent(set, cmp);
		return tmp.freeze();
	}

	@Override
	public ImmutableSet<K> __retainAll(final ImmutableSet<? extends K> set) {
		TransientSet<K> tmp = asTransient();
		tmp.__retainAll(set);
		return tmp.freeze();
	}

	@Override
	public ImmutableSet<K> __retainAllEquivalent(final ImmutableSet<? extends K> set,
					final Comparator<Object> cmp) {
		TransientSet<K> tmp = asTransient();
		tmp.__retainAllEquivalent(set, cmp);
		return tmp.freeze();
	}

	@Override
	public ImmutableSet<K> __removeAll(final ImmutableSet<? extends K> set) {
		TransientSet<K> tmp = asTransient();
		tmp.__removeAll(set);
		return tmp.freeze();
	}

	@Override
	public ImmutableSet<K> __removeAllEquivalent(final ImmutableSet<? extends K> set,
					final Comparator<Object> cmp) {
		TransientSet<K> tmp = asTransient();
		tmp.__removeAllEquivalent(set, cmp);
		return tmp.freeze();
	}

	@Override
	public boolean add(final K key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(final java.lang.Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends K> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		for (Object item : c) {
			if (!contains(item)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean containsAllEquivalent(final Collection<?> c, final Comparator<Object> cmp) {
		for (Object item : c) {
			if (!containsEquivalent(item, cmp)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int size() {
		return cachedSize;
	}

	@Override
	public boolean isEmpty() {
		return cachedSize == 0;
	}

	@Override
	public Iterator<K> iterator() {
		return keyIterator();
	}

	@Override
	public Iterator<K> keyIterator() {
		return new SetKeyIterator<>(rootNode);
	}

	@Override
	public java.lang.Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTransientSupported() {
		return true;
	}

	@Override
	public TransientSet<K> asTransient() {
		return new TransientTrieSet_BleedingEdge<K>(this);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		}
		if (other == null) {
			return false;
		}

		if (other instanceof TrieSet_BleedingEdge) {
			TrieSet_BleedingEdge<?> that = (TrieSet_BleedingEdge<?>) other;

			if (this.size() != that.size()) {
				return false;
			}

			return rootNode.equals(that.rootNode);
		}

		return super.equals(other);
	}

	/*
	 * For analysis purposes only.
	 */
	protected AbstractSetNode<K> getRootNode() {
		return rootNode;
	}

	/*
	 * For analysis purposes only.
	 */
	protected Iterator<AbstractSetNode<K>> nodeIterator() {
		return new TrieSet_BleedingEdgeNodeIterator<>(rootNode);
	}

	/*
	 * For analysis purposes only.
	 */
	protected int getNodeCount() {
		final Iterator<AbstractSetNode<K>> it = nodeIterator();
		int sumNodes = 0;

		for (; it.hasNext(); it.next()) {
			sumNodes += 1;
		}

		return sumNodes;
	}

	/*
	 * For analysis purposes only. Payload X Node
	 */
	protected int[][] arityCombinationsHistogram() {
		final Iterator<AbstractSetNode<K>> it = nodeIterator();
		final int[][] sumArityCombinations = new int[33][33];

		while (it.hasNext()) {
			final AbstractSetNode<K> node = it.next();
			sumArityCombinations[node.payloadArity()][node.nodeArity()] += 1;
		}

		return sumArityCombinations;
	}

	/*
	 * For analysis purposes only.
	 */
	protected int[] arityHistogram() {
		final int[][] sumArityCombinations = arityCombinationsHistogram();
		final int[] sumArity = new int[33];

		final int maxArity = 32; // TODO: factor out constant

		for (int j = 0; j <= maxArity; j++) {
			for (int maxRestArity = maxArity - j, k = 0; k <= maxRestArity - j; k++) {
				sumArity[j + k] += sumArityCombinations[j][k];
			}
		}

		return sumArity;
	}

	/*
	 * For analysis purposes only.
	 */
	public void printStatistics() {
		final int[][] sumArityCombinations = arityCombinationsHistogram();
		final int[] sumArity = arityHistogram();
		final int sumNodes = getNodeCount();

		final int[] cumsumArity = new int[33];
		for (int cumsum = 0, i = 0; i < 33; i++) {
			cumsum += sumArity[i];
			cumsumArity[i] = cumsum;
		}

		final float threshhold = 0.01f; // for printing results
		for (int i = 0; i < 33; i++) {
			float arityPercentage = (float) (sumArity[i]) / sumNodes;
			float cumsumArityPercentage = (float) (cumsumArity[i]) / sumNodes;

			if (arityPercentage != 0 && arityPercentage >= threshhold) {
				// details per level
				StringBuilder bldr = new StringBuilder();
				int max = i;
				for (int j = 0; j <= max; j++) {
					for (int k = max - j; k <= max - j; k++) {
						float arityCombinationsPercentage = (float) (sumArityCombinations[j][k])
										/ sumNodes;

						if (arityCombinationsPercentage != 0
										&& arityCombinationsPercentage >= threshhold) {
							bldr.append(String.format("%d/%d: %s, ", j, k, new DecimalFormat(
											"0.00%").format(arityCombinationsPercentage)));
						}
					}
				}
				final String detailPercentages = bldr.toString();

				// overview
				System.out.println(String.format("%2d: %s\t[cumsum = %s]\t%s", i,
								new DecimalFormat("0.00%").format(arityPercentage),
								new DecimalFormat("0.00%").format(cumsumArityPercentage),
								detailPercentages));
			}
		}
	}

	abstract static class Optional<T> {
		private static final Optional EMPTY = new Optional() {
			@Override
			boolean isPresent() {
				return false;
			}

			@Override
			Object get() {
				return null;
			}
		};

		@SuppressWarnings("unchecked")
		static <T> Optional<T> empty() {
			return EMPTY;
		}

		static <T> Optional<T> of(T value) {
			return new Value<T>(value);
		}

		abstract boolean isPresent();

		abstract T get();

		private static final class Value<T> extends Optional<T> {
			private final T value;

			private Value(T value) {
				this.value = value;
			}

			@Override
			boolean isPresent() {
				return true;
			}

			@Override
			T get() {
				return value;
			}
		}
	}

	static final class Result<K> {
		private K replacedValue;
		private boolean isModified;
		private boolean isReplaced;

		// update: inserted/removed single element, element count changed
		public void modified() {
			this.isModified = true;
		}

		public void updated(K replacedValue) {
			this.replacedValue = replacedValue;
			this.isModified = true;
			this.isReplaced = true;
		}

		// update: neither element, nor element count changed
		public static <K> Result<K> unchanged() {
			return new Result<>();
		}

		private Result() {
		}

		public boolean isModified() {
			return isModified;
		}

		public boolean hasReplacedValue() {
			return isReplaced;
		}

		public K getReplacedValue() {
			return replacedValue;
		}
	}

	protected static interface INode<K, V> {
	}

	protected static abstract class AbstractSetNode<K> implements INode<K, java.lang.Void> {

		static final int TUPLE_LENGTH = 1;

		abstract boolean containsKey(final K key, final int keyHash, final int shift);

		abstract boolean containsKey(final K key, final int keyHash, final int shift,
						final Comparator<Object> cmp);

		abstract Optional<K> findByKey(final K key, final int keyHash, final int shift);

		abstract Optional<K> findByKey(final K key, final int keyHash, final int shift,
						final Comparator<Object> cmp);

		abstract CompactSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details);

		abstract CompactSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details,
						final Comparator<Object> cmp);

		abstract CompactSetNode<K> removed(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details);

		abstract CompactSetNode<K> removed(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details,
						final Comparator<Object> cmp);

		static final boolean isAllowedToEdit(AtomicReference<Thread> x, AtomicReference<Thread> y) {
			return x != null && y != null && (x == y || x.get() == y.get());
		}

		abstract AbstractSetNode<K> getNode(final int index);

		abstract boolean hasNodes();

		abstract int nodeArity();

		@Deprecated
		Iterator<? extends AbstractSetNode<K>> nodeIterator() {
			return new Iterator<AbstractSetNode<K>>() {

				int nextIndex = 0;
				final int nodeArity = AbstractSetNode.this.nodeArity();

				@Override
				public void remove() {
					throw new UnsupportedOperationException();
				}

				@Override
				public AbstractSetNode<K> next() {
					if (!hasNext())
						throw new NoSuchElementException();
					return AbstractSetNode.this.getNode(nextIndex++);
				}

				@Override
				public boolean hasNext() {
					return nextIndex < nodeArity;
				}
			};
		}

		abstract K getKey(final int index);

		abstract boolean hasPayload();

		abstract int payloadArity();

		@Deprecated
		abstract java.lang.Object getSlot(final int index);

		abstract boolean hasSlots();

		abstract int slotArity();

		/**
		 * The arity of this trie node (i.e. number of values and nodes stored
		 * on this level).
		 * 
		 * @return sum of nodes and values stored within
		 */

		int arity() {
			return payloadArity() + nodeArity();
		}

		int size() {
			final Iterator<K> it = new SetKeyIterator<>(this);

			int size = 0;
			while (it.hasNext()) {
				size += 1;
				it.next();
			}

			return size;
		}

	}

	private static abstract class CompactSetNode<K> extends AbstractSetNode<K> {

		static final int HASH_CODE_LENGTH = 32;

		static final int BIT_PARTITION_SIZE = 5;
		static final int BIT_PARTITION_MASK = 0b11111;

		static final int mask(final int keyHash, final int shift) {
			return (keyHash >>> shift) & BIT_PARTITION_MASK;
		}

		static final int bitpos(final int mask) {
			return (int) (1L << mask);
		}

		abstract int nodeMap();

		abstract int dataMap();

		static final byte SIZE_EMPTY = 0b00;
		static final byte SIZE_ONE = 0b01;
		static final byte SIZE_MORE_THAN_ONE = 0b10;

		/**
		 * Abstract predicate over a node's size. Value can be either
		 * {@value #SIZE_EMPTY}, {@value #SIZE_ONE}, or
		 * {@value #SIZE_MORE_THAN_ONE}.
		 * 
		 * @return size predicate
		 */
		abstract byte sizePredicate();

		@Override
		abstract CompactSetNode<K> getNode(final int index);

		boolean nodeInvariant() {
			boolean inv1 = (size() - payloadArity() >= 2 * (arity() - payloadArity()));
			boolean inv2 = (this.arity() == 0) ? sizePredicate() == SIZE_EMPTY : true;
			boolean inv3 = (this.arity() == 1 && payloadArity() == 1) ? sizePredicate() == SIZE_ONE
							: true;
			boolean inv4 = (this.arity() >= 2) ? sizePredicate() == SIZE_MORE_THAN_ONE : true;

			boolean inv5 = (this.nodeArity() >= 0) && (this.payloadArity() >= 0)
							&& ((this.payloadArity() + this.nodeArity()) == this.arity());

			return inv1 && inv2 && inv3 && inv4 && inv5;
		}

		abstract CompactSetNode<K> copyAndInsertValue(AtomicReference<Thread> mutator,
						final int bitpos, final K key);

		abstract CompactSetNode<K> copyAndRemoveValue(AtomicReference<Thread> mutator,
						final int bitpos);

		abstract CompactSetNode<K> copyAndSetNode(AtomicReference<Thread> mutator,
						final int bitpos, CompactSetNode<K> node);

		abstract CompactSetNode<K> copyAndMigrateFromInlineToNode(
						final AtomicReference<Thread> mutator, final int bitpos,
						final CompactSetNode<K> node);

		abstract CompactSetNode<K> copyAndMigrateFromNodeToInline(
						final AtomicReference<Thread> mutator, final int bitpos,
						final CompactSetNode<K> node);

		/*
		 * TODO: specialize removed(..) to remove this method from this
		 * interface
		 */

		@SuppressWarnings("unchecked")
		static final <K> CompactSetNode<K> mergeTwoKeyValPairs(final K key0, final int keyHash0,
						final K key1, final int keyHash1, final int shift) {
			assert !(key0.equals(key1));

			if (shift >= HASH_CODE_LENGTH) {
				return new HashCollisionSetNode_BleedingEdge<>(keyHash0, (K[]) new Object[] { key0,
								key1 });
			}

			final int mask0 = mask(keyHash0, shift);
			final int mask1 = mask(keyHash1, shift);

			if (mask0 != mask1) {
				// both nodes fit on same level
				final int dataMap = (int) (bitpos(mask0) | bitpos(mask1));

				if (mask0 < mask1) {
					return nodeOf(null, (int) (0), dataMap, new Object[] { key0, key1 },
									(byte) (2), (byte) (0));
				} else {
					return nodeOf(null, (int) (0), dataMap, new Object[] { key1, key0 },
									(byte) (2), (byte) (0));
				}
			} else {
				final CompactSetNode<K> node = mergeTwoKeyValPairs(key0, keyHash0, key1, keyHash1,
								shift + BIT_PARTITION_SIZE);
				// values fit on next level

				final int nodeMap = bitpos(mask0);
				return nodeOf(null, nodeMap, (int) (0), new Object[] { node }, (byte) (0),
								(byte) (1));
			}
		}

		static final CompactSetNode EMPTY_NODE;

		static {

			EMPTY_NODE = new BitmapIndexedSetNode<>(null, (int) (0), (int) (0), new Object[] {},
							(byte) (0), (byte) (0));

		};

		static final <K> CompactSetNode<K> nodeOf(final AtomicReference<Thread> mutator,
						final int nodeMap, final int dataMap, final java.lang.Object[] nodes,
						final byte payloadArity, final byte nodeArity) {
			return new BitmapIndexedSetNode<>(mutator, nodeMap, dataMap, nodes, payloadArity,
							nodeArity);
		}

		@SuppressWarnings("unchecked")
		static final <K> CompactSetNode<K> nodeOf(AtomicReference<Thread> mutator) {
			return EMPTY_NODE;
		}

		static final <K> CompactSetNode<K> nodeOf(AtomicReference<Thread> mutator,
						final int nodeMap, final int dataMap, final K key) {
			assert nodeMap == 0;
			return nodeOf(mutator, (int) (0), dataMap, new Object[] { key }, (byte) (1), (byte) (0));
		}

		static final int index(final int bitmap, final int bitpos) {
			return java.lang.Integer.bitCount(bitmap & (bitpos - 1));
		}

		static final int index(final int bitmap, final int mask, final int bitpos) {
			return (bitmap == -1) ? mask : index(bitmap, bitpos);
		}

		int dataIndex(final int bitpos) {
			return java.lang.Integer.bitCount(dataMap() & (bitpos - 1));
		}

		int nodeIndex(final int bitpos) {
			return java.lang.Integer.bitCount(nodeMap() & (bitpos - 1));
		}

		K keyAt(final int bitpos) {
			return getKey(dataIndex(bitpos));
		}

		CompactSetNode<K> nodeAt(final int bitpos) {
			return getNode(nodeIndex(bitpos));
		}

		@Override
		boolean containsKey(final K key, final int keyHash, final int shift) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			final int dataMap = dataMap();
			if ((dataMap & bitpos) != 0) {
				final int index = index(dataMap, mask, bitpos);
				return getKey(index).equals(key);
			}

			final int nodeMap = nodeMap();
			if ((nodeMap & bitpos) != 0) {
				final int index = index(nodeMap, mask, bitpos);
				return getNode(index).containsKey(key, keyHash, shift + BIT_PARTITION_SIZE);
			}

			return false;
		}

		@Override
		boolean containsKey(final K key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			final int dataMap = dataMap();
			if ((dataMap & bitpos) != 0) {
				final int index = index(dataMap, mask, bitpos);
				return cmp.compare(getKey(index), key) == 0;
			}

			final int nodeMap = nodeMap();
			if ((nodeMap & bitpos) != 0) {
				final int index = index(nodeMap, mask, bitpos);
				return getNode(index).containsKey(key, keyHash, shift + BIT_PARTITION_SIZE, cmp);
			}

			return false;
		}

		@Override
		Optional<K> findByKey(final K key, final int keyHash, final int shift) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				if (keyAt(bitpos).equals(key)) {
					final K _key = keyAt(bitpos);

					return Optional.of(_key);
				}

				return Optional.empty();
			}

			if ((nodeMap() & bitpos) != 0) { // node (not value)
				final AbstractSetNode<K> subNode = nodeAt(bitpos);

				return subNode.findByKey(key, keyHash, shift + BIT_PARTITION_SIZE);
			}

			return Optional.empty();
		}

		@Override
		Optional<K> findByKey(final K key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				if (cmp.compare(keyAt(bitpos), key) == 0) {
					final K _key = keyAt(bitpos);

					return Optional.of(_key);
				}

				return Optional.empty();
			}

			if ((nodeMap() & bitpos) != 0) { // node (not value)
				final AbstractSetNode<K> subNode = nodeAt(bitpos);

				return subNode.findByKey(key, keyHash, shift + BIT_PARTITION_SIZE, cmp);
			}

			return Optional.empty();
		}

		@Override
		CompactSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);
				final K currentKey = getKey(dataIndex);

				if (currentKey.equals(key)) {
					return this;
				} else {

					final CompactSetNode<K> subNodeNew = mergeTwoKeyValPairs(currentKey,
									improve(currentKey.hashCode()), key, keyHash, shift
													+ BIT_PARTITION_SIZE);

					details.modified();
					return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew);

				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactSetNode<K> subNode = nodeAt(bitpos);
				final CompactSetNode<K> subNodeNew = subNode.updated(mutator, key, keyHash, shift
								+ BIT_PARTITION_SIZE, details);

				if (details.isModified()) {
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				} else {
					return this;
				}
			} else {
				// no value
				details.modified();
				return copyAndInsertValue(mutator, bitpos, key);
			}
		}

		@Override
		CompactSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details,
						final Comparator<Object> cmp) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);
				final K currentKey = getKey(dataIndex);

				if (cmp.compare(currentKey, key) == 0) {
					return this;
				} else {

					final CompactSetNode<K> subNodeNew = mergeTwoKeyValPairs(currentKey,
									improve(currentKey.hashCode()), key, keyHash, shift
													+ BIT_PARTITION_SIZE);

					details.modified();
					return copyAndMigrateFromInlineToNode(mutator, bitpos, subNodeNew);

				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactSetNode<K> subNode = nodeAt(bitpos);
				final CompactSetNode<K> subNodeNew = subNode.updated(mutator, key, keyHash, shift
								+ BIT_PARTITION_SIZE, details, cmp);

				if (details.isModified()) {
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				} else {
					return this;
				}
			} else {
				// no value
				details.modified();
				return copyAndInsertValue(mutator, bitpos, key);
			}
		}

		@Override
		CompactSetNode<K> removed(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);

				if (getKey(dataIndex).equals(key)) {
					details.modified();

					if (this.payloadArity() == 2 && this.nodeArity() == 0) {
						/*
						 * Create new node with remaining pair. The new node
						 * will a) either become the new root returned, or b)
						 * unwrapped and inlined during returning.
						 */
						final int newDataMap = (shift == 0) ? (int) (dataMap() ^ bitpos)
										: bitpos(mask(keyHash, 0));

						if (dataIndex == 0) {
							return CompactSetNode.<K> nodeOf(mutator, (int) 0, newDataMap,
											getKey(1));
						} else {
							return CompactSetNode.<K> nodeOf(mutator, (int) 0, newDataMap,
											getKey(0));
						}
					} else {
						return copyAndRemoveValue(mutator, bitpos);
					}
				} else {
					return this;
				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactSetNode<K> subNode = nodeAt(bitpos);
				final CompactSetNode<K> subNodeNew = subNode.removed(mutator, key, keyHash, shift
								+ BIT_PARTITION_SIZE, details);

				if (!details.isModified()) {
					return this;
				}

				switch (subNodeNew.sizePredicate()) {
				case 0: {
					throw new IllegalStateException("Sub-node must have at least one element.");
				}
				case 1: {
					if (this.payloadArity() == 0 && this.nodeArity() == 1) {
						// escalate (singleton or empty) result
						return subNodeNew;
					} else {
						// inline value (move to front)
						return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew);
					}
				}
				default: {
					// modify current node (set replacement node)
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				}
				}
			}

			return this;
		}

		@Override
		CompactSetNode<K> removed(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details,
						final Comparator<Object> cmp) {
			final int mask = mask(keyHash, shift);
			final int bitpos = bitpos(mask);

			if ((dataMap() & bitpos) != 0) { // inplace value
				final int dataIndex = dataIndex(bitpos);

				if (cmp.compare(getKey(dataIndex), key) == 0) {
					details.modified();

					if (this.payloadArity() == 2 && this.nodeArity() == 0) {
						/*
						 * Create new node with remaining pair. The new node
						 * will a) either become the new root returned, or b)
						 * unwrapped and inlined during returning.
						 */
						final int newDataMap = (shift == 0) ? (int) (dataMap() ^ bitpos)
										: bitpos(mask(keyHash, 0));

						if (dataIndex == 0) {
							return CompactSetNode.<K> nodeOf(mutator, (int) 0, newDataMap,
											getKey(1));
						} else {
							return CompactSetNode.<K> nodeOf(mutator, (int) 0, newDataMap,
											getKey(0));
						}
					} else {
						return copyAndRemoveValue(mutator, bitpos);
					}
				} else {
					return this;
				}
			} else if ((nodeMap() & bitpos) != 0) { // node (not value)
				final CompactSetNode<K> subNode = nodeAt(bitpos);
				final CompactSetNode<K> subNodeNew = subNode.removed(mutator, key, keyHash, shift
								+ BIT_PARTITION_SIZE, details, cmp);

				if (!details.isModified()) {
					return this;
				}

				switch (subNodeNew.sizePredicate()) {
				case 0: {
					throw new IllegalStateException("Sub-node must have at least one element.");
				}
				case 1: {
					if (this.payloadArity() == 0 && this.nodeArity() == 1) {
						// escalate (singleton or empty) result
						return subNodeNew;
					} else {
						// inline value (move to front)
						return copyAndMigrateFromNodeToInline(mutator, bitpos, subNodeNew);
					}
				}
				default: {
					// modify current node (set replacement node)
					return copyAndSetNode(mutator, bitpos, subNodeNew);
				}
				}
			}

			return this;
		}

		/**
		 * @return 0 <= mask <= 2^BIT_PARTITION_SIZE - 1
		 */
		static byte recoverMask(int map, byte i_th) {
			assert 1 <= i_th && i_th <= 32;

			byte cnt1 = 0;
			byte mask = 0;

			while (mask < 32) {
				if ((map & 0x01) == 0x01) {
					cnt1 += 1;

					if (cnt1 == i_th) {
						return mask;
					}
				}

				map = (int) (map >> 1);
				mask += 1;
			}

			assert cnt1 != i_th;
			throw new RuntimeException("Called with invalid arguments.");
		}

		@Override
		public String toString() {
			final StringBuilder bldr = new StringBuilder();
			bldr.append('[');

			for (byte i = 0; i < payloadArity(); i++) {
				final byte pos = recoverMask(dataMap(), (byte) (i + 1));
				bldr.append(String.format("@%d: ", pos, getKey(i)));

				if (!((i + 1) == payloadArity())) {
					bldr.append(", ");
				}
			}

			if (payloadArity() > 0 && nodeArity() > 0) {
				bldr.append(", ");
			}

			for (byte i = 0; i < nodeArity(); i++) {
				final byte pos = recoverMask(nodeMap(), (byte) (i + 1));
				bldr.append(String.format("@%d: %s", pos, getNode(i)));

				if (!((i + 1) == nodeArity())) {
					bldr.append(", ");
				}
			}

			bldr.append(']');
			return bldr.toString();
		}

	}

	private static abstract class CompactMixedSetNode<K> extends CompactSetNode<K> {

		private final int nodeMap;
		private final int dataMap;

		CompactMixedSetNode(final AtomicReference<Thread> mutator, final int nodeMap,
						final int dataMap) {
			this.nodeMap = nodeMap;
			this.dataMap = dataMap;
		}

		@Override
		public int nodeMap() {
			return nodeMap;
		}

		@Override
		public int dataMap() {
			return dataMap;
		}

	}

	private static final class BitmapIndexedSetNode<K> extends CompactMixedSetNode<K> {

		final AtomicReference<Thread> mutator;
		final java.lang.Object[] nodes;
		final byte payloadArity;
		final byte nodeArity;

		private BitmapIndexedSetNode(final AtomicReference<Thread> mutator, final int nodeMap,
						final int dataMap, final java.lang.Object[] nodes, final byte payloadArity,
						final byte nodeArity) {
			super(mutator, nodeMap, dataMap);

			this.mutator = mutator;
			this.nodes = nodes;
			this.payloadArity = payloadArity;
			this.nodeArity = nodeArity;

			if (DEBUG) {
				assert (payloadArity == java.lang.Integer.bitCount(dataMap));

				assert (TUPLE_LENGTH * java.lang.Integer.bitCount(dataMap)
								+ java.lang.Integer.bitCount(nodeMap) == nodes.length);

				for (int i = 0; i < TUPLE_LENGTH * payloadArity(); i++) {
					assert ((nodes[i] instanceof CompactSetNode) == false);
				}
				for (int i = TUPLE_LENGTH * payloadArity(); i < nodes.length; i++) {
					assert ((nodes[i] instanceof CompactSetNode) == true);
				}
			}

			assert nodeInvariant();
		}

		@SuppressWarnings("unchecked")
		@Override
		K getKey(final int index) {
			return (K) nodes[TUPLE_LENGTH * index];
		}

		@SuppressWarnings("unchecked")
		@Override
		CompactSetNode<K> getNode(final int index) {
			final int offset = TUPLE_LENGTH * payloadArity;
			return (CompactSetNode<K>) nodes[offset + index];
		}

		@Override
		boolean hasPayload() {
			return payloadArity != 0;
		}

		@Override
		int payloadArity() {
			return payloadArity;
		}

		@Override
		boolean hasNodes() {
			return nodeArity != 0;
		}

		@Override
		int nodeArity() {
			return nodeArity;
		}

		@Override
		java.lang.Object getSlot(final int index) {
			return nodes[index];
		}

		@Override
		boolean hasSlots() {
			return nodes.length != 0;
		}

		@Override
		int slotArity() {
			return nodes.length;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 0;
			result = prime * result + ((int) dataMap());
			result = prime * result + ((int) dataMap());
			result = prime * result + Arrays.hashCode(nodes);
			return result;
		}

		@Override
		public boolean equals(final java.lang.Object other) {
			if (null == other) {
				return false;
			}
			if (this == other) {
				return true;
			}
			if (getClass() != other.getClass()) {
				return false;
			}
			BitmapIndexedSetNode<?> that = (BitmapIndexedSetNode<?>) other;
			if (nodeMap() != that.nodeMap()) {
				return false;
			}
			if (dataMap() != that.dataMap()) {
				return false;
			}
			if (!Arrays.equals(nodes, that.nodes)) {
				return false;
			}
			return true;
		}

		@Override
		byte sizePredicate() {
			if (this.nodeArity() == 0) {
				switch (this.payloadArity()) {
				case 0:
					return SIZE_EMPTY;
				case 1:
					return SIZE_ONE;
				default:
					return SIZE_MORE_THAN_ONE;
				}
			} else {
				return SIZE_MORE_THAN_ONE;
			}
		}

		@Override
		CompactSetNode<K> copyAndSetNode(final AtomicReference<Thread> mutator, final int bitpos,
						final CompactSetNode<K> node) {

			final int idx = TUPLE_LENGTH * payloadArity + nodeIndex(bitpos);

			if (isAllowedToEdit(this.mutator, mutator)) {
				// no copying if already editable
				this.nodes[idx] = node;
				return this;
			} else {
				final java.lang.Object[] src = this.nodes;
				final java.lang.Object[] dst = (java.lang.Object[]) new Object[src.length];

				// copy 'src' and set 1 element(s) at position 'idx'
				System.arraycopy(src, 0, dst, 0, src.length);
				dst[idx + 0] = node;

				return nodeOf(mutator, nodeMap(), dataMap(), dst, payloadArity, nodeArity);
			}
		}

		@Override
		CompactSetNode<K> copyAndInsertValue(final AtomicReference<Thread> mutator,
						final int bitpos, final K key) {
			final int idx = TUPLE_LENGTH * dataIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = (java.lang.Object[]) new Object[src.length + 1];

			// copy 'src' and insert 1 element(s) at position 'idx'
			System.arraycopy(src, 0, dst, 0, idx);
			dst[idx + 0] = key;
			System.arraycopy(src, idx, dst, idx + 1, src.length - idx);

			return nodeOf(mutator, nodeMap(), (int) (dataMap() | bitpos), dst,
							(byte) (payloadArity + 1), nodeArity);
		}

		@Override
		CompactSetNode<K> copyAndRemoveValue(final AtomicReference<Thread> mutator, final int bitpos) {
			final int idx = TUPLE_LENGTH * dataIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = (java.lang.Object[]) new Object[src.length - 1];

			// copy 'src' and remove 1 element(s) at position 'idx'
			System.arraycopy(src, 0, dst, 0, idx);
			System.arraycopy(src, idx + 1, dst, idx, src.length - idx - 1);

			return nodeOf(mutator, nodeMap(), (int) (dataMap() ^ bitpos), dst,
							(byte) (payloadArity - 1), nodeArity);
		}

		@Override
		CompactSetNode<K> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
						final int bitpos, final CompactSetNode<K> node) {

			final int idxOld = TUPLE_LENGTH * dataIndex(bitpos);
			final int idxNew = TUPLE_LENGTH * (payloadArity - 1) + nodeIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = new Object[src.length - 1 + 1];

			// copy 'src' and remove 1 element(s) at position 'idxOld' and
			// insert 1 element(s) at position 'idxNew' (TODO: carefully test)
			assert idxOld <= idxNew;
			System.arraycopy(src, 0, dst, 0, idxOld);
			System.arraycopy(src, idxOld + 1, dst, idxOld, idxNew - idxOld);
			dst[idxNew + 0] = node;
			System.arraycopy(src, idxNew + 1, dst, idxNew + 1, src.length - idxNew - 1);

			return nodeOf(mutator, (int) (nodeMap() | bitpos), (int) (dataMap() ^ bitpos), dst,
							(byte) (payloadArity - 1), (byte) (nodeArity + 1));
		}

		@Override
		CompactSetNode<K> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
						final int bitpos, final CompactSetNode<K> node) {

			final int idxOld = TUPLE_LENGTH * payloadArity + nodeIndex(bitpos);
			final int idxNew = dataIndex(bitpos);

			final java.lang.Object[] src = this.nodes;
			final java.lang.Object[] dst = new Object[src.length - 1 + 1];

			// copy 'src' and remove 1 element(s) at position 'idxOld' and
			// insert 1 element(s) at position 'idxNew' (TODO: carefully test)
			assert idxOld >= idxNew;
			System.arraycopy(src, 0, dst, 0, idxNew);
			dst[idxNew + 0] = node.getKey(0);
			System.arraycopy(src, idxNew, dst, idxNew + 1, idxOld - idxNew);
			System.arraycopy(src, idxOld + 1, dst, idxOld + 1, src.length - idxOld - 1);

			return nodeOf(mutator, (int) (nodeMap() ^ bitpos), (int) (dataMap() | bitpos), dst,
							(byte) (payloadArity + 1), (byte) (nodeArity - 1));
		}

	}

	private static final class HashCollisionSetNode_BleedingEdge<K> extends CompactSetNode<K> {
		private final K[] keys;

		private final int hash;

		HashCollisionSetNode_BleedingEdge(final int hash, final K[] keys) {
			this.keys = keys;

			this.hash = hash;

			assert payloadArity() >= 2;
		}

		@Override
		boolean containsKey(final K key, final int keyHash, final int shift) {

			if (this.hash == keyHash) {
				for (K k : keys) {
					if (k.equals(key)) {
						return true;
					}
				}
			}
			return false;

		}

		@Override
		boolean containsKey(final K key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {

			if (this.hash == keyHash) {
				for (K k : keys) {
					if (cmp.compare(k, key) == 0) {
						return true;
					}
				}
			}
			return false;

		}

		@Override
		Optional<K> findByKey(final K key, final int keyHash, final int shift) {

			for (int i = 0; i < keys.length; i++) {
				final K _key = keys[i];
				if (key.equals(_key)) {
					return Optional.of(_key);
				}
			}
			return Optional.empty();

		}

		@Override
		Optional<K> findByKey(final K key, final int keyHash, final int shift,
						final Comparator<Object> cmp) {

			for (int i = 0; i < keys.length; i++) {
				final K _key = keys[i];
				if (cmp.compare(key, _key) == 0) {
					return Optional.of(_key);
				}
			}
			return Optional.empty();

		}

		@Override
		CompactSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details) {
			assert this.hash == keyHash;

			for (int idx = 0; idx < keys.length; idx++) {
				if (keys[idx].equals(key)) {

					return this;

				}
			}

			@SuppressWarnings("unchecked")
			final K[] keysNew = (K[]) new Object[this.keys.length + 1];

			// copy 'this.keys' and insert 1 element(s) at position
			// 'keys.length'
			System.arraycopy(this.keys, 0, keysNew, 0, keys.length);
			keysNew[keys.length + 0] = key;
			System.arraycopy(this.keys, keys.length, keysNew, keys.length + 1, this.keys.length
							- keys.length);

			details.modified();
			return new HashCollisionSetNode_BleedingEdge<>(keyHash, keysNew);
		}

		@Override
		CompactSetNode<K> updated(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details,
						final Comparator<Object> cmp) {
			assert this.hash == keyHash;

			for (int idx = 0; idx < keys.length; idx++) {
				if (cmp.compare(keys[idx], key) == 0) {

					return this;

				}
			}

			@SuppressWarnings("unchecked")
			final K[] keysNew = (K[]) new Object[this.keys.length + 1];

			// copy 'this.keys' and insert 1 element(s) at position
			// 'keys.length'
			System.arraycopy(this.keys, 0, keysNew, 0, keys.length);
			keysNew[keys.length + 0] = key;
			System.arraycopy(this.keys, keys.length, keysNew, keys.length + 1, this.keys.length
							- keys.length);

			details.modified();
			return new HashCollisionSetNode_BleedingEdge<>(keyHash, keysNew);
		}

		@Override
		CompactSetNode<K> removed(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details) {

			for (int idx = 0; idx < keys.length; idx++) {
				if (keys[idx].equals(key)) {

					if (this.arity() == 1) {
						return nodeOf(mutator);
					} else if (this.arity() == 2) {
						/*
						 * Create root node with singleton element. This node
						 * will be a) either be the new root returned, or b)
						 * unwrapped and inlined.
						 */
						final K theOtherKey = (idx == 0) ? keys[1] : keys[0];

						return CompactSetNode.<K> nodeOf(mutator).updated(mutator, theOtherKey,
										keyHash, 0, details);
					} else {
						@SuppressWarnings("unchecked")
						final K[] keysNew = (K[]) new Object[this.keys.length - 1];

						// copy 'this.keys' and remove 1 element(s) at position
						// 'idx'
						System.arraycopy(this.keys, 0, keysNew, 0, idx);
						System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx
										- 1);

						return new HashCollisionSetNode_BleedingEdge<>(keyHash, keysNew);
					}
				}
			}
			return this;

		}

		@Override
		CompactSetNode<K> removed(final AtomicReference<Thread> mutator, final K key,
						final int keyHash, final int shift, final Result<K> details,
						final Comparator<Object> cmp) {

			for (int idx = 0; idx < keys.length; idx++) {
				if (cmp.compare(keys[idx], key) == 0) {

					if (this.arity() == 1) {
						return nodeOf(mutator);
					} else if (this.arity() == 2) {
						/*
						 * Create root node with singleton element. This node
						 * will be a) either be the new root returned, or b)
						 * unwrapped and inlined.
						 */
						final K theOtherKey = (idx == 0) ? keys[1] : keys[0];

						return CompactSetNode.<K> nodeOf(mutator).updated(mutator, theOtherKey,
										keyHash, 0, details, cmp);
					} else {
						@SuppressWarnings("unchecked")
						final K[] keysNew = (K[]) new Object[this.keys.length - 1];

						// copy 'this.keys' and remove 1 element(s) at position
						// 'idx'
						System.arraycopy(this.keys, 0, keysNew, 0, idx);
						System.arraycopy(this.keys, idx + 1, keysNew, idx, this.keys.length - idx
										- 1);

						return new HashCollisionSetNode_BleedingEdge<>(keyHash, keysNew);
					}
				}
			}
			return this;

		}

		@Override
		boolean hasPayload() {
			return true;
		}

		@Override
		int payloadArity() {
			return keys.length;
		}

		@Override
		boolean hasNodes() {
			return false;
		}

		@Override
		int nodeArity() {
			return 0;
		}

		@Override
		int arity() {
			return payloadArity();
		}

		@Override
		byte sizePredicate() {
			return SIZE_MORE_THAN_ONE;
		}

		@Override
		K getKey(int index) {
			return keys[index];
		}

		@Override
		public CompactSetNode<K> getNode(int index) {
			throw new IllegalStateException("Is leaf node.");
		}

		@Override
		java.lang.Object getSlot(final int index) {
			throw new UnsupportedOperationException();
		}

		@Override
		boolean hasSlots() {
			throw new UnsupportedOperationException();
		}

		@Override
		int slotArity() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 0;
			result = prime * result + hash;
			result = prime * result + Arrays.hashCode(keys);
			return result;
		}

		@Override
		public boolean equals(Object other) {
			if (null == other) {
				return false;
			}
			if (this == other) {
				return true;
			}
			if (getClass() != other.getClass()) {
				return false;
			}

			HashCollisionSetNode_BleedingEdge<?> that = (HashCollisionSetNode_BleedingEdge<?>) other;

			if (hash != that.hash) {
				return false;
			}

			if (arity() != that.arity()) {
				return false;
			}

			/*
			 * Linear scan for each key, because of arbitrary element order.
			 */
			outerLoop: for (int i = 0; i < that.payloadArity(); i++) {
				final java.lang.Object otherKey = that.getKey(i);

				for (int j = 0; j < keys.length; j++) {
					final K key = keys[j];

					if (key.equals(otherKey)) {
						continue outerLoop;
					}
				}
				return false;

			}

			return true;
		}

		@Override
		CompactSetNode<K> copyAndInsertValue(AtomicReference<Thread> mutator, final int bitpos,
						final K key) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactSetNode<K> copyAndRemoveValue(AtomicReference<Thread> mutator, final int bitpos) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactSetNode<K> copyAndSetNode(AtomicReference<Thread> mutator, final int bitpos,
						CompactSetNode<K> node) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactSetNode<K> copyAndMigrateFromInlineToNode(final AtomicReference<Thread> mutator,
						final int bitpos, final CompactSetNode<K> node) {
			throw new UnsupportedOperationException();
		}

		@Override
		CompactSetNode<K> copyAndMigrateFromNodeToInline(final AtomicReference<Thread> mutator,
						final int bitpos, final CompactSetNode<K> node) {
			throw new UnsupportedOperationException();
		}

		@Override
		int nodeMap() {
			throw new UnsupportedOperationException();
		}

		@Override
		int dataMap() {
			throw new UnsupportedOperationException();
		}

	}

	/**
	 * Iterator skeleton that uses a fixed stack in depth.
	 */
	private static abstract class AbstractSetIterator<K> {

		private static final int MAX_DEPTH = 7;

		protected int currentValueCursor;
		protected int currentValueLength;
		protected AbstractSetNode<K> currentValueNode;

		private int currentStackLevel = -1;
		private final int[] nodeCursorsAndLengths = new int[MAX_DEPTH * 2];

		@SuppressWarnings("unchecked")
		AbstractSetNode<K>[] nodes = new AbstractSetNode[MAX_DEPTH];

		AbstractSetIterator(AbstractSetNode<K> rootNode) {
			if (rootNode.hasNodes()) {
				currentStackLevel = 0;

				nodes[0] = rootNode;
				nodeCursorsAndLengths[0] = 0;
				nodeCursorsAndLengths[1] = rootNode.nodeArity();
			}

			if (rootNode.hasPayload()) {
				currentValueNode = rootNode;
				currentValueCursor = 0;
				currentValueLength = rootNode.payloadArity();
			}
		}

		/*
		 * search for next node that contains values
		 */
		private boolean searchNextValueNode() {
			while (currentStackLevel >= 0) {
				final int currentCursorIndex = currentStackLevel * 2;
				final int currentLengthIndex = currentCursorIndex + 1;

				final int nodeCursor = nodeCursorsAndLengths[currentCursorIndex];
				final int nodeLength = nodeCursorsAndLengths[currentLengthIndex];

				if (nodeCursor < nodeLength) {
					final AbstractSetNode<K> nextNode = nodes[currentStackLevel]
									.getNode(nodeCursor);
					nodeCursorsAndLengths[currentCursorIndex]++;

					if (nextNode.hasNodes()) {
						/*
						 * put node on next stack level for depth-first
						 * traversal
						 */
						final int nextStackLevel = ++currentStackLevel;
						final int nextCursorIndex = nextStackLevel * 2;
						final int nextLengthIndex = nextCursorIndex + 1;

						nodes[nextStackLevel] = nextNode;
						nodeCursorsAndLengths[nextCursorIndex] = 0;
						nodeCursorsAndLengths[nextLengthIndex] = nextNode.nodeArity();
					}

					if (nextNode.hasPayload()) {
						/*
						 * found next node that contains values
						 */
						currentValueNode = nextNode;
						currentValueCursor = 0;
						currentValueLength = nextNode.payloadArity();
						return true;
					}
				} else {
					currentStackLevel--;
				}
			}

			return false;
		}

		public boolean hasNext() {
			if (currentValueCursor < currentValueLength) {
				return true;
			} else {
				return searchNextValueNode();
			}
		}

		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	private static final class SetKeyIterator<K> extends AbstractSetIterator<K> implements
					Iterator<K> {

		SetKeyIterator(AbstractSetNode<K> rootNode) {
			super(rootNode);
		}

		@Override
		public K next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			} else {
				return currentValueNode.getKey(currentValueCursor++);
			}
		}

	}

	/**
	 * Iterator that first iterates over inlined-values and then continues depth
	 * first recursively.
	 */
	private static class TrieSet_BleedingEdgeNodeIterator<K> implements
					Iterator<AbstractSetNode<K>> {

		final Deque<Iterator<? extends AbstractSetNode<K>>> nodeIteratorStack;

		TrieSet_BleedingEdgeNodeIterator(AbstractSetNode<K> rootNode) {
			nodeIteratorStack = new ArrayDeque<>();
			nodeIteratorStack.push(Collections.singleton(rootNode).iterator());
		}

		@Override
		public boolean hasNext() {
			while (true) {
				if (nodeIteratorStack.isEmpty()) {
					return false;
				} else {
					if (nodeIteratorStack.peek().hasNext()) {
						return true;
					} else {
						nodeIteratorStack.pop();
						continue;
					}
				}
			}
		}

		@Override
		public AbstractSetNode<K> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}

			AbstractSetNode<K> innerNode = nodeIteratorStack.peek().next();

			if (innerNode.hasNodes()) {
				nodeIteratorStack.push(innerNode.nodeIterator());
			}

			return innerNode;
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	static final class TransientTrieSet_BleedingEdge<K> extends AbstractSet<K> implements
					TransientSet<K> {
		final private AtomicReference<Thread> mutator;
		private AbstractSetNode<K> rootNode;
		private int hashCode;
		private int cachedSize;

		TransientTrieSet_BleedingEdge(TrieSet_BleedingEdge<K> trieSet_BleedingEdge) {
			this.mutator = new AtomicReference<Thread>(Thread.currentThread());
			this.rootNode = trieSet_BleedingEdge.rootNode;
			this.hashCode = trieSet_BleedingEdge.hashCode;
			this.cachedSize = trieSet_BleedingEdge.cachedSize;
			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
		}

		private boolean checkHashCodeAndSize(final int targetHash, final int targetSize) {
			int hash = 0;
			int size = 0;

			for (Iterator<K> it = keyIterator(); it.hasNext();) {
				final K key = it.next();

				hash += key.hashCode();
				size += 1;
			}

			return hash == targetHash && size == targetSize;
		}

		@Override
		public boolean contains(Object o) {
			try {
				@SuppressWarnings("unchecked")
				final K key = (K) o;
				return rootNode.containsKey(key, improve(key.hashCode()), 0);
			} catch (ClassCastException unused) {
				return false;
			}
		}

		@Override
		public boolean containsEquivalent(Object o, Comparator<Object> cmp) {
			try {
				@SuppressWarnings("unchecked")
				final K key = (K) o;
				return rootNode.containsKey(key, improve(key.hashCode()), 0, cmp);
			} catch (ClassCastException unused) {
				return false;
			}
		}

		@Override
		public K get(Object o) {
			try {
				@SuppressWarnings("unchecked")
				final K key = (K) o;
				final Optional<K> result = rootNode.findByKey(key, improve(key.hashCode()), 0);

				if (result.isPresent()) {
					return result.get();
				} else {
					return null;
				}
			} catch (ClassCastException unused) {
				return null;
			}
		}

		@Override
		public K getEquivalent(Object o, Comparator<Object> cmp) {
			try {
				@SuppressWarnings("unchecked")
				final K key = (K) o;
				final Optional<K> result = rootNode.findByKey(key, improve(key.hashCode()), 0, cmp);

				if (result.isPresent()) {
					return result.get();
				} else {
					return null;
				}
			} catch (ClassCastException unused) {
				return null;
			}
		}

		@Override
		public boolean __insert(final K key) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			final int keyHash = key.hashCode();
			final Result<K> details = Result.unchanged();

			final CompactSetNode<K> newRootNode = rootNode.updated(mutator, key, improve(keyHash),
							0, details);

			if (details.isModified()) {
				rootNode = newRootNode;

				hashCode += keyHash;
				cachedSize += 1;

				if (DEBUG) {
					assert checkHashCodeAndSize(hashCode, cachedSize);
				}
				return true;
			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return false;
		}

		@Override
		public boolean __insertEquivalent(final K key, final Comparator<Object> cmp) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			final int keyHash = key.hashCode();
			final Result<K> details = Result.unchanged();

			final CompactSetNode<K> newRootNode = rootNode.updated(mutator, key, improve(keyHash),
							0, details, cmp);

			if (details.isModified()) {
				rootNode = newRootNode;

				hashCode += keyHash;
				cachedSize += 1;

				if (DEBUG) {
					assert checkHashCodeAndSize(hashCode, cachedSize);
				}
				return true;
			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return false;
		}

		@Override
		public boolean __insertAll(final ImmutableSet<? extends K> set) {
			boolean modified = false;

			for (final K key : set) {
				modified |= __insert(key);
			}

			return modified;
		}

		@Override
		public boolean __insertAllEquivalent(final ImmutableSet<? extends K> set,
						final Comparator<Object> cmp) {
			boolean modified = false;

			for (final K key : set) {
				modified |= __insertEquivalent(key, cmp);
			}

			return modified;
		}

		@Override
		public boolean __removeAll(final ImmutableSet<? extends K> set) {
			boolean modified = false;

			for (final K key : set) {
				modified |= __remove(key);
			}

			return modified;
		}

		@Override
		public boolean __removeAllEquivalent(final ImmutableSet<? extends K> set,
						final Comparator<Object> cmp) {
			boolean modified = false;

			for (final K key : set) {
				modified |= __removeEquivalent(key, cmp);
			}

			return modified;
		}

		@Override
		public boolean __remove(final K key) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");

			}

			final int keyHash = key.hashCode();
			final Result<K> details = Result.unchanged();

			final CompactSetNode<K> newRootNode = rootNode.removed(mutator, key, improve(keyHash),
							0, details);

			if (details.isModified()) {

				rootNode = newRootNode;
				hashCode -= keyHash;
				cachedSize -= 1;

				if (DEBUG) {
					assert checkHashCodeAndSize(hashCode, cachedSize);
				}
				return true;

			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return false;
		}

		@Override
		public boolean __removeEquivalent(final K key, Comparator<Object> cmp) {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			final int keyHash = key.hashCode();
			final Result<K> details = Result.unchanged();

			final CompactSetNode<K> newRootNode = rootNode.removed(mutator, key, improve(keyHash),
							0, details, cmp);

			if (details.isModified()) {

				rootNode = newRootNode;
				hashCode -= keyHash;
				cachedSize -= 1;

				if (DEBUG) {
					assert checkHashCodeAndSize(hashCode, cachedSize);
				}
				return true;

			}

			if (DEBUG) {
				assert checkHashCodeAndSize(hashCode, cachedSize);
			}
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			for (Object item : c) {
				if (!contains(item)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean containsAllEquivalent(Collection<?> c, Comparator<Object> cmp) {
			for (Object item : c) {
				if (!containsEquivalent(item, cmp)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public boolean __retainAll(ImmutableSet<? extends K> set) {
			boolean modified = false;

			Iterator<K> thisIterator = iterator();
			while (thisIterator.hasNext()) {
				if (!set.contains(thisIterator.next())) {
					thisIterator.remove();
					modified = true;
				}
			}

			return modified;
		}

		@Override
		public boolean __retainAllEquivalent(ImmutableSet<? extends K> set, Comparator<Object> cmp) {
			boolean modified = false;

			Iterator<K> thisIterator = iterator();
			while (thisIterator.hasNext()) {
				if (!set.containsEquivalent(thisIterator.next(), cmp)) {
					thisIterator.remove();
					modified = true;
				}
			}

			return modified;
		}

		@Override
		public int size() {
			return cachedSize;
		}

		@Override
		public Iterator<K> iterator() {
			return keyIterator();
		}

		@Override
		public Iterator<K> keyIterator() {
			return new TransientSetKeyIterator<>(this);
		}

		/**
		 * Iterator that first iterates over inlined-values and then continues
		 * depth first recursively.
		 */
		private static class TransientSetKeyIterator<K> extends AbstractSetIterator<K> implements
						Iterator<K> {

			final TransientTrieSet_BleedingEdge<K> transientTrieSet_BleedingEdge;
			K lastKey;

			TransientSetKeyIterator(TransientTrieSet_BleedingEdge<K> transientTrieSet_BleedingEdge) {
				super(transientTrieSet_BleedingEdge.rootNode);
				this.transientTrieSet_BleedingEdge = transientTrieSet_BleedingEdge;
			}

			@Override
			public K next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				} else {
					lastKey = currentValueNode.getKey(currentValueCursor++);
					return lastKey;
				}
			}

			/*
			 * TODO: test removal with iteration rigorously
			 */
			@Override
			public void remove() {
				boolean success = transientTrieSet_BleedingEdge.__remove(lastKey);

				if (!success) {
					throw new IllegalStateException("Key from iteration couldn't be deleted.");
				}
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			}
			if (other == null) {
				return false;
			}

			if (other instanceof TransientTrieSet_BleedingEdge) {
				TransientTrieSet_BleedingEdge<?> that = (TransientTrieSet_BleedingEdge<?>) other;

				if (this.size() != that.size()) {
					return false;
				}

				return rootNode.equals(that.rootNode);
			}

			return super.equals(other);
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public ImmutableSet<K> freeze() {
			if (mutator.get() == null) {
				throw new IllegalStateException("Transient already frozen.");
			}

			mutator.set(null);
			return new TrieSet_BleedingEdge<K>(rootNode, hashCode, cachedSize);
		}
	}

}