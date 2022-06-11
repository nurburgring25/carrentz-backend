package dev.burikk.carrentz.app.api.service.user.account;

import dev.burikk.carrentz.app.api.service.user.account.request.RegisterRequest;
import dev.burikk.carrentz.app.api.service.user.account.request.SignInRequest;
import dev.burikk.carrentz.app.api.service.user.account.request.VerificationRequest;
import dev.burikk.carrentz.app.api.service.user.account.response.SignInResponse;
import dev.burikk.carrentz.app.entity.*;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.TimeManager;
import dev.burikk.carrentz.engine.datasource.DMLManager;
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
@Path("/users/")
public class AccountService {
    @POST
    @Path("/register")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(RegisterRequest registerRequest) throws Exception {
        SessionManager.getInstance().createSystemSession();

        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();

            // Masukkan data pengguna
            UserEntity userEntity = new UserEntity();

            userEntity.markNew();
            userEntity.setId(registerRequest.getEmail());
            userEntity.setPassword(Digest.MD5(registerRequest.getEmail(), registerRequest.getPassword()));
            userEntity.setName(registerRequest.getName());
            userEntity.setPhoneNumber(registerRequest.getPhoneNumber());

            // Simpan data pengguna
            dmlManager.store(userEntity);
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

            UserEntity userEntity = DMLManager.getWynixResultFromQuery(
                    "SELECT * FROM users WHERE id = ?",
                    UserEntity.class,
                    signInRequest.getEmail()
            );

            Validators.validate(userEntity, "Email tidak dapat ditemukan.");

            SignInResponse signInResponse = new SignInResponse();

            signInResponse.setEmail(userEntity.getId());
            signInResponse.setName(userEntity.getName());

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

            UserEntity userEntity = DMLManager.getWynixResultFromQuery(
                    "SELECT * FROM users WHERE id = ?",
                    UserEntity.class,
                    verificationRequest.getEmail()
            );

            Validators.validate(userEntity, "Email tidak dapat ditemukan.");

            String jwtToken = Jwts.builder()
                    .setSubject(userEntity.getId())
                    .setAudience(userEntity.getName())
                    .setExpiration(Date.from(TimeManager.getInstance().now().plusYears(5).atZone(ZoneId.systemDefault()).toInstant()))
                    .setIssuedAt(new Date())
                    .setIssuer(Constant.Application.NAME)
                    .setId(String.valueOf(userEntity.getId()))
                    .claim("RoleGroup", UserEntity.class.getSimpleName())
                    .signWith(SignatureAlgorithm.RS512, Crypt.getKey())
                    .compact();

            Validators.validate(!StringUtils.equals(userEntity.getPassword(), Digest.MD5(verificationRequest.getEmail(), verificationRequest.getPassword())), "Kata sandi tidak sesuai.");

            UserSessionEntity userSessionEntity = DMLManager.getWynixResultFromQuery(
                    "SELECT * FROM user_sessions WHERE user_id = ? AND fingerprint_id = ?",
                    UserSessionEntity.class,
                    userEntity.getId(),
                    SessionManager.getInstance().getRequestMeta().getFingerprintId()
            );

            if (userSessionEntity == null) {
                userSessionEntity = new UserSessionEntity();
                userSessionEntity.markNew();
                userSessionEntity.setUserId(userEntity.getId());
                userSessionEntity.setFingerprintId(SessionManager.getInstance().getRequestMeta().getFingerprintId());
            } else {
                userSessionEntity.markUpdate();
            }

            userSessionEntity.setSessionId(jwtToken);
            userSessionEntity.setPlatform(SessionManager.getInstance().getRequestMeta().getPlatform());
            userSessionEntity.setVersion(SessionManager.getInstance().getRequestMeta().getVersion());
            userSessionEntity.setDescription(SessionManager.getInstance().getRequestMeta().getDescription());
            userSessionEntity.setIpAddress(SessionManager.getInstance().getRequestMeta().getIpAddress());
            userSessionEntity.setLocation(SessionManager.getInstance().getRequestMeta().getLocation());
            userSessionEntity.setLastActive(LocalDateTime.now());

            dmlManager.store(userSessionEntity);
            dmlManager.commit();

            return Response.ok()
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Access-Control-Expose-Headers", "Authorization")
                    .build();
        }
    }
}