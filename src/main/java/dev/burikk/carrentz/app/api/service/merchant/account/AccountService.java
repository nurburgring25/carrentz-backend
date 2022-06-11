package dev.burikk.carrentz.app.api.service.merchant.account;

import dev.burikk.carrentz.app.api.service.merchant.account.request.RegisterRequest;
import dev.burikk.carrentz.app.api.service.merchant.account.request.SignInRequest;
import dev.burikk.carrentz.app.api.service.merchant.account.request.VerificationRequest;
import dev.burikk.carrentz.app.api.service.merchant.account.response.SignInResponse;
import dev.burikk.carrentz.app.entity.MerchantEntity;
import dev.burikk.carrentz.app.entity.OwnerEntity;
import dev.burikk.carrentz.app.entity.OwnerSessionEntity;
import dev.burikk.carrentz.app.entity.StoreEntity;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.TimeManager;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.entity.HashEntity;
import dev.burikk.carrentz.engine.security.Crypt;
import dev.burikk.carrentz.engine.security.Digest;
import dev.burikk.carrentz.engine.util.Validators;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 12.27
 */
@Path("/merchants/")
public class AccountService {
    @POST
    @Path("/register")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(RegisterRequest registerRequest) throws Exception {
        SessionManager.getInstance().createSystemSession();

        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();

            // Masukkan data merchant
            MerchantEntity merchantEntity = new MerchantEntity();

            merchantEntity.markNew();
            merchantEntity.setName(registerRequest.getBusinessName());

            Long merchantId = dmlManager.store(merchantEntity);

            // Masukkan data cabang
            StoreEntity storeEntity = new StoreEntity();

            storeEntity.markNew();
            storeEntity.setMerchantId(merchantId);
            storeEntity.setName(registerRequest.getBusinessName());
            storeEntity.setPhoneNumber(registerRequest.getPhoneNumber());
            storeEntity.setAddress(registerRequest.getAddress());
            storeEntity.setCity(registerRequest.getCity());

            // Masukkan data pemilik
            OwnerEntity ownerEntity = new OwnerEntity();

            ownerEntity.markNew();
            ownerEntity.setMerchantId(merchantId);
            ownerEntity.setId(registerRequest.getEmail());
            ownerEntity.setPassword(Digest.MD5(registerRequest.getEmail(), registerRequest.getPassword()));
            ownerEntity.setName(registerRequest.getName());

            // Simpan data cabang dan pemilik
            dmlManager.store(storeEntity);
            dmlManager.store(ownerEntity);
            dmlManager.commit();
        }

        return Response
                .ok()
                .build();
    }

    @POST
    @Path("/sign-in")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response signIn(SignInRequest signInRequest) throws Exception {
        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();

            HashEntity hashEntity = DMLAssembler
                    .create()
                    .select("a.id AS id")
                    .select("a.name AS name")
                    .select("b.id AS merchant_id")
                    .select("b.name AS merchant_name")
                    .from("owners a")
                    .join("merchants b ON b.id = a.merchant_id", JoinType.INNER_JOIN)
                    .equalTo("a.id", signInRequest.getEmail())
                    .getWynixResult(HashEntity.class);

            Validators.validate(hashEntity, "Email tidak dapat ditemukan.");

            SignInResponse signInResponse = new SignInResponse();

            signInResponse.setEmail(hashEntity.get("id"));
            signInResponse.setName(hashEntity.get("name"));
            signInResponse.setMerchantId(hashEntity.get("merchant_id"));
            signInResponse.setMerchantName(hashEntity.get("merchant_name"));

            return Response
                    .ok(signInResponse)
                    .build();
        }
    }

    @POST
    @Path("/verify")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response verify(VerificationRequest verificationRequest) throws Exception {
        SessionManager.getInstance().createSystemSession();

        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();

            OwnerEntity ownerEntity = DMLManager.getWynixResultFromQuery(
                    "SELECT * FROM owners WHERE id = ?",
                    OwnerEntity.class,
                    verificationRequest.getEmail()
            );

            Validators.validate(ownerEntity, "Email tidak dapat ditemukan.");

            String jwtToken = Jwts.builder()
                    .setSubject(ownerEntity.getId())
                    .setAudience(ownerEntity.getName())
                    .setExpiration(Date.from(TimeManager.getInstance().now().plusYears(5).atZone(ZoneId.systemDefault()).toInstant()))
                    .setIssuedAt(new Date())
                    .setIssuer(Constant.Application.NAME)
                    .setId(String.valueOf(ownerEntity.getId()))
                    .claim("RoleGroup", OwnerEntity.class.getSimpleName())
                    .signWith(SignatureAlgorithm.RS512, Crypt.getKey())
                    .compact();

            Validators.validate(!StringUtils.equals(ownerEntity.getPassword(), Digest.MD5(verificationRequest.getEmail(), verificationRequest.getPassword())), "Kata sandi tidak sesuai.");

            OwnerSessionEntity ownerSessionEntity = DMLManager.getWynixResultFromQuery(
                    "SELECT * FROM owner_sessions WHERE owner_id = ? AND fingerprint_id = ?",
                    OwnerSessionEntity.class,
                    ownerEntity.getId(),
                    SessionManager.getInstance().getRequestMeta().getFingerprintId()
            );

            if (ownerSessionEntity == null) {
                ownerSessionEntity = new OwnerSessionEntity();
                ownerSessionEntity.markNew();
                ownerSessionEntity.setOwnerId(ownerEntity.getId());
                ownerSessionEntity.setFingerprintId(SessionManager.getInstance().getRequestMeta().getFingerprintId());
            } else {
                ownerSessionEntity.markUpdate();
            }

            ownerSessionEntity.setSessionId(jwtToken);
            ownerSessionEntity.setPlatform(SessionManager.getInstance().getRequestMeta().getPlatform());
            ownerSessionEntity.setVersion(SessionManager.getInstance().getRequestMeta().getVersion());
            ownerSessionEntity.setDescription(SessionManager.getInstance().getRequestMeta().getDescription());
            ownerSessionEntity.setIpAddress(SessionManager.getInstance().getRequestMeta().getIpAddress());
            ownerSessionEntity.setLocation(SessionManager.getInstance().getRequestMeta().getLocation());
            ownerSessionEntity.setLastActive(LocalDateTime.now());

            dmlManager.store(ownerSessionEntity);
            dmlManager.commit();

            return Response.ok()
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Access-Control-Expose-Headers", "Authorization")
                    .build();
        }
    }
}