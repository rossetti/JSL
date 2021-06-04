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
package jsl.modeling.elements.entity;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/** This class encapsulates the holding of Requests for
 *  resources. 
 *
 * @author rossetti
 */
public class RequestQueue {

    /** Holds requests that are waiting for some
     *  units of the resource
     *
     */
    protected List<Request> myRequests;

    public RequestQueue() {
        myRequests = new LinkedList<Request>();
    }

    /** Adds the request to the list of waiting requests
     *  based on the Comparable interface for Request
     *
     * @param request
     */
    public void add(Request request) {
        // nothing in the list, just add to beginning
        if (myRequests.isEmpty()) {
            myRequests.add(request);
            return;
        }

        // might as well check for worse case, if larger than the largest
        // then put it at the end and return
        if (request.compareTo(myRequests.get(myRequests.size() - 1)) >= 0) {
            myRequests.add(request);
            return;
        }

        // now iterate through the list
        for (ListIterator<Request> i = myRequests.listIterator(); i.hasNext();) {
            if (request.compareTo(i.next()) < 0) {
                // next() move the iterator forward, if it is < what was returned by next(), then it
                // must be inserted at the previous index
                myRequests.add(i.previousIndex(), request);
                return;
            }
        }
    }

    public int size() {
        return myRequests.size();
    }

    public Request set(int arg0, Request arg1) {
        return myRequests.set(arg0, arg1);
    }

    public Request remove(int arg0) {
        return myRequests.remove(arg0);
    }

    public boolean remove(Request arg0) {
        return myRequests.remove(arg0);
    }

    public ListIterator<Request> listIterator(int arg0) {
        return myRequests.listIterator(arg0);
    }

    public ListIterator<Request> listIterator() {
        return myRequests.listIterator();
    }

    public int lastIndexOf(Request arg0) {
        return myRequests.lastIndexOf(arg0);
    }

    public Iterator<Request> iterator() {
        return myRequests.iterator();
    }

    public boolean isEmpty() {
        return myRequests.isEmpty();
    }

    public int indexOf(Request arg0) {
        return myRequests.indexOf(arg0);
    }

    public Request get(int arg0) {
        return myRequests.get(arg0);
    }

    public boolean containsAll(Collection<Request> arg0) {
        return myRequests.containsAll(arg0);
    }

    public boolean contains(Request arg0) {
        return myRequests.contains(arg0);
    }

    public void clear() {
        myRequests.clear();
    }
}
