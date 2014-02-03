/*******************************************************************************
 * Copyright (c) 2013 CWI
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

import java.util.Comparator;

public interface TransientSet<E> extends Iterable<E> { // extends ImmutableCollection<E>, Set<E> {

    boolean contains(Object o);
    
	boolean containsEquivalent(Object o, Comparator<Object> cmp);
	
	boolean __insert(E e);
	
	boolean __insertEquivalent(E e, Comparator<Object> cmp);

	boolean __insertAll(ImmutableSet<? extends E> set);	
	
	boolean __insertAllEquivalent(ImmutableSet<? extends E> set, Comparator<Object> cmp);

	boolean __insertAllEquivalent(ImmutableSet<? extends E> set, Comparator<Object> cmp, Consumer<E> onSuccess, Consumer<E> onFailure);
	
	boolean __retainAll(ImmutableSet<? extends E> set);
	
	boolean __retainAllEquivalent(ImmutableSet<? extends E> set, Comparator<Object> cmp);
	
	boolean __retainAllEquivalent(ImmutableSet<? extends E> set, Comparator<Object> cmp, Consumer<E> onSuccess, Consumer<E> onFailure);
	
	boolean __remove(E e);
	
	boolean __removeEquivalent(E e, Comparator<Object> cmp);

	boolean __removeAll(ImmutableSet<? extends E> set);
	
	boolean __removeAllEquivalent(ImmutableSet<? extends E> set, Comparator<Object> cmp);
	
	boolean __removeAllEquivalent(ImmutableSet<? extends E> set, Comparator<Object> cmp, Consumer<E> onSuccess, Consumer<E> onFailure);
	
	ImmutableSet<E> freeze();
	
}
