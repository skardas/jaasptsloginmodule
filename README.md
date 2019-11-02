# jaas4apachemq

first run: mvn clean package -DskipTest

then put mysql connector jar and original-jaas-auth-1.0-SNAPSHOT.jar into lib folder or Artemis instance.

Create a mysql db with apachemq and devices table with clientID, username and password
Then update login.config file in the etc of artemis as follows (with correct db creadentials and table field):
~~~xml
activemq {

   com.jaas.auth.JaasLoginModule required
       debug=false
	   dbUser="root"
	   dbPassword=""
	   dataSourceUrl="jdbc:mysql://localhost/ptstakip?useUnicode=true&characterEncoding=utf8&useSSL=false&useLegacyDatetimeCode=false&serverTimezone=UTC"
	   driver="com.mysql.cj.jdbc.Driver"
	   tableName="artemis_credential"
	   usernameFieldName="username"
	   passwordFieldName="password"
	   roleFieldName="role"
	   reload=true;
	   
	org.apache.activemq.artemis.spi.core.security.jaas.PropertiesLoginModule sufficient
       debug=false
       reload=true
       org.apache.activemq.jaas.properties.user="artemis-users.properties"
       org.apache.activemq.jaas.properties.role="artemis-roles.properties";

};
~~~


