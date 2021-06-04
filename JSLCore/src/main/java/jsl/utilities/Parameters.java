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
package jsl.utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author rossetti
 */
public class Parameters {
    
    private List<String> myPNames;
    
    private Map<String, Double> myParams;

    public Parameters() {
        myPNames = new ArrayList<String>();
        myParams = new HashMap<String, Double>();
    }
    
    public void setParameter(String name, double value){
        if (!myParams.containsKey(name)){
            myPNames.add(name);
        } 
        myParams.put(name, value);
    }
    
    public void setParameter(int i, double value){
        myParams.put(myPNames.get(i), value);
    }
    
    public double getParameter(String name){
        if (!myParams.containsKey(name)){
            throw new IllegalArgumentException("A parameter callsed " + name + " doesn't exist");
        }
        return myParams.get(name);
    }
    
    public double getParameter(int i){
        return myParams.get(myPNames.get(i));
    }
    
}
