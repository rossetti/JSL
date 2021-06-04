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
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/** LinkedListEventCalendar is a concrete implementation of the CalendarIfc for use with the Scheduler
 *  This class provides an event calendar by using a java.util.LinkedList to hold the underlying events.
 *
*/
public class LinkedListEventCalendar implements CalendarIfc {
    
    private final List<JSLEvent> myEventSet;
    
    /** Creates new Calendar */
    public LinkedListEventCalendar(){
        myEventSet = new LinkedList<>();
    }
           
    @Override
    public void add(JSLEvent e){
        
        // nothing in calendar, just add it, and return
        if (myEventSet.isEmpty()){ 
            myEventSet.add(e);
            return;
        }
        
        // might as well check for worse case, if larger than the largest then put it at the end and return
        if (e.compareTo(myEventSet.get(myEventSet.size()-1)) >= 0){
            myEventSet.add(e);
            return;
        }
        
         // now iterate through the list
        for (ListIterator<JSLEvent> i=myEventSet.listIterator(); i.hasNext(); ){
            if ( e.compareTo(i.next()) < 0 ){
                // next() move the iterator forward, if it is < what was returned by next(), then it
                // must be inserted at the previous index
                myEventSet.add(i.previousIndex(),e);
                return;
            }
        }
    }
       
    @Override
    public JSLEvent nextEvent(){
        if (!isEmpty())
            return ((JSLEvent)myEventSet.remove(0));
        else
            return(null);
    }
      
    @Override
    public JSLEvent peekNext(){
        if (!isEmpty())
            return ((JSLEvent)myEventSet.get(0));
        else
            return(null);
    }
       
    @Override
    public boolean isEmpty(){
        return myEventSet.isEmpty();
    }
      
    @Override
    public void clear(){
        myEventSet.clear();
    }
      
    @Override
    public void cancel(JSLEvent e){
        e.setCanceledFlag(true);
    }
       
    @Override
    public int size(){
        return(myEventSet.size());
    }
     
    @Override
    public String toString(){
        return(myEventSet.toString());
    }   
}