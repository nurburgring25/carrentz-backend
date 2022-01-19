package dev.burikk.carrentz.engine.api.exception.mapper;

import dev.burikk.carrentz.app.entity.LogEntity;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.exception.WynixException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.sql.SQLException;

/**
 * @author Muhammad Irfan
 * @since 21/02/2019 21.20
 */
@Provider
public class GeneralExceptionMapper implements ExceptionMapper<Throwable> {
    private static final transient Logger LOGGER = LogManager.getLogger(GeneralExceptionMapper.class);

    @Context
    private HttpServletRequest httpServletRequest;

    @Override
    public Response toResponse(Throwable exception) {
        LOGGER.debug(ExceptionUtils.getStackTrace(exception));

        try {
            LogEntity logEntity = DMLManager.getEntity(LogEntity.class, this.httpServletRequest.getAttribute("requestId"));

            if (logEntity != null) {
                logEntity.markUpdate();
                logEntity.setExceptionStackTrace(ExceptionUtils.getStackTrace(exception));

                DMLManager.storeImmediately(logEntity);
            }
        } catch (Exception ex) {
            LOGGER.debug(ExceptionUtils.getStackTrace(ex));
        }

        int status = Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();

        if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        }

        String message = "Something wrong, please try again.";

        if (exception instanceof WynixException) {
            message = exception.getLocalizedMessage();
        } else if (exception instanceof SQLException) {
            SQLException sqlException = (SQLException) exception;

            message = sqlException.getLocalizedMessage();
        } else if (exception instanceof WebApplicationException) {
            switch (status) {
                case 403:
                    message = "You don't have privilege to access this resource.";
                    break;
                case 401:
                    message = "Your session is already expired. Please relogin to continue.";
                    break;
                case 404:
                    message = "Resource that you want to access is unavailable.";
            }

            Object entity = ((WebApplicationException) exception).getResponse().getEntity();

            if (entity != null) {
                if (entity instanceof String) {
                    if (StringUtils.isNotBlank((CharSequence) entity)) {
                        message = (String) entity;
                    }
                }
            }
        }

        return Response
                .status(status)
                .entity(message)
                .build();
    }
}