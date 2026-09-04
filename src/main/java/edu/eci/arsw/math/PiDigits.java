package edu.eci.arsw.math;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

///  <summary>
///  An implementation of the Bailey-Borwein-Plouffe formula for calculating hexadecimal
///  digits of pi.
///  https://en.wikipedia.org/wiki/Bailey%E2%80%93Borwein%E2%80%93Plouffe_formula
///  *** Translated from C# code: https://github.com/mmoroney/DigitsOfPi ***
///  </summary>
public class PiDigits {

    private static int DigitsPerSum = 8;
    private static double Epsilon = 1e-17;

    
    /**
     * Returns a range of hexadecimal digits of pi, calculating them en paralelo
     * mediante N hilos.
     * @param start The starting location of the range.
     * @param count The number of digits to return
     * @param numThreads Cantidad de hilos entre los que se reparte el cálculo.
     * @return An array containing the hexadecimal digits.
     */
    public static byte[] getDigits(int start, int count, int numThreads) {
        if (start < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (count < 0) {
            throw new RuntimeException("Invalid Interval");
        }

        if (numThreads <= 0) {
            throw new RuntimeException("Invalid number of threads");
        }

        byte[] digits = new byte[count];

        int baseChunkSize = count / numThreads;
        int remainder = count % numThreads;

        PauseControl pauseControl = new PauseControl();
        PiDigitsThread[] threads = new PiDigitsThread[numThreads];
        int offset = 0;

        for (int i = 0; i < numThreads; i++) {
            int chunkSize = baseChunkSize + (i < remainder ? 1 : 0);
            threads[i] = new PiDigitsThread(start + offset, chunkSize, digits, offset, pauseControl);
            threads[i].start();
            offset += chunkSize;
        }

        reportProgressPeriodically(threads, pauseControl);

        for (PiDigitsThread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Cálculo de dígitos de pi interrumpido", e);
            }
        }

        return digits;
    }

    /**
     * Cada 5 segundos detiene a los hilos, imprime cuántos dígitos ha
     * procesado cada uno, y espera a que el usuario presione ENTER para
     * reanudar el cálculo. Se repite hasta que todos los hilos terminen.
     */
    private static void reportProgressPeriodically(PiDigitsThread[] threads, PauseControl pauseControl) {
        BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in));

        while (anyAlive(threads)) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (!anyAlive(threads)) {
                break;
            }

            pauseControl.pause();

            System.out.println("--- Hilos pausados ---");
            for (int i = 0; i < threads.length; i++) {
                System.out.println("Hilo " + i + ": " + threads[i].getDigitsProcessed() + " dígitos procesados");
            }
            System.out.print("Presione ENTER para continuar...");
            System.out.flush();

            try {
                consoleReader.readLine();
            } catch (IOException e) {
                // Sin consola disponible: se continúa sin bloquear.
            }

            pauseControl.resume();
        }
    }

    private static boolean anyAlive(PiDigitsThread[] threads) {
        for (PiDigitsThread thread : threads) {
            if (thread.isAlive()) {
                return true;
            }
        }
        return false;
    }

    /// <summary>
    /// Returns the sum of 16^(n - k)/(8 * k + m) from 0 to k.
    /// </summary>
    /// <param name="m"></param>
    /// <param name="n"></param>
    /// <returns></returns>
    static double sum(int m, int n) {
        double sum = 0;
        int d = m;
        int power = n;

        while (true) {
            double term;

            if (power > 0) {
                term = (double) hexExponentModulo(power, d) / d;
            } else {
                term = Math.pow(16, power) / d;
                if (term < Epsilon) {
                    break;
                }
            }

            sum += term;
            power--;
            d += 8;
        }

        return sum;
    }

    /// <summary>
    /// Return 16^p mod m.
    /// </summary>
    /// <param name="p"></param>
    /// <param name="m"></param>
    /// <returns></returns>
    static int hexExponentModulo(int p, int m) {
        int power = 1;
        while (power * 2 <= p) {
            power *= 2;
        }

        int result = 1;

        while (power > 0) {
            if (p >= power) {
                result *= 16;
                result %= m;
                p -= power;
            }

            power /= 2;

            if (power > 0) {
                result *= result;
                result %= m;
            }
        }

        return result;
    }

}
