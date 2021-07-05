/*
 * Class:        ArithmeticMod
 * Description:  multiplications of scalars, vectors and matrices modulo m
 * Environment:  Java
 * Software:     SSJ
 * Copyright (C) 2001  Pierre L'Ecuyer and Universite de Montreal
 * Organization: DIRO, Universite de Montreal
 * @author
 * @since
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package ksl.utilities.random.rng

/**
 * This class provides facilities to compute multiplications of scalars, of
 * vectors and of matrices modulo m. All algorithms are present in three
 * different versions. These allow operations on `double`, `int` and `long`.
 * The `int` and `long` versions work exactly like the `double` ones.
 *
 * This class comes from:  https://github.com/umontreal-simul/ssj
 * package umontreal.ssj.util.ArithmeticMod with some updates.
 *
 * It is used to facilitate creation of random number streams
 */
object ArithmeticMod {
    //private constants
    private const val two17 = 131072.0
    private const val two53 = 9007199254740992.0

    /**
     * Computes (a x s + c) mod m. Where `m` must be smaller
     * than 2 to the 35. Works also if `s` or `c` are negative. The result
     * is always positive (and thus always between 0 and `m` - 1).
     * @param a            the first factor of the multiplication
     * @param s            the second factor of the multiplication
     * @param c            the second term of the addition
     * @param m            the modulus
     * @return the result of the multiplication and the addition modulo
     * `m`
     */
    fun multModM(a: Double, s: Double, c: Double, m: Double): Double {
        var aa = a
        var a1: Int
        var v: Double = aa * s + c
        if (v >= two53 || v <= -two53) {
            a1 = (aa / two17).toInt()
            aa -= a1 * two17
            v = a1 * s
            a1 = (v / m).toInt()
            v -= a1 * m
            v = v * two17 + aa * s + c
        }
        a1 = (v / m).toInt()
        v = v - a1 * m
        if (v < 0.0) {
            v = v + m
            return v
        } else {
            return v
        }
    }

    /**
     * Computes the result of (A times s) mod m and puts the result in `v`. Where `s` and `v` are both
     * column vectors. This method works even if `s` = `v`.
     * @param A            the multiplication matrix
     * @param s            the multiplied vector
     * @param v            the result of the multiplication
     * @param m            the modulus
     */
    fun matVecModM(A: Array<DoubleArray>, s: DoubleArray, v: DoubleArray, m: Double) {
        var i: Int
        val x = DoubleArray(v.size)
        i = 0
        while (i < v.size) {
            x[i] = 0.0
            for (j in s.indices) x[i] = multModM(A[i][j], s[j], x[i], m)
            ++i
        }
        i = 0
        while (i < v.size) {
            v[i] = x[i]
            ++i
        }
    }

    /**
     * Computes (A times B) mod m  and puts the
     * result in `C`. Works even if `A` = `C`, `B` = `C` or `A` = `B` =
     * `C`.
     * @param A            the first factor of the multiplication
     * @param B            the second factor of the multiplication
     * @param C            the result of the multiplication
     * @param m            the modulus
     */
    fun matMatModM(A: Array<DoubleArray>, B: Array<DoubleArray>, C: Array<DoubleArray>, m: Double) {
        var i: Int
        var j: Int
        val r = C.size //# of rows of C
        val c: Int = C[0].size //# of columns of C
        val V = DoubleArray(r)
        val W = Array(r) { DoubleArray(c) }
        i = 0
        while (i < c) {
            j = 0
            while (j < r) {
                V[j] = B[j][i]
                ++j
            }
            matVecModM(A, V, V, m)
            j = 0
            while (j < r) {
                W[j][i] = V[j]
                ++j
            }
            ++i
        }
        i = 0
        while (i < r) {
            j = 0
            while (j < c) {
                C[i][j] = W[i][j]
                ++j
            }
            ++i
        }
    }

    /**
     * Computes (A raised to (2 to e)) mod m and puts the
     * result in `B`. B = A^{2^e} Works even if `A` = `B`.
     * @param A            the matrix to raise to a power
     * @param B            the result of exponentiation
     * @param m            the modulus
     * @param e            the log_2 of the exponent
     */
    fun matTwoPowModM(A: Array<DoubleArray>, B: Array<DoubleArray>, m: Double, e: Int) {
        var i: Int
        var j: Int
        /* initialize: B = A */
        //       if (A != B) { //TODO testing the references?, if not equal then make them equal, just make them equal
        i = 0
        while (i < A.size) {
            j = 0
            while (j < A.size) {
                //A is square
                B[i][j] = A[i][j]
                ++j
            }
            i++
        }
        //       }
        /* Compute B = A^{2^e} */i = 0
        while (i < e) {
            matMatModM(B, B, B, m)
            i++
        }
    }

    /**
     * Computes (A raised to c) mod m and puts the result in `B`.
     * Works even if `A` = `B`.
     * @param A            the matrix to raise to a power
     * @param B            the result of the exponentiation
     * @param m            the modulus
     * @param c            the exponent
     */
    fun matPowModM(A: Array<DoubleArray>, B: Array<DoubleArray>, m: Double, c: Int) {
        var i: Int
        var j: Int
        var n = c
        val s = A.size //we suppose that A is square
        val W = Array(s) { DoubleArray(s) }

        /* initialize: W = A; B = I */i = 0
        while (i < s) {
            j = 0
            while (j < s) {
                W[i][j] = A[i][j]
                B[i][j] = 0.0
                ++j
            }
            i++
        }
        j = 0
        while (j < s) {
            B[j][j] = 1.0
            ++j
        }

        /* Compute B = A^c mod m using the binary decomp. of c */while (n > 0) {
            if (n % 2 == 1) matMatModM(W, B, B, m)
            matMatModM(W, W, W, m)
            n /= 2
        }
    }

    /**
     * Computes (a times s + c) mod m. Works also if `s` or `c`
     * are negative. The result is always positive (and thus always between
     * 0 and `m` - 1).
     * @param a            the first factor of the multiplication
     * @param s            the second factor of the multiplication
     * @param c            the second term of the addition
     * @param m            the modulus
     * @return the result of the multiplication and the addition modulo
     * `m`
     */
    fun multModM(a: Int, s: Int, c: Int, m: Int): Int {
        val r = ((a.toLong() * s + c) % m).toInt()
        return if (r < 0) r + m else r
    }

    /**
     * Exactly like matVecModM(double[][],double[],double[],double) using
     * `double`, but with `int` instead of `double`.
     * @param A            the multiplication matrix
     * @param s            the multiplied vector
     * @param v            the result of the multiplication
     * @param m            the modulus
     */
    fun matVecModM(A: Array<IntArray>, s: IntArray, v: IntArray, m: Int) {
        var i: Int
        val x = IntArray(v.size)
        i = 0
        while (i < v.size) {
            x[i] = 0
            for (j in s.indices) x[i] = multModM(A[i][j], s[j], x[i], m)
            ++i
        }
        i = 0
        while (i < v.size) {
            v[i] = x[i]
            ++i
        }
    }

    /**
     * Exactly like matMatModM(double[][],double[][],double[][],double)
     * using `double`, but with `int` instead of `double`.
     * @param A            the first factor of the multiplication
     * @param B            the second factor of the multiplication
     * @param C            the result of the multiplication
     * @param m            the modulus
     */
    fun matMatModM(A: Array<IntArray>, B: Array<IntArray>, C: Array<IntArray>, m: Int) {
        var i: Int
        var j: Int
        val r = C.size //# of rows of C
        val c: Int = C[0].size //# of columns of C
        val V = IntArray(r)
        val W = Array(r) { IntArray(c) }
        i = 0
        while (i < c) {
            j = 0
            while (j < r) {
                V[j] = B[j][i]
                ++j
            }
            matVecModM(A, V, V, m)
            j = 0
            while (j < r) {
                W[j][i] = V[j]
                ++j
            }
            ++i
        }
        i = 0
        while (i < r) {
            j = 0
            while (j < c) {
                C[i][j] = W[i][j]
                ++j
            }
            ++i
        }
    }

    /**
     * Exactly like matTwoPowModM(double[][],double[][],double,int) using
     * `double`, but with `int` instead of `double`.
     * @param A            the matrix to raise to a power
     * @param B            the result of exponentiation
     * @param m            the modulus
     * @param e            the log_2 of the exponent
     */
    fun matTwoPowModM(A: Array<IntArray>, B: Array<IntArray>, m: Int, e: Int) {
        var i: Int
        var j: Int
        /* initialize: B = A */
        //       if (A != B) { //TODO reference testing
        i = 0
        while (i < A.size) {
            j = 0
            while (j < A.size) {
                //A is square
                B[i][j] = A[i][j]
                ++j
            }
            i++
        }
        //       }
        /* Compute B = A^{2^e} */i = 0
        while (i < e) {
            matMatModM(B, B, B, m)
            i++
        }
    }

    /**
     * Exactly like matPowModM(double[][],double[][],double,int) using
     * `double`, but with `int` instead of `double`.
     * @param A            the matrix to raise to a power
     * @param B            the result of the exponentiation
     * @param m            the modulus
     * @param c            the exponent
     */
    fun matPowModM(A: Array<IntArray>, B: Array<IntArray>, m: Int, c: Int) {
        var i: Int
        var j: Int
        var n = c
        val s = A.size //we suppose that A is square (it must be)
        val W = Array(s) { IntArray(s) }

        /* initialize: W = A; B = I */i = 0
        while (i < s) {
            j = 0
            while (j < s) {
                W[i][j] = A[i][j]
                B[i][j] = 0
                ++j
            }
            i++
        }
        j = 0
        while (j < s) {
            B[j][j] = 1
            ++j
        }

        /* Compute B = A^c mod m using the binary decomp. of c */while (n > 0) {
            if (n % 2 == 1) matMatModM(W, B, B, m)
            matMatModM(W, W, W, m)
            n /= 2
        }
    }

    /**
     * Computes (a times s + c) mod m. Works also if `s` or `c`
     * are negative. The result is always positive (and thus always between
     * 0 and `m` - 1).
     * @param a            the first factor of the multiplication
     * @param s            the second factor of the multiplication
     * @param c            the second term of the addition
     * @param m            the modulus
     * @return the result of the multiplication and the addition modulo
     * `m`
     */
    fun multModM(a: Long, s: Long, c: Long, m: Long): Long {

        /* Suppose que 0 < a < m  et  0 < s < m.   Retourne (a*s + c) % m.
       * Cette procedure est tiree de :
       * L'Ecuyer, P. et Cote, S., A Random Number Package with
       * Splitting Facilities, ACM TOMS, 1991.
       * On coupe les entiers en blocs de d bits. H doit etre egal a 2^d.  */
        val H = 2147483648L // = 2^d  used in MultMod
        val a0: Long
        var a1: Long
        var q: Long
        val qh: Long
        val rh: Long
        var k: Long
        var p: Long
        if (a < H) {
            a0 = a
            p = 0
        } else {
            a1 = a / H
            a0 = a - H * a1
            qh = m / H
            rh = m - H * qh
            if (a1 >= H) {
                a1 = a1 - H
                k = s / qh
                p = H * (s - k * qh) - k * rh
                if (p < 0) p = (p + 1) % m + m - 1
            } else  /* p = (A2 * s * h) % m.      */ p = 0
            if (a1 != 0L) {
                q = m / a1
                k = s / q
                p -= k * (m - a1 * q)
                if (p > 0) p -= m
                p += a1 * (s - k * q)
                if (p < 0) p = (p + 1) % m + m - 1
            } /* p = ((A2 * h + a1) * s) % m. */
            k = p / qh
            p = H * (p - k * qh) - k * rh
            if (p < 0) p = (p + 1) % m + m - 1
        } /* p = ((A2 * h + a1) * h * s) % m  */
        if (a0 != 0L) {
            q = m / a0
            k = s / q
            p -= k * (m - a0 * q)
            if (p > 0) p -= m
            p += a0 * (s - k * q)
            if (p < 0) p = (p + 1) % m + m - 1
        }
        p = p - m + c
        if (p < 0) p += m
        return p
    }

    /**
     * Exactly like  matVecModM(double[][],double[],double[],double) using
     * `double`, but with `long` instead of `double`.
     * @param A            the multiplication matrix
     * @param s            the multiplied vector
     * @param v            the result of the multiplication
     * @param m            the modulus
     */
    fun matVecModM(A: Array<LongArray>, s: LongArray, v: LongArray, m: Long) {
        var i: Int
        val x = LongArray(v.size)
        i = 0
        while (i < v.size) {
            x[i] = 0
            for (j in s.indices) x[i] = multModM(A[i][j], s[j], x[i], m)
            ++i
        }
        i = 0
        while (i < v.size) {
            v[i] = x[i]
            ++i
        }
    }

    /**
     * Exactly like  matMatModM(double[][],double[][],double[][],double)
     * using `double`, but with `long` instead of `double`.
     * @param A            the first factor of the multiplication
     * @param B            the second factor of the multiplication
     * @param C            the result of the multiplication
     * @param m            the modulus
     */
    fun matMatModM(A: Array<LongArray>, B: Array<LongArray>, C: Array<LongArray>, m: Long) {
        var i: Int
        var j: Int
        val r = C.size //# of rows of C
        val c: Int = C[0].size //# of columns of C
        val V = LongArray(r)
        val W = Array(r) { LongArray(c) }
        i = 0
        while (i < c) {
            j = 0
            while (j < r) {
                V[j] = B[j][i]
                ++j
            }
            matVecModM(A, V, V, m)
            j = 0
            while (j < r) {
                W[j][i] = V[j]
                ++j
            }
            ++i
        }
        i = 0
        while (i < r) {
            j = 0
            while (j < c) {
                C[i][j] = W[i][j]
                ++j
            }
            ++i
        }
    }

    /**
     * Exactly like  matTwoPowModM(double[][],double[][],double,int) using
     * `double`, but with `long` instead of `double`.
     * @param A            the matrix to raise to a power
     * @param B            the result of exponentiation
     * @param m            the modulus
     * @param e            the log_2 of the exponent
     */
    fun matTwoPowModM(A: Array<LongArray>, B: Array<LongArray>, m: Long, e: Int) {
        var i: Int
        var j: Int
        /* initialize: B = A */
//        if (A != B) { //TODO reference testing
        i = 0
        while (i < A.size) {
            j = 0
            while (j < A.size) {
                //A is square
                B[i][j] = A[i][j]
                ++j
            }
            i++
            //           }
        }
        /* Compute B = A^{2^e} */i = 0
        while (i < e) {
            matMatModM(B, B, B, m)
            i++
        }
    }

    /**
     * Exactly like matPowModM(double[][],double[][],double,int) using
     * `double`, but with `long` instead of `double`.
     * @param A            the matrix to raise to a power
     * @param B            the result of the exponentiation
     * @param m            the modulus
     * @param c            the exponent
     */
    fun matPowModM(A: Array<LongArray>, B: Array<LongArray>, m: Long, c: Int) {
        var i: Int
        var j: Int
        var n = c
        val s = A.size //we suppose that A is square (it must be)
        val W = Array(s) { LongArray(s) }

        /* initialize: W = A; B = I */i = 0
        while (i < s) {
            j = 0
            while (j < s) {
                W[i][j] = A[i][j]
                B[i][j] = 0
                ++j
            }
            i++
        }
        j = 0
        while (j < s) {
            B[j][j] = 1
            ++j
        }

        /* Compute B = A^c mod m using the binary decomp. of c */
        while (n > 0) {
            if (n % 2 == 1) matMatModM(W, B, B, m)
            matMatModM(W, W, W, m)
            n /= 2
        }
    }
}