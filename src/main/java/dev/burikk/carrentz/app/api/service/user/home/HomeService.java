package dev.burikk.carrentz.app.api.service.user.home;

import dev.burikk.carrentz.app.api.service.user.home.item.HomeVehicleTypeItem;
import dev.burikk.carrentz.app.api.service.user.home.response.HomeVehicleTypeResponse;
import dev.burikk.carrentz.engine.common.WynixResults;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.entity.HashEntity;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/users/homes/")
public class HomeService {
    @GET
    @Path("/vehicle-type")
    @Produces(MediaType.APPLICATION_JSON)
    public Response vehicleType() throws Exception {
        HomeVehicleTypeResponse homeVehicleTypeResponse = new HomeVehicleTypeResponse();

        WynixResults<HashEntity> hashEntities = DMLManager.getWynixResultsFromQuery(
                "SELECT id, name, image FROM vehicle_types",
                HashEntity.class
        );

        for (HashEntity hashEntity : hashEntities) {
            HomeVehicleTypeItem homeVehicleTypeItem = new HomeVehicleTypeItem();

            homeVehicleTypeItem.setId(hashEntity.get("id"));
            homeVehicleTypeItem.setName(hashEntity.get("name"));
            homeVehicleTypeItem.setImage(hashEntity.get("image"));

            homeVehicleTypeResponse.getDetails().add(homeVehicleTypeItem);
        }

        return Response
                .ok(homeVehicleTypeResponse)
                .build();
    }
}