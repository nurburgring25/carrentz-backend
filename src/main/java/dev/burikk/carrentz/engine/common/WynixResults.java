package dev.burikk.carrentz.engine.common;

import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.HashEntity;

import java.util.ArrayList;

/**
 * @author Muhammad Irfan
 * @since 06/11/2017 18:00
 */
public class WynixResults<E extends WynixResult> extends ArrayList<E> {
    public void markAllDelete() {
        this.forEach(mWynixResult -> {
            if (mWynixResult instanceof Entity) {
                ((Entity) mWynixResult).markDelete();
            }
        });
    }

    public void uppercasedKeys() {
        this.forEach(mWynixResult -> {
            if (mWynixResult instanceof HashEntity) {
                ((HashEntity) mWynixResult).uppercasedKeys();
            }
        });
    }
}