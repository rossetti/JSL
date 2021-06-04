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
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jsl.modeling.elements.station;

/** Defines an interface for (list) iterator for ReceiveQObjectIfc implementors.  
 *  Note that unlike ListIterator null is returned if there is not a next or previous 
 *  element.
 *
 * @author rossetti
 */
public interface ReceiverIteratorIfc {

    /** Returns the next element in the list and advances the cursor position. 
     * This method may be called repeatedly to iterate through the list, or
     * intermixed with calls to previous() to go back and forth. 
     * (Note that alternating calls to next and previous will return the 
     * same element repeatedly.)
     * 
     * @return the next receiver or null
     */
    public ReceiveQObjectIfc nextReceiver();

    /** Returns the previous element in the list and moves the cursor position 
     * backwards. This method may be called repeatedly to iterate through 
     * the list backwards, or intermixed with calls to next() to go back and 
     * forth. (Note that alternating calls to next and previous will return 
     * the same element repeatedly.)
     * 
     * @return the previous receiver or null
     */
    public ReceiveQObjectIfc previousReceiver();

    /** Returns true if this list iterator has more elements when traversing 
     * the list in the forward direction. (In other words, returns true 
     * if next() would return an element rather null.)
     * 
     * @return true if there is a next receiver
     */
    public boolean hasNextReceiver();

    /** Returns true if this list iterator has more elements when traversing 
     * the list in the reverse direction. (In other words, returns true if
     * previous() would return an element rather than null.)
     * 
     * @return true if there is a previous receiver
     */
    public boolean hasPreviousReceiver();

    /** Returns the index of the element that would be returned by a subsequent 
     * call to nextReceiver(). (Returns list size if the list iterator is at
     * the end of the list.)
     * 
     * @return the index of the element that would be returned by a subsequent 
     * call to nextReceiver(), or list size if the list iterator is at the 
     * end of the list
     */
    public int nextReceiverIndex();

    /** 
     * Returns the index of the element that would be returned by a subsequent 
     * call to previous(). (Returns -1 if the list iterator is at the 
     * beginning of the list.)
     * 
     * @return the index of the element that would be returned by a subsequent
     * call to previousReceiver(), or -1 if the list iterator is at the 
     * beginning of the list
     */
    public int previousReceiverIndex();
}
