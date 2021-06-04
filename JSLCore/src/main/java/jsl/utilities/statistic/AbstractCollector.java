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
package jsl.utilities.statistic;

import jsl.utilities.Identity;
import jsl.utilities.IdentityIfc;

/**
 * An abstract base class for building sub-classes that implement the
 * CollectorIfc.  Permits saving of collected data to an array via the ArraySaverIfc
 * Implementations must implement saving during collection process. The default is
 * to not save data automatically.
 *
 * @author rossetti
 *
 */
public abstract class AbstractCollector implements CollectorIfc, IdentityIfc, ArraySaverIfc {

    private final Identity myIdentity;
    private final ArraySaverIfc myArraySaver;

    /**
     *
     */
    public AbstractCollector() {
        this(null);
    }

    /**
     *
     * @param name the name of the collector
     */
    public AbstractCollector(String name) {
        myIdentity = new Identity(name);
        // default is to not save data automatically
        myArraySaver = new ArraySaver(false);
    }

    @Override
    public final String getName() {
        return myIdentity.getName();
    }

    /**
     * Sets the name
     *
     * @param str The name as a string.
     */
    public final void setName(String str) {
        myIdentity.setName(str);
    }

    @Override
    public final int getId() {
        return (myIdentity.getId());
    }

    @Override
    public final boolean getSaveOption() {
        return myArraySaver.getSaveOption();
    }

    @Override
    public final void save(double x) {
        myArraySaver.save(x);
    }

    @Override
    public final void save(double[] values) {
        myArraySaver.save(values);
    }

    @Override
    public final double[] getSavedData() {
        return myArraySaver.getSavedData();
    }

    @Override
    public final void setArraySizeIncrement(int n) {
        myArraySaver.setArraySizeIncrement(n);
    }

    @Override
    public final void setSaveOption(boolean flag) {
        myArraySaver.setSaveOption(flag);
    }

    @Override
    public final void clearSavedData() {
        myArraySaver.clearSavedData();
    }
}
