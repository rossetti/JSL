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
package jsl.utilities.random.robj;

import java.util.HashMap;
import java.util.Map;

public class DUniformList<T> extends RList<T> {

    public DUniformList() {
        super();
    }

    @Override
    public T getRandomElement() {
        if (myElements.isEmpty()) {
            return null;
        }

        return (myElements.get(myStream.randInt(0, myElements.size() - 1)));
    }

    @Override
    public RList<T> newInstance() {
        DUniformList<T> l = new DUniformList<T>();
        l.addAll(this);
        return l;
    }

    public static void main(String[] args) {

        DUniformList<String> originSet = new DUniformList<String>();

        originSet.add("KC");
        originSet.add("CH");
        originSet.add("NY");

        for (int i = 1; i <= 10; i++) {
            System.out.println(originSet.getRandomElement());
        }

        Map<String, DUniformList<String>> od = new HashMap<String, DUniformList<String>>();

        DUniformList<String> kcdset = new DUniformList<String>();

        kcdset.add("CO");
        kcdset.add("AT");
        kcdset.add("NY");

        DUniformList<String> chdset = new DUniformList<String>();

        chdset.add("AT");
        chdset.add("NY");
        chdset.add("KC");

        DUniformList<String> nydset = new DUniformList<String>();

        nydset.add("AT");
        nydset.add("KC");
        nydset.add("CH");

        od.put("KC", kcdset);
        od.put("CH", chdset);
        od.put("NY", nydset);

        for (int i = 1; i <= 10; i++) {
            String key = originSet.getRandomElement();
            DUniformList<String> rs = od.get(key);
            System.out.println(rs.getRandomElement());
        }
    }
}
