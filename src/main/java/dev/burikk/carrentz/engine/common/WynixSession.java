package dev.burikk.carrentz.engine.common;

import javax.naming.NamingException;
import java.sql.SQLException;

/**
 * @author Muhammad Irfan
 * @since 09/01/2019 17.57
 */
public interface WynixSession {
    WynixUser getWynixUser() throws SQLException, InstantiationException, IllegalAccessException, NamingException;
}