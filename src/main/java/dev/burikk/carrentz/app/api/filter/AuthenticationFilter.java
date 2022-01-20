package dev.burikk.carrentz.app.api.filter;

import dev.burikk.carrentz.app.entity.OwnerEntity;
import dev.burikk.carrentz.app.entity.OwnerSessionEntity;
import dev.burikk.carrentz.app.entity.UserEntity;
import dev.burikk.carrentz.app.entity.UserSessionEntity;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.security.Crypt;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Priority;
import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.naming.NamingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Method;
import java.security.cert.CertificateException;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Muhammad Irfan
 * @since 11/01/2019 14.41
 */
@Provider
@Priority(2)
public class AuthenticationFilter implements ContainerRequestFilter {
    @Context
    ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws WebApplicationException {
        Method method = this.resourceInfo.getResourceMethod();

        // Everybody can access
        if (!method.isAnnotationPresent(PermitAll.class)) {
            // Nobody can access
            if (method.isAnnotationPresent(DenyAll.class)) {
                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
            }

            // Get authorization header from the request
            String authorizationHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

            // When authorization header not exists, response with forbidden status
            if (StringUtils.isBlank(authorizationHeader)) {
                throw new WebApplicationException(
                        Response
                                .status(Response.Status.UNAUTHORIZED)
                                .entity("Missing bearer authorization header.")
                                .build()
                );
            }

            // Extract jwt token from authorization header
            String jwtToken = authorizationHeader.substring("Bearer".length()).trim();

            // Validate jwt token
            try {
                try {
                    Jwts.parser()
                            .setSigningKey(Crypt.getPublicKey())
                            .parseClaimsJws(jwtToken);
                } catch (ExpiredJwtException ex) {
                    throw new WebApplicationException(
                            Response
                                    .status(Response.Status.UNAUTHORIZED)
                                    .entity("Sesi anda telah habis. Harap untuk melakukan login ulang untuk melanjutkan.")
                                    .build()
                    );
                }

                UserSessionEntity userSessionEntity = DMLManager.getWynixResultFromQuery(
                        "SELECT *\n" +
                                "FROM user_sessions\n" +
                                "WHERE session_id = ?",
                        UserSessionEntity.class,
                        jwtToken
                );

                if (userSessionEntity != null) {
                    if (method.isAnnotationPresent(RolesAllowed.class)) {
                        RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);

                        if (!Arrays.asList(rolesAllowed.value()).contains(UserEntity.class.getSimpleName())) {
                            throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
                        }
                    }

                    SessionManager.getInstance().setWynixSession(userSessionEntity);
                } else {
                    OwnerSessionEntity ownerSessionEntity = DMLManager.getWynixResultFromQuery(
                            "SELECT *\n" +
                                    "FROM owner_sessions\n" +
                                    "WHERE session_id = ?",
                            OwnerSessionEntity.class,
                            jwtToken
                    );

                    if (ownerSessionEntity != null) {
                        if (method.isAnnotationPresent(RolesAllowed.class)) {
                            RolesAllowed rolesAllowed = method.getAnnotation(RolesAllowed.class);

                            if (!Arrays.asList(rolesAllowed.value()).contains(OwnerEntity.class.getSimpleName())) {
                                throw new WebApplicationException(Response.status(Response.Status.FORBIDDEN).build());
                            }
                        }

                        SessionManager.getInstance().setWynixSession(ownerSessionEntity);
                    } else {
                        throw new WebApplicationException(
                                Response
                                        .status(Response.Status.UNAUTHORIZED)
                                        .entity("Sesi anda telah habis. Harap untuk melakukan login ulang untuk melanjutkan.")
                                        .build()
                        );
                    }
                }
            } catch (CertificateException | InstantiationException | NamingException | SQLException | IllegalAccessException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}