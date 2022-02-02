package dev.burikk.carrentz.app.api.service.user.store;

import dev.burikk.carrentz.app.api.service.user.store.item.UserStoreItem;
import dev.burikk.carrentz.app.api.service.user.store.response.UserStoreListResponse;
import dev.burikk.carrentz.app.entity.StoreEntity;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLManager;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

            userStoreListResponse.getDetails().add(storeItem);
        }

        return Response
                .ok(userStoreListResponse)
                .build();
    }
}