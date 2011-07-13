/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Colin J. Fuller's code.
 *
 * The Initial Developer of the Original Code is
 * Colin J. Fuller.
 * Portions created by the Initial Developer are Copyright (C) 2011
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s): Colin J. Fuller
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK ***** */
package edu.stanford.cfuller.imageanalysistools.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * This class represents a sorted priority deque.  Adding an element will insert it according to its natural ordering.
 * <p>
 * Depending on the arguments to the constructor, the SortedDeque can either have a fixed capacity or not.
 * This capacity is distinct from the normal definition of capacity in that the add and offer based methods will not 
 * fail if the SortedDeque is at capacity, but will push elements off the end instead.
 * <p>
 * If the capacity is
 * not fixed, then the method addFirst and addLast do the same thing; the element will be added at its appropriate place in the ordering.
 * <p>
 * In the case of a SortedDeque with fixed size, then the addFirst and addLast methods have the additional effect of (if at capacity) controlling from which
 * end and element is removed; addFirst pushes an element off the end; addLast pushes an element off the front.
 * 
 * 
 * @author cfuller
 *
 */
public class SortedDeque<E extends Comparable<E> > implements Deque<E>, List<E> {

	ArrayList<E> storage;
	int capacity;
	boolean isFixedCapacity;
	
	public SortedDeque() {
		this.storage = new ArrayList<E>();
		this.isFixedCapacity = false;
		this.capacity = Integer.MAX_VALUE;
	}
	
	public SortedDeque(int capacity, boolean isFixedCapacity) {
		this.capacity = capacity;
		this.isFixedCapacity = isFixedCapacity;
		this.storage = new ArrayList<E>(capacity);
	}
	
	protected static class ReversingIterator<E> implements Iterator<E> {
		
		ListIterator<E> it;
		
		public ReversingIterator(ListIterator<E> it) {
			this.it = it;
		}
		
		public boolean hasNext() {
			return it.hasPrevious();
		}
		
		public E next() {
			return it.previous();
		}
		
		public void remove() {
			throw new UnsupportedOperationException("ReversingIterator does not support remove.");
		}
		
	}
	
	/**
	 * Determines whether the SortedDeque is at capacity.
	 * @return  true if this.size() == this.capacity() and this is a fixed capacity sorted Deque; false otherwise.
	 */
	public boolean isAtCapacity() {
		return (this.isFixedCapacity && this.size() == this.capacity());
	}
	
	/**
	 * Gets the capacity of the SortedDeque.  If this is not a fixed capacity SortedDeque, may return either Integer.MAX_VALUE
	 * or the initial capacity specified in the constructor.
	 * @return	The capacity (or initial capacity) of the SortedDeque.
	 */
	public int capacity() {
		return this.capacity;
	}
	
	
	/* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
	@Override
	public boolean addAll(Collection<? extends E> c) {
		
		for(E e : c) {
			this.add(e);
		}
		
		if (! c.isEmpty()) return true;
		return false;
		
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
	@Override
	public void clear() {
		this.storage.clear();
		
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
	@Override
	public boolean containsAll(Collection<?> c) {
		return this.storage.containsAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
	@Override
	public boolean isEmpty() {
		return this.storage.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
	@Override
	public boolean removeAll(Collection<?> c) {
		return this.storage.removeAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
	@Override
	public boolean retainAll(Collection<?> c) {
		return this.storage.retainAll(c);
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray()
	 */
	@Override
	public Object[] toArray() {
		return this.storage.toArray();
	}

	/* (non-Javadoc)
	 * @see java.util.Collection#toArray(T[])
	 */
	@Override
	public <T> T[] toArray(T[] a) {
		return this.storage.toArray(a);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#add(java.lang.Object)
	 */
	@Override
	public boolean add(E e) {
		this.addLast(e);
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#addFirst(java.lang.Object)
	 */
	@Override
	public void addFirst(E e) {

		this.add(0, e);
		
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#addLast(java.lang.Object)
	 */
	@Override
	public void addLast(E e) {
		this.add(this.size(), e);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#contains(java.lang.Object)
	 */
	@Override
	public boolean contains(Object o) {
		return this.storage.contains(o);
		//TODO: better performance for contains method, given that the list is sorted.
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#descendingIterator()
	 */
	@Override
	public Iterator<E> descendingIterator() {
		return new ReversingIterator<E>(this.storage.listIterator(this.storage.size()));
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#element()
	 */
	@Override
	public E element() {
		return this.getFirst();
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#getFirst()
	 */
	@Override
	public E getFirst() {
		if (this.storage.isEmpty()) {throw new NoSuchElementException("SortedDeque is empty.");}
		return storage.get(0);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#getLast()
	 */
	@Override
	public E getLast() {
		if (this.storage.isEmpty()) {throw new NoSuchElementException("SortedDeque is empty.");}
		return this.storage.get(this.storage.size() - 1);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
		return this.storage.iterator();
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#offer(java.lang.Object)
	 */
	@Override
	public boolean offer(E e) {
		return this.offerLast(e);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#offerFirst(java.lang.Object)
	 */
	@Override
	public boolean offerFirst(E e) {
		this.addFirst(e);
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#offerLast(java.lang.Object)
	 */
	@Override
	public boolean offerLast(E e) {
		this.addLast(e);
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#peek()
	 */
	@Override
	public E peek() {
		return this.peekFirst();
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#peekFirst()
	 */
	@Override
	public E peekFirst() {
		if (this.storage.isEmpty()) {return null;}
		return this.storage.get(0);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#peekLast()
	 */
	@Override
	public E peekLast() {
		if (this.storage.isEmpty()) {return null;}
		return this.storage.get(this.storage.size() - 1);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#poll()
	 */
	@Override
	public E poll() {
		return this.pollFirst();
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#pollFirst()
	 */
	@Override
	public E pollFirst() {
		if (this.storage.isEmpty()) {return null;}
		return this.storage.remove(0);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#pollLast()
	 */
	@Override
	public E pollLast() {
		if (this.storage.isEmpty()) {return null;}
		return this.storage.remove(this.storage.size() - 1);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#pop()
	 */
	@Override
	public E pop() {
		return this.removeFirst();
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#push(java.lang.Object)
	 */
	@Override
	public void push(E e) {
		this.addFirst(e);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#remove()
	 */
	@Override
	public E remove() {
		return this.removeFirst();
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#remove(java.lang.Object)
	 */
	@Override
	public boolean remove(Object o) {
		return this.storage.remove(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#removeFirst()
	 */
	@Override
	public E removeFirst() {
		if (this.storage.isEmpty()) {throw new NoSuchElementException("SortedDeque is empty.");}
		return this.storage.remove(0);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#removeFirstOccurrence(java.lang.Object)
	 */
	@Override
	public boolean removeFirstOccurrence(Object o) {
		return this.storage.remove(o);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#removeLast()
	 */
	@Override
	public E removeLast() {
		if (this.storage.isEmpty()) {throw new NoSuchElementException("SortedDeque is empty.");}
		return this.storage.remove(this.storage.size() - 1);
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#removeLastOccurrence(java.lang.Object)
	 */
	@Override
	public boolean removeLastOccurrence(Object o) {
		int index = this.storage.lastIndexOf(o);
		
		if (index == -1) {return false;}
		
		this.storage.remove(index);
		return true;
		
	}

	/* (non-Javadoc)
	 * @see java.util.Deque#size()
	 */
	@Override
	public int size() {
		return this.storage.size();
	}

	/* (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	@Override
	public void add(int arg0, E arg1) {
		
		int upperBound = this.size();
		int lowerBound = 0;
		
		int currentGuess = this.size() >> 1;
		
		int previousIndex = currentGuess - 1;
		
		//						 insert from left		and		[at pos 0		or 		previous pos < element being inserted]		and next pos >= element being inserted
		
		boolean conditionLower = (arg0 <= currentGuess) && ((previousIndex < 0) || (this.get(previousIndex).compareTo(arg1) < 0) && this.get(currentGuess).compareTo(arg1) >= 0);
		
//						 insert from right		and		[at pos 0		or 		previous pos <= element being inserted]		and next pos > element being inserted
		
		boolean conditionUpper = (arg0 > currentGuess) && ((previousIndex < 0) || (this.get(previousIndex).compareTo(arg1) <= 0) && this.get(currentGuess).compareTo(arg1) > 0);
		
		while (! (conditionLower || conditionUpper)) {
			
			
			if (this.get(currentGuess).compareTo(arg1) < 0) {
				//case0: .get(currentGuess) < arg1  -> increase currentGuess halfway between current and upper bound
				
				int increment =  (upperBound - currentGuess) >> 1;
				
				if (increment < 1) increment = 1;
				
				lowerBound = currentGuess;
				
				currentGuess += increment;
				
				if (currentGuess >= this.size()) {currentGuess = this.size(); break;}
				
			} else if (this.get(currentGuess).compareTo(arg1) > 0) {
				//case1: .get(currentGuess) > arg1 -> decrease currentGuess halfway between current and lower bound
				
				int increment = (currentGuess - lowerBound) >> 1;
				if (increment < 1) increment = 1;
				
				upperBound = currentGuess;
				
				currentGuess -= increment;
				
				if (currentGuess <= 0) {currentGuess = 0; break;}
				
			} else {
				//case2: equality; insert here
				break;
				
			}
			
		}
		
		this.storage.add(currentGuess, arg1);
		
		if (this.isFixedCapacity && this.size() > this.capacity) {
			if (arg0 < currentGuess || arg0 == 0) {
				this.remove(this.size());
			} else {
				this.remove(0);
			}
		}
		
		
		
	}

	/* (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	@Override
	public boolean addAll(int arg0, Collection<? extends E> arg1) {
		
		if (arg1.isEmpty()) {return false;}

		for (E e: arg1) {
			this.add(arg0, e);
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
	@Override
	public E get(int arg0) {
		return this.storage.get(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	@Override
	public int indexOf(Object arg0) {
		return this.storage.indexOf(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	@Override
	public int lastIndexOf(Object arg0) {
		return this.storage.lastIndexOf(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
	@Override
	public ListIterator<E> listIterator() {
		return this.storage.listIterator();
	}

	/* (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
	@Override
	public ListIterator<E> listIterator(int arg0) {
		return this.storage.listIterator(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.List#remove(int)
	 */
	@Override
	public E remove(int arg0) {
		return this.storage.remove(arg0);
	}

	/* (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	@Override
	public E set(int arg0, E arg1) {
		throw new UnsupportedOperationException("Set is not supported by SortedDeque.");
	}

	/* (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public List<E> subList(int arg0, int arg1) {
		SortedDeque<E> sub = new SortedDeque<E>(arg1-arg0, this.isFixedCapacity);
		
		sub.addAll(this.storage.subList(arg0, arg1));
		
		return sub;
	}

}
