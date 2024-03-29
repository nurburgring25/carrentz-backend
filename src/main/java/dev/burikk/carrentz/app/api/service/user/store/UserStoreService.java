package dev.burikk.carrentz.app.api.service.user.store;

import dev.burikk.carrentz.app.api.service.user.store.item.UserStoreItem;
import dev.burikk.carrentz.app.api.service.user.store.response.UserStoreListResponse;
import dev.burikk.carrentz.app.entity.StoreEntity;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLManager;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.util.Arrays;

@Path("/users/")
public class UserStoreService {
    @GET
    @Path("/stores")
    @RolesAllowed("UserEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws Exception {
        UserStoreListResponse userStoreListResponse = new UserStoreListResponse();

        WynixResults<StoreEntity> storeEntities = DMLManager.getWynixResultsFromQuery(
                "SELECT a.*, b.name AS merchant_name FROM stores a INNER JOIN merchants b ON b.id = a.merchant_id ORDER BY a.name",
                StoreEntity.class
        );

        for (StoreEntity storeEntity : storeEntities) {
            UserStoreItem storeItem = new UserStoreItem();

            storeItem.setId(storeEntity.getId());
            storeItem.setMerchantId(storeEntity.getMerchantId());
            storeItem.setMerchantName(storeEntity.getMerchantName());
            storeItem.setName(storeEntity.getName());
            storeItem.setPhoneNumber(storeEntity.getPhoneNumber());
            storeItem.setAddress(storeEntity.getAddress());
            storeItem.setCity(storeEntity.getCity());
            storeItem.setImageUrl(Constant.Application.BASE_URL + "users/stores/images/" + storeItem.getId());

            userStoreListResponse.getDetails().add(storeItem);
        }

        return Response
                .ok(userStoreListResponse)
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