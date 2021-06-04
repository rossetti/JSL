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
/**
 *  This package contains implementations of random variables in order to generate random variates from
 *  various distributions.  In most cases, the inverse transform technique is used.  The JSLRandom class
 *  has static methods for various distributions.  Creating an instance of a random variable allows the
 *  programmer to better leverage object-oriented principles. In addition, each random variable instance
 *  has its own underlying stream. Thus, different instances of random variables produce (for all practical purposes)
 *  independent random variates based on their independent stream instances.  To control dependenced between
 *  generated instances, users can provide streams and control the streams to manipulate it randomness.
 */
package jsl.utilities.random.rvariable;
