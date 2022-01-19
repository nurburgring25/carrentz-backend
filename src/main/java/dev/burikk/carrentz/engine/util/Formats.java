package dev.burikk.carrentz.engine.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * @author Muhammad Irfan
 * @since 08/12/2019 15.38
 */
public class Formats {
    private static DecimalFormat DECIMAL_FORMAT;

    static {
        DECIMAL_FORMAT = new DecimalFormat("###,##0");
        DECIMAL_FORMAT.setParseBigDecimal(true);
        DECIMAL_FORMAT.setMinimumFractionDigits(0);
        DECIMAL_FORMAT.setMaximumFractionDigits(0);
    }

    public static String decimal(BigDecimal bigDecimal) {
        return DECIMAL_FORMAT.format(bigDecimal);
    }
}