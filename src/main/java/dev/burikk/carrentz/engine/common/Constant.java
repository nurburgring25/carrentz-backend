package dev.burikk.carrentz.engine.common;

import dev.burikk.carrentz.app.entity.*;
import dev.burikk.carrentz.engine.entity.Entity;

import java.util.Arrays;
import java.util.List;

/**
 * @author Muhammad Irfan
 * @since 8/22/2017 12:22 PM
 */
public class Constant {
    public static class Application {
        public static final String NAME = "Carrentz";
        public static final String INIT_VECTOR = "RandomInitVector";
        public static final String KEY = "Burikk_Carrentz;";
        public static final Boolean USING_SERVER_TIME = true;
        public static final Boolean EMBED = false;
    }

    public static class Crypto {
        public static final String PASSWORD = "carrentz";
        public static final String ALIAS = "carrentz";
    }

    public static class Database {
        public static final String SCHEMA_NAME = "public";
        public static final String PRIMARY_DS_NAME = Application.EMBED ? "carrentz" : "CARRENTZ";
    }

    public static class Path {
        public static class Folder {
            public static final String DATABASE_READJUST = Application.EMBED ? System.getProperty("wynix.database.readjust.folder") : System.getenv("wynix.database.readjust.folder");
            public static final String LANGUAGE = Application.EMBED ? "lang" : "/lang";
        }
    }

    public static class Reflection {
        public static final String ENTITY_PACKAGE = "dev.burikk.carrentz.app.entity";
        public static final String FIELD_CREATED = "created";
        public static final String FIELD_CREATOR = "creator";
        public static final String FIELD_MODIFIED = "modified";
        public static final String FIELD_MODIFICATOR = "modificator";
        public static final String FIELD_DELETED = "deleted";
    }

    public static class DocumentStatus {
        public static final String OPENED = "OPENED";
        public static final String ONGOING = "ONGOING";
        public static final String CLOSED = "CLOSED";
        public static final String CANCELLED = "CANCELLED";
    }

    public static List<Class<? extends Entity>> ENTITY_CLASSES = Arrays.asList(
            ConfigurationEntity.class,
            LogEntity.class,
            MerchantEntity.class,
            OwnerEntity.class,
            OwnerSessionEntity.class,
            RentEntity.class,
            StoreEntity.class,
            UserEntity.class,
            UserSessionEntity.class,
            VehicleEntity.class,
            VehicleImageEntity.class,
            VehicleTypeEntity.class
    );
}