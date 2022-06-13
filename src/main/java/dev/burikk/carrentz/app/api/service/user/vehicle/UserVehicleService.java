package dev.burikk.carrentz.app.api.service.user.vehicle;

import dev.burikk.carrentz.app.api.service.user.vehicle.item.UserVehicleImageItem;
import dev.burikk.carrentz.app.api.service.user.vehicle.item.UserVehicleItem;
import dev.burikk.carrentz.app.api.service.user.vehicle.response.UserVehicleListResponse;
import dev.burikk.carrentz.app.entity.VehicleEntity;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.entity.HashEntity;
import org.apache.commons.lang3.SerializationUtils;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayInputStream;
import java.net.URLConnection;
import java.time.LocalDate;
import java.util.Arrays;

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

            WynixResults<HashEntity> hashEntities = DMLAssembler
                    .create()
                    .select(
                            "id",
                            "thumbnail"
                    )
                    .from("vehicle_images")
                    .equalTo("vehicle_id", vehicleEntity.getId())
                    .getWynixResults(HashEntity.class);

            for (HashEntity hashEntity : hashEntities) {
                UserVehicleImageItem userVehicleImageItem = new UserVehicleImageItem();

                userVehicleImageItem.setId(hashEntity.get("id"));
                userVehicleImageItem.setThumbnail(hashEntity.get("thumbnail"));
                userVehicleImageItem.setUrl("http://192.168.100.76:8080/carrentz/api/users/vehicles/images/" + userVehicleImageItem.getId());

                userVehicleItem.getImages().add(userVehicleImageItem);
            }

            userVehicleListResponse.getDetails().add(userVehicleItem);

            WynixResults<HashEntity> bookedDates = DMLAssembler
                    .create()
                    .select(
                            "start",
                            "until"
                    )
                    .from("rents a")
                    .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                    .equalTo("a.vehicle_id", vehicleEntity.getId())
                    .and()
                    .notEqualTo("a.status", Constant.DocumentStatus.CANCELLED)
                    .and()
                    .greaterThanOrEqualTo("start", LocalDate.now())
                    .asc("start")
                    .asc("until")
                    .getWynixResults(HashEntity.class);

            LocalDate localDate = LocalDate.now();

            for (HashEntity bookedDate : bookedDates) {
                while (true) {
                    if (localDate.compareTo(bookedDate.get("start")) >= 0 && localDate.compareTo(bookedDate.get("until")) <= 0) {
                        userVehicleItem.getBookedDates().add(SerializationUtils.clone(localDate));
                    }

                    if (localDate.compareTo(bookedDate.get("until")) == 0) {
                        localDate = localDate.plusDays(1);

                        break;
                    }

                    localDate = localDate.plusDays(1);
                }
            }
        }

        return Response
                .ok(userVehicleListResponse)
                .build();
    }

    @GET
    @PermitAll
    @Path("/vehicles/images/{id}")
    public Response image(
            @Context Request request,
            @PathParam("id") long id
    ) throws Exception {
        byte[] bytes = DMLManager.getObjectFromQuery(
                "SELECT image FROM vehicle_images WHERE id = ?",
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