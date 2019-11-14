package com.pts.jaas;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class Tester {

    public static void main(String[] args) {
        WebTarget client = ClientBuilder.newClient().target("http://localhost:8080/api/account");
        client.register(HttpAuthenticationFeature.basic("test1", "test1"));
        //client.register(OAuth2ClientSupport.feature("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfU0VOU09SLFJPTEVfVVNFUiIsImV4cCI6MTU3Mzg1NTI2OH0.aDv4DxA74pm7KnFu_8DUBMzELTCwdl8GRjfWOoUkNZKzupL5wJ9INA7K5qZA6aNq6fWZkV223w914Gy3YJFqEg"));
        Response response = client
            .request(MediaType.APPLICATION_JSON)
            .get();

        System.out.println(response.getStatus());
        System.out.println(response.readEntity(UserDTO.class).getAuthorities());



    }
}
