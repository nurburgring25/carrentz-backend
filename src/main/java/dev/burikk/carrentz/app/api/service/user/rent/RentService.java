package dev.burikk.carrentz.app.api.service.user.rent;

import dev.burikk.carrentz.app.api.service.user.rent.item.UserRentItem;
import dev.burikk.carrentz.app.api.service.user.rent.item.UserRentStoreItem;
import dev.burikk.carrentz.app.api.service.user.rent.item.UserRentUserItem;
import dev.burikk.carrentz.app.api.service.user.rent.item.UserRentVehicleItem;
import dev.burikk.carrentz.app.api.service.user.rent.request.RentRequest;
import dev.burikk.carrentz.app.api.service.user.rent.response.UserRentListResponse;
import dev.burikk.carrentz.app.entity.PaymentEntity;
import dev.burikk.carrentz.app.entity.RentEntity;
import dev.burikk.carrentz.app.entity.VehicleEntity;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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
        UserRentListResponse userRentListResponse = new UserRentListResponse();

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
                .select("f.amount AS down_payment")
                .from("rents a")
                .join("users b ON b.id = a.user_id", JoinType.INNER_JOIN)
                .join("vehicles c ON c.id = a.vehicle_id", JoinType.INNER_JOIN)
                .join("stores d ON d.id = c.store_id", JoinType.INNER_JOIN)
                .join("vehicle_types e ON e.id = c.vehicle_type_id", JoinType.INNER_JOIN)
                .join("payments f ON f.rent_id = a.id", JoinType.INNER_JOIN)
                .equalTo("b.id", SessionManager.getInstance().getWynixUser().getIdentity())
                .and()
                .equalTo("f.type", Constant.PaymentType.DOWN_PAYMENT)
                .desc("a.start")
                .getWynixResults(HashEntity.class);

        for (HashEntity hashEntity : hashEntities) {
            UserRentItem userRentItem = new UserRentItem();

            userRentItem.setId(hashEntity.get("id"));

            {
                UserRentUserItem userRentUserItem = new UserRentUserItem();

                userRentUserItem.setId(hashEntity.get("user_id"));
                userRentUserItem.setName(hashEntity.get("user_name"));
                userRentUserItem.setPhoneNumber(hashEntity.get("phone_number"));

                userRentItem.setUser(userRentUserItem);
            }

            {
                UserRentVehicleItem userRentVehicleItem = new UserRentVehicleItem();

                userRentVehicleItem.setId(hashEntity.get("vehicle_id"));
                userRentVehicleItem.setVehicleType(hashEntity.get("vehicle_type"));
                userRentVehicleItem.setLicenseNumber(hashEntity.get("license_number"));
                userRentVehicleItem.setName(hashEntity.get("vehicle_name"));

                userRentItem.setVehicle(userRentVehicleItem);
            }

            {
                UserRentStoreItem userRentStoreItem = new UserRentStoreItem();

                userRentStoreItem.setId(hashEntity.get("store_id"));
                userRentStoreItem.setName(hashEntity.get("store_name"));

                userRentItem.setStore(userRentStoreItem);
            }

            userRentItem.setNumber(hashEntity.get("number"));
            userRentItem.setStatus(hashEntity.get("status"));
            userRentItem.setStart(hashEntity.get("start"));
            userRentItem.setUntil(hashEntity.get("until"));
            userRentItem.setDuration(hashEntity.get("duration"));
            userRentItem.setCostPerDay(hashEntity.get("cost_per_day"));
            userRentItem.setTotal(hashEntity.get("total"));
            userRentItem.setDownPayment(hashEntity.get("down_payment"));

            userRentListResponse.getDetails().add(userRentItem);
        }

        return Response
                .ok(userRentListResponse)
                .build();
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
            try (DMLManager dmlManager = new DMLManager()) {
                dmlManager.begin();

                RentEntity rentEntity = new RentEntity();

                rentEntity.markNew();
                rentEntity.setUserId(SessionManager.getInstance().getWynixUser().getIdentity());
                rentEntity.setVehicleId(rentRequest.getVehicleId());
                rentEntity.setNumber("TRX-" + StringUtils.upperCase(Long.toHexString(System.currentTimeMillis())));
                rentEntity.setStatus(Constant.DocumentStatus.OPENED);
                rentEntity.setStart(rentRequest.getStart());
                rentEntity.setUntil(rentRequest.getUntil());
                rentEntity.setDuration((int) (ChronoUnit.DAYS.between(rentEntity.getStart(), rentEntity.getUntil()) + 1));
                rentEntity.setCostPerDay(vehicleEntity.getCostPerDay());
                rentEntity.setTotal(vehicleEntity.getCostPerDay().multiply(new BigDecimal(rentEntity.getDuration())));

                Long rentId = dmlManager.store(rentEntity);

                PaymentEntity paymentEntity = new PaymentEntity();

                paymentEntity.markNew();
                paymentEntity.setRentId(rentId);
                paymentEntity.setType(Constant.PaymentType.DOWN_PAYMENT);
                paymentEntity.setAmount(rentEntity.getTotal().multiply(new BigDecimal(20)).divide(new BigDecimal(100), 0, RoundingMode.CEILING));

                dmlManager.store(paymentEntity);
                dmlManager.commit();

                return Response
                        .ok()
                        .build();
            }
        } else {
            throw new WynixException("Mobil dengan id " + rentRequest.getVehicleId() + " tidak dapat ditemukan.");
        }
    }

    @DELETE
    @Path("/rents/{id}")
    @RolesAllowed("UserEntity")
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
    @Path("/rents/{id}/take-the-car")
    @RolesAllowed("UserEntity")
    public Response takeTheCar (
            @PathParam("id") Long id,
            @QueryParam("code") String code
    ) throws Exception {
        RentEntity rentEntity = DMLManager.getEntity(RentEntity.class, id);

        if (rentEntity != null) {
            if (StringUtils.equals(rentEntity.getRentCode(), code)) {
                PaymentEntity downPayment = DMLManager.getWynixResultFromQuery(
                        "SELECT * FROM payments WHERE rent_id = ? AND type = ?",
                        PaymentEntity.class,
                        rentEntity.getId(),
                        Constant.PaymentType.DOWN_PAYMENT
                );

                if (downPayment != null) {
                    try (DMLManager dmlManager = new DMLManager()) {
                        dmlManager.begin();

                        rentEntity.markUpdate();
                        rentEntity.setStatus(Constant.DocumentStatus.ONGOING);

                        PaymentEntity paymentEntity = new PaymentEntity();

                        paymentEntity.markNew();
                        paymentEntity.setRentId(rentEntity.getId());
                        paymentEntity.setType(Constant.PaymentType.RENT);
                        paymentEntity.setAmount(rentEntity.getTotal().subtract(downPayment.getAmount()));

                        dmlManager.store(rentEntity);
                        dmlManager.store(paymentEntity);
                        dmlManager.commit();

                        return Response
                                .ok()
                                .build();
                    }
                }
            } else {
                throw new WynixException("Kode rental tidak sesuai.");
            }
        }

        throw new WynixException("Dokumen dengan id " + id + " tidak dapat ditemukan.");
    }

    @GET
    @Path("/rents/{id}/return-the-car")
    @RolesAllowed("UserEntity")
    public Response returnTheCar (
            @PathParam("id") Long id,
            @QueryParam("code") String code
    ) throws Exception {
        RentEntity rentEntity = DMLManager.getEntity(RentEntity.class, id);

        if (rentEntity != null) {
            if (StringUtils.equals(rentEntity.getReturnCode(), code)) {
                VehicleEntity vehicleEntity = DMLAssembler
                        .create()
                        .select("*")
                        .from("vehicles")
                        .equalTo("id", rentEntity.getVehicleId())
                        .getWynixResult(VehicleEntity.class);

                if (vehicleEntity != null) {
                    rentEntity.markUpdate();
                    rentEntity.setStatus(Constant.DocumentStatus.CLOSED);
                    rentEntity.setReturnAt(LocalDate.now());

                    int lateReturnDuration = (int) (ChronoUnit.DAYS.between(rentEntity.getUntil(), rentEntity.getReturnAt()));

                    if (lateReturnDuration > 0) {
                        rentEntity.setLateReturnDuration(lateReturnDuration);
                        rentEntity.setLateReturnFinePerDay(vehicleEntity.getLateReturnFinePerDay());
                        rentEntity.setLateReturnFine(vehicleEntity.getLateReturnFinePerDay().multiply(new BigDecimal(rentEntity.getLateReturnDuration())));
                    }

                    DMLManager.storeImmediately(rentEntity);

                    return Response
                            .ok()
                            .build();
                }
            } else {
                throw new WynixException("Kode rental tidak sesuai.");
            }
        }

        throw new WynixException("Dokumen dengan id " + id + " tidak dapat ditemukan.");
    }
}