package dev.burikk.carrentz.app.api.service.merchant.store;

import dev.burikk.carrentz.app.api.service.merchant.store.item.StoreItem;
import dev.burikk.carrentz.app.api.service.merchant.store.response.StoreListResponse;
import dev.burikk.carrentz.app.entity.StoreEntity;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.Arrays;

@Path("/merchants/")
public class StoreService {
    @GET
    @Path("/stores")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws Exception {
        StoreListResponse storeListResponse = new StoreListResponse();

        WynixResults<StoreEntity> storeEntities = DMLManager.getWynixResultsFromQuery(
                "SELECT * FROM stores WHERE merchant_id = ? ORDER BY name",
                StoreEntity.class,
                SessionManager.getInstance().getMerchantId()
        );

        for (StoreEntity storeEntity : storeEntities) {
            StoreItem storeItem = new StoreItem();

            storeItem.setId(storeEntity.getId());
            storeItem.setName(storeEntity.getName());
            storeItem.setPhoneNumber(storeEntity.getPhoneNumber());
            storeItem.setAddress(storeEntity.getAddress());
            storeItem.setCity(storeEntity.getCity());
            storeItem.setImageUrl(Constant.Application.BASE_URL + "merchants/stores/images/" + storeItem.getId());

            storeListResponse.getDetails().add(storeItem);
        }

        return Response
                .ok(storeListResponse)
                .build();
    }

    @POST
    @Path("/stores")
    @RolesAllowed("OwnerEntity")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(
            @FormDataParam("image") FormDataBodyPart formDataBodyPart,
            @FormDataParam("data") StoreItem storeItem
    ) throws Exception {
        StoreEntity storeEntity = new StoreEntity();

        storeEntity.markNew();
        storeEntity.setMerchantId(SessionManager.getInstance().getMerchantId());
        storeEntity.setName(storeItem.getName());
        storeEntity.setPhoneNumber(storeItem.getPhoneNumber());
        storeEntity.setAddress(storeItem.getAddress());
        storeEntity.setCity(storeItem.getCity());

        if (formDataBodyPart != null) {
            InputStream inputStream = formDataBodyPart.getValueAs(InputStream.class);

            if (inputStream != null) {
                storeEntity.setImage(IOUtils.toByteArray(inputStream));
            }
        }

        DMLManager.storeImmediately(storeEntity);

        return Response
                .ok()
                .build();
    }

    @PUT
    @Path("/stores/{id}")
    @RolesAllowed("OwnerEntity")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response put(
            @PathParam("id") long id,
            @FormDataParam("image") FormDataBodyPart formDataBodyPart,
            @FormDataParam("data") StoreItem storeItem
    ) throws Exception {
        StoreEntity storeEntity = DMLManager.getEntity(StoreEntity.class, id);

        if (storeEntity == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        storeEntity.markUpdate();
        storeEntity.setName(storeItem.getName());
        storeEntity.setPhoneNumber(storeItem.getPhoneNumber());
        storeEntity.setAddress(storeItem.getAddress());
        storeEntity.setCity(storeItem.getCity());

        if (formDataBodyPart != null) {
            InputStream inputStream = formDataBodyPart.getValueAs(InputStream.class);

            if (inputStream != null) {
                storeEntity.setImage(IOUtils.toByteArray(inputStream));
            }
        }

        DMLManager.storeImmediately(storeEntity);

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Path("/stores/{id}")
    @RolesAllowed("OwnerEntity")
    public Response post(@PathParam("id") long id) throws Exception {
        StoreEntity storeEntity = DMLManager.getEntity(StoreEntity.class, id);

        if (storeEntity == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        storeEntity.markDelete();

        DMLManager.storeImmediately(storeEntity);

        return Response
                .ok()
                .build();
    }

    @GET
    @PermitAll
    @Path("/stores/images/{id}")
    public Response image(
            @Context Request request,
            @PathParam("id") long id
    ) throws Exception {
        byte[] bytes = DMLManager.getObjectFromQuery(
                "SELECT image FROM stores WHERE id = ?",
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