package dev.burikk.carrentz.app.common;

import dev.burikk.carrentz.engine.datasource.DMLManager;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;

/**
 * @author Muhammad Irfan
 * @since 08/02/2019 09.08
 */
public class TransactionNumbers {
    public static String rent(LocalDate localDate) throws Exception {
        String runningNumber = DMLManager.getObjectFromQuery("SELECT fn_get_number('rent', ?);", localDate);

        String fixed = "RNT";

        int year = localDate.getYear();

        return fixed
                + "/"
                + StringUtils.leftPad(String.valueOf(localDate.getDayOfMonth()), 2, '0')
                + "/"
                + StringUtils.leftPad(String.valueOf(localDate.getMonthValue()), 2, '0')
                + "/"
                + String.valueOf(year).substring(2, 4)
                + "/"
                + runningNumber;
    }
}