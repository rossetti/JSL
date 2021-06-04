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
package jsl.observers;

/**
 *
 * @author rossetti
 */
public class TestObserverableObserver {

    public TestObserverableObserver() {
    }

    public void test() {
        Observable w = new Observable();

        Observer o = new Observer();

        w.addObserver(o);

        w.changeState();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        TestObserverableObserver t = new TestObserverableObserver();

        t.test();

    }

    public class Observer implements ObserverIfc {

        public void update(Object theObserved, Object arg) {
            System.out.println("update");
        }
    }

    public class Observable implements ObservableIfc {

        private ObservableComponent myObservableComponent = new ObservableComponent();

        public void addObserver(ObserverIfc observer) {
            myObservableComponent.addObserver(observer);
        }

        public void deleteObserver(ObserverIfc observer) {
            myObservableComponent.deleteObserver(observer);
        }

        public boolean contains(ObserverIfc observer) {
            return myObservableComponent.contains(observer);
        }

        public void deleteObservers() {
            myObservableComponent.deleteObservers();
        }

        public int countObservers() {
            return myObservableComponent.countObservers();
        }

        public void changeState() {
            myObservableComponent.notifyObservers(this, null);
        }
    }
}
