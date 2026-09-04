package edu.eci.arsw.math;

/**
 * Mecanismo de pausa/reanudación compartido entre varios hilos.
 * Los hilos trabajadores consultan periódicamente {@link #awaitIfPaused()};
 * mientras el control esté en pausa quedan bloqueados hasta que se llame a
 * {@link #resume()}.
 */
public class PauseControl {

    private volatile boolean paused = false;

    public synchronized void pause() {
        paused = true;
    }

    public synchronized void resume() {
        paused = false;
        notifyAll();
    }

    public synchronized void awaitIfPaused() {
        while (paused) {
            try {
                wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }
}
