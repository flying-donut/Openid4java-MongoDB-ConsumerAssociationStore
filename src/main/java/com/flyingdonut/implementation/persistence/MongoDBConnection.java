package com.flyingdonut.implementation.persistence;

import com.mongodb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jtzikas
 * Date: 9/18/12
 * Time: 5:28 PM
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
