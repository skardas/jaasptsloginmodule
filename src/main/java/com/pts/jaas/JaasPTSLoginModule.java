package com.pts.jaas;
import com.google.gson.Gson;
import okhttp3.*;
import org.apache.activemq.artemis.spi.core.security.jaas.RolePrincipal;
import org.apache.activemq.artemis.spi.core.security.jaas.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.callback.Callback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import java.io.IOException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author skardas
 */
public class JaasPTSLoginModule implements LoginModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaasPTSLoginModule.class);
    // initial state
    private Subject subject;
    private CallbackHandler callbackHandler;
    private Map options;

    // configurable option
    private boolean debug = false;
    // the authentication status
    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;
    //user credentials
    private String url;
    private String username = null;
    private char[] password = null;
    //user principle
    private final Set<Principal> principals = new HashSet<>();
     @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.options = options;
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        this.url = (String) options.get("authURL");
     }

    @Override
    public boolean login() throws LoginException {
        loginSucceeded = true;
        if (callbackHandler == null) {
            throw new LoginException("Error: no CallbackHandler available " +
                    "to garner authentication information from the user");
        }
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("username");
        callbacks[1] = new PasswordCallback("password", false);
        try {
            callbackHandler.handle(callbacks);
            username = ((NameCallback) callbacks[0]).getName();
            password = ((PasswordCallback) callbacks[1]).getPassword();
            if (debug) {
                LOGGER.debug("Username :" + username);
                LOGGER.debug("Password :" + password);
            }
            if (username == null) {
                LOGGER.error("Callback handler does not return login data properly");
                throw new LoginException("Callback handler does not return login data properly");
            }
            if (isValidUser()) { //validate user.
                loginSucceeded = true;
                principals.add(new UserPrincipal(username));
                //principals.add(new RolePrincipal("Sensor"));
            }
            else
                loginSucceeded = false;
        } catch (Exception e) {
            LOGGER.error("LoginModule",e.getCause());
            loginSucceeded = false;
        }
        return loginSucceeded;
    }
    @Override
    public boolean commit() throws LoginException {
        if (loginSucceeded) {
            subject.getPrincipals().addAll(principals);
            LOGGER.debug("Commit::LoginSucceeded");
        }
        else
        {
            clearAll();
            return false;
        }
        if (debug) {
            LOGGER.debug("commit: " + loginSucceeded);
        }
        return (commitSucceeded = true);
    }
    private void clearAll() {
        principals.clear();
    }
    @Override
    public boolean abort() throws LoginException {
        if (debug) {
            LOGGER.debug("abort");
        }
        if(!loginSucceeded)
            return false;
        if (commitSucceeded){
            logout();
        }
        return true;
    }
    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().removeAll(principals);
        if (debug) {
            LOGGER.debug("logout");
        }
        clearAll();
        commitSucceeded = false;
        loginSucceeded = false;
        return true;
    }
    private boolean isValidUser() throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(
                        new DefaultContentTypeInterceptor("application/json"))
                .build();

       Request request;
       if(password != null)
        {
            request = new Request.Builder()
                    .url(this.url).addHeader("Authorization", Credentials.basic(username, new String(password)))
                    .build();
        }
        else
        {
            request = new Request.Builder()
                    .url(this.url)
                    .addHeader("Authorization", "Bearer " + username)
                    .addHeader("cache-control", "no-cache")
                    .build();
        }
        Response response = client.newCall(request).execute();
        String buffer = response.body().string();
        UserDTO userDTO =  new Gson().fromJson(buffer,UserDTO.class);
        for(String  next: userDTO.getAuthorities())
        {
            principals.add(new RolePrincipal(next));
        }
        response.close();
        return true;
    }
    static class DefaultContentTypeInterceptor implements Interceptor {
         String contentType;

        public DefaultContentTypeInterceptor(String contentType) {
            this.contentType = contentType;
        }

        public Response intercept(Interceptor.Chain chain)
                throws IOException {

            Request originalRequest = chain.request();
            Request requestWithUserAgent = originalRequest
                    .newBuilder()
                    .header("Content-Type", contentType)
                    .build();

            return chain.proceed(requestWithUserAgent);
        }
    }
}
