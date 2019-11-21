# jaasptsloginmodule

first run: mvn clean package -DskipTest

then put  original-jaas-auth-1.0-SNAPSHOT.jar into lib folder or Artemis instance.

Create a mysql db with apachemq and devices table with clientID, username and password
Then update login.config file in the etc of artemis as follows (with correct db creadentials and table field):
~~~xml
activemq {

   com.pts.jaas.JaasPTSLoginModule required
 	   authURL="http://localhost:8080/api/account"
 	   debug=true
	   reload=true;
	   
	org.apache.activemq.artemis.spi.core.security.jaas.PropertiesLoginModule sufficient
       debug=false
       reload=true
       org.apache.activemq.jaas.properties.user="artemis-users.properties"
       org.apache.activemq.jaas.properties.role="artemis-roles.properties";

};
~~~


