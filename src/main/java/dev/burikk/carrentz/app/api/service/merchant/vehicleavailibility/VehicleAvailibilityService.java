package dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility;

import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.item.RentItem;
import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.item.StoreItem;
import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.item.VehicleItem;
import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.response.VehicleAvailibilityResourceResponse;
import dev.burikk.carrentz.app.api.service.merchant.vehicleavailibility.response.VehicleAvailibilityResponse;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.entity.HashEntity;
import org.apache.commons.lang3.SerializationUtils;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.LocalDate;

@Path("/merchants/")
public class VehicleAvailibilityService {
    @GET
    @Path("vehicle-availibilities")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@QueryParam("vehicleId") long vehicleId) throws Exception {
        VehicleAvailibilityResponse vehicleAvailibilityResponse = new VehicleAvailibilityResponse();

        WynixResults<HashEntity> bookedDates = DMLAssembler
                .create()
                .select(
                        "a.id",
                        "a.start",
                        "a.until"
                )
                .from("rents a")
                .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                .equalTo("a.vehicle_id", vehicleId)
                .and()
                .notEqualTo("a.status", Constant.DocumentStatus.CANCELLED)
                .and()
                .greaterThanOrEqualTo("a.start", LocalDate.now())
                .asc("a.start")
                .asc("a.until")
                .getWynixResults(HashEntity.class);

        LocalDate localDate = LocalDate.now();

        for (HashEntity bookedDate : bookedDates) {
            while (true) {
                if (localDate.compareTo(bookedDate.get("start")) >= 0 && localDate.compareTo(bookedDate.get("until")) <= 0) {
                    RentItem rentItem = new RentItem();

                    rentItem.setId(bookedDate.get("id"));
                    rentItem.setDate(SerializationUtils.clone(localDate));

                    vehicleAvailibilityResponse.getRentItems().add(rentItem);
                }

                if (localDate.compareTo(bookedDate.get("until")) == 0) {
                    localDate = localDate.plusDays(1);

                    break;
                }

                localDate = localDate.plusDays(1);
            }
        }

        return Response
                .ok(vehicleAvailibilityResponse)
                .build();
    }

    @GET
    @Path("/vehicle-availibilities/resource")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resource() throws Exception {
        VehicleAvailibilityResourceResponse vehicleAvailibilityResourceResponse = new VehicleAvailibilityResourceResponse();

        {
            WynixResults<HashEntity> hashEntities = DMLManager.getWynixResultsFromQuery(
                    "SELECT id, name FROM stores WHERE merchant_id = ?",
                    HashEntity.class,
                    SessionManager.getInstance().getMerchantId()
            );

            for (HashEntity hashEntity : hashEntities) {
                StoreItem storeItem = new StoreItem();

                storeItem.setId(hashEntity.get("id"));
                storeItem.setDescription(hashEntity.get("name"));

                vehicleAvailibilityResourceResponse.getStoreItems().add(storeItem);
            }
        }

        {
            WynixResults<HashEntity> hashEntities = DMLManager.getWynixResultsFromQuery(
                    "SELECT a.id, a.store_id, (a.name || ' (' || a.license_number || ')') AS description FROM vehicles a INNER JOIN stores b ON b.id = a.store_id WHERE b.merchant_id = ?",
                    HashEntity.class,
                    SessionManager.getInstance().getMerchantId()
            );

            for (HashEntity hashEntity : hashEntities) {
                VehicleItem vehicleItem = new VehicleItem();

                vehicleItem.setId(hashEntity.get("id"));
                vehicleItem.setStoreId(hashEntity.get("store_id"));
                vehicleItem.setDescription(hashEntity.get("description"));

                vehicleAvailibilityResourceResponse.getVehicleItems().add(vehicleItem);
            }
        }

        return Response
                .ok(vehicleAvailibilityResourceResponse)
                .build();
    }
}