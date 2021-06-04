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

package jsl.modeling.resource;

/**
 *  An interface to listen for state changes on a FailureProcess and their FailureNotices.  These
 *  methods are called after the FailureNotice transitions into each state and after
 *  the FailureProcess has been notified of the failure notice state change.
 *  The isXXX() methods of the FailureProcess and FailureNotice can be used to determine
 *  what to do.
 *
 */
public interface FailureProcessListenerIfc {

    void changed(FailureProcess failureProcess, FailureNotice failureNotice);
}
