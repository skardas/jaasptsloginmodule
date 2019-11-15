package com.pts.jaas;

import com.google.gson.Gson;
import okhttp3.*;

import java.io.IOException;


public class Tester {

    public static void main(String[] args) throws IOException {

        //client.register(OAuth2ClientSupport.feature("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfU0VOU09SLFJPTEVfVVNFUiIsImV4cCI6MTU3Mzg1NTI2OH0.aDv4DxA74pm7KnFu_8DUBMzELTCwdl8GRjfWOoUkNZKzupL5wJ9INA7K5qZA6aNq6fWZkV223w914Gy3YJFqEg"));
       String url = "http://localhost:8080/api/account";

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(
                        new JaasPTSLoginModule.DefaultContentTypeInterceptor("application/json"))
                .build();
        Request request = new Request.Builder()
                .url(url).addHeader("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfTU9ERVJBVE9SLFJPTEVfU0VOU09SLFJPTEVfVVNFUiIsImV4cCI6MTU3MzkwNTM2NH0.SXY0etvUIE9m4C376FdWA-iYV75uzNYf42YrZ6ZDShUAaH4NpXrdgq4NL2sSeUpYf6SKpAevyiGxQ4rfM4xXOA")
                .addHeader("cache-control", "no-cache")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        UserDTO userDTO =  new Gson().fromJson(response.body().string(),UserDTO.class);
        for(String  next: userDTO.getAuthorities())
        {
             System.out.println("LoginModule:  next Role : "+ next);
        }
        response.close();


    }
}
