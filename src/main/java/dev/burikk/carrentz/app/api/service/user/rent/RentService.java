package dev.burikk.carrentz.app.api.service.user.rent;

import dev.burikk.carrentz.app.api.service.user.rent.request.RentRequest;
import dev.burikk.carrentz.app.entity.RentEntity;
import dev.burikk.carrentz.app.entity.VehicleEntity;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.entity.HashEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.time.Period;

/**
 * @author Muhammad Irfan
 * @since 18/02/2022 14.03
 */
@Path("/users/")
public class RentService {
    @GET
    @Path("/rents")
    @RolesAllowed("UserEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws Exception {
        WynixResults<HashEntity> hashEntities = DMLAssembler
                .create()
                .getWynixResults(HashEntity.class);
    }

    @POST
    @Path("/rents")
    @RolesAllowed("UserEntity")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response post(RentRequest rentRequest) throws Exception {
        VehicleEntity vehicleEntity = DMLAssembler
                .create()
                .select("*")
                .from("vehicles")
                .equalTo("id", rentRequest.getVehicleId())
                .getWynixResult(VehicleEntity.class);

        if (vehicleEntity != null) {
            RentEntity rentEntity = new RentEntity();

            rentEntity.markNew();
            rentEntity.setUserId(SessionManager.getInstance().getWynixUser().getIdentity());
            rentEntity.setVehicleId(rentRequest.getVehicleId());
            rentEntity.setStatus(Constant.DocumentStatus.OPENED);
            rentEntity.setStart(rentRequest.getStart());
            rentEntity.setUntil(rentRequest.getUntil());
            rentEntity.setDuration(Period.between(rentEntity.getStart(), rentEntity.getUntil()).getDays());
            rentEntity.setCostPerDay(vehicleEntity.getCostPerDay());
            rentEntity.setTotal(vehicleEntity.getCostPerDay().multiply(new BigDecimal(rentEntity.getDuration())));

            DMLManager.storeImmediately(rentEntity);

            return Response
                    .ok()
                    .build();
        } else {
            throw new Exception("Mobil dengan id " + rentRequest.getVehicleId() + " tidak dapat ditemukan.");
        }
    }
}