# jaas4apachemq

first run: mvn clean package -DskipTest

then put mysql connector jar and original-jaas-auth-1.0-SNAPSHOT.jar into lib folder or ApacheMQ.

Create a mysql db with apachemq and devices table with clientID, username and password
Then update apachemq config as follows (with correct db creadentials):
~~~xml
<broker xmlns="http://activemq.apache.org/schema/core" brokerName="localhost" dataDirectory="${activemq.data}">
.
.
.
<plugins>
		<bean id="authPlugin" class="com.pts.jaas.auth.JaasAuthPlugin" xmlns="http://www.springframework.org/schema/beans">
		<property name="url">
		<value>jdbc:mysql://localhost:3306/apachemq?useSSL=false&amp;serverTimezone=UTC&amp;useLegacyDatetimeCode=false&amp;</value>
		</property>
		<property name="username">
		<value>root</value>
		</property>
		<property name="password">
		<value></value>
		</property>
		</bean>
</plugins>
</code>
~~~


