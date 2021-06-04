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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.utilities.misc;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/** This class encapsulates the a list that is ordered
 *
 * @author rossetti
 * @param <T> the type held in the list
 */
public class OrderedList<T extends Comparable<T>> implements Collection<T> {

    /** Holds requests that are waiting for some
     *  units of the resource
     *
     */
    protected List<T> myList;

    public OrderedList() {
        myList = new LinkedList<>();
    }

    @Override
    public boolean add(T obj) {
        // nothing in the list, just add to beginning
        if (myList.isEmpty()) {
            return myList.add(obj);
        }

        // might as well check for worse case, if larger than the largest
        // then put it at the end and return
        if (obj.compareTo(myList.get(myList.size() - 1)) >= 0) {
            return myList.add(obj);
        }

        // now iterate through the list
        for (ListIterator<T> i = myList.listIterator(); i.hasNext();) {
            if (obj.compareTo(i.next()) < 0) {
                // next() move the iterator forward, if it is < what was returned by next(), then it
                // must be inserted at the previous index
                myList.add(i.previousIndex(), obj);
                return true;
            }
        }
        return false;
    }

    @Override
    public int size() {
        return myList.size();
    }

    public T set(int arg0, T arg1) {
        return myList.set(arg0, arg1);
    }

    public T peekNext() {
        if (myList.isEmpty()) {
            return null;
        } else {
            return myList.get(0);
        }
    }

    public T removeNext() {
         if (myList.isEmpty()) {
            return null;
        } else {
             T obj = myList.get(0);
             myList.remove(0);
            return obj;
        }
    }

    public T remove(int arg0) {
        return myList.remove(arg0);
    }

    public boolean remove(T arg0) {
        return myList.remove(arg0);
    }

    public ListIterator<T> listIterator(int arg0) {
        return myList.listIterator(arg0);
    }

    public ListIterator<T> listIterator() {
        return myList.listIterator();
    }

    public int lastIndexOf(T arg0) {
        return myList.lastIndexOf(arg0);
    }

    @Override
    public Iterator<T> iterator() {
        return myList.iterator();
    }

    @Override
    public boolean isEmpty() {
        return myList.isEmpty();
    }

    public int indexOf(T arg0) {
        return myList.indexOf(arg0);
    }

    public T get(int arg0) {
        return myList.get(arg0);
    }

    public boolean contains(T arg0) {
        return myList.contains(arg0);
    }

    @Override
    public void clear() {
        myList.clear();
    }

    public boolean contains(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public <T> T[] toArray(T[] arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean remove(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean containsAll(Collection<?> arg0) {
        return myList.containsAll(arg0);
    }

    public boolean addAll(Collection<? extends T> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean removeAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean retainAll(Collection<?> arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
