package dev.burikk.carrentz.engine.common;

import dev.burikk.carrentz.app.entity.OwnerEntity;
import org.apache.commons.lang3.StringUtils;

import javax.naming.NamingException;
import java.sql.SQLException;
import java.util.Locale;

/**
 * @author Muhammad Irfan
 * @since 9/21/2017 5:50 AM
 */
public class SessionManager {
    private static final transient ThreadLocal<SessionManager> INSTANCE;

    static {
        INSTANCE = ThreadLocal.withInitial(SessionManager::new);
    }

    private WynixSession wynixSession;
    private RequestMeta requestMeta;
    private Locale locale;

    {
        this.requestMeta = new RequestMeta();
    }

    public void createSystemSession() {
        this.wynixSession = () -> (WynixUser) () -> "System";
    }

    public WynixUser getWynixUser() throws SQLException, NamingException, InstantiationException, IllegalAccessException {
        return this.wynixSession.getWynixUser();
    }

    public WynixSession getWynixSession() {
        return this.wynixSession;
    }

    public void setWynixSession(WynixSession mWynixSession) {
        this.wynixSession = mWynixSession;
    }

    public Locale getLocale() {
        return this.locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public RequestMeta getRequestMeta() {
        return this.requestMeta;
    }

    public boolean isWeb() {
        return StringUtils.equalsIgnoreCase("Web", this.requestMeta.getPlatform());
    }

    public boolean isMobile() {
        return StringUtils.equalsIgnoreCase("Mobile", this.requestMeta.getPlatform());
    }

    public Long getMerchantId() throws Exception {
        return ((OwnerEntity) this.getWynixUser()).getMerchantId();
    }

    public static SessionManager getInstance() {
        return INSTANCE.get();
    }
}