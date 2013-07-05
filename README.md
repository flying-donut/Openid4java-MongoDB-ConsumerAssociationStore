MongoDB ConsumerAssociationStore
================================

This is an implementation of the org.openid4java.consumer.ConsumerAssociationStore interface of the Openid4Java library. This interface is used in the org.openid4java.consumer.ConsumerManager to store and retrieve instances of org.openid4java.association.Association. This implementation uses a collection in a MongoDb database as persistent storage. It is part of Flying Donut (https://www.flying-donut.com) and is donated to the community by the Flying Donut team.

Code and configuration
----

The code consists of just two classes: com.flyingdonut.implementation.helpers.MongoDbConsumerAssociationStore, which is the implementation proper, and com.flyingdonut.implementation.persistence.MongoDBConnection, which is a helper class encapsulating basic functionality for interacting with a MongoDb database. Both classes are designed with dependency injection in mind. Follows sample configuration using the Spring framework, in particular, which you will be able to read, even without familiarity with the particular framework, and translate it to any other DI framework or plain Java code. However, even though DI need not be provided by Spring, there is a dependency on Spring string utilities.

```xml
	<bean id="mongoDBConnection" class="com.flyingdonut.implementation.persistence.MongoDBConnection" init-method="initDatabase" destroy-method="deinitDatabase">
        <property name="hosts" value="_the host_"/>
        <property name="mongoURI" value="_URI_"/>
        <property name="dbName" value="_Database name_"/>
        <property name="userName" value="_Username_"/>
        <property name="password" value="_Password_"/>
    </bean>
	
    <bean id="associationStore" class="com.flyingdonut.implementation.helpers.MongoDbConsumerAssociationStore">
        <property name="mongoDBConnection" ref="mongoDBConnection"/>
    </bean>
```

Dependencies
-------------

```xml
    <dependencies>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>${slf4j.version}</version>
        </dependency>
        <dependency>
            <groupId>org.mongodb</groupId>
            <artifactId>mongo-java-driver</artifactId>
            <version>${mongo-java-driver.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-core</artifactId>
            <version>${springframework.version}</version>
        </dependency>
    </dependencies>
```

Copyright and License
---

Copyright 2013 Flying Donut

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.

