package dev.burikk.carrentz.app.api.service.merchant.store;

import dev.burikk.carrentz.app.api.service.merchant.store.item.StoreItem;
import dev.burikk.carrentz.app.api.service.merchant.store.response.StoreListResponse;
import dev.burikk.carrentz.app.entity.StoreEntity;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLManager;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

            storeListResponse.getDetails().add(storeItem);
        }

        return Response
                .ok(storeListResponse)
                .build();
    }

    @POST
    @Path("/stores")
    @RolesAllowed("OwnerEntity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(StoreItem storeItem) throws Exception {
        StoreEntity storeEntity = new StoreEntity();

        storeEntity.markNew();
        storeEntity.setMerchantId(SessionManager.getInstance().getMerchantId());
        storeEntity.setName(storeItem.getName());
        storeEntity.setPhoneNumber(storeItem.getPhoneNumber());
        storeEntity.setAddress(storeItem.getAddress());
        storeEntity.setCity(storeItem.getCity());

        DMLManager.storeImmediately(storeEntity);

        return Response
                .ok()
                .build();
    }

    @PUT
    @Path("/stores/{id}")
    @RolesAllowed("OwnerEntity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response put(
            @PathParam("id") long id,
            StoreItem storeItem
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
}