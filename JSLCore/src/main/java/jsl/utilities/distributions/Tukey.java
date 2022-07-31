package jsl.utilities.distributions;
/*
 * Copyright (c) 2022. Manuel D. Rossetti, rossetti@uark.edu
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
/*
 *  Mathlib : A C Library of Special Functions
 *  Copyright (C) 1998   Ross Ihaka
 *  Copyright (C) 2000-9 The R Development Core Team
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, a copy is available at
 *  http://www.r-project.org/Licenses/
 */

import jsl.utilities.reporting.JSL;

import static java.lang.Math.*;

/**
 *    Computes the probability and quantile that the studentized
 *    range, each based on n means and with df degrees of freedom
 *
 *    See functions: qtukey() and ptukey() from statistical software: R
 *
 *    The algorithm is based on that of the reference.
 *
 *  REFERENCE
 *    {@literal
 *    Copenhaver, Margaret Diponzio & Holland, Burt S.
 *    Multiple comparisons of simple effects in
 *    the two-way analysis of variance with fixed effects.
 *    Journal of Statistical Computation and Simulation,
 *    Vol.30, pp.1-15, 1988}
 *
 */
public class Tukey {

    public static void main(String[] args) {
        double[] k = {2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double p = 0.99;
        double df = 5.0;
        for (int i = 0; i < k.length; i++) {
            double result = invCDF(p, k[i], df);
            System.out.printf("p = %f \t df = %f \t k =%f \t result = %f %n", p, df, k[i], result);
        }

        System.out.println();

        double[] x = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0};
        double nMeans = 2.0;
        for (int i = 0; i < k.length; i++) {
            double result = cdf(x[i], nMeans, df);
            System.out.printf("nMeans = %f \t df =%f \t x = %f \t result = %f %n", nMeans, df, x[i], result);
        }

        // matches Table 8.1 of Goldsman and Nelson chapter 8, table 8.1
        double q = invCDF(0.95, 4, 20);
        System.out.printf("p = %f \t df = %f \t k =%f \t result = %f %n", 0.95, 20.0, 4.0, q);
    }

    private static double qTukeyEPS = 0.0001;
    private static int qTukeyMaxIterations = 50;

    /**
     *  Sets the precision for the computation of the invCDF
     *  Default is 0.0001
     * @param eps the desired precision
     */
    public static void setQTukeyPrecision(double eps){
        if (eps <= 0.0){
            qTukeyEPS = 0.0001;
        }
        qTukeyEPS = eps;
    }

    /**
     *  Sets the maximum number of iterations for the computation of invCDF
     *  default is 50
     *
     * @param iterations the number of iterations
     */
    public static void setQTukeyMaxIterations(int iterations){
        qTukeyMaxIterations = max(50, iterations);
    }

    /**
     * @param p      the probability, typically a confidence level (1-alpha), must be in (0,1)
     * @param nMeans the number of columns or treatments (means), must be greater than or equal to 2.0
     * @param df     the degrees of freedom, must be greater than or equal to 1.0
     * @return the quantile of the Tukey distribution
     */
    public static double invCDF(double p, double nMeans, double df) {
        if ((p < 0.0) || (p > 1.0)) {
            throw new IllegalArgumentException("Supplied probability was " + p + " Probability must be (0,1)");
        }
        if (nMeans <= 1.0) {
            throw new IllegalArgumentException("The number of groups must be >= 2");
        }
        if (df < 1.0) {
            throw new IllegalArgumentException("The degrees of freedom must be >= 1");
        }
        return qtukey(p, nMeans, df, 1.0, true, false);
    }

    /**
     * @param q      value of studentized range, must be greater than or equal to 0.0
     * @param nMeans the number of columns or treatments (means), must be greater than or equal to 2.0
     * @param df     the degrees of freedom, must be greater than or equal to 1.0
     * @return the probability integral over [0, q]
     */
    public static double cdf(double q, double nMeans, double df) {
        if (nMeans <= 1.0) {
            throw new IllegalArgumentException("The number of groups must be >= 2");
        }
        if (df < 1.0) {
            throw new IllegalArgumentException("The degrees of freedom must be >= 1");
        }
        if (q < 0.0) {
            throw new IllegalArgumentException("The value of the range for evaluation must be >= 0.0");
        }
        if (q == 0.0){
            return 0.0;
        }
        return ptukey(q, nMeans, df, 1.0, true, false);
    }

    /*  wprob() :

    This function calculates probability integral of Hartley's
    form of the range.

    w     = value of range
    rr    = no. of rows or groups
    cc    = no. of columns or treatments
    ir    = error flag = 1 if pr_w probability > 1
    pr_w = returned probability integral from (0, w)

    program will not terminate if ir is raised.

    bb = upper limit of legendre integration
    iMax = maximum acceptable value of integral
    nleg = order of legendre quadrature
    ihalf = int ((nleg + 1) / 2)
    wlar = value of range above which wincr1 intervals are used to
           calculate second part of integral,
           else wincr2 intervals are used.
    C1, C2, C3 = values which are used as cutoffs for terminating
    or modifying a calculation.

    M_1_SQRT_2PI = 1 / sqrt(2 * pi);  from abramowitz & stegun, p. 3.
    M_SQRT2 = sqrt(2)
    xleg = legendre 12-point nodes
    aleg = legendre 12-point coefficients
     */

    /**
     * This function calculates probability integral of Hartley's form of the range.
     *
     * @param w  the value of range
     * @param rr the number of ranges, always 1.0
     * @param cc the number of columns or treatments
     * @return the value of the probability integral
     */
    private static double wprob(double w, double rr, double cc) {
        final int nleg = 12, ihalf = 6;
        final double M_1_SQRT_2PI = 0.398942280401432677939946059934;
        /* const double iMax  = 1.; not used if = 1*/
        final double C1 = -30.;
        final double C2 = -50.;
        final double C3 = 60.;
        final double bb = 8.;
        final double wlar = 3.;
        final double wincr1 = 2.;
        final double wincr2 = 3.;
        final double[] xleg = {
                0.981560634246719250690549090149,
                0.904117256370474856678465866119,
                0.769902674194304687036893833213,
                0.587317954286617447296702418941,
                0.367831498998180193752691536644,
                0.125233408511468915472441369464
        };
        final double[] aleg = {
                0.047175336386511827194615961485,
                0.106939325995318430960254718194,
                0.160078328543346226334652529543,
                0.203167426723065921749064455810,
                0.233492536538354808760849898925,
                0.249147045813402785000562436043
        };
        double a, ac, pr_w, b, binc, blb, c, cc1,
                pminus, pplus, qexpo, qsqz, rinsum, wi, wincr, xx;
        double bub, einsum, elsum;
        int j, jj;

        qsqz = w * 0.5;

        /* if w >= 16 then the integral lower bound (occurs for c=20) */
        /* is 0.99999999999995 so return a value of 1. */

        if (qsqz >= bb)
            return 1.0;

        /* find (f(w/2) - 1) ^ cc */
        /* (first term in integral of hartley's form). */
        pr_w = 2.0 * Normal.stdNormalCDF(qsqz) - 1.0;
        /* if pr_w ^ cc < 2e-22 then set pr_w = 0 */
        if (pr_w >= exp(C2 / cc))
            pr_w = pow(pr_w, cc);
        else
            pr_w = 0.0;

        /* if w is large then the second component of the */
        /* integral is small, so fewer intervals are needed. */

        if (w > wlar)
            wincr = wincr1;
        else
            wincr = wincr2;

        /* find the integral of second term of hartley's form */
        /* for the integral of the range for equal-length */
        /* intervals using legendre quadrature.  limits of */
        /* integration are from (w/2, 8).  two or three */
        /* equal-length intervals are used. */

        /* blb and bub are lower and upper limits of integration. */

        blb = qsqz;
        binc = (bb - qsqz) / wincr;
        bub = blb + binc;
        einsum = 0.0;

        /* integrate over each interval */

        cc1 = cc - 1.0;
        for (wi = 1; wi <= wincr; wi++) {
            elsum = 0.0;
            a = 0.5 * (bub + blb);

            /* legendre quadrature with order = nleg */

            b = 0.5 * (bub - blb);

            for (jj = 1; jj <= nleg; jj++) {
                if (ihalf < jj) {
                    j = (nleg - jj) + 1;
                    xx = xleg[j - 1];
                } else {
                    j = jj;
                    xx = -xleg[j - 1];
                }
                c = b * xx;
                ac = a + c;

                /* if exp(-qexpo/2) < 9e-14, */
                /* then doesn't contribute to integral */

                qexpo = ac * ac;
                if (qexpo > C3)
                    break;

                pplus = 2.0 * Normal.stdNormalCDF(ac);
                pminus = 2.0 * Normal.stdNormalCDF(ac - w);
                /* if rinsum ^ (cc-1) < 9e-14, */
                /* then doesn't contribute to integral */

                rinsum = (pplus * 0.5) - (pminus * 0.5);
                if (rinsum >= exp(C1 / cc1)) {
                    rinsum = (aleg[j - 1] * exp(-(0.5 * qexpo))) * pow(rinsum, cc1);
                    elsum += rinsum;
                }
            }
            elsum *= (((2.0 * b) * cc) * M_1_SQRT_2PI);
            einsum += elsum;
            blb = bub;
            bub += binc;
        }

        /* if pr_w ^ rr < 9e-14, then return 0 */
        pr_w = einsum + pr_w;
        if (pr_w <= exp(C1 / rr))
            return 0.;

        pr_w = pow(pr_w, rr);
        if (pr_w >= 1.)/* 1 was iMax was eps */
            return 1.;
        return pr_w;
    }

    /*  function ptukey() [was qprob() ]:

    q = value of studentized range
    rr = no. of rows or groups
    cc = no. of columns or treatments
    df = degrees of freedom of error term
    ir[0] = error flag = 1 if wprob probability > 1
    ir[1] = error flag = 1 if qprob probability > 1

    qprob = returned probability integral over [0, q]

    The program will not terminate if ir[0] or ir[1] are raised.

    All references in wprob to Abramowitz and Stegun
    are from the following reference:

    Abramowitz, Milton and Stegun, Irene A.
    Handbook of Mathematical Functions.
    New York:  Dover publications, Inc. (1970).

    All constants taken from this text are
    given to 25 significant digits.

    nlegq = order of legendre quadrature
    ihalfq = int ((nlegq + 1) / 2)
    eps = max. allowable value of integral
    eps1 & eps2 = values below which there is
              no contribution to integral.

    d.f. <= dhaf:	integral is divided into ulen1 length intervals.  else
    d.f. <= dquar:	integral is divided into ulen2 length intervals.  else
    d.f. <= deigh:	integral is divided into ulen3 length intervals.  else
    d.f. <= dlarg:	integral is divided into ulen4 length intervals.

    d.f. > dlarg:	the range is used to calculate integral.

    M_LN2 = log(2)

    xlegq = legendre 16-point nodes
    alegq = legendre 16-point coefficients

    The coefficients and nodes for the legendre quadrature used in
    qprob and wprob were calculated using the algorithms found in:

    Stroud, A. H. and Secrest, D.
    Gaussian Quadrature Formulas.
    Englewood Cliffs,
    New Jersey:  Prentice-Hall, Inc, 1966.

    All values matched the tables (provided in same reference)
    to 30 significant digits.

    f(x) = .5 + erf(x / sqrt(2)) / 2      for x > 0

    f(x) = erfc( -x / sqrt(2)) / 2	      for x < 0

    where f(x) is standard normal c. d. f.

    if degrees of freedom large, approximate integral
    with range distribution.
     */

    public static double ptukey(double q, double nMeans, double df, double nRanges, boolean lower_tail, boolean log_p) {
        final int nlegq = 16, ihalfq = 8;
        final double M_LN2 = 0.693147180559945309417232121458;
        /*  const double eps = 1.0; not used if = 1 */
        final double eps1 = -30.0;
        final double eps2 = 1.0e-14;
        final double dhaf = 100.0;
        final double dquar = 800.0;
        final double deigh = 5000.0;
        final double dlarg = 25000.0;
        final double ulen1 = 1.0;
        final double ulen2 = 0.5;
        final double ulen3 = 0.25;
        final double ulen4 = 0.125;
        final double[] xlegq = {
                0.989400934991649932596154173450,
                0.944575023073232576077988415535,
                0.865631202387831743880467897712,
                0.755404408355003033895101194847,
                0.617876244402643748446671764049,
                0.458016777657227386342419442984,
                0.281603550779258913230460501460,
                0.950125098376374401853193354250e-1
        };
        final double[] alegq = {
                0.271524594117540948517805724560e-1,
                0.622535239386478928628438369944e-1,
                0.951585116824927848099251076022e-1,
                0.124628971255533872052476282192,
                0.149595988816576732081501730547,
                0.169156519395002538189312079030,
                0.182603415044923588866763667969,
                0.189450610455068496285396723208
        };
        double ans, f2, f21, f2lf, ff4, otsum = 0, qsqz, rotsum, t1, twa1, ulen, wprb;
        int i, j, jj;

        if (Double.isInfinite(q) || Double.isInfinite(nRanges) || Double.isInfinite(nMeans) || Double.isInfinite(df))
            return Double.NaN;

        if (q <= 0)
            return (lower_tail ? (log_p ? Double.NEGATIVE_INFINITY : 0.) : (log_p ? 0. : 1.));

        /* df must be > 1 */
        /* there must be at least two values */

        if (df < 2 || nRanges < 1 || nMeans < 2) return Double.NaN;

        if (Double.isInfinite(q))
            return (lower_tail ? (log_p ? 0. : 1.) : (log_p ? Double.NEGATIVE_INFINITY : 0.));

        if (df > dlarg) {
            //return R_DT_val(wprob(q, rr, cc));
            double x = wprob(q, nRanges, nMeans);
            return (lower_tail ? (log_p ? log(x) : (x)) : (log_p ? log1p(-(x)) : (0.5 - (x) + 0.5)));
        }

        /* calculate leading constant */

        f2 = df * 0.5;
        /* lgammafn(u) = log(gamma(u)) */
//        f2lf = ((f2 * log(df)) - (df * M_LN2)) - lgammafn(f2);
        f2lf = ((f2 * log(df)) - (df * M_LN2)) - Gamma.logGammaFunction(f2);

        f21 = f2 - 1.0;

        /* integral is divided into unit, half-unit, quarter-unit, or */
        /* eighth-unit length intervals depending on the value of the */
        /* degrees of freedom. */

        ff4 = df * 0.25;
        if (df <= dhaf) ulen = ulen1;
        else if (df <= dquar) ulen = ulen2;
        else if (df <= deigh) ulen = ulen3;
        else ulen = ulen4;

        f2lf += log(ulen);

        /* integrate over each subinterval */

        ans = 0.0;

        for (i = 1; i <= 50; i++) {
            otsum = 0.0;

            /* legendre quadrature with order = nlegq */
            /* nodes (stored in xlegq) are symmetric around zero. */

            twa1 = (2 * i - 1) * ulen;

            for (jj = 1; jj <= nlegq; jj++) {
                if (ihalfq < jj) {
                    j = jj - ihalfq - 1;
                    t1 = (f2lf + (f21 * log(twa1 + (xlegq[j] * ulen))))
                            - (((xlegq[j] * ulen) + twa1) * ff4);
                } else {
                    j = jj - 1;
                    t1 = (f2lf + (f21 * log(twa1 - (xlegq[j] * ulen))))
                            + (((xlegq[j] * ulen) - twa1) * ff4);
                }

                /* if exp(t1) < 9e-14, then doesn't contribute to integral */
                if (t1 >= eps1) {
                    if (ihalfq < jj) {
                        qsqz = q * sqrt(((xlegq[j] * ulen) + twa1) * 0.5);
                    } else {
                        qsqz = q * sqrt(((-(xlegq[j] * ulen)) + twa1) * 0.5);
                    }

                    /* call wprob to find integral of range portion */

                    wprb = wprob(qsqz, nRanges, nMeans);
                    rotsum = (wprb * alegq[j]) * exp(t1);
                    otsum += rotsum;
                }
                /* end legendre integral for interval i */
                /* L200: */
            }

            /* if integral for interval i < 1e-14, then stop.
             * However, in order to avoid small area under left tail,
             * at least  1 / ulen  intervals are calculated.
             */
            if (i * ulen >= 1.0 && otsum <= eps2)
                break;

            /* end of interval i */
            /* L330: */

            ans += otsum;
        }

        if (otsum > eps2) { /* not converged */
            JSL.getInstance().LOGGER.warn("The computation for Tukey cdf did not converge due to precision!");
            return Double.NaN;
        }
        if (ans > 1.)
            ans = 1.;
        //return R_DT_val(ans);
        return (lower_tail ? (log_p ? log(ans) : (ans)) : (log_p ? log1p(-(ans)) : (0.5 - (ans) + 0.5)));
    }

    /* qinv() :
     *	this function finds percentage point of the studentized range
     *	which is used as initial estimate for the secant method.
     *	function is adapted from portion of algorithm as 70
     *	from applied statistics (1974) ,vol. 23, no. 1
     *	by odeh, r. e. and evans, j. o.
     *
     *	  p = percentage point
     *	  nMeans = no. of columns or treatments (means)
     *	  df = degrees of freedom
     *	  qinv = returned initial estimate
     *
     *	vmax is cutoff above which degrees of freedom
     *	is treated as infinity.
     */
    private static double qinv(double p, double nMeans, double df) {
        final double p0 = 0.322232421088;
        final double q0 = 0.993484626060e-01;
        final double p1 = -1.0;
        final double q1 = 0.588581570495;
        final double p2 = -0.342242088547;
        final double q2 = 0.531103462366;
        final double p3 = -0.204231210125;
        final double q3 = 0.103537752850;
        final double p4 = -0.453642210148e-04;
        final double q4 = 0.38560700634e-02;
        final double c1 = 0.8832;
        final double c2 = 0.2368;
        final double c3 = 1.214;
        final double c4 = 1.208;
        final double c5 = 1.4142;
        final double vmax = 120.0;

        double ps, q, t, yi;

        ps = 0.5 - 0.5 * p;
        yi = sqrt(log(1.0 / (ps * ps)));
        t = yi + ((((yi * p4 + p3) * yi + p2) * yi + p1) * yi + p0)
                / ((((yi * q4 + q3) * yi + q2) * yi + q1) * yi + q0);
        if (df < vmax) t += (t * t * t + t) / df / 4.0;
        q = c1 - c2 * t;
        if (df < vmax) q += -c3 / df + c4 * t / df;
        return t * (q * log(nMeans - 1.0) + c5);
    }

    /*
     *  Copenhaver, Margaret Diponzio & Holland, Burt S.
     *  Multiple comparisons of simple effects in
     *  the two-way analysis of variance with fixed effects.
     *  Journal of Statistical Computation and Simulation,
     *  Vol.30, pp.1-15, 1988.
     *
     *  Uses the secant method to find critical values.
     *
     *  p = confidence level (1 - alpha)
     *  rr = no. of rows or groups
     *  cc = no. of columns or treatments
     *  df = degrees of freedom of error term
     *
     *  ir(1) = error flag = 1 if wprob probability > 1
     *  ir(2) = error flag = 1 if ptukey probability > 1
     *  ir(3) = error flag = 1 if convergence not reached in 50 iterations
     *		       = 2 if df < 2
     *
     *  qtukey = returned critical value
     *
     *  If the difference between successive iterates is less than eps,
     *  the search is terminated
     */
    public static double qtukey(double p, double nMeans, double df, double nRanges, boolean lower_tail, boolean log_p) {
        final double eps = qTukeyEPS;
        final int maxiter = qTukeyMaxIterations;

        double ans = 0.0, valx0, valx1, x0, x1, xabs;
        int iter;

        if (Double.isNaN(p) || Double.isNaN(nRanges) || Double.isNaN(nMeans) || Double.isNaN(df)) {
            //ML_ERROR(ME_DOMAIN, "qtukey");
            return p + nRanges + nMeans + df;
        }

        /* df must be > 1 ; there must be at least two values */
        if (df < 2 || nRanges < 1 || nMeans < 2) return Double.NaN;

        //R_Q_P01_boundaries(p, 0, ML_POSINF);
        if (log_p) {
            if (p > 0)
                return Double.NaN;
            if (p == 0) /* upper bound*/
                return lower_tail ? Double.POSITIVE_INFINITY : 0;
            if (p == Double.NEGATIVE_INFINITY)
                return lower_tail ? 0 : Double.POSITIVE_INFINITY;
        } else { /* !log_p */
            if (p < 0 || p > 1)
                return Double.NaN;
            if (p == 0)
                return lower_tail ? 0 : Double.POSITIVE_INFINITY;
            if (p == 1)
                return lower_tail ? Double.POSITIVE_INFINITY : 0;
        }

        //p = R_DT_qIv(p); /* lower_tail,non-log "p" */
        p = (log_p ? (lower_tail ? exp(p) : -expm1(p)) : (lower_tail ? (p) : (0.5 - (p) + 0.5)));

        /* Initial value */

        x0 = qinv(p, nMeans, df);

        /* Find prob(value < x0) */

        valx0 = ptukey(x0, nMeans, df, nRanges, /*LOWER*/true, /*LOG_P*/false) - p;

        /* Find the second iterate and prob(value < x1). */
        /* If the first iterate has probability value */
        /* exceeding p then second iterate is 1 less than */
        /* first iterate; otherwise it is 1 greater. */

        if (valx0 > 0.0)
            x1 = max(0.0, x0 - 1.0);
        else
            x1 = x0 + 1.0;
        valx1 = ptukey(x1, nMeans, df, nRanges, /*LOWER*/true, /*LOG_P*/false) - p;

        /* Find new iterate */

        for (iter = 1; iter < maxiter; iter++) {
            ans = x1 - ((valx1 * (x1 - x0)) / (valx1 - valx0));
            valx0 = valx1;

            /* New iterate must be >= 0 */

            x0 = x1;
            if (ans < 0.0) {
                ans = 0.0;
                valx1 = -p;
            }
            /* Find prob(value < new iterate) */

            valx1 = ptukey(ans, nMeans, df, nRanges, /*LOWER*/true, /*LOG_P*/false) - p;
            x1 = ans;

            /* If the difference between two successive */
            /* iterates is less than eps, stop */

            xabs = abs(x1 - x0);
            if (xabs < eps)
                return ans;
        }

        /* The process did not converge in 'maxiter' iterations */
        //ML_ERROR(ME_NOCONV, "qtukey");
        JSL.getInstance().LOGGER.warn("The computation of invCDF did not converge after {} iterations", maxiter);
        return Double.NaN;
    }

}

