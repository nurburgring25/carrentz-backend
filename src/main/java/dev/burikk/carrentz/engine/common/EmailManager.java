package dev.burikk.carrentz.engine.common;

/**
 * @author Muhammad Irfan
 * @since 03/04/2019 19.21
 */
public class EmailManager {
    private static EmailManager INSTANCE;

    private EmailManager() {}

    public static EmailManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new EmailManager();
        }

        return INSTANCE;
    }

    public void ownerRegistration() {}
}
