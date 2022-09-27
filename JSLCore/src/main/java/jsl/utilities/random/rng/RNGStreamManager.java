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
package jsl.utilities.random.rng;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/** A wrapper for holding a list of streams so that
 *  all streams can be managed together
 *
 *  The methods of the RandomStreamIfc are applied
 *  to all contained random number streams
 *
 * @author rossetti
 */
public class RNGStreamManager implements RandomStreamManagerIfc {

    /** Holds the streams
     */
    protected List<RNStreamIfc> myStreams;

    public RNGStreamManager() {
        myStreams = new ArrayList<>();
    }

    /** Makes a stream manager and fills it with streams from
     *  RNStreamFactory.getDefaultFactory()
     * @param numStreams, must be &gt; 0
     * @return
     */
    public static RNGStreamManager makeRngStreams(int numStreams) {
        return makeRngStreams(numStreams, RNStreamFactory.getDefaultFactory());
    }

    /** Makes RNStreams and fills a RNGStreamManager
     *
     * @param numStreams, must be &gt; 0
     * @param f the factory
     * @return the manager
     */
    public static RNGStreamManager makeRngStreams(int numStreams, RNStreamFactory f) {
        if (numStreams <= 0) {
            throw new IllegalArgumentException("The supplied number of streams to make was <= 0");
        }
        if (f == null) {
            throw new IllegalArgumentException("The supplied RNStreamFactory was null");
        }
        RNGStreamManager m = new RNGStreamManager();
        for (int i = 1; i <= numStreams; i++) {
            m.add(f.getStream());
        }
        return m;
    }

    @Override
    public void resetStartStream() {
        for (RNStreamIfc r : myStreams) {
            r.resetStartStream();
        }
    }

    @Override
    public void resetStartSubStream() {
        for (RNStreamIfc r : myStreams) {
            r.resetStartSubStream();
        }
    }

    @Override
    public void advanceToNextSubStream() {
        for (RNStreamIfc r : myStreams) {
            r.advanceToNextSubStream();
        }
    }

    /** Causes all managed streams to advance their
     *  to the next nth substream
     *
     * @param n
     */
    public void advanceToNextSubStream(int n) {
        if (n <= 0) {
            return;
        }
        for (int i = 1; i <= n; i++) {
            advanceToNextSubStream();
        }
    }

    @Override
    public void setAntitheticOption(boolean flag) {
        for (RNStreamIfc r : myStreams) {
            r.setAntitheticOption(flag);
        }
    }

    @Override
    public boolean getAntitheticOption() {
        if(myStreams.isEmpty()){
            throw new IllegalStateException("There were no streams present");
        }

        ListIterator<RNStreamIfc> listIterator = myStreams.listIterator();
        boolean b = listIterator.next().getAntitheticOption();

        while( listIterator.hasNext()){
            b = b && listIterator.next().getAntitheticOption();
        }
        return b;
    }

    @Override
    public int size() {
        return myStreams.size();
    }

    /** Sets the stream at the index
     *
     * @param index, must be a valid index
     * @param element, must not be null
     * @return the RandomStreamIfc
     */
    public RNStreamIfc set(int index, RNStreamIfc element) {
        if (element == null) {
            throw new IllegalArgumentException("The supplied RngIfc was null");
        }
        return myStreams.set(index, element);
    }

    /**
     *
     * @param index must be a valid index
     * @return RandomStreamIfc
     */
    public RNStreamIfc remove(int index) {
        return myStreams.remove(index);
    }

    public boolean remove(RNStreamIfc o) {
        return myStreams.remove(o);
    }

    public Iterator<RNStreamIfc> iterator() {
        return myStreams.iterator();
    }

    @Override
    public boolean isEmpty() {
        return myStreams.isEmpty();
    }

    @Override
    public int indexOf(RNStreamIfc o) {
        return myStreams.indexOf(o);
    }

    /**
     *
     * @param index must be a valid index
     * @return RandomStreamIfc
     */
    @Override
    public RNStreamIfc get(int index) {
        return myStreams.get(index);
    }

    @Override
    public boolean contains(RNStreamIfc o) {
        return myStreams.contains(o);
    }

    public void clear() {
        myStreams.clear();
    }

    /** Adds the stream to the manager
     * 
     * @param index, must be a valid index
     * @param element, must not be null
     */
    public void add(int index, RNStreamIfc element) {
        if (element == null) {
            throw new IllegalArgumentException("The supplied RngIfc was null");
        }
        myStreams.add(index, element);
    }

    /** Adds the stream to the manager
     * 
     * @param e must not be null
     * @return true if added
     */
    public boolean add(RNStreamIfc e) {
        if (e == null) {
            throw new IllegalArgumentException("The supplied RngIfc was null");
        }
        return myStreams.add(e);
    }

    /** Adds a stream from RNStreamFactory.getDefaultFactory()
     *
     * @return the added RNStream
     */
    public RNStreamIfc addNewRNStream() {
        return addNewRNStream(RNStreamFactory.getDefaultFactory());
    }

    /** Creates a new stream from the supplied factory and adds it
     *  to the list of managed streams
     * @param f, must not be null
     * @return the created stream
     */
    public RNStreamIfc addNewRNStream(RNStreamFactory f) {
        if (f == null) {
            throw new IllegalArgumentException("The supplied RNStreamFactory was null");
        }
        RNStreamIfc s = f.getStream();
        add(s);
        return s;
    }

    @Override
    public boolean getResetNextSubStreamOption() {
        if(myStreams.isEmpty()){
            throw new IllegalStateException("There were no streams present");
        }

        ListIterator<RNStreamIfc> listIterator = myStreams.listIterator();
        boolean b = listIterator.next().getResetNextSubStreamOption();

        while( listIterator.hasNext()){
            b = b && listIterator.next().getResetNextSubStreamOption();
        }
        return b;
    }

    @Override
    public boolean getResetStartStreamOption() {
        if(myStreams.isEmpty()){
            throw new IllegalStateException("There were no streams present");
        }

        ListIterator<RNStreamIfc> listIterator = myStreams.listIterator();
        boolean b = listIterator.next().getResetStartStreamOption();

        while( listIterator.hasNext()){
            b = b && listIterator.next().getResetStartStreamOption();
        }
        return b;
    }

    @Override
    public void setResetNextSubStreamOption(boolean b) {
        for (RNStreamIfc r : myStreams) {
            r.setResetNextSubStreamOption(b);
        }
    }

    @Override
    public void setResetStartStreamOption(boolean b) {
        for (RNStreamIfc r : myStreams) {
            r.setResetStartStreamOption(b);
        }
    }
}
