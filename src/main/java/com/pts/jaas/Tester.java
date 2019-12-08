package com.pts.jaas;

import com.google.gson.Gson;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;


public class Tester {

    public static void main(String[] args) throws IOException {

        //client.register(OAuth2ClientSupport.feature("eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfU0VOU09SLFJPTEVfVVNFUiIsImV4cCI6MTU3Mzg1NTI2OH0.aDv4DxA74pm7KnFu_8DUBMzELTCwdl8GRjfWOoUkNZKzupL5wJ9INA7K5qZA6aNq6fWZkV223w914Gy3YJFqEg"));
        String url = "http://localhost/api/account";

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(
                        new JaasPTSLoginModule.DefaultContentTypeInterceptor("application/json"))
                .build();
        Request request = new Request.Builder()
                .url(url).addHeader("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImF1dGgiOiJST0xFX0FETUlOLFJPTEVfVVNFUiIsImV4cCI6MTU3NTkwMzE0Mn0.RMFv78T-K9Yz_tQzmLl2z0J29zG-lbheKmj6apYqqI8rReHvp-gb73hxdjlYxXOlkGq6ilbkpWAB_RGLGa2oMw")
                .addHeader("cache-control", "no-cache")
                .build();

        Call call = client.newCall(request);
        Response response = call.execute();
        String string = response.body().string();
        System.out.println(string);
        UserDTO userDTO = new Gson().fromJson(string, UserDTO.class);
        System.out.println(new Gson().toJson(userDTO));
        for (String next : userDTO.getAuthorities()) {
            System.out.println("LoginModule:  next Role : " + next);
        }
        response.close();


    }
}
