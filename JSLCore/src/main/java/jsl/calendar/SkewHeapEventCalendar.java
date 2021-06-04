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
package jsl.calendar;


import jsl.simulation.JSLEvent;

/** This class provides an event calendar by using a skew heap to hold the underlying events.
*/
public class SkewHeapEventCalendar implements CalendarIfc {
    
    private BinaryNode myRoot;
    private int myNumEvents;
    
    /** Creates new Calendar */
    public SkewHeapEventCalendar(){
        myNumEvents = 0;
        myRoot = null;
    }
    
    @Override
    public void add(JSLEvent e){
        myRoot = merge(myRoot, new BinaryNode(e));
        myNumEvents++;
    }
       
    @Override
    public JSLEvent nextEvent(){
        if (!isEmpty()){
            JSLEvent e = (JSLEvent)myRoot.value;
            myRoot = merge(myRoot.leftChild, myRoot.rightChild);
            myNumEvents--;
            return (e);
        }
        else
            return(null);
    }
    
    @Override
    public JSLEvent peekNext(){
        if (!isEmpty()){
            JSLEvent e = (JSLEvent)myRoot.value;
            return (e);
        }
        else
            return(null);   	
    }
    
    @Override
    public boolean isEmpty(){
        return (myRoot == null);
    }
       
    @Override
    public void clear(){
        while (nextEvent() != null){
        }
    }
       
    @Override
    public void cancel(JSLEvent e){
        e.setCanceledFlag(true);
    }
       
    @Override
    public int size(){
        return(myNumEvents);
    }
       
    @Override
    public String toString(){
        return("Number of events = " + myNumEvents);
    }
    
    private BinaryNode merge(BinaryNode left, BinaryNode right) {
        if (left == null) return right;
        if (right == null) return left;
        
        JSLEvent leftValue = (JSLEvent)left.value;
        JSLEvent rightValue = (JSLEvent)right.value;
        
        if (leftValue.compareTo(rightValue) < 0) {
            BinaryNode swap = left.leftChild;
            left.leftChild = merge(left.rightChild, right);
            left.rightChild = swap;
            return left;
        } else {
            BinaryNode swap = right.rightChild;
            right.rightChild = merge(right.leftChild, left);
            right.leftChild = swap;
            return right;
        }
    }    
    
    private class BinaryNode {
        /**
         * value being held by node
         */
        public Object value;
        
        /**
         * left child of node
         */
        public BinaryNode leftChild = null;
        
        /**
         * right child of node
         */
        public BinaryNode rightChild = null;
        
        /**
         * initialize a newly created binary node
         */
        public BinaryNode(){
            value = null;
        }
        
        /**
         * initialize a newly created binary node
         *
         * @param v value to be associated with new node
         */
        public BinaryNode(Object v){
            value = v;
        }
        
        /** return true if we are not a sentinel node
         * @return true if not a sentinal node
         */
        public boolean isEmpty() {
            return false;
        }
    }
}
