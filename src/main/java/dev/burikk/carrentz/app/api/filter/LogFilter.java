package dev.burikk.carrentz.app.api.filter;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.burikk.carrentz.app.entity.LogEntity;
import dev.burikk.carrentz.engine.api.common.JacksonConfigurator;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.TimeManager;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.glassfish.jersey.message.internal.MediaTypes;
import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.ContainerResponse;

import javax.annotation.Priority;
import javax.annotation.security.PermitAll;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Locale;

/**
 * @author Muhammad Irfan
 * @since 04/03/2019 16.19
 */
@Provider
@Priority(1)
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter {
    private static final transient Logger LOGGER = LogManager.getLogger(LogFilter.class);

    @Context
    ResourceInfo resourceInfo;
    @Context
    JacksonConfigurator jacksonConfigurator;
    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) {
        Method method = this.resourceInfo.getResourceMethod();

        if (!method.isAnnotationPresent(PermitAll.class)) {
            // Get fingerprint id header from the request
            String fingerprintId = containerRequestContext.getHeaderString("Carrentz-Fingerprint-Id");

            // When fingerprint id header not exists, response with forbidden status
            if (StringUtils.isBlank(fingerprintId)) {
                throw new WebApplicationException(
                        Response
                                .status(Response.Status.UNAUTHORIZED)
                                .entity("Missing fingerprint id header.")
                                .build()
                );
            }

            // Set fingerprint id header to session manager
            SessionManager.getInstance().getRequestMeta().setFingerprintId(fingerprintId);

            // Get platform type header from the request
            String platformTypeHeader = containerRequestContext.getHeaderString("Carrentz-Platform-Type");

            // When platform type header not exists, response with forbidden status
            if (StringUtils.isBlank(platformTypeHeader)) {
                throw new WebApplicationException(
                        Response
                                .status(Response.Status.UNAUTHORIZED)
                                .entity("Missing platform type header.")
                                .build()
                );
            }

            // Set platform type header to session manager
            SessionManager.getInstance().getRequestMeta().setPlatform(platformTypeHeader);

            // Get version header from the request
            String version = containerRequestContext.getHeaderString("Carrentz-Version");

            // When version header not exists, response with forbidden status
            if (StringUtils.isBlank(version)) {
                throw new WebApplicationException(
                        Response
                                .status(Response.Status.UNAUTHORIZED)
                                .entity("Missing version header.")
                                .build()
                );
            }

            // Set version header to session manager
            SessionManager.getInstance().getRequestMeta().setVersion(version);

            // Get description header from the request
            String description = containerRequestContext.getHeaderString("Carrentz-Description");

            // When description header not exists, response with forbidden status
            if (StringUtils.isBlank(description)) {
                throw new WebApplicationException(
                        Response
                                .status(Response.Status.UNAUTHORIZED)
                                .entity("Missing description header.")
                                .build()
                );
            }

            // Set description header to session manager
            SessionManager.getInstance().getRequestMeta().setDescription(description);

            // Get locale header from the request
            String locale = containerRequestContext.getHeaderString("Carrentz-Locale");

            // If locale is blank, set it to english
            if (StringUtils.isBlank(locale)) {
                locale = "en";
            }

            // Set description header to session manager
            SessionManager.getInstance().setLocale(new Locale(locale));

            // Set ip address to session manager
            SessionManager.getInstance().getRequestMeta().setIpAddress(this.httpServletRequest.getRemoteAddr());

            // Get location from ip
            try {
                URL url = new URL("http://ip-api.com/json/" + SessionManager.getInstance().getRequestMeta().getIpAddress() + "?fields=17");

                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                httpURLConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                httpURLConnection.setRequestProperty("Accept", "application/json");
                httpURLConnection.setRequestMethod("GET");

                httpURLConnection.setConnectTimeout(3000);
                httpURLConnection.setReadTimeout(3000);

                StringBuilder stringBuilder = new StringBuilder();

                if (httpURLConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream(), StandardCharsets.UTF_8));

                    String line;

                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line).append("\n");
                    }

                    bufferedReader.close();

                    JsonObject jsonObject = new JsonParser().parse(stringBuilder.toString()).getAsJsonObject();

                    // Set location to session manager
                    SessionManager.getInstance().getRequestMeta().setLocation(jsonObject.get("city").getAsString() + ", " + jsonObject.get("country").getAsString());
                }
            } catch (Exception ignored) {}

            try {
                if (containerRequestContext instanceof ContainerRequest) {
                    ContainerRequest containerRequest = (ContainerRequest) containerRequestContext;

                    LogEntity logEntity = new LogEntity();

                    logEntity.markNew();
                    logEntity.setFingerprintId(fingerprintId);
                    logEntity.setRequestAt(TimeManager.getInstance().now());
                    logEntity.setRequestHeader(containerRequest.getRequestHeaders().toString());

                    if (containerRequestContext.hasEntity() && MediaTypes.typeEqual(MediaType.APPLICATION_JSON_TYPE, containerRequest.getMediaType())) {
                        containerRequest.bufferEntity();

                        String requestData = containerRequest.readEntity(String.class);

                        logEntity.setRequestBody(requestData);
                    }

                    logEntity.setResourcePath(containerRequest.getRequestUri().toString());
                    logEntity.setHttpMethod(this.httpServletRequest.getMethod());
                    logEntity.setPlatform(SessionManager.getInstance().getRequestMeta().getPlatform());
                    logEntity.setVersion(SessionManager.getInstance().getRequestMeta().getVersion());
                    logEntity.setDescription(SessionManager.getInstance().getRequestMeta().getDescription());
                    logEntity.setIpAddress(SessionManager.getInstance().getRequestMeta().getIpAddress());
                    logEntity.setLocation(SessionManager.getInstance().getRequestMeta().getLocation());

                    Long id = DMLManager.storeImmediately(logEntity);

                    containerRequestContext.setProperty("requestId", id);
                }
            } catch (Exception ex) {
                LOGGER.catching(ex);
            }
        }
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext, ContainerResponseContext containerResponseContext) {
        Method method = this.resourceInfo.getResourceMethod();

        // Everybody can access
        if (!method.isAnnotationPresent(PermitAll.class)) {
            try {
                if (containerResponseContext instanceof ContainerResponse) {
                    ContainerResponse containerResponse = (ContainerResponse) containerResponseContext;

                    LogEntity logEntity = DMLManager.getEntity(LogEntity.class, containerRequestContext.getProperty("requestId"));

                    if (logEntity != null) {
                        logEntity.markUpdate();
                        logEntity.setResponseAt(TimeManager.getInstance().now());
                        logEntity.setResponseHeader(containerResponse.getHeaders().toString());

                        if (containerResponse.getEntity() != null) {
                            logEntity.setResponseBody(new Gson().toJson(containerResponse.getEntity()));
                        }

                        logEntity.setResponseStatus(containerResponse.getStatus());
                        logEntity.setTimeElapsed(Duration.between(logEntity.getRequestAt(), logEntity.getResponseAt()).toMillis());

                        DMLManager.storeImmediately(logEntity);
                    }
                }
            } catch (Exception ex) {
                LOGGER.catching(ex);
            }
        }
    }
}