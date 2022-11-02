package dev.burikk.carrentz.app.api.service.merchant.dashboard;

import dev.burikk.carrentz.app.api.service.merchant.dashboard.response.DashboardResponse;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Path("/merchants/")
public class DashboardService {
    @GET
    @Path("/dashboard")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws Exception {
        DashboardResponse dashboardResponse = new DashboardResponse();

        {
            WynixResults<HashEntity> hashEntities = DMLManager.getWynixResultsFromQuery(
                    "SELECT date::DATE AS date,\n" +
                            "COALESCE(SUM(b.amount), 0) AS amount\n" +
                            "FROM GENERATE_SERIES(CURRENT_DATE - INTERVAL '6' DAY, CURRENT_DATE, '1 DAY') AS date\n" +
                            "LEFT JOIN (\n" +
                            "SELECT d.created::DATE AS tanggal,\n" +
                            "d.amount AS amount\n" +
                            "FROM rents a\n" +
                            "INNER JOIN vehicles b ON b.id = a.vehicle_id\n" +
                            "INNER JOIN stores c ON c.id = b.store_id\n" +
                            "INNER JOIN payments d ON d.rent_id = a.id\n" +
                            "WHERE c.merchant_id = ?\n" +
                            ") b ON b.tanggal = date::DATE\n" +
                            "GROUP BY date::DATE\n" +
                            "ORDER BY date::DATE",
                    HashEntity.class,
                    SessionManager.getInstance().getMerchantId()
            );

            BigDecimal today = BigDecimal.ZERO;
            BigDecimal yesterday = BigDecimal.ZERO;

            for (HashEntity hashEntity : hashEntities) {
                dashboardResponse.getLast7DaysIncomingPayment().put(hashEntity.get("date"), hashEntity.get("amount"));

                if (LocalDate.now().isEqual(hashEntity.get("date"))) {
                    today = hashEntity.get("amount");
                }

                if (LocalDate.now().minusDays(1).isEqual(hashEntity.get("date"))) {
                    yesterday = hashEntity.get("amount");
                }
            }

            dashboardResponse.setIncomingPayment(today);
            dashboardResponse.setIncomingPaymentDifferenceAmount(today.subtract(yesterday));

            if (yesterday.compareTo(BigDecimal.ZERO) != 0) {
                dashboardResponse.setIncomingPaymentDifferencePercentage(
                        dashboardResponse.getIncomingPaymentDifferenceAmount()
                                .divide(yesterday, 2, RoundingMode.CEILING)
                                .multiply(new BigDecimal(100))
                                .setScale(0, RoundingMode.CEILING)
                );
            } else {
                dashboardResponse.setIncomingPaymentDifferencePercentage(new BigDecimal(100));
            }

            if (dashboardResponse.getIncomingPaymentDifferencePercentage().compareTo(new BigDecimal(100)) > 0) {
                dashboardResponse.setIncomingPaymentDifferencePercentage(new BigDecimal(100));
            } else if (dashboardResponse.getIncomingPaymentDifferencePercentage().compareTo(new BigDecimal(-100)) < 0) {
                dashboardResponse.setIncomingPaymentDifferencePercentage(new BigDecimal(-100));
            }
        }

        {
            HashEntity hashEntity = DMLAssembler
                    .create()
                    .select(
                            "b.name AS name",
                            "SUM(a.duration) AS duration"
                    )
                    .from("rents a")
                    .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                    .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                    .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                    .and()
                    .notEqualTo("a.status", Constant.DocumentStatus.CANCELLED)
                    .and()
                    .customWhere("a.created::DATE >= (CURRENT_DATE - INTERVAL '6' DAY)")
                    .and()
                    .customWhere("a.created::DATE <= CURRENT_DATE")
                    .groupBy(
                            "b.id",
                            "b.name"
                    )
                    .desc("SUM(a.duration)")
                    .limit(1)
                    .getWynixResult(HashEntity.class);

            if (hashEntity != null) {
                dashboardResponse.setMostFavoriteVehicle(hashEntity.get("name"));
                dashboardResponse.setMostFavoriteVehicleValue(hashEntity.<Long>get("duration").intValue());
            }
        }

        {
            HashEntity hashEntity = DMLAssembler
                    .create()
                    .select(
                            "d.name AS name",
                            "SUM(a.duration) AS duration"
                    )
                    .from("rents a")
                    .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                    .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                    .join("vehicle_types d ON d.id = b.vehicle_type_id", JoinType.INNER_JOIN)
                    .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                    .and()
                    .notEqualTo("a.status", Constant.DocumentStatus.CANCELLED)
                    .and()
                    .customWhere("a.created::DATE >= (CURRENT_DATE - INTERVAL '6' DAY)")
                    .and()
                    .customWhere("a.created::DATE <= CURRENT_DATE")
                    .groupBy(
                            "d.id",
                            "d.name"
                    )
                    .desc("SUM(a.duration)")
                    .limit(1)
                    .getWynixResult(HashEntity.class);

            if (hashEntity != null) {
                dashboardResponse.setMostFavoriteVehicleType(hashEntity.get("name"));
                dashboardResponse.setMostFavoriteVehicleTypeValue(hashEntity.get("duration"));
            }
        }

        {
            HashEntity hashEntity = DMLAssembler
                    .create()
                    .select(
                            "c.name AS name",
                            "SUM(d.amount) AS amount"
                    )
                    .from("rents a")
                    .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                    .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                    .join("payments d ON d.rent_id = a.id", JoinType.INNER_JOIN)
                    .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                    .and()
                    .customWhere("d.created::DATE >= (CURRENT_DATE - INTERVAL '6' DAY)")
                    .and()
                    .customWhere("d.created::DATE <= CURRENT_DATE")
                    .groupBy(
                            "c.id",
                            "c.name"
                    )
                    .desc("SUM(d.amount)")
                    .limit(1)
                    .getWynixResult(HashEntity.class);

            if (hashEntity != null) {
                dashboardResponse.setMostFavoriteStore(hashEntity.get("name"));
                dashboardResponse.setMostFavoriteStoreValue(hashEntity.get("amount"));
            }
        }

        {
            HashEntity hashEntity = DMLAssembler
                    .create()
                    .select(
                            "e.name AS name",
                            "SUM(d.amount) AS amount"
                    )
                    .from("rents a")
                    .join("vehicles b ON b.id = a.vehicle_id", JoinType.INNER_JOIN)
                    .join("stores c ON c.id = b.store_id", JoinType.INNER_JOIN)
                    .join("payments d ON d.rent_id = a.id", JoinType.INNER_JOIN)
                    .join("users e ON e.id = a.user_id", JoinType.INNER_JOIN)
                    .equalTo("c.merchant_id", SessionManager.getInstance().getMerchantId())
                    .and()
                    .customWhere("d.created::DATE >= (CURRENT_DATE - INTERVAL '6' DAY)")
                    .and()
                    .customWhere("d.created::DATE <= CURRENT_DATE")
                    .groupBy(
                            "e.id",
                            "e.name"
                    )
                    .desc("SUM(d.amount)")
                    .limit(1)
                    .getWynixResult(HashEntity.class);

            if (hashEntity != null) {
                dashboardResponse.setMostFavoriteCustomer(hashEntity.get("name"));
                dashboardResponse.setMostFavoriteCustomerValue(hashEntity.get("amount"));
            }
        }

        return Response
                .ok(dashboardResponse)
                .build();
    }
}