package dev.burikk.carrentz.engine.common;

import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.LocalizedMessage;

import javax.servlet.ServletContext;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static dev.burikk.carrentz.engine.common.Constant.Application.NAME;

/**
 * @author Muhammad Irfan
 * @since 8/22/2017 12:16 PM
 */
public class LanguageManager {
    private static final transient Logger LOGGER = LogManager.getLogger(LanguageManager.class);

    private static final transient Map<Locale, ResourceBundle> RESOURCE_BUNDLE_MAP;

    static {
        RESOURCE_BUNDLE_MAP = new HashMap<>();
    }

    private static void init(@NotNull URL mURL) {
        if (mURL == null) {
            throw new NullPointerException("Folder lang cannot be located.");
        }

        try {
            Path mLanguagePath = Paths.get(mURL.toURI());

            try (DirectoryStream<Path> mDirectoryStream = Files.newDirectoryStream(mLanguagePath)) {
                mDirectoryStream.forEach(mPath -> {
                    String mFileName = mPath.toFile().getName();

                    try {
                        if ((NAME + "_").equals(mFileName.substring(0, (NAME.length() + 1))) && "properties".equals(mFileName.substring(mFileName.lastIndexOf(".") + 1, mFileName.length()))) {
                            String mLanguageTag = mFileName.substring(mFileName.indexOf((NAME + "_")) + (NAME.length() + 1), mFileName.lastIndexOf("."));

                            Locale mLocale = Locale.forLanguageTag(mLanguageTag);

                            ResourceBundle mResourceBundle = ResourceBundle.getBundle(NAME, mLocale, new URLClassLoader(new URL[]{mURL}));

                            RESOURCE_BUNDLE_MAP.put(mLocale, mResourceBundle);
                        }
                    } catch (Exception ex) {
                        LOGGER.error("Unexpected error when processing file : " + mFileName + ".");
                    }
                });

                if (RESOURCE_BUNDLE_MAP.isEmpty()) {
                    throw new RuntimeException("There is no resource bundle detected.");
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void init(@NotNull ServletContext mServletContext) {
        if (mServletContext == null) {
            throw new NullPointerException("mServletContext cannot be null.");
        }

        try {
            init(mServletContext.getResource(Constant.Path.Folder.LANGUAGE));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void init() {
        try {
            init(new File(Constant.Path.Folder.LANGUAGE).toURI().toURL());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String retrieve(
            @NotNull Locale mLocale,
            @NotNull String mKey,
            @NotNull Object... mParameters
    ) {
        Parameters.requireNotNull(mKey, "mKey");
        Parameters.requireNotNull(mParameters, "mParameters");

        if (mLocale == null) {
            mLocale = Locale.ENGLISH;
        }

        ResourceBundle mResourceBundle = RESOURCE_BUNDLE_MAP.get(mLocale);

        if (mResourceBundle == null) {
            throw new NoSuchBundleException(mLocale);
        }

        try {
            LocalizedMessage mLocalizedMessage = new LocalizedMessage(mResourceBundle, mKey, mParameters);

            return mLocalizedMessage.getFormattedMessage();
        } catch (MissingResourceException ex) {
            return mKey;
        }
    }

    public static String retrieve(
            @NotNull String mKey,
            @NotNull Object... mParameters
    ) {
        return retrieve(Locale.ENGLISH, mKey, mParameters);
    }

    private static class NoSuchBundleException extends RuntimeException {
        private final Locale mLocale;

        private NoSuchBundleException(@NotNull Locale mLocale) {
            this.mLocale = mLocale;
        }

        @Override
        public String getMessage() {
            return "Resource language for " + this.mLocale.getDisplayLanguage() + " can't be located.";
        }
    }

    private static class NoSuchStringException extends RuntimeException {
        private final String mKey;

        private NoSuchStringException(@NotNull String mKey) {
            this.mKey = mKey;
        }

        @Override
        public String getMessage() {
            return "Cannot retrieve string with key : " + this.mKey + ".";
        }
    }
}