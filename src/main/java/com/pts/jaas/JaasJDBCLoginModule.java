package com.pts.jaas;

import org.apache.activemq.artemis.spi.core.security.jaas.RolePrincipal;
import org.apache.activemq.artemis.spi.core.security.jaas.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.sql.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * @author skardas
 */
public class JaasJDBCLoginModule implements LoginModule {

    private static final Logger LOGGER = LoggerFactory.getLogger(JaasJDBCLoginModule.class);
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
                LOGGER.debug("Password : " + password);
            }

            if (username == null || password == null) {
                LOGGER.error("Callback handler does not return login data properly");
                throw new LoginException("Callback handler does not return login data properly");
            }

            if (isValidUser()) { //validate user.
                loginSucceeded = true;
                principals.add(new UserPrincipal(username));
                principals.add(new RolePrincipal("sensor"));
            }
            else
                loginSucceeded = false;

        } catch (IOException e) {
            e.printStackTrace();
        } catch (UnsupportedCallbackException e) {
            e.printStackTrace();
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

        String tableName = (String) options.get("tableName");
        String usernameFieldName = (String) options.get("usernameFieldName");
        String passwordFieldName = (String) options.get("passwordFieldName");
        String roleFieldName = (String) options.get("roleFieldName");

        String sql = String.format("select * from %s where %s=? and %s=?",tableName, usernameFieldName, passwordFieldName);
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        boolean isSuccess = false;

        try {
            con = getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setString(1, username.toLowerCase(Locale.ENGLISH));
            stmt.setString(2, new String(password).toLowerCase(Locale.ENGLISH));

            rs = stmt.executeQuery();
            if (rs.next()) { //User exist with the given user name and password.
                principals.add(new RolePrincipal(rs.getString(roleFieldName)));
                isSuccess = true;
            }
        } catch (Exception e) {
            LOGGER.error("Error when loading user from the database " + e);
            e.printStackTrace();
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                LOGGER.error("Error when closing result set." + e);
            }
            try {
                stmt.close();
            } catch (SQLException e) {
                LOGGER.error("Error when closing statement." + e);
            }
            try {
                con.close();
            } catch (SQLException e) {
                LOGGER.error("Error when closing connection." + e);
            }
        }
        return isSuccess;
    }


    /**
     * Returns JDBC connection
     *
     * @return
     * @throws LoginException
     */
    private Connection getConnection() throws LoginException {

        String dBUser = (String) options.get("user");
        String dBPassword = (String) options.get("password");
        String dBUrl = (String) options.get("url");
        String dBDriver = (String) options.get("driver");

        Connection con = null;
        try {
            //loading driver
            Class.forName(dBDriver);
            con = DriverManager.getConnection(dBUrl, dBUser, dBPassword);
        } catch (Exception e) {
            LOGGER.error("Error when creating database connection" + e);
            e.printStackTrace();
        } finally {
        }
        return con;
    }


}
