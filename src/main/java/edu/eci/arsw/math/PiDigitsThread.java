package edu.eci.arsw.math;

/**
 * Hilo encargado de calcular un tramo de dígitos hexadecimales de pi,
 * comenzando en la posición start, con una longitud de count dígitos.
 * El resultado se escribe directamente sobre digits, a partir de offset,
 * para que el arreglo pueda ser compartido entre varios hilos sin
 * necesidad de combinarlo al final.
 */
public class PiDigitsThread extends Thread {

    private static final int DIGITS_PER_SUM = 8;

    private final int start;
    private final int count;
    private final byte[] digits;
    private final int offset;
    private final PauseControl pauseControl;

    private volatile int digitsProcessed = 0;

    public PiDigitsThread(int start, int count, byte[] digits, int offset, PauseControl pauseControl) {
        this.start = start;
        this.count = count;
        this.digits = digits;
        this.offset = offset;
        this.pauseControl = pauseControl;
    }

    public int getDigitsProcessed() {
        return digitsProcessed;
    }

    @Override
    public void run() {
        double sum = 0;
        int position = start;

        for (int i = 0; i < count; i++) {
            pauseControl.awaitIfPaused();

            if (i % DIGITS_PER_SUM == 0) {
                sum = 4 * PiDigits.sum(1, position)
                        - 2 * PiDigits.sum(4, position)
                        - PiDigits.sum(5, position)
                        - PiDigits.sum(6, position);
                position += DIGITS_PER_SUM;
            }

            sum = 16 * (sum - Math.floor(sum));
            digits[offset + i] = (byte) sum;
            digitsProcessed++;
        }
    }
}
