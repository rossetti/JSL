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
package examples.entity;

import jsl.simulation.Model;
import jsl.simulation.ModelElement;
import jsl.simulation.Simulation;
import jsl.modeling.elements.entity.Delay;
import jsl.modeling.elements.entity.Entity;
import jsl.modeling.elements.entity.EntityGenerator;
import jsl.modeling.elements.entity.EntityReceiver;
import jsl.modeling.elements.entity.EntityReceiverAbstract;
import jsl.modeling.elements.entity.EntityType;
import jsl.modeling.elements.entity.DisposeEntity;
import jsl.modeling.elements.entity.Request;
import jsl.modeling.elements.entity.Resource;
import jsl.modeling.elements.entity.ResourcedActivity;
import jsl.modeling.elements.entity.SQSRWorkStation;
import jsl.modeling.elements.entity.WorkStation;
import jsl.utilities.random.RandomIfc;
import jsl.simulation.SimulationReporter;
import jsl.utilities.random.rvariable.ConstantRV;
import jsl.utilities.random.rvariable.ExponentialRV;
import jsl.utilities.random.rvariable.UniformRV;

/**
 *
 * @author rossetti
 */
public class TestEntityPackage {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         test1();
        // test2();
        // test3();
        //test4();
        //test5();
        //test6();
        //test7();
        //test8();
        //test9();
        //test10();
        //test11();
        //test12();
        //test13();
        //test14();
    }

    public static void test1() {

        Simulation s = new Simulation("test1");

        Model m = s.getModel();

        EntityGenerator g = new EntityGenerator(m, new ConstantRV(10.0),
                new ConstantRV(10.0));
        g.setDirectEntityReceiver(new NothingReceiver());
        g.useDefaultEntityType();

        s.setNumberOfReplications(5);
        s.setLengthOfReplication(100.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
    }

    public static void test2() {
        Simulation s = new Simulation("test2");

        Model m = s.getModel();

        EntityGenerator g = new EntityGenerator(m, new ConstantRV(10.0),
                new ConstantRV(10.0));
        g.setDirectEntityReceiver(new TestReceiver(m));
        g.useDefaultEntityType();

        s.setNumberOfReplications(5);
        s.setLengthOfReplication(100.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
    }

    public static void test3() {
        Simulation s = new Simulation("test3");

        Model m = s.getModel();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.useDefaultEntityType();

        WorkStation w = new WorkStation(m);
        w.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        g.setDirectEntityReceiver(w);

        DisposeEntity x = new DisposeEntity();
        w.setDirectEntityReceiver(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test4() {
        Simulation s = new Simulation("test4");

        Model m = s.getModel();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.useDefaultEntityType();

        WorkStation w1 = new WorkStation(m);
        w1.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        g.setDirectEntityReceiver(w1);

        WorkStation w2 = new WorkStation(m);
        w2.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        w1.setDirectEntityReceiver(w2);

        DisposeEntity x = new DisposeEntity();
        w2.setDirectEntityReceiver(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test5() {
        Simulation s = new Simulation("test5");

        Model m = s.getModel();

        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);

        WorkStation w = new WorkStation(m);
        w.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        g.setDirectEntityReceiver(w);

        DisposeEntity x = new DisposeEntity();
        w.setDirectEntityReceiver(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test6() {
        Simulation s = new Simulation("test6");

        Model m = s.getModel();

        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);
        g.setSendingOption(EntityType.SendOption.SEQ);

        WorkStation w1 = new WorkStation(m);
        w1.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        w1.setSendingOption(EntityType.SendOption.SEQ);

        WorkStation w2 = new WorkStation(m);
        w2.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        w2.setSendingOption(EntityType.SendOption.SEQ);

        DisposeEntity x = new DisposeEntity();

        et.addToSequence(w1);
        et.addToSequence(w2);
        et.addToSequence(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test7() {
        Simulation s = new Simulation("test7");

        Model m = s.getModel();

        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);
        g.setSendingOption(EntityType.SendOption.BY_TYPE);

        WorkStation w1 = new WorkStation(m);
        w1.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        w1.setSendingOption(EntityType.SendOption.BY_TYPE);

        WorkStation w2 = new WorkStation(m);
        w2.setServiceTimeInitialRandomSource(new ExponentialRV(0.5));
        w2.setSendingOption(EntityType.SendOption.BY_TYPE);

        DisposeEntity x = new DisposeEntity();

        et.addDestination(g, w1);
        et.addDestination(w1, w2);
        et.addDestination(w2, x);

        // set the parameters of the experiment
        s.setNumberOfReplications(10);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test8() {
        Simulation s = new Simulation("test8");

        Model m = s.getModel();
        ConstantRV c1 = new ConstantRV(10.0);
        EntityGenerator g = new EntityGenerator(m, c1, c1);
        ConstantRV c2 = ConstantRV.TWO;
        Delay a1 = new Delay(m);
        a1.setDelayTime(c2);
        Delay a2 = new Delay(m);
        a2.setDelayTime(c2);

        g.useDefaultEntityType();
        g.setDirectEntityReceiver(a1);
        a1.setDirectEntityReceiver(a2);

        DisposeEntity x = new DisposeEntity();
        a2.setDirectEntityReceiver(x);

        s.setNumberOfReplications(5);
        s.setLengthOfReplication(100.0);
        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
    }

    public static void test9() {
        System.out.println("Tandem Queue");
        Simulation s = new Simulation("test9");

        Model m = s.getModel();

        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);
        g.setSendingOption(EntityType.SendOption.SEQ);

        WorkStation w1 = new WorkStation(m, "W1");
        w1.setServiceTimeInitialRandomSource(new ExponentialRV(0.7));
        w1.setSendingOption(EntityType.SendOption.SEQ);

        WorkStation w2 = new WorkStation(m, "W2");
        w2.setServiceTimeInitialRandomSource(new ExponentialRV(0.9));
        w2.setSendingOption(EntityType.SendOption.SEQ);

        DisposeEntity x = new DisposeEntity();

        et.addToSequence(w1);
        et.addToSequence(w2);
        et.addToSequence(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(30);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test10() {
        System.out.println("Tandem Queue with Transport Delay");

        Simulation s = new Simulation("test10");

        Model m = s.getModel();
        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);
        g.setSendingOption(EntityType.SendOption.SEQ);

        WorkStation w1 = new WorkStation(m, "W1");
        w1.setServiceTimeInitialRandomSource(new ExponentialRV(0.7));
        w1.setSendingOption(EntityType.SendOption.SEQ);

        Delay a1 = new Delay(m);
        a1.setDelayTime(new UniformRV(0.0, 2.0));
        a1.setSendingOption(EntityType.SendOption.SEQ);

        WorkStation w2 = new WorkStation(m, "W2");
        w2.setServiceTimeInitialRandomSource(new ExponentialRV(0.9));
        w2.setSendingOption(EntityType.SendOption.SEQ);

        DisposeEntity x = new DisposeEntity();

        et.addToSequence(w1);
        et.addToSequence(a1);
        et.addToSequence(w2);
        et.addToSequence(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(30);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test11() {
        Simulation s = new Simulation("test11");

        Model m = s.getModel();

        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);

        Resource r1 = new Resource(m, "W1_R");
        ResourcedActivity w1 = new ResourcedActivity(m, "W1");
        w1.setDelayTime(new ExponentialRV(0.5));
        w1.addSeizeRequirement(r1, 1, Request.DEFAULT_PRIORITY, false);
        w1.addReleaseRequirement(r1, 1);
        g.setDirectEntityReceiver(w1);

        DisposeEntity x = new DisposeEntity();
        w1.setDirectEntityReceiver(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(30);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test12() {
        Simulation s = new Simulation("test12");

        Model m = s.getModel();

        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);

        Resource r1 = new Resource(m, "W1_R");
        ResourcedActivity w1 = new ResourcedActivity(m, "W1");
        w1.setDelayTime(new ExponentialRV(0.7));
        w1.addSeizeRequirement(r1, 1);
        w1.addReleaseRequirement(r1, 1);
        g.setDirectEntityReceiver(w1);

        Resource r2 = new Resource(m, "W2_R");
        ResourcedActivity w2 = new ResourcedActivity(m, "W2");
        w2.setDelayTime(new ExponentialRV(0.9));
        w2.addSeizeRequirement(r2, 1);
        w2.addReleaseRequirement(r2, 1);
        w1.setDirectEntityReceiver(w2);

        DisposeEntity x = new DisposeEntity();
        w2.setDirectEntityReceiver(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(30);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test13() {
        Simulation s = new Simulation("test13");

        Model m = s.getModel();

        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);

        SQSRWorkStation w = new SQSRWorkStation(m);
        w.setDelayTime(new ExponentialRV(0.5));
        g.setDirectEntityReceiver(w);

        DisposeEntity x = new DisposeEntity();
        w.setDirectEntityReceiver(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(30);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    public static void test14() {
        Simulation s = new Simulation("test14");

        Model m = s.getModel();
        EntityType et = new EntityType(m, "Job");
        et.turnOnNumberInSystemCollection();
        et.turnOnTimeInSystemCollection();

        RandomIfc tba = new ExponentialRV(1.0);
        EntityGenerator g = new EntityGenerator(m, tba, tba);
        g.setEntityType(et);

        SQSRWorkStation w1 = new SQSRWorkStation(m, "W1");
        w1.setDelayTime(new ExponentialRV(0.7));
        g.setDirectEntityReceiver(w1);

        SQSRWorkStation w2 = new SQSRWorkStation(m, "W2");
        w2.setDelayTime(new ExponentialRV(0.9));
        w1.setDirectEntityReceiver(w2);

        DisposeEntity x = new DisposeEntity();
        w2.setDirectEntityReceiver(x);

        // set the parameters of the experiment
        s.setNumberOfReplications(30);
        s.setLengthOfReplication(20000.0);
        s.setLengthOfWarmUp(5000.0);

        s.run();

        SimulationReporter r = s.makeSimulationReporter();
        r.printFullAcrossReplicationStatistics();
        System.out.println("Done!");

    }

    static protected class NothingReceiver extends EntityReceiverAbstract {

        @Override
        protected void receive(Entity entity) {
            EntityType et = entity.getType();

            System.out.println(et.getTime() + " > " + entity + " was received");
        }
    }

    static protected class TestReceiver extends EntityReceiver {

        public TestReceiver(ModelElement parent, String name) {
            super(parent, name);
        }

        public TestReceiver(ModelElement parent) {
            super(parent);
        }

        @Override
        protected void receive(Entity entity) {
            EntityType et = entity.getType();

            System.out.println(et.getTime() + " > " + entity + " was received");
        }
    }
}
