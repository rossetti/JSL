package jslx.random.qmc;

import jsl.utilities.JSLArrayUtil;
import org.apache.commons.math3.primes.Primes;

import java.util.LinkedHashSet;
import java.util.Set;

public class MakePrimes {

    public static void main(String[] args) {
        int n = 100;
//        Set<Integer> primes = new LinkedHashSet<>();
        int i = 1;
        int j = 1;
        double[] primes = new double[n];
        while(i <= n){
            if (Primes.isPrime(j)){
                primes[i-1] = Math.sqrt(j);
                i++;
            }
            j++;
        }
        String s = JSLArrayUtil.toCSVString(primes);
        System.out.println(s);

//            p[i] = Math.sqrt(Primes.nextPrime(i + 2)); // uses Apache Math Commons
    }
}
