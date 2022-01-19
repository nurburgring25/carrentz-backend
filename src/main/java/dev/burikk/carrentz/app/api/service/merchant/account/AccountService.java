package dev.burikk.carrentz.app.api.service.merchant.account;

import dev.burikk.carrentz.app.api.service.merchant.account.request.RegisterRequest;
import dev.burikk.carrentz.app.entity.MerchantEntity;
import dev.burikk.carrentz.app.entity.OwnerEntity;
import dev.burikk.carrentz.app.entity.StoreEntity;
import dev.burikk.carrentz.engine.datasource.DMLManager;
import dev.burikk.carrentz.engine.security.Digest;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author Muhammad Irfan
 * @since 19/01/2022 12.27
 */
@Path("/api/merchants/")
public class AccountService {
    @POST
    @Path("/register")
    @PermitAll
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(RegisterRequest registerRequest) throws Exception {
        try (DMLManager dmlManager = new DMLManager()) {
            dmlManager.begin();

            // Masukkan data merchant
            MerchantEntity merchantEntity = new MerchantEntity();

            merchantEntity.setName(registerRequest.getBusinessName());

            Long merchantId = dmlManager.store(merchantEntity);

            // Masukkan data cabang
            StoreEntity storeEntity = new StoreEntity();

            storeEntity.setMerchantId(merchantId);
            storeEntity.setName(registerRequest.getBusinessName());
            storeEntity.setPhoneNumber(registerRequest.getPhoneNumber());
            storeEntity.setAddress(registerRequest.getAddress());
            storeEntity.setCity(registerRequest.getCity());

            // Masukkan data pemilik
            OwnerEntity ownerEntity = new OwnerEntity();

            ownerEntity.setMerchantId(merchantId);
            ownerEntity.setId(registerRequest.getEmail());
            ownerEntity.setPassword(Digest.MD5(registerRequest.getEmail(), registerRequest.getPassword()));
            ownerEntity.setName(registerRequest.getName());

            // Simpan data cabang dan pemilik
            dmlManager.store(storeEntity);
            dmlManager.store(ownerEntity);
            dmlManager.commit();
        }

        return Response
                .ok()
                .build();
    }
}