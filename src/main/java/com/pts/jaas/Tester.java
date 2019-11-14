package com.pts.jaas;

 import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Tester {

    public static void main(String[] args) {
        WebTarget client = ClientBuilder.newClient().target("http://localhost:8080/api/account");
        client.register(HttpAuthenticationFeature.basic("admin", "admin".getBytes()));
        Response response = client
            .request(MediaType.APPLICATION_JSON)
            .get();

        System.out.println(response.getStatus());
        System.out.println(response.readEntity(UserDTO.class).getAuthorities());



    }
}
