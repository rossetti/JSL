/*
 * Copyright (c) 2018. Manuel D. Rossetti, rossetti@uark.edu
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package jsl.utilities.random.robj;

import jsl.utilities.random.rng.RNStreamIfc;
import jsl.utilities.random.rvariable.JSLRandom;

import java.util.*;

/** Defines an abstract base class for random lists
 *
 * @param <T> the type in the list
 */
abstract public class RList<T> implements RListIfc<T> {


    protected List<T> myElements;

    protected RNStreamIfc myStream;

    public RList() {
        myElements = new ArrayList<>();
        myStream = JSLRandom.nextRNStream();
    }

    /**
     * The object cannot be null, but it can be added more than once
     * See how List handles multiple instances of the same object
     *
     * @param obj the object to add
     */
    @Override
    public boolean add(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("The object was null");
        }
        return myElements.add(obj);
    }

    @Override
    public boolean remove(Object obj) {
        return myElements.remove(obj);
    }

    @Override
    abstract public T getRandomElement();

    abstract public RList<T> newInstance();

    @Override
    public boolean contains(Object arg0) {
        return myElements.contains(arg0);
    }

    @Override
    public boolean containsAll(Collection<?> arg0) {
        return myElements.containsAll(arg0);
    }

    @Override
    public int indexOf(Object arg0) {
        return myElements.indexOf(arg0);
    }

    @Override
    public boolean isEmpty() {
        return myElements.isEmpty();
    }

    @Override
    public int size() {
        return myElements.size();
    }

    public List<T> getList() {
        return (Collections.unmodifiableList(myElements));
    }

    @Override
    public void add(int index, T element) {
        if (element == null) {
            throw new IllegalArgumentException("The object was null");
        }
        myElements.add(index, element);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        if (c == null) {
            throw new IllegalArgumentException("The collection was null");
        }
        if (c.contains(null)) {
            throw new IllegalArgumentException("The an object in the collection was null");
        }
        return myElements.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        if (c == null) {
            throw new IllegalArgumentException("The collection was null");
        }
        if (c.contains(null)) {
            throw new IllegalArgumentException("The an object in the collection was null");
        }
        return myElements.addAll(index, c);
    }

    @Override
    public void clear() {
        myElements.clear();
    }

    @Override
    public T get(int index) {
        return myElements.get(index);
    }

    @Override
    public Iterator<T> iterator() {
        return myElements.iterator();
    }

    @Override
    public int lastIndexOf(Object o) {
        return myElements.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return myElements.listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return myElements.listIterator(index);
    }

    @Override
    public T remove(int index) {
        return myElements.remove(index);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return myElements.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return myElements.retainAll(c);
    }

    @Override
    public T set(int index, T element) {
        if (element == null) {
            throw new IllegalArgumentException("The object was null");
        }
        return myElements.set(index, element);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return myElements.subList(fromIndex, toIndex);
    }

    @Override
    public Object[] toArray() {
        return myElements.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return myElements.toArray(a);
    }

    @Override
    public void advanceToNextSubStream() {
        myStream.advanceToNextSubStream();
    }

    @Override
    public void resetStartStream() {
        myStream.resetStartStream();
    }

    @Override
    public void resetStartSubStream() {
        myStream.resetStartSubStream();
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        myStream.setAntitheticOption(flag);
    }

    @Override
    public final boolean getAntitheticOption() {
        return myStream.getAntitheticOption();
    }


    @Override
    public final RNStreamIfc getRandomNumberStream() {
        return myStream;
    }

    @Override
    public void setRandomNumberStream(RNStreamIfc stream) {
        Objects.requireNonNull(stream, "The supplied stream was null");
        myStream = stream;
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        return myStream.getResetNextSubStreamOption();
    }

    @Override
    public boolean getResetStartStreamOption() {
        return myStream.getResetStartStreamOption();
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        myStream.setResetNextSubStreamOption(b);
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        myStream.setResetStartStreamOption(b);
    }
}
