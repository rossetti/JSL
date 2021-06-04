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
package jsl.modeling.elements.spatial.transporter;

import java.util.List;
import jsl.modeling.queue.QObject;

public interface TransporterSelectionRuleIfc {

    /** Returns a reference to the next Transporter to be selected
     * from the supplied list.  The transporter is not removed from the list.
     * @param list The list to be peeked into
     * @param request The request that needs a transporter
     * @return The FreePathTransporter2D that is next, or null if the list is empty
     */
    public Transporter selectTransporter(List<Transporter> list, QObject request);

    public void addIdleTransporter(List<Transporter> list, Transporter transporter);
}
