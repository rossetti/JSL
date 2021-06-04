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

import java.util.Map;
import java.util.Set;
import java.util.Collections;

import jsl.modeling.elements.variable.AcrossReplicationStatisticIfc;
import jsl.modeling.elements.variable.BatchStatisticsIfc;
import jsl.modeling.elements.variable.WithinReplicationStatisticIfc;
import jsl.utilities.statistic.StatisticAccessorIfc;

/** This class acts like a Map to allow named responses and
 *  their associated values to be viewed.  
 *  
 *  Implementors of ResponseMakerIfc are responsible for making
 *  instances of this class that are filled appropriately
 * 
 *  Note: None of the underlying Maps are made.  Implementers
 *  of sub-classes must create and populate the maps.
 * 
 *  Attempting to access a map when it has not been created
 *  will result in an IllegalStateException.
 * 
 *  The hasXResponse() methods can be used to check if the 
 *  response datatype has been defined.
 *
 */
abstract public class Responses {

    /** The ResponseMakerIfc that uses the Responses
     * 
     */
    protected ResponseMakerIfc myResponseMaker;

    /** The Map that hold the responses as pairs
     *  key = name of response
     *  value = value of the response as a Double
     *  Not allocated unless at least one response is supplied
     * 
     */
    protected Map<String, Double> myDoubleResponses;

    /** The Map that hold the responses as pairs
     *  key = name of response
     *  value = value of the response as a double[]
     *  Not allocated unless at least one response is supplied
     * 
     */
    protected Map<String, double[]> myDoubleArrayResponses;

    /** The Map that hold the responses as pairs
     *  key = name of response
     *  value = value of the response as a StatisticAccessorIfc
     *  Not allocated unless at least one response is supplied
     * 
     */
    protected Map<String, StatisticAccessorIfc> myStatisticAccessorResponses;

    /** The Map that hold the responses as pairs
     *  key = name of response
     *  value = value of the response as a AcrossReplicationStatisticIfc
     *  Not allocated unless at least one response is supplied
     * 
     */
    protected Map<String, AcrossReplicationStatisticIfc> myAcrossReplicationResponses;

    /** The Map that hold the responses as pairs
     *  key = name of response
     *  value = value of the response as a WithinReplicationStatisticIfc
     *  Not allocated unless at least one response is supplied
     * 
     */
    protected Map<String, WithinReplicationStatisticIfc> myWithinReplicationResponses;

    /** The Map that hold the responses as pairs
     *  key = name of response
     *  value = value of the response as a WithinReplicationStatisticIfc
     *  Not allocated unless at least one response is supplied
     * 
     */
    protected Map<String, BatchStatisticsIfc> myBatchStatisticsResponses;

    /** The Map that hold the responses as pairs
     *  key = name of response
     *  value = value of the response as a Responses
     *  Not allocated unless at least one response is supplied
     * 
     */
    protected Map<String, Responses> myResponses;

    /**
     * 
     * @param responseMaker 
     */
    protected Responses(ResponseMakerIfc responseMaker) {
        if (responseMaker == null) {
            throw new IllegalArgumentException("The ResponseMakerIfc was null");
        }
        myResponseMaker = responseMaker;
    }

    /**
     * 
     * @return
     */
    public ResponseMakerIfc getResponseMaker() {
        return myResponseMaker;
    }

    /** Gets the value associated with the supplied key as a double.  The key must already
     *  exist in the responses and cannot be null.
     * 
     * @param key
     * @return
     */
    public double getDoubleResponse(String key) {

        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (myDoubleResponses == null) {
            throw new IllegalStateException("Attempted to get double response when no double responses have been set.");
        }

        if (!myDoubleResponses.containsKey(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a response value");
        }

        return myDoubleResponses.get(key).doubleValue();
    }

    /** Gets the value associated with the supplied key as a Responses.  The key must already
     *  exist in the responses and cannot be null.
     * 
     * @param key
     * @return
     */
    public Responses getResponse(String key) {

        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (myResponses == null) {
            throw new IllegalStateException("Attempted to get Responses response when no double responses have been set.");
        }

        if (!myResponses.containsKey(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a response value");
        }

        return myResponses.get(key);
    }

    /** Gets the value associated with the supplied key as a StatisticAccessorIfc.  The key must already
     *  exist in the responses and cannot be null.
     * 
     * @param key
     * @return
     */
    public StatisticAccessorIfc getStatisticAccessorIfcResponse(String key) {

        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (myStatisticAccessorResponses == null) {
            throw new IllegalStateException("Attempted to get StatisticAccessorIfc response when no double responses have been set.");
        }

        if (!myStatisticAccessorResponses.containsKey(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a response value");
        }

        return myStatisticAccessorResponses.get(key);
    }

    /** Gets the value associated with the supplied key as a double{].  The key must already
     *  exist in the responses and cannot be null. A copy of the stored double[] is returned.
     * 
     * @param key
     * @return
     */
    public double[] getDoubleArrayResponse(String key) {

        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (myDoubleArrayResponses == null) {
            throw new IllegalStateException("Attempted to get double[] response when no double[] responses have been set.");
        }

        if (!myDoubleArrayResponses.containsKey(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a response value");
        }

        double[] value = myDoubleArrayResponses.get(key);
        double[] tmp = new double[value.length];
        System.arraycopy(value, 0, tmp, 0, value.length);
        return tmp;
    }

    /** Gets the value associated with the supplied key as a AcrossReplicationStatisticsIfc.  The key must already
     *  exist in the responses and cannot be null.
     * 
     * @param key
     * @return
     */
    public AcrossReplicationStatisticIfc getAcrossReplicationStatisticIfcResponse(String key) {

        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (myAcrossReplicationResponses == null) {
            throw new IllegalStateException("Attempted to get AcrossReplicationStatisticIfc response when no double responses have been set.");
        }

        if (!myAcrossReplicationResponses.containsKey(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a response value");
        }

        return myAcrossReplicationResponses.get(key);
    }

    /** Gets the value associated with the supplied key as a WithinReplicationStatisticsIfc.  The key must already
     *  exist in the responses and cannot be null.
     * 
     * @param key
     * @return
     */
    public WithinReplicationStatisticIfc getWithinReplicationStatisticIfcResponse(String key) {

        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (myWithinReplicationResponses == null) {
            throw new IllegalStateException("Attempted to get WithinReplicationStatisticIfc response when no double responses have been set.");
        }

        if (!myWithinReplicationResponses.containsKey(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a response value");
        }

        return myWithinReplicationResponses.get(key);
    }

    /** Gets the value associated with the supplied key as a BatchStatisticsIfc.  The key must already
     *  exist in the responses and cannot be null.
     * 
     * @param key
     * @return
     */
    public BatchStatisticsIfc getBatchStatisticIfcResponse(String key) {

        if (key == null) {
            throw new IllegalArgumentException("The supplied key cannot be null");
        }

        if (myBatchStatisticsResponses == null) {
            throw new IllegalStateException("Attempted to get BatchStatisticIfc response when no double responses have been set.");
        }

        if (!myBatchStatisticsResponses.containsKey(key)) {
            throw new IllegalArgumentException("The supplied key is not associated with a response value");
        }

        return myBatchStatisticsResponses.get(key);
    }

    /** Checks if the supplied key is contained in the responses
     * 
     * @param key
     * @return
     */
    public boolean containsResponse(String key) {

        if ((myAcrossReplicationResponses != null) && (myAcrossReplicationResponses.containsKey(key))) {
            return true;
        } else if ((myWithinReplicationResponses != null) && (myWithinReplicationResponses.containsKey(key))) {
            return true;
        } else if ((myBatchStatisticsResponses != null) && (myBatchStatisticsResponses.containsKey(key))) {
            return true;
        } else if ((myDoubleResponses != null) && (myDoubleResponses.containsKey(key))) {
            return true;
        } else if ((myDoubleArrayResponses != null) && (myDoubleArrayResponses.containsKey(key))) {
            return true;
        } else if ((myStatisticAccessorResponses != null) && (myStatisticAccessorResponses.containsKey(key))) {
            return true;
        } else if ((myResponses != null) && (myResponses.containsKey(key))) {
            return true;
        } else {
            return false;
        }
    }

    /** Returns true if at least one BatchStatisticIfc response has been set
     * 
     * @return
     */
    public boolean hasBatchStatisticResponse() {
        return (myBatchStatisticsResponses != null) && (!myBatchStatisticsResponses.isEmpty());
    }

    /** Returns true if at least one WithinReplicationStatisticIfc response has been set
     * 
     * @return
     */
    public boolean hasWithinReplicationResponse() {
        return (myWithinReplicationResponses != null) && (!myWithinReplicationResponses.isEmpty());
    }

    /** Returns true if at least one AcrossReplicationStatisticIfc response has been set
     * 
     * @return
     */
    public boolean hasAcrossReplicationResponse() {
        return (myAcrossReplicationResponses != null) && (!myAcrossReplicationResponses.isEmpty());
    }

    /** Returns true if at least one Double response has been set
     * 
     * @return
     */
    public boolean hasDoubleResponse() {
        return (myDoubleResponses != null) && (!myDoubleResponses.isEmpty());
    }

    /** Returns true if at least one double[] response has been set
     * 
     * @return
     */
    public boolean hasDoubleArrayResponse() {
        return (myDoubleArrayResponses != null) && (!myDoubleArrayResponses.isEmpty());
    }

    /** Returns true if at least one Responses response has been set
     * 
     * @return
     */
    public boolean hasResponsesResponse() {
        return (myResponses != null) && (!myResponses.isEmpty());
    }

    /** Returns true if at least one StatisticAccessorIfc response has been set
     * 
     * @return
     */
    public boolean hasStatisticAccessorResponse() {
        return (myStatisticAccessorResponses != null) && (!myStatisticAccessorResponses.isEmpty());
    }

    /** Returns an unmodifiable Set of the response's keys
     *  for AcrossReplicationStatisticIfc responses
     *  
     * @return
     */
    public Set<String> getAcrossReplicationResponseKeySet() {
        if (myAcrossReplicationResponses != null) {
            return Collections.unmodifiableSet(myAcrossReplicationResponses.keySet());
        } else {
            return null;
        }
    }

    /** Returns an unmodifiable Set of the response's keys
     *  for WithinReplicationStatisticIfc responses
     *  
     * @return
     */
    public Set<String> getWithinReplicationResponseKeySet() {
        if (myWithinReplicationResponses != null) {
            return Collections.unmodifiableSet(myWithinReplicationResponses.keySet());
        } else {
            return null;
        }
    }

    /** Returns an unmodifiable Set of the response's keys
     *  for BatchStatisticIfc responses
     *  
     * @return
     */
    public Set<String> getBatchStatisticResponseKeySet() {
        if (myBatchStatisticsResponses != null) {
            return Collections.unmodifiableSet(myBatchStatisticsResponses.keySet());
        } else {
            return null;
        }
    }

    /** Returns an unmodifiable Set of the response's keys
     *  for StatisticAccessorIfc responses
     *  
     * @return
     */
    public Set<String> getStatisticAccessorResponseKeySet() {
        if (myStatisticAccessorResponses != null) {
            return Collections.unmodifiableSet(myStatisticAccessorResponses.keySet());
        } else {
            return null;
        }
    }

    /** Returns an unmodifiable Set of the response's keys
     *  for Responses responses
     *  
     * @return
     */
    public Set<String> getResponsesKeySet() {
        if (myResponses != null) {
            return Collections.unmodifiableSet(myResponses.keySet());
        } else {
            return null;
        }
    }

    /** Returns an unmodifiable Set of the response's keys
     *  for Responses
     *  
     * @return
     */
    public Set<String> getDoubleResponseKeySet() {
        if (myDoubleResponses != null) {
            return Collections.unmodifiableSet(myDoubleResponses.keySet());
        } else {
            return null;
        }
    }

    /** Returns an unmodifiable Set of the response's keys
     *  for double[] Controls
     *  
     * @return
     */
    public Set<String> getDoubleResponseArrayKeySet() {
        if (myDoubleArrayResponses != null) {
            return Collections.unmodifiableSet(myDoubleArrayResponses.keySet());
        } else {
            return null;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ID \t " + myResponseMaker.getId() + "\n");
        sb.append("Name \t " + myResponseMaker.getName() + "\n");
        if (myDoubleResponses != null) {
            sb.append(myDoubleResponses.toString());
            sb.append("\n");
        }
        if (myStatisticAccessorResponses != null) {
            sb.append(myStatisticAccessorResponses.toString());
            sb.append("\n");
        }
        if (myResponses != null) {
            sb.append(myResponses.toString());
            sb.append("\n");
        }
        //TODO double[], WitinReplication, AcrossReplication, BatchStatistic
        return sb.toString();
    }
}
