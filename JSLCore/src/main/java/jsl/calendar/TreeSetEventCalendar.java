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
import java.util.SortedSet;
import java.util.TreeSet;

/** This class provides an event calendar by using a tree set to hold the underlying events.
*/
public class TreeSetEventCalendar implements CalendarIfc {
    
    private final SortedSet<JSLEvent> myEventSet;
    
    /** Creates new Calendar */
    public TreeSetEventCalendar(){
        myEventSet = new TreeSet<>();
    }
         
    @Override
    public void add(JSLEvent e){
        myEventSet.add(e);
    }
     
    @Override
    public JSLEvent nextEvent(){
        if (!isEmpty()){
            JSLEvent e = (JSLEvent)myEventSet.first();
            myEventSet.remove(e);
            return (e);
        }
        else
            return(null);
    }
   
    @Override
    public JSLEvent peekNext(){
        if (!isEmpty())
            return ((JSLEvent)myEventSet.first());
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
