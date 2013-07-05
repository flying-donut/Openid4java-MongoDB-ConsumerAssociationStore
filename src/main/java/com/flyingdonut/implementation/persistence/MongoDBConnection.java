package com.flyingdonut.implementation.persistence;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2013 Flying Donut P.C.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

public class MongoDBConnection {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private String dbName;
    private String userName = null;
    private String password = null;
    private String mongoURI = null;

    private DB db;
    private Mongo mongo;
    /*Comma delimited string with hosts names*/
    private String hosts;

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMongoURI() {
        return mongoURI;
    }

    public void setMongoURI(String mongoURI) {
        this.mongoURI = mongoURI;
    }

    public DB getDb() {
        return db;
    }

    public Mongo getMongo() {
        return mongo;
    }

    public String getHosts() {
        return hosts;
    }

    public void setHosts(String hosts) {
        this.hosts = hosts;
    }

    public void initDatabase() throws UnknownHostException {
        if (StringUtils.hasText(getMongoURI())) {
            mongoURIInit();
        } else {
            simpleInit();
        }
        if(getUserName() != null && getUserName().trim().length() > 0) {
            db.authenticate(getUserName(), getPassword().toCharArray());
        }
    }

    private void mongoURIInit() {
        MongoURI uri = new MongoURI(getMongoURI());
        try {
            logger.info("Connecting to " + uri + "...");
            mongo = new Mongo(uri);
            logger.info("...success!");
            String uriDatabase = uri.getDatabase();
            if(StringUtils.hasText(uriDatabase)) {
                db = mongo.getDB(uriDatabase);
            } else {
                db = mongo.getDB(dbName);
            }
        } catch (UnknownHostException e) {
            logger.error("Could not init the database", e);
        }
    }

    private void simpleInit() throws UnknownHostException {
        List<ServerAddress> serverAddresses = new ArrayList<ServerAddress>();
        String[] hostNames = getHosts().split(",");

        for (String host : hostNames) {
            String[] hostPort = host.split(":");
            String h = hostPort[0];
            Integer p = (hostPort.length == 2 ? Integer.parseInt(hostPort[1]) : null);
            ServerAddress serverAddress;
            if (p == null) {
                serverAddress = new ServerAddress(h);
            } else {
                serverAddress = new ServerAddress(h, p);
            }
            serverAddresses.add(serverAddress);
        }
        logger.info("Connecting to " + serverAddresses + "...");
        mongo = new Mongo(serverAddresses);
        logger.info("...success!");
        db = mongo.getDB(dbName);
    }

    public void deinitDatabase() {
        if(mongo!=null) {
            mongo.close();
        }
    }

    public DBCollection getCollection(String collectionName) {
        return getDb().getCollection(collectionName);
    }

}
