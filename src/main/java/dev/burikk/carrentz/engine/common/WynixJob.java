package dev.burikk.carrentz.engine.common;

/**
 * @author Muhammad Irfan
 * @since 07/12/2019 08.35
 */
public abstract class WynixJob implements Runnable {
    protected long id;

    protected WynixJob(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }
}