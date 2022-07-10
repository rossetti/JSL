package examples.general.utilities.random;

import jsl.modeling.elements.variable.RandomVariable;
import jsl.simulation.Model;
import jsl.simulation.Simulation;
import jsl.utilities.random.rvariable.*;
import jsl.utilities.reporting.JSONUtil;

public class TestRVParameters {

    public static void main(String[] args) {
        testRVParameters();

        testRVParameterSetter();
    }

    public static void testRVParameterSetter() {
        Simulation simulation = new Simulation();
        Model model = simulation.getModel();
        RandomVariable rv1 = new RandomVariable(model, new BinomialRV(0.8, 10), "rv1");
        RandomVariable rv2 = new RandomVariable(model, new TriangularRV(10.0, 15.0, 25.0), "rv2");
        RandomVariable rv3 = new RandomVariable(model, new NormalRV(10.0, 4.0), "rv3");
        RVariableIfc de = new DEmpiricalRV(new double[]{1.0, 2.0, 3.0}, new double[]{0.35, 0.80, 1.0});
        RandomVariable rv4 = new RandomVariable(model, de, "rv4");

        RVParameterSetter setter = new RVParameterSetter();
        setter.extractParameters(model);
        System.out.println(JSONUtil.rvParameterSetterToJson(setter));
        RVParameters parameters1 = setter.getRVParameters("rv1");
        parameters1.changeDoubleParameter("probOfSuccess", 0.66);
        RVParameters parameters2 = setter.getRVParameters("rv4");
        parameters2.changeDoubleArrayParameter("values", new double[]{5.0, 6.0, 8.0});
        System.out.println();
        System.out.println(JSONUtil.rvParameterSetterToJson(setter));
        int numChanged = setter.applyParameterChanges(model);
        System.out.println();
        System.out.println("number of parameter changes = " + numChanged);
        RVParameterSetter setter2 = new RVParameterSetter();
        setter2.extractParameters(model);
        System.out.println();
        String json = JSONUtil.rvParameterSetterToJson(setter2);
        System.out.println(json);

        RVParameterSetter setter3 = JSONUtil.fromJSON(json);
        System.out.println();
        System.out.println("From JSON string");
        System.out.println(JSONUtil.rvParameterSetterToJson(setter3));
    }

    public static void testRVParameters() {
        RVParameters p1 = RVType.Binomial.getRVParameters();
        RVParameters p2 = RVType.Normal.getRVParameters();
        RVParameters p3 = RVType.Triangular.getRVParameters();
        RVParameters p4 = RVType.Triangular.getRVParameters();

        System.out.println(JSONUtil.toJSONPretty(p1));
        System.out.println();

        System.out.println(JSONUtil.toJSONPretty(p2));
        System.out.println();

        System.out.println(JSONUtil.toJSONPretty(p3));
        System.out.println();

        System.out.println(JSONUtil.toJSONPretty(p4));
        System.out.println();

        if (p3.equals(p4)) {
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()) {
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        if (p1.equals(p2)) {
            System.out.println("p1 == p2");
        } else {
            System.out.println("p1 != p2");
        }

        p3.changeDoubleParameter("min", -5.0);
        if (p3.equals(p4)) {
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()) {
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        // not copy them over so that they are back to being the same
        p4.copyFrom(p3);
        if (p3.equals(p4)) {
            System.out.println("p3 == p4");
        } else {
            System.out.println("p3 != p4");
        }

        if (p3.hashCode() == p4.hashCode()) {
            System.out.println("hashcode p3 == p4");
        } else {
            System.out.println("hashcode p3 != p4");
        }

        System.out.println(JSONUtil.toJSONPretty(p3));
        System.out.println();

        System.out.println(JSONUtil.toJSONPretty(p4));
        System.out.println();
    }
}
