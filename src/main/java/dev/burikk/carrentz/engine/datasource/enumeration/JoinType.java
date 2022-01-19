package dev.burikk.carrentz.engine.datasource.enumeration;

import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

/**
 * @author Muhammad Irfan
 * @since 19/06/2017 16:08
 */
public enum JoinType {
    INNER_JOIN("INNER JOIN"),
    LEFT_JOIN("LEFT JOIN"),
    RIGHT_JOIN("RIGHT JOIN"),
    FULL_JOIN("FULL JOIN"),
    CROSS_JOIN("CROSS JOIN");

    private final String mText;

    JoinType(@NotNull String mText) {
        Parameters.requireNotNull(mText, "mText");

        this.mText = mText;
    }

    public String getText() {
        return mText;
    }
}