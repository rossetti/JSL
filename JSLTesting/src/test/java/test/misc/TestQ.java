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

package test.misc;

import jsl.simulation.Simulation;
import jsl.modeling.queue.QObject;
import jsl.modeling.queue.Queue;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestQ {


    @Test
    public void test1() {
        Simulation s = new Simulation();

        Queue<QObject> q = new Queue<>(s.getModel());
        
//        Queue q1 = new Queue(s.getModel());
//        q1.enqueue(queueingObject);
//        QObject remove = q1.remove(0);
        QObject r1 = new QObject(q.getTime(), "A");
        q.enqueue(r1);
        QObject r2 = new QObject(q.getTime(), "B");
        q.enqueue(r2);
        QObject r3 = new QObject(q.getTime(), "A");
        q.enqueue(r3);
        QObject r4 = new QObject(q.getTime(), "C");
        q.enqueue(r4);
        QObject r5 = new QObject(q.getTime(), "D");
        q.enqueue(r5);

        System.out.println("Before");
        for (QObject qo : q) {
            System.out.println(qo);
        }

        for (int i = 0; i < q.size(); i++) {
            QObject v = q.peekAt(i);
            if (v.getName().equals("A")) {
                q.remove(v);
            }
        }

        System.out.println("After");
        boolean t = true;
        int i = 1;
        for (QObject qo : q) {
            System.out.println(qo);
            if (i == 1 && !qo.getName().equals("B")) {
                t = false;
                break;
            }
            if (i == 2 && !qo.getName().equals("C")) {
                t = false;
                break;
            }
            if (i == 3 && !qo.getName().equals("D")) {
                t = false;
                break;
            }
            i++;
        }
        assertTrue(t);
    }

}
