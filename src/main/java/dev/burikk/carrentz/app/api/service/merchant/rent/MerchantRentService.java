package dev.burikk.carrentz.app.api.service.merchant.rent;

import dev.burikk.carrentz.app.api.service.merchant.rent.item.MerchantRentItem;
import dev.burikk.carrentz.app.api.service.merchant.rent.item.MerchantRentStoreItem;
import dev.burikk.carrentz.app.api.service.merchant.rent.item.MerchantRentUserItem;
import dev.burikk.carrentz.app.api.service.merchant.rent.item.MerchantRentVehicleItem;
import dev.burikk.carrentz.app.api.service.merchant.rent.response.MerchantRentListResponse;
import dev.burikk.carrentz.app.entity.RentEntity;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.entity.HashEntity;
import dev.burikk.carrentz.engine.exception.WynixException;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/merchants/")
public class MerchantRentService {
    @GET
    @Path("/rents")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws Exception {
        MerchantRentListResponse merchantRentListResponse = new MerchantRentListResponse();

        WynixResults<HashEntity> hashEntities = DMLAssembler
                .create()
                .select("a.id AS id")
                .select("b.id AS user_id")
                .select("b.name AS user_name")
                .select("b.phone_number AS phone_number")
                .select("c.id AS vehicle_id")
                .select("e.name AS vehicle_type")
                .select("c.license_number AS license_number")
                .select("c.name AS vehicle_name")
                .select("d.id AS store_id")
                .select("d.name AS store_name")
                .select("a.number AS number")
                .select("a.status AS status")
                .select("a.start AS start")
                .select("a.until AS until")
                .select("a.duration AS duration")
                .select("a.cost_per_day AS cost_per_day")
                .select("a.total AS total")
                .select("a.rent_code AS rent_code")
                .select("a.return_code AS return_code")
                .from("rents a")
                .join("users b ON b.id = a.user_id", JoinType.INNER_JOIN)
                .join("vehicles c ON c.id = a.vehicle_id", JoinType.INNER_JOIN)
                .join("stores d ON d.id = c.store_id", JoinType.INNER_JOIN)
                .join("vehicle_types e ON e.id = c.vehicle_type_id", JoinType.INNER_JOIN)
                .equalTo("d.merchant_id", SessionManager.getInstance().getMerchantId())
                .and()
                .in("a.status", Constant.DocumentStatus.OPENED, Constant.DocumentStatus.ONGOING)
                .desc("a.start")
                .getWynixResults(HashEntity.class);

        for (HashEntity hashEntity : hashEntities) {
            MerchantRentItem merchantRentItem = new MerchantRentItem();

            merchantRentItem.setId(hashEntity.get("id"));

            {
                MerchantRentUserItem merchantRentUserItem = new MerchantRentUserItem();

                merchantRentUserItem.setId(hashEntity.get("user_id"));
                merchantRentUserItem.setName(hashEntity.get("user_name"));
                merchantRentUserItem.setPhoneNumber(hashEntity.get("phone_number"));

                merchantRentItem.setUser(merchantRentUserItem);
            }

            {
                MerchantRentVehicleItem merchantRentVehicleItem = new MerchantRentVehicleItem();

                merchantRentVehicleItem.setId(hashEntity.get("vehicle_id"));
                merchantRentVehicleItem.setVehicleType(hashEntity.get("vehicle_type"));
                merchantRentVehicleItem.setLicenseNumber(hashEntity.get("license_number"));
                merchantRentVehicleItem.setName(hashEntity.get("vehicle_name"));

                merchantRentItem.setVehicle(merchantRentVehicleItem);
            }

            {
                MerchantRentStoreItem merchantRentStoreItem = new MerchantRentStoreItem();

                merchantRentStoreItem.setId(hashEntity.get("store_id"));
                merchantRentStoreItem.setName(hashEntity.get("store_name"));

                merchantRentItem.setStore(merchantRentStoreItem);
            }

            merchantRentItem.setNumber(hashEntity.get("number"));
            merchantRentItem.setStatus(hashEntity.get("status"));
            merchantRentItem.setStart(hashEntity.get("start"));
            merchantRentItem.setUntil(hashEntity.get("until"));
            merchantRentItem.setDuration(hashEntity.get("duration"));
            merchantRentItem.setCostPerDay(hashEntity.get("cost_per_day"));
            merchantRentItem.setTotal(hashEntity.get("total"));
            merchantRentItem.setRentCode(hashEntity.get("rent_code"));
            merchantRentItem.setReturnCode(hashEntity.get("return_code"));

            merchantRentListResponse.getDetails().add(merchantRentItem);
        }

        return Response
                .ok(merchantRentListResponse)
                .build();
    }

    @GET
    @Path("/rents/{id}/get-rent-code")
    @RolesAllowed("OwnerEntity")
    public Response getRentCode(
            @PathParam("id") Long id
    ) throws Exception {
        RentEntity rentEntity = DMLManager.getEntity(RentEntity.class, id);

        if (rentEntity != null) {
            rentEntity.markUpdate();
            rentEntity.setRentCode(StringUtils.upperCase(StringUtils.right(Long.toHexString(System.currentTimeMillis()), 4)));

            DMLManager.storeImmediately(rentEntity);

            return Response
                    .ok(rentEntity.getRentCode())
                    .build();
        }

        throw new WynixException("Dokumen dengan id " + id + " tidak dapat ditemukan.");
    }

    @GET
    @Path("/rents/{id}/get-return-code")
    @RolesAllowed("OwnerEntity")
    public Response getReturnCode(
            @PathParam("id") Long id
    ) throws Exception {
        RentEntity rentEntity = DMLManager.getEntity(RentEntity.class, id);

        if (rentEntity != null) {
            rentEntity.markUpdate();
            rentEntity.setReturnCode(StringUtils.upperCase(StringUtils.right(Long.toHexString(System.currentTimeMillis()), 4)));

            DMLManager.storeImmediately(rentEntity);

            return Response
                    .ok(rentEntity.getReturnCode())
                    .build();
        }

        throw new WynixException("Dokumen dengan id " + id + " tidak dapat ditemukan.");
    }

    @DELETE
    @Path("/rents/{id}")
    @RolesAllowed("OwnerEntity")
    public Response cancel(
            @PathParam("id") Long id
    ) throws Exception {
        RentEntity rentEntity = DMLManager.getEntity(RentEntity.class, id);

        if (rentEntity != null) {
            rentEntity.markUpdate();
            rentEntity.setStatus(Constant.DocumentStatus.CANCELLED);

            DMLManager.storeImmediately(rentEntity);

            return Response
                    .ok()
                    .build();
        }

        throw new WynixException("Dokumen dengan id " + id + " tidak dapat ditemukan.");
    }

    @GET
    @Path("/rents/{id}")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("id") long id) throws Exception {
        HashEntity hashEntity = DMLAssembler
                .create()
                .select("a.id AS id")
                .select("b.id AS user_id")
                .select("b.name AS user_name")
                .select("b.phone_number AS phone_number")
                .select("c.id AS vehicle_id")
                .select("e.name AS vehicle_type")
                .select("c.license_number AS license_number")
                .select("c.name AS vehicle_name")
                .select("d.id AS store_id")
                .select("d.name AS store_name")
                .select("a.number AS number")
                .select("a.status AS status")
                .select("a.start AS start")
                .select("a.until AS until")
                .select("a.duration AS duration")
                .select("a.cost_per_day AS cost_per_day")
                .select("a.total AS total")
                .select("a.rent_code AS rent_code")
                .select("a.return_code AS return_code")
                .from("rents a")
                .join("users b ON b.id = a.user_id", JoinType.INNER_JOIN)
                .join("vehicles c ON c.id = a.vehicle_id", JoinType.INNER_JOIN)
                .join("stores d ON d.id = c.store_id", JoinType.INNER_JOIN)
                .join("vehicle_types e ON e.id = c.vehicle_type_id", JoinType.INNER_JOIN)
                .equalTo("a.id", id)
                .getWynixResult(HashEntity.class);

        MerchantRentItem merchantRentItem = new MerchantRentItem();

        merchantRentItem.setId(hashEntity.get("id"));

        {
            MerchantRentUserItem merchantRentUserItem = new MerchantRentUserItem();

            merchantRentUserItem.setId(hashEntity.get("user_id"));
            merchantRentUserItem.setName(hashEntity.get("user_name"));
            merchantRentUserItem.setPhoneNumber(hashEntity.get("phone_number"));

            merchantRentItem.setUser(merchantRentUserItem);
        }

        {
            MerchantRentVehicleItem merchantRentVehicleItem = new MerchantRentVehicleItem();

            merchantRentVehicleItem.setId(hashEntity.get("vehicle_id"));
            merchantRentVehicleItem.setVehicleType(hashEntity.get("vehicle_type"));
            merchantRentVehicleItem.setLicenseNumber(hashEntity.get("license_number"));
            merchantRentVehicleItem.setName(hashEntity.get("vehicle_name"));

            merchantRentItem.setVehicle(merchantRentVehicleItem);
        }

        {
            MerchantRentStoreItem merchantRentStoreItem = new MerchantRentStoreItem();

            merchantRentStoreItem.setId(hashEntity.get("store_id"));
            merchantRentStoreItem.setName(hashEntity.get("store_name"));

            merchantRentItem.setStore(merchantRentStoreItem);
        }

        merchantRentItem.setNumber(hashEntity.get("number"));
        merchantRentItem.setStatus(hashEntity.get("status"));
        merchantRentItem.setStart(hashEntity.get("start"));
        merchantRentItem.setUntil(hashEntity.get("until"));
        merchantRentItem.setDuration(hashEntity.get("duration"));
        merchantRentItem.setCostPerDay(hashEntity.get("cost_per_day"));
        merchantRentItem.setTotal(hashEntity.get("total"));
        merchantRentItem.setRentCode(hashEntity.get("rent_code"));
        merchantRentItem.setReturnCode(hashEntity.get("return_code"));

        return Response
                .ok(merchantRentItem)
                .build();
    }
}