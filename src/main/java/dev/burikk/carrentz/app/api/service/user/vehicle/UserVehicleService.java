package dev.burikk.carrentz.app.api.service.user.vehicle;

import dev.burikk.carrentz.app.api.service.user.vehicle.item.UserVehicleItem;
import dev.burikk.carrentz.app.api.service.user.vehicle.response.UserVehicleListResponse;
import dev.burikk.carrentz.app.entity.VehicleEntity;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users/")
public class UserVehicleService {
    @GET
    @Path("/vehicles")
    @RolesAllowed("UserEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@QueryParam("storeId") long storeId) throws Exception {
        UserVehicleListResponse userVehicleListResponse = new UserVehicleListResponse();

        WynixResults<VehicleEntity> vehicleEntities = DMLAssembler
                .create()
                .select("a.*")
                .select("b.name AS store_name")
                .select("c.name AS vehicle_type_name")
                .from("vehicles a")
                .join("stores b ON b.id = a.store_id", JoinType.INNER_JOIN)
                .join("vehicle_types c ON c.id = a.vehicle_type_id", JoinType.INNER_JOIN)
                .equalTo("a.store_id", storeId)
                .asc("a.name")
                .getWynixResults(VehicleEntity.class);

        for (VehicleEntity vehicleEntity : vehicleEntities) {
            UserVehicleItem userVehicleItem = new UserVehicleItem();

            userVehicleItem.setId(vehicleEntity.getId());
            userVehicleItem.setStoreId(vehicleEntity.getStoreId());
            userVehicleItem.setVehicleTypeId(vehicleEntity.getVehicleTypeId());
            userVehicleItem.setStoreName(vehicleEntity.getStoreName());
            userVehicleItem.setVehicleTypeName(vehicleEntity.getVehicleTypeName());
            userVehicleItem.setLicenseNumber(vehicleEntity.getLicenseNumber());
            userVehicleItem.setName(vehicleEntity.getName());
            userVehicleItem.setDescription(vehicleEntity.getDescription());
            userVehicleItem.setCostPerDay(vehicleEntity.getCostPerDay().longValue());
            userVehicleItem.setLateReturnFinePerDay(vehicleEntity.getLateReturnFinePerDay().longValue());

            userVehicleListResponse.getDetails().add(userVehicleItem);
        }

        return Response
                .ok(userVehicleListResponse)
                .build();
    }
}