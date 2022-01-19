package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.datasource.enumeration.Platform;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;

import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 11:59
 */
class SQLManager implements AutoCloseable {
    Platform mPlatform;
    Connection mConnection;

    SQLManager() throws SQLException, NamingException {
        this.mConnection = ConnectionManager.getInstance().get();
        this.mPlatform = ConnectionManager.getInstance().getPlatform(this.mConnection);
    }

    SQLManager(@NotNull String mDataSourceName) throws SQLException, NamingException {
        Parameters.requireNotNull(mDataSourceName, "mDataSourceName");

        this.mConnection = ConnectionManager.getInstance().get(mDataSourceName);
    }

    public void begin() throws SQLException {
        if (this.mConnection.getAutoCommit()) {
            this.mConnection.setAutoCommit(false);
        }
    }

    public void commit() throws SQLException {
        if (!this.mConnection.getAutoCommit()) {
            this.mConnection.commit();
            this.mConnection.setAutoCommit(true);
        }
    }

    public void rollback() throws SQLException {
        if (!this.mConnection.getAutoCommit()) {
            if (!this.mConnection.isClosed()) {
                this.mConnection.rollback();
            }
        }
    }

    @Override
    public void close() throws SQLException {
        if (this.mConnection != null) {
            if (!this.mConnection.getAutoCommit()) {
                if (!this.mConnection.isClosed()) {
                    this.mConnection.rollback();
                }

                this.mConnection.setAutoCommit(true);
            }

            this.mConnection.close();
        }
    }
}