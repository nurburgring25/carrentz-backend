package dev.burikk.carrentz.app.api.service.merchant.report;

import dev.burikk.carrentz.app.api.service.merchant.report.item.*;
import dev.burikk.carrentz.app.api.service.merchant.report.response.*;
import dev.burikk.carrentz.engine.common.Constant;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.entity.HashEntity;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.Instant;
import java.time.ZoneId;

@Path("/merchants/")
public class ReportService {
    @GET
    @Path("/reports/daily-rents")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response dailyRents(
            @QueryParam("start") long start,
            @QueryParam("until") long until
    ) throws Exception {
        DailyRentResponse dailyRentResponse = new DailyRentResponse();

        WynixResults<HashEntity> hashEntities = DMLManager.getWynixResultsFromQuery(
                "SELECT date::DATE AS date,\n" +
                        "COALESCE(SUM(CASE WHEN b.type = ? THEN b.amount ELSE 0 END), 0) AS down_payment,\n" +
                        "COALESCE(SUM(CASE WHEN b.type = ? THEN b.amount ELSE 0 END), 0) AS amount,\n" +
                        "COALESCE(SUM(CASE WHEN b.type = ? THEN b.amount ELSE 0 END), 0) AS late_return_fine\n" +
                        "FROM GENERATE_SERIES(CURRENT_DATE - INTERVAL '6' DAY, CURRENT_DATE, '1 DAY') AS date\n" +
                        "LEFT JOIN (\n" +
                        "SELECT d.created::DATE AS tanggal,\n" +
                        "d.amount AS amount,\n" +
                        "d.type AS type\n" +
                        "FROM rents a\n" +
                        "INNER JOIN vehicles b ON b.id = a.vehicle_id\n" +
                        "INNER JOIN stores c ON c.id = b.store_id\n" +
                        "INNER JOIN payments d ON d.rent_id = a.id\n" +
                        "WHERE c.merchant_id = ?\n" +
                        ") b ON b.tanggal = date::DATE\n" +
                        "GROUP BY date::DATE\n" +
                        "ORDER BY date::DATE;",
                HashEntity.class,
                Constant.PaymentType.DOWN_PAYMENT,
                Constant.PaymentType.RENT,
                Constant.PaymentType.LATE_RETURN_FINE,
                SessionManager.getInstance().getMerchantId()
        );

        for (HashEntity hashEntity : hashEntities) {
            DailyRentItem dailyRentItem = new DailyRentItem();

            dailyRentItem.setDate(hashEntity.get("date"));
            dailyRentItem.setDownPayment(hashEntity.get("down_payment"));
            dailyRentItem.setAmount(hashEntity.get("amount"));
            dailyRentItem.setLateReturnFine(hashEntity.get("late_return_fine"));

            dailyRentResponse.getDailyRentItems().add(dailyRentItem);
        }

        return Response
                .ok(dailyRentResponse)
                .build();
    }

    @GET
    @Path("/reports/rent-by-vehicles")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rentByVehicles(
            @QueryParam("start") long start,
            @QueryParam("until") long until
    ) throws Exception {
        RentByVehicleResponse rentByVehicleResponse = new RentByVehicleResponse();

        WynixResults<HashEntity> hashEntities = DMLAssembler
                .create()
                .select(
                        "e.id AS image_id",
                        "b.name AS name",
                        "d.name AS vehicle_type_name",
                        "b.license_number AS license_number",
                        "SUM(a.duration) AS duration",
                        "SUM(a.total) AS amount"
                )
                .from("rents a")
                .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                .join("vehicle_types d ON d.id = b.vehicle_type_id", JoinType.INNER_JOIN)
                .join("vehicle_images e ON e.vehicle_id = b.id AND e.thumbnail = TRUE", JoinType.INNER_JOIN)
                .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                .and()
                .notEqualTo("a.status", Constant.DocumentStatus.CANCELLED)
                .and()
                .greaterThanOrEqualTo("a.created::DATE", Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate())
                .and()
                .lessThanOrEqualTo("a.created::DATE", Instant.ofEpochMilli(until).atZone(ZoneId.systemDefault()).toLocalDate())
                .groupBy(
                        "e.id",
                        "b.name",
                        "d.name",
                        "b.license_number"
                )
                .desc("b.name")
                .getWynixResults(HashEntity.class);

        for (HashEntity hashEntity : hashEntities) {
            RentByVehicleItem rentByVehicleItem = new RentByVehicleItem();

            rentByVehicleItem.setName(hashEntity.get("name"));
            rentByVehicleItem.setVehicleTypeName(hashEntity.get("vehicle_type_name"));
            rentByVehicleItem.setLicenseNumber(hashEntity.get("license_number"));
            rentByVehicleItem.setImageUrl(Constant.Application.BASE_URL + "merchants/vehicles/images/" + hashEntity.get("image_id"));
            rentByVehicleItem.setDuration(hashEntity.get("duration"));
            rentByVehicleItem.setAmount(hashEntity.get("amount"));

            rentByVehicleResponse.getRentByVehicleItems().add(rentByVehicleItem);
        }

        return Response
                .ok(rentByVehicleResponse)
                .build();
    }

    @GET
    @Path("/reports/rent-by-vehicle-types")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rentByVehicleTypes(
            @QueryParam("start") long start,
            @QueryParam("until") long until
    ) throws Exception {
        RentByVehicleTypeResponse rentByVehicleTypeResponse = new RentByVehicleTypeResponse();

        WynixResults<HashEntity> hashEntities = DMLAssembler
                .create()
                .select(
                        "d.name AS name",
                        "SUM(a.duration) AS duration",
                        "SUM(a.total) AS amount"
                )
                .from("rents a")
                .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                .join("vehicle_types d ON d.id = b.vehicle_type_id", JoinType.INNER_JOIN)
                .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                .and()
                .notEqualTo("a.status", Constant.DocumentStatus.CANCELLED)
                .and()
                .greaterThanOrEqualTo("a.created::DATE", Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate())
                .and()
                .lessThanOrEqualTo("a.created::DATE", Instant.ofEpochMilli(until).atZone(ZoneId.systemDefault()).toLocalDate())
                .groupBy(
                        "d.id",
                        "d.name"
                )
                .desc("d.name")
                .getWynixResults(HashEntity.class);

        for (HashEntity hashEntity : hashEntities) {
            RentByVehicleTypeItem rentByVehicleTypeItem = new RentByVehicleTypeItem();

            rentByVehicleTypeItem.setName(hashEntity.get("name"));
            rentByVehicleTypeItem.setDuration(hashEntity.get("duration"));
            rentByVehicleTypeItem.setAmount(hashEntity.get("amount"));

            rentByVehicleTypeResponse.getRentByVehicleTypeItems().add(rentByVehicleTypeItem);
        }

        return Response
                .ok(rentByVehicleTypeResponse)
                .build();
    }

    @GET
    @Path("/reports/rent-by-stores")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rentByStores(
            @QueryParam("start") long start,
            @QueryParam("until") long until
    ) throws Exception {
        RentByStoreResponse rentByStoreResponse = new RentByStoreResponse();

        WynixResults<HashEntity> hashEntities = DMLAssembler
                .create()
                .select(
                        "c.id AS id",
                        "c.name AS name",
                        "c.phone_number AS phone_number",
                        "c.address AS address",
                        "c.city AS city",
                        "SUM(d.amount) AS amount"
                )
                .from("rents a")
                .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                .join("payments d ON d.rent_id = a.id", JoinType.INNER_JOIN)
                .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                .and()
                .greaterThanOrEqualTo("d.created::DATE", Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate())
                .and()
                .lessThanOrEqualTo("d.created::DATE", Instant.ofEpochMilli(until).atZone(ZoneId.systemDefault()).toLocalDate())
                .groupBy(
                        "c.id",
                        "c.name",
                        "c.phone_number",
                        "c.address",
                        "c.city"
                )
                .desc("c.name")
                .getWynixResults(HashEntity.class);

        for (HashEntity hashEntity : hashEntities) {
            RentByStoreItem rentByStoreItem = new RentByStoreItem();

            rentByStoreItem.setName(hashEntity.get("name"));
            rentByStoreItem.setPhoneNumber(hashEntity.get("phone_number"));
            rentByStoreItem.setAddress(hashEntity.get("address"));
            rentByStoreItem.setCity(hashEntity.get("city"));
            rentByStoreItem.setImageUrl(Constant.Application.BASE_URL + "merchants/stores/images/" + hashEntity.get("id"));
            rentByStoreItem.setAmount(hashEntity.get("amount"));

            rentByStoreResponse.getRentByStoreItems().add(rentByStoreItem);
        }

        return Response
                .ok(rentByStoreResponse)
                .build();
    }

    @GET
    @Path("/reports/rent-by-customers")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rentByCustomers(
            @QueryParam("start") long start,
            @QueryParam("until") long until
    ) throws Exception {
        RentByCustomerResponse rentByCustomerResponse = new RentByCustomerResponse();

        WynixResults<HashEntity> hashEntities = DMLAssembler
                .create()
                .select(
                        "e.name AS name",
                        "e.phone_number AS phone_number",
                        "e.id AS email_address",
                        "SUM(d.amount) AS amount"
                )
                .from("rents a")
                .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                .join("payments d ON d.rent_id = a.id", JoinType.INNER_JOIN)
                .join("users e ON e.id = a.user_id", JoinType.INNER_JOIN)
                .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                .and()
                .greaterThanOrEqualTo("d.created::DATE", Instant.ofEpochMilli(start).atZone(ZoneId.systemDefault()).toLocalDate())
                .and()
                .lessThanOrEqualTo("d.created::DATE", Instant.ofEpochMilli(until).atZone(ZoneId.systemDefault()).toLocalDate())
                .groupBy(
                        "e.name",
                        "e.phone_number",
                        "e.id"
                )
                .desc("e.name")
                .getWynixResults(HashEntity.class);

        for (HashEntity hashEntity : hashEntities) {
            RentByCustomerItem rentByCustomerItem = new RentByCustomerItem();

            rentByCustomerItem.setName(hashEntity.get("name"));
            rentByCustomerItem.setPhoneNumber(hashEntity.get("phone_number"));
            rentByCustomerItem.setEmailAddress(hashEntity.get("email_address"));
            rentByCustomerItem.setAmount(hashEntity.get("amount"));

            rentByCustomerResponse.getRentByCustomerItems().add(rentByCustomerItem);
        }

        return Response
                .ok(rentByCustomerResponse)
                .build();
    }
}