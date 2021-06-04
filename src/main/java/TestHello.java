import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import examples.queueing.DriverLicenseBureau;
import examples.queueing.DriverLicenseBureauWithQ;
import examples.running.UsingJSLDbExamples;
import jsl.utilities.random.rvariable.LognormalRV;
import jsl.utilities.random.rvariable.NormalRV;
import jsl.utilities.random.rvariable.ShiftedRV;
import jsl.utilities.random.rvariable.TriangularRV;
import jsl.utilities.reporting.JSL;
import jsl.utilities.statistic.Statistic;
import org.slf4j.LoggerFactory;

public class TestHello {

    public static void main(String[] args) {
        // assume SLF4J is bound to logback in the current environment
//        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//        // print logback's internal status
//        StatusPrinter.print(lc);

        System.out.println("Hello");

//        Statistic s = new Statistic();
//        NormalRV n = new NormalRV();
//        s.collect(n.sample(199));
//        System.out.println(s);

        //DriverLicenseBureauWithQ.runExperiment();
//        UsingJSLDbExamples.testJSLDatabase();

//        LognormalRV rv = new LognormalRV(22.0, 25.0*25.0);
//        ShiftedRV srv = new ShiftedRV(43.0, rv);
//        LognormalRV rv = new LognormalRV(70.0, 1459);
//        ShiftedRV srv = new ShiftedRV(60.0, rv);
        TriangularRV srv = new TriangularRV(56.0, 128.0,166.0 );
        for (int i = 0; i < 70; i++) {
            JSL.out.printf("%.1f %n", srv.getValue());
        }
    }
}
