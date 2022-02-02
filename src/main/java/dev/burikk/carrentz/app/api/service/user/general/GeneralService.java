package dev.burikk.carrentz.app.api.service.user.general;

import dev.burikk.carrentz.engine.datasource.DMLManager;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.Arrays;

@Path("/users/generals")
public class GeneralService {
    @GET
    @PermitAll
    @Path("/merchant-logo/{id}")
    public Response merchantLogo(
            @Context Request request,
            @PathParam("id") long id
    ) throws Exception {
        byte[] bytes = DMLManager.getObjectFromQuery(
                "SELECT image FROM merchants WHERE id = ?",
                id
        );

        if (bytes != null) {
            EntityTag entityTag = new EntityTag(Integer.toString(Arrays.hashCode(bytes)));

            Response.ResponseBuilder responseBuilder = request.evaluatePreconditions(entityTag);

            if (responseBuilder == null) {
                responseBuilder = Response
                        .ok(bytes)
                        .header("Content-Type", URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(bytes)));

                responseBuilder.tag(entityTag);
            }

            return responseBuilder.build();
        }

        return Response
                .ok()
                .build();
    }
}