package dev.burikk.carrentz.engine.datasource;

import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.datasource.annotation.MarkColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkEncryptedColumn;
import dev.burikk.carrentz.engine.datasource.annotation.MarkTable;
import dev.burikk.carrentz.engine.datasource.enumeration.Platform;
import dev.burikk.carrentz.engine.datasource.meta.ColumnMeta;
import dev.burikk.carrentz.engine.datasource.meta.DatabaseMeta;
import dev.burikk.carrentz.engine.datasource.meta.TableMeta;
import dev.burikk.carrentz.engine.entity.Entity;
import dev.burikk.carrentz.engine.entity.EntityCache;
import dev.burikk.carrentz.engine.util.Models;
import dev.burikk.carrentz.engine.util.Parameters;
import com.sun.istack.internal.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 11:45
 */
@SuppressWarnings("WeakerAccess")
public class ConnectionManager {
    private static final transient Logger LOGGER = LogManager.getLogger(ConnectionManager.class);

    private static final ConnectionManager INSTANCE;

    static {
        INSTANCE = new ConnectionManager();
    }

    private String mJNDIPrefix = Constant.Application.EMBED ? "jdbc/" : "";
    private Context mContext;

    private final Map<String, DataSource> mDataSourceMap;

    {
        this.mDataSourceMap = new HashMap<>();
    }

    private ConnectionManager() {
        if (Constant.Application.EMBED) {
            try {
                Context mInitialContext = new InitialContext();

                this.mContext = (Context) mInitialContext.lookup("java:comp/env");
            } catch (NamingException e) {
                try {
                    this.mContext = new InitialContext();
                    this.mJNDIPrefix = "java:/";
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        } else {
            try {
                Class.forName("weblogic.jndi.WLInitialContextFactory");

                Properties mProperties = new Properties();

                mProperties.put(Context.INITIAL_CONTEXT_FACTORY, "weblogic.jndi.WLInitialContextFactory");

                this.mContext = new InitialContext(mProperties);
            } catch (ClassNotFoundException | NamingException e) {
                try {
                    this.mContext = new InitialContext();
                    this.mJNDIPrefix = "java:/";
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
    }

    public static ConnectionManager getInstance() {
        return INSTANCE;
    }

    public Connection get(@NotNull String mName) throws SQLException, NamingException {
        Parameters.requireNotNull(mName, "mName");

        DataSource mDataSource;
        if (!this.mDataSourceMap.containsKey(mName)) {
            mDataSource = (DataSource) this.mContext.lookup(this.mJNDIPrefix + mName);

            this.mDataSourceMap.put(mName, mDataSource);
        } else {
            mDataSource = this.mDataSourceMap.get(mName);
        }

        return mDataSource.getConnection();
    }

    public Connection get() throws SQLException, NamingException {
        return this.get(Constant.Database.PRIMARY_DS_NAME);
    }

    public Platform getPlatform(@NotNull Connection mConnection) {
        Parameters.requireNotNull(mConnection, "mConnection");

        try {
            String mDatabaseProductName = mConnection.getMetaData().getDatabaseProductName();

            switch (mDatabaseProductName) {
                case "PostgreSQL":
                    return Platform.POSTGRESQL;
                case "MySQL":
                    return Platform.MYSQL;
                case "Microsoft SQL Server":
                    return Platform.SQLSERVER;
                default:
                    throw new RuntimeException("Unsupported database platform : " + mDatabaseProductName + ".");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void init() {
        try (DDLManager mDDLManager = new DDLManager()) {
            StringBuilder mStringBuilder = new StringBuilder();

            DatabaseMeta.scan(mDDLManager.mConnection);

            List<Class<? extends Entity>> mClassEntities = Constant.ENTITY_CLASSES;

            mClassEntities.sort(Comparator.<Class <? extends Entity>>comparingInt(o -> EntityCache.getInstance().getEntityDesign(o).getDependOnTables().size()).thenComparing(o -> -(EntityCache.getInstance().getEntityDesign(o).getDependentTables().size())));

            Models.scan(mDDLManager, mClassEntities, mStringBuilder);

            for (TableMeta mTableMeta : DatabaseMeta.TABLE_METAS) {
                boolean mTableFound = false;

                for (Class<? extends Entity> mClassEntity : mClassEntities) {
                    List<Field> mAnnotatedFields = Models.getAnnotatedFields(mClassEntity, MarkColumn.class, MarkEncryptedColumn.class);

                    if (mClassEntity.isAnnotationPresent(MarkTable.class)) {
                        MarkTable mMarkTable = mClassEntity.getAnnotation(MarkTable.class);

                        String mTableName = mMarkTable.value();

                        if (StringUtils.isNotBlank(mTableName)) {
                            if (mTableName.equals(mTableMeta.getTableName())) {
                                for (ColumnMeta mColumnMeta : mTableMeta.getColumnMetas()) {
                                    boolean mColumnFound = false;

                                    for (Field mField : mAnnotatedFields) {
                                        if (mField.isAnnotationPresent(MarkColumn.class)) {
                                            MarkColumn mMarkColumn = mField.getAnnotation(MarkColumn.class);

                                            String mColumnName = mMarkColumn.value();

                                            if (StringUtils.isNotBlank(mColumnName)) {
                                                if (mColumnName.equals(mColumnMeta.getName())) {
                                                    mColumnFound = true;
                                                }
                                            }
                                        } else {
                                            MarkEncryptedColumn mMarkEncryptedColumn = mField.getAnnotation(MarkEncryptedColumn.class);

                                            String mColumnName = mMarkEncryptedColumn.value();

                                            if (StringUtils.isNotBlank(mColumnName)) {
                                                if (mColumnName.equals(mColumnMeta.getName())) {
                                                    mColumnFound = true;
                                                }
                                            }
                                        }
                                    }

                                    if (!mColumnFound) {
                                        LOGGER.warn("Column " + mColumnMeta.getName() + " on table " + mTableMeta.getTableName() + " isn't available on " + mClassEntity.getSimpleName() + " entity class. If it not used anymore, please drop it.");
                                        mStringBuilder
                                                .append(mDDLManager.dropColumnScript(mTableName, mColumnMeta.getName()))
                                                .append("\n");
                                    }
                                }

                                mTableFound = true;
                            }
                        }
                    }
                }

                if (!mTableFound) {
                    LOGGER.warn("Table " + mTableMeta.getTableName() + " isn't available on any entity class. If it not used anymore, please drop it.");
                    mStringBuilder
                            .append(mDDLManager.dropTableScript(mTableMeta.getTableName()))
                            .append("\n");
                }
            }


            if (mStringBuilder.length() > 0) {
                File mScriptFile = new File(new File(Constant.Path.Folder.DATABASE_READJUST), LocalDateTime.now().format(DateTimeFormatter.ofPattern("ddMMyyyy_HHmmss")) + ".sql");

                if (!mScriptFile.exists()) {
                    try {
                        if (!mScriptFile.createNewFile()) {
                            throw new RuntimeException("Cannot create database readjust file.");
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException("Cannot create database readjust file.");
                    }
                }

                try (
                        FileWriter mFileWriter = new FileWriter(mScriptFile, !mScriptFile.exists());
                        PrintWriter mPrintWriter = new PrintWriter(mFileWriter)
                ) {
                    mPrintWriter.print(mStringBuilder);
                }
            }
        } catch (Exception ex) {
            LOGGER.catching(ex);
        }
    }
}