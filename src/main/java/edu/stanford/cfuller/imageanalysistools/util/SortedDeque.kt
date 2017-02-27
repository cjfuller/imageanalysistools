package edu.stanford.cfuller.imageanalysistools.util

import java.util.ArrayList
import java.util.Deque
import java.util.NoSuchElementException

/**
 * This class represents a sorted priority deque.  Adding an element will insert it according to its natural ordering.
 *
 * Depending on the arguments to the constructor, the SortedDeque can either have a fixed capacity or not.
 * This capacity is distinct from the normal definition of capacity in that the add and offer based methods will not
 * fail if the SortedDeque is at capacity, but will push elements off the end instead.
 *
 * If the capacity is
 * not fixed, then the method addFirst and addLast do the same thing; the element will be added at its appropriate place in the ordering.
 *
 * In the case of a SortedDeque with fixed size, then the addFirst and addLast methods have the additional effect of (if at capacity) controlling from which
 * end and element is removed; addFirst pushes an element off the end; addLast pushes an element off the front.
 * @author Colin J. Fuller
 */
class SortedDeque<E : Comparable<E>> : Deque<E>, MutableList<E> {
    internal var storage: ArrayList<E> = ArrayList<E>()
    internal var capacity: Int = Integer.MAX_VALUE
    internal var isFixedCapacity: Boolean = false
    override val size: Int
        get() = this.storage.size

    constructor(capacity: Int, isFixedCapacity: Boolean) {
        this.capacity = capacity
        this.isFixedCapacity = isFixedCapacity
        this.storage = ArrayList<E>(capacity)
    }

    private class ReversingIterator<E>(internal var it: ListIterator<E>) : Iterator<E> {
        override fun hasNext(): Boolean {
            return it.hasPrevious()
        }
        override fun next(): E {
            return it.previous()
        }
    }

    /**
     * Determines whether the SortedDeque is at capacity.
     * @return  true if this.size() == this.capacity() and this is a fixed capacity sorted Deque; false otherwise.
     */
    val isAtCapacity: Boolean
        get() = this.isFixedCapacity && this.size == this.capacity()

    /**
     * Gets the capacity of the SortedDeque.  If this is not a fixed capacity SortedDeque, may return either Integer.MAX_VALUE
     * or the initial capacity specified in the constructor.
     * @return    The capacity (or initial capacity) of the SortedDeque.
     */
    fun capacity(): Int {
        return this.capacity
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#addAll(java.util.Collection)
	 */
    override fun addAll(c: Collection<E>): Boolean {
        for (e in c) {
            this.add(e)
        }
        if (!c.isEmpty()) return true
        return false
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#clear()
	 */
    override fun clear() {
        this.storage.clear()
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#containsAll(java.util.Collection)
	 */
    override fun containsAll(c: Collection<E>): Boolean {
        return this.storage.containsAll(c)
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#isEmpty()
	 */
    override fun isEmpty(): Boolean {
        return this.storage.isEmpty()
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#removeAll(java.util.Collection)
	 */
    override fun removeAll(c: Collection<E>): Boolean {
        return this.storage.removeAll(c)
    }

    /* (non-Javadoc)
	 * @see java.util.Collection#retainAll(java.util.Collection)
	 */
    override fun retainAll(c: Collection<E>): Boolean {
        return this.storage.retainAll(c)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#add(java.lang.Object)
	 */
    override fun add(e: E): Boolean {
        this.addLast(e)
        return true
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#addFirst(java.lang.Object)
	 */
    override fun addFirst(e: E) {
        this.add(0, e)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#addLast(java.lang.Object)
	 */
    override fun addLast(e: E) {
        this.add(this.size, e)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#contains(java.lang.Object)
	 */
    override operator fun contains(o: E): Boolean {
        return this.storage.contains(o)
        //TODO: better performance for contains method, given that the list is sorted.
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#descendingIterator()
	 */
    override fun descendingIterator(): Iterator<E> {
        return ReversingIterator(this.storage.listIterator(this.storage.size))
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#element()
	 */
    override fun element(): E {
        return this.first
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#getFirst()
	 */
    override fun getFirst(): E {
        if (this.storage.isEmpty()) {
            throw NoSuchElementException("SortedDeque is empty.")
        }
        return storage[0]
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#getLast()
	 */
    override fun getLast(): E {
        if (this.storage.isEmpty()) {
            throw NoSuchElementException("SortedDeque is empty.")
        }
        return this.storage[this.storage.size - 1]
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#iterator()
	 */
    override fun iterator(): MutableIterator<E> {
        return this.storage.iterator()
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#offer(java.lang.Object)
	 */
    override fun offer(e: E): Boolean {
        return this.offerLast(e)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#offerFirst(java.lang.Object)
	 */
    override fun offerFirst(e: E): Boolean {
        this.addFirst(e)
        return true
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#offerLast(java.lang.Object)
	 */
    override fun offerLast(e: E): Boolean {
        this.addLast(e)
        return true
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#peek()
	 */
    override fun peek(): E {
        return this.peekFirst()!!
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#peekFirst()
	 */
    override fun peekFirst(): E? {
        if (this.storage.isEmpty()) {
            return null
        }
        return this.storage[0]
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#peekLast()
	 */
    override fun peekLast(): E? {
        if (this.storage.isEmpty()) {
            return null
        }
        return this.storage[this.storage.size - 1]
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#poll()
	 */
    override fun poll(): E {
        return this.pollFirst()!!
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#pollFirst()
	 */
    override fun pollFirst(): E? {
        if (this.storage.isEmpty()) {
            return null
        }
        return this.storage.removeAt(0)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#pollLast()
	 */
    override fun pollLast(): E? {
        if (this.storage.isEmpty()) {
            return null
        }
        return this.storage.removeAt(this.storage.size - 1)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#pop()
	 */
    override fun pop(): E {
        return this.removeFirst()
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#push(java.lang.Object)
	 */
    override fun push(e: E) {
        this.addFirst(e)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#remove()
	 */
    override fun remove(): E {
        return this.removeFirst()
    }

    override fun removeAt(i: Int): E {
        return this.storage.removeAt(i)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#remove(java.lang.Object)
	 */
    override fun remove(o: E): Boolean {
        return this.storage.remove(o)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#removeFirst()
	 */
    override fun removeFirst(): E {
        if (this.storage.isEmpty()) {
            throw NoSuchElementException("SortedDeque is empty.")
        }
        return this.storage.removeAt(0)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#removeFirstOccurrence(java.lang.Object)
	 */
    override fun removeFirstOccurrence(o: Any): Boolean {
        return this.storage.remove(o)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#removeLast()
	 */
    override fun removeLast(): E {
        if (this.storage.isEmpty()) {
            throw NoSuchElementException("SortedDeque is empty.")
        }
        return this.storage.removeAt(this.storage.size - 1)
    }

    /* (non-Javadoc)
	 * @see java.util.Deque#removeLastOccurrence(java.lang.Object)
	 */
    override fun removeLastOccurrence(o: Any): Boolean {
        val index = this.storage.lastIndexOf(o)

        if (index == -1) {
            return false
        }

        this.storage.removeAt(index)
        return true

    }

    /* (non-Javadoc)
	 * @see java.util.List#add(int, java.lang.Object)
	 */
    override fun add(arg0: Int, arg1: E) {
        var upperBound = this.size
        var lowerBound = 0
        var currentGuess = this.size shr 1
        val previousIndex = currentGuess - 1
        val conditionLower = arg0 <= currentGuess && (previousIndex < 0 || this[previousIndex] < arg1 && this[currentGuess] >= arg1)
        val conditionUpper = arg0 > currentGuess && (previousIndex < 0 || this[previousIndex] <= arg1 && this[currentGuess] > arg1)
        while (!(conditionLower || conditionUpper)) {
            if (this[currentGuess] < arg1) {
                //case0: .get(currentGuess) < arg1  -> increase currentGuess halfway between current and upper bound
                var increment = upperBound - currentGuess shr 1
                if (increment < 1) increment = 1
                lowerBound = currentGuess
                currentGuess += increment
                if (currentGuess >= this.size) {
                    currentGuess = this.size
                    break
                }
            } else if (this[currentGuess] > arg1) {
                //case1: .get(currentGuess) > arg1 -> decrease currentGuess halfway between current and lower bound
                var increment = currentGuess - lowerBound shr 1
                if (increment < 1) increment = 1
                upperBound = currentGuess
                currentGuess -= increment
                if (currentGuess <= 0) {
                    currentGuess = 0
                    break
                }
            } else {
                //case2: equality; insert here
                break
            }
        }
        this.storage.add(currentGuess, arg1)
        if (this.isFixedCapacity && this.size > this.capacity) {
            if (arg0 < currentGuess || arg0 == 0) {
                this.removeAt(this.size)
            } else {
                this.removeAt(0)
            }
        }
    }

    /* (non-Javadoc)
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
    override fun addAll(arg0: Int, arg1: Collection<E>): Boolean {
        if (arg1.isEmpty()) {
            return false
        }
        for (e in arg1) {
            this.add(arg0, e)
        }
        return true
    }

    /* (non-Javadoc)
	 * @see java.util.List#get(int)
	 */
    override fun get(arg0: Int): E {
        return this.storage[arg0]
    }

    /* (non-Javadoc)
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
    override fun indexOf(arg0: E): Int {
        return this.storage.indexOf(arg0)
    }

    /* (non-Javadoc)
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
    override fun lastIndexOf(arg0: E): Int {
        return this.storage.lastIndexOf(arg0)
    }

    /* (non-Javadoc)
	 * @see java.util.List#listIterator()
	 */
    override fun listIterator(): MutableListIterator<E> {
        return this.storage.listIterator()
    }

    /* (non-Javadoc)
	 * @see java.util.List#listIterator(int)
	 */
    override fun listIterator(arg0: Int): MutableListIterator<E> {
        return this.storage.listIterator(arg0)
    }

    /* (non-Javadoc)
	 * @see java.util.List#set(int, java.lang.Object)
	 */
    override operator fun set(arg0: Int, arg1: E): E {
        throw UnsupportedOperationException("Set is not supported by SortedDeque.")
    }

    /* (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
    override fun subList(arg0: Int, arg1: Int): MutableList<E> {
        val sub = SortedDeque<E>(arg1 - arg0, this.isFixedCapacity)
        sub.addAll(this.storage.subList(arg0, arg1))
        return sub
    }
}
