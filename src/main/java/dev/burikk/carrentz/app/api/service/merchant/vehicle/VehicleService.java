package dev.burikk.carrentz.app.api.service.merchant.vehicle;

import dev.burikk.carrentz.app.api.item.LovItem;
import dev.burikk.carrentz.app.api.service.merchant.vehicle.item.VehicleImageItem;
import dev.burikk.carrentz.app.api.service.merchant.vehicle.item.VehicleItem;
import dev.burikk.carrentz.app.api.service.merchant.vehicle.response.VehicleListResponse;
import dev.burikk.carrentz.app.api.service.merchant.vehicle.response.VehicleResourceResponse;
import dev.burikk.carrentz.app.entity.VehicleEntity;
import dev.burikk.carrentz.app.entity.VehicleImageEntity;
import dev.burikk.carrentz.engine.common.SessionManager;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLAssembler;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.datasource.enumeration.JoinType;
import dev.burikk.carrentz.engine.entity.HashEntity;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.media.multipart.*;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;

@Path("/merchants/")
public class VehicleService {
    @GET
    @Path("/vehicles")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws Exception {
        VehicleListResponse vehicleListResponse = new VehicleListResponse();

        WynixResults<VehicleEntity> vehicleEntities = DMLAssembler
                .create()
                .select("a.*")
                .select("b.name AS store_name")
                .select("c.name AS vehicle_type_name")
                .from("vehicles a")
                .join("stores b ON b.id = a.store_id", JoinType.INNER_JOIN)
                .join("vehicle_types c ON c.id = a.vehicle_type_id", JoinType.INNER_JOIN)
                .equalTo("b.merchant_id", SessionManager.getInstance().getMerchantId())
                .asc("a.name")
                .getWynixResults(VehicleEntity.class);

        for (VehicleEntity vehicleEntity : vehicleEntities) {
            VehicleItem vehicleItem = new VehicleItem();

            vehicleItem.setId(vehicleEntity.getId());
            vehicleItem.setStoreId(vehicleEntity.getStoreId());
            vehicleItem.setVehicleTypeId(vehicleEntity.getVehicleTypeId());
            vehicleItem.setStoreName(vehicleEntity.getStoreName());
            vehicleItem.setVehicleTypeName(vehicleEntity.getVehicleTypeName());
            vehicleItem.setLicenseNumber(vehicleEntity.getLicenseNumber());
            vehicleItem.setName(vehicleEntity.getName());
            vehicleItem.setDescription(vehicleEntity.getDescription());
            vehicleItem.setCostPerDay(vehicleEntity.getCostPerDay().longValue());
            vehicleItem.setLateReturnFinePerDay(vehicleEntity.getLateReturnFinePerDay().longValue());

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
                VehicleImageItem vehicleImageItem = new VehicleImageItem();

                vehicleImageItem.setId(hashEntity.get("id"));
                vehicleImageItem.setThumbnail(hashEntity.get("thumbnail"));
                vehicleImageItem.setUrl("http://192.168.100.76:8080/carrentz/api/merchants/vehicles/images/" + vehicleImageItem.getId());

                vehicleItem.getImages().add(vehicleImageItem);
            }

            vehicleListResponse.getDetails().add(vehicleItem);
        }

        return Response
                .ok(vehicleListResponse)
                .build();
    }

    @POST
    @Path("/vehicles")
    @RolesAllowed("OwnerEntity")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response post(
            @FormDataParam("images") List<FormDataBodyPart> formDataBodyParts,
            @FormDataParam("data") VehicleItem vehicleItem
    ) throws Exception {
        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();

            VehicleEntity vehicleEntity = new VehicleEntity();

            vehicleEntity.markNew();
            vehicleEntity.setStoreId(vehicleItem.getStoreId());
            vehicleEntity.setVehicleTypeId(vehicleItem.getVehicleTypeId());
            vehicleEntity.setLicenseNumber(vehicleItem.getLicenseNumber());
            vehicleEntity.setName(vehicleItem.getName());
            vehicleEntity.setDescription(vehicleItem.getDescription());
            vehicleEntity.setCostPerDay(new BigDecimal(vehicleItem.getCostPerDay()));
            vehicleEntity.setLateReturnFinePerDay(new BigDecimal(vehicleItem.getLateReturnFinePerDay()));

            long id = dmlManager.store(vehicleEntity);

            for (FormDataBodyPart formDataBodyPart : formDataBodyParts) {
                FormDataContentDisposition formDataContentDisposition = (FormDataContentDisposition) formDataBodyPart.getContentDisposition();
                InputStream inputStream = formDataBodyPart.getValueAs(InputStream.class);

                VehicleImageEntity vehicleImageEntity = new VehicleImageEntity();

                vehicleImageEntity.markNew();
                vehicleImageEntity.setVehicleId(id);
                vehicleImageEntity.setImage(IOUtils.toByteArray(inputStream));
                vehicleImageEntity.setThumbnail(StringUtils.startsWith(formDataContentDisposition.getFileName(), "**"));

                vehicleEntity.getVehicleImageEntities().add(vehicleImageEntity);
            }

            dmlManager.store(vehicleEntity.getVehicleImageEntities());
            dmlManager.commit();
        }

        return Response
                .ok()
                .build();
    }

    @PUT
    @Path("/vehicles/{id}")
    @RolesAllowed("OwnerEntity")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response put(
            @PathParam("id") long id,
            @FormDataParam("images") List<FormDataBodyPart> formDataBodyParts,
            @FormDataParam("data") VehicleItem vehicleItem
    ) throws Exception {
        VehicleEntity vehicleEntity = DMLManager.getEntity(VehicleEntity.class, id);

        if (vehicleEntity == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();

            vehicleEntity.markUpdate();
            vehicleEntity.setStoreId(vehicleItem.getStoreId());
            vehicleEntity.setVehicleTypeId(vehicleItem.getVehicleTypeId());
            vehicleEntity.setLicenseNumber(vehicleItem.getLicenseNumber());
            vehicleEntity.setName(vehicleItem.getName());
            vehicleEntity.setDescription(vehicleItem.getDescription());
            vehicleEntity.setCostPerDay(new BigDecimal(vehicleItem.getCostPerDay()));
            vehicleEntity.setLateReturnFinePerDay(new BigDecimal(vehicleItem.getLateReturnFinePerDay()));
            vehicleEntity.getVehicleImageEntities().markAllDelete();

            for (FormDataBodyPart formDataBodyPart : formDataBodyParts) {
                FormDataContentDisposition formDataContentDisposition = (FormDataContentDisposition) formDataBodyPart.getContentDisposition();
                InputStream inputStream = formDataBodyPart.getValueAs(InputStream.class);

                VehicleImageEntity vehicleImageEntity = new VehicleImageEntity();

                vehicleImageEntity.markNew();
                vehicleImageEntity.setVehicleId(id);
                vehicleImageEntity.setImage(IOUtils.toByteArray(inputStream));
                vehicleImageEntity.setThumbnail(StringUtils.startsWith(formDataContentDisposition.getFileName(), "**"));

                vehicleEntity.getVehicleImageEntities().add(vehicleImageEntity);
            }

            dmlManager.store(vehicleEntity);
            dmlManager.store(vehicleEntity.getVehicleImageEntities());
            dmlManager.commit();
        }

        return Response
                .ok()
                .build();
    }

    @DELETE
    @Path("/vehicles/{id}")
    @RolesAllowed("OwnerEntity")
    public Response post(@PathParam("id") long id) throws Exception {
        VehicleEntity vehicleEntity = DMLManager.getEntity(VehicleEntity.class, id);

        if (vehicleEntity == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).build());
        }

        vehicleEntity.markDelete();
        vehicleEntity.getVehicleImageEntities().markAllDelete();

        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();
            dmlManager.store(vehicleEntity.getVehicleImageEntities());
            dmlManager.store(vehicleEntity);
            dmlManager.commit();
        }

        return Response
                .ok()
                .build();
    }

    @GET
    @Path("/vehicles/resource")
    @RolesAllowed("OwnerEntity")
    @Produces(MediaType.APPLICATION_JSON)
    public Response lov() throws Exception {
        VehicleResourceResponse vehicleResourceResponse = new VehicleResourceResponse();

        {
            WynixResults<HashEntity> hashEntities = DMLManager.getWynixResultsFromQuery(
                    "SELECT id, name FROM stores WHERE merchant_id = ?",
                    HashEntity.class,
                    SessionManager.getInstance().getMerchantId()
            );

            for (HashEntity hashEntity : hashEntities) {
                LovItem lovItem = new LovItem();

                lovItem.setIdentifier(hashEntity.get("id"));
                lovItem.setDescription(hashEntity.get("name"));

                vehicleResourceResponse.getStores().add(lovItem);
            }
        }

        {
            WynixResults<HashEntity> hashEntities = DMLManager.getWynixResultsFromQuery(
                    "SELECT id, name FROM vehicle_types",
                    HashEntity.class
            );

            for (HashEntity hashEntity : hashEntities) {
                LovItem lovItem = new LovItem();

                lovItem.setIdentifier(hashEntity.get("id"));
                lovItem.setDescription(hashEntity.get("name"));

                vehicleResourceResponse.getVehicleTypes().add(lovItem);
            }
        }

        return Response
                .ok(vehicleResourceResponse)
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