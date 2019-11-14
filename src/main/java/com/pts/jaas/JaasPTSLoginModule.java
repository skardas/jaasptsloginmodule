package com.pts.jaas;
import org.apache.activemq.artemis.spi.core.security.jaas.RolePrincipal;
import org.apache.activemq.artemis.spi.core.security.jaas.UserPrincipal;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.client.oauth2.OAuth2ClientSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    WebTarget client;
    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        this.options = options;
        debug = "true".equalsIgnoreCase((String) options.get("debug"));
        this.url = (String) options.get("authURL");
        this.client = ClientBuilder.newClient().target(url);
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
            LOGGER.debug("LoginModule",e.getCause());
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
    private boolean isValidUser() throws LoginException {
       if(password == null)
        {
            client.register(OAuth2ClientSupport.feature(username));
        }
        else
        {
            client.register(HttpAuthenticationFeature.basic(username, new String(password)));
        }

        Response response = client
                .request(MediaType.APPLICATION_JSON)
                .get();
        if(response.getStatus() != 200) return false;

        for(String  next: response.readEntity(UserDTO.class).getAuthorities())
        {
            principals.add(new RolePrincipal(next));
            LOGGER.debug("LoginModule:  next Role : ", next);
        }
        return true;
    }
}
