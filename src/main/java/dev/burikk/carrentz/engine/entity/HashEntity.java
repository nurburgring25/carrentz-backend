package dev.burikk.carrentz.engine.entity;

import dev.burikk.carrentz.engine.common.WynixResult;
import dev.burikk.carrentz.engine.security.Crypt;
import dev.burikk.carrentz.engine.util.Parameters;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Muhammad Irfan
 * @since 23/08/2017 13:54
 */
public class HashEntity extends LinkedHashMap<String, Object> implements WynixResult {
    public void add(
            @NotNull String mKey,
            @Null Object mValue
    ) {
        Parameters.requireNotNull(mKey, "mKey");

        this.put(mKey, mValue);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(@NotNull String mKey) {
        Parameters.requireNotNull(mKey, "mKey");

        return (T) this.get((Object) mKey);
    }

    public <T> T getDecrypted(@NotNull String mKey, @NotNull Class<T> mClass) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        Parameters.requireNotNull(mKey, "mKey");
        Parameters.requireNotNull(mClass, "mClass");

        return Crypt.decrypt(this.get(mKey), mClass);
    }

    public void uppercasedKeys() {
        Map<String, Object> mMap = new HashMap<>();

        this.forEach((s, o) -> mMap.put(s.toUpperCase(), o));
        this.clear();
        this.putAll(mMap);
    }
}