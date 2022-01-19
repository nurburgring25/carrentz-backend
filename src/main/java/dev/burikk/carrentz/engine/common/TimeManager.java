package dev.burikk.carrentz.engine.common;

import dev.burikk.carrentz.engine.datasource.DMLManager;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * @author Muhammad Irfan
 * @since 03/04/2019 18.39
 */
public class TimeManager {
    private static TimeManager INSTANCE;

    private TimeManager() {}

    public static TimeManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new TimeManager();
        }

        return INSTANCE;
    }

    public LocalDateTime now() throws SQLException, NamingException {
        if (Constant.Application.USING_SERVER_TIME) {
            return LocalDateTime.now();
        } else {
            return DMLManager.getObjectFromQuery("SELECT NOW();");
        }
    }
}