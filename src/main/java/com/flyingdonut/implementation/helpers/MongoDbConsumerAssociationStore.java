package com.flyingdonut.implementation.helpers;

import com.flyingdonut.implementation.persistence.MongoDBConnection;
import com.mongodb.*;
import org.apache.commons.codec.binary.Base64;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.openid4java.consumer.ConsumerAssociationStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Copyright 2013 Flying Donut P.C.
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with the License. You may obtain a copy of the License in the LICENSE file, or at:
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
public class MongoDbConsumerAssociationStore implements ConsumerAssociationStore {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String opurlField = "opurl";
    private final String handleField = "handle";
    private final String typeField = "type";
    private final String mackeyField = "mackey";
    private final String expdateField = "expdate";
    private MongoDBConnection mongoDBConnection;
    private final String id = "_id";

    @Override
    public void save(String opUrl, Association association) {
        removeExpired();
        BasicDBObject dbObject = new BasicDBObject();
        dbObject.put(opurlField, opUrl);
        dbObject.put(handleField, association.getHandle());
        dbObject.put(typeField, association.getType());
        dbObject.put(mackeyField, association.getMacKey() == null ?
                null : new String(Base64.encodeBase64(association.getMacKey().getEncoded())));
        dbObject.put(expdateField, association.getExpiry());
        String collection = getCollectionName();
        WriteResult writeResult = getMongoDBConnection().getCollection(collection).insert(dbObject, WriteConcern.SAFE);
    }

    @Override
    public Association load(String opUrl, String handle) {
        Association assoc = null;
        BasicDBObject query = new BasicDBObject();
        BasicDBObject sortBy = null;
        query.put(opurlField, opUrl);
        if (handle != null) {
            query.put(handleField, handle);
        } else {
            sortBy = new BasicDBObject();
            sortBy.put(handleField, -1);
        }
        mongoEnsureIndex(query, sortBy);
        DBObject res = getMongoDBConnection().getCollection(getCollectionName()).findOne(query);
        if (res != null) {
            String type = (String) res.get(typeField);
            String macKey = (String) res.get(mackeyField);
            Date expDate = (Date) res.get(expdateField);
            handle = (String) res.get(handleField);

            try {
                if (type == null || macKey == null || expDate == null) {
                    throw new AssociationException(
                            "Invalid association data retrieved from MongoDb; cannot create Association "
                                    + "object for handle: " + handle);
                }
                if (Association.TYPE_HMAC_SHA1.equals(type)) {
                    assoc = Association.createHmacSha1(handle,
                            Base64.decodeBase64(macKey.getBytes()),
                            expDate);
                } else if (Association.TYPE_HMAC_SHA256.equals(type)) {
                    assoc = Association.createHmacSha256(handle,
                            Base64.decodeBase64(macKey.getBytes()),
                            expDate);
                } else {
                    throw new AssociationException(
                            "Invalid association type "
                                    + "retrieved from database: "
                                    + type);
                }
            } catch (AssociationException ase) {
                logger.error("Error retrieving association from MongoDb", ase);
                return null;
            }
        }
        return assoc;
    }

    @Override
    public Association load(String opUrl) {
        return load(opUrl, null);
    }

    @Override
    public void remove(String opUrl, String handle) {
        BasicDBObject query = new BasicDBObject();
        query.put(opurlField, opUrl);
        query.put(handleField, handle);
        mogoDelete(query);
    }

    private void removeExpired() {
        BasicDBObject query = new BasicDBObject().append(expdateField, new BasicDBObject().append("$lt", new Date()));
        mogoDelete(query);
    }

    private void mongoEnsureIndex(BasicDBObject query, BasicDBObject sort) {
        if (query.containsField(id) || query.keySet().isEmpty()) {
            return;
        }

        DBObject indexDoc = new BasicDBObject();
        for (String k : query.keySet()) {
            if (k.startsWith("$")) {
                mongoEnsureIndexAddAll(indexDoc, (BasicDBList) query.get(k));
            } else {
                indexDoc.put(k, 1);
            }
        }

        if (sort != null) {
            for (String k : sort.keySet()) {
                indexDoc.put(k, sort.get(k));
            }
        }

        getMongoDBConnection().getCollection(getCollectionName()).ensureIndex(indexDoc);
    }

    private void mongoEnsureIndexAddAll(DBObject indexDoc, BasicDBList queryKeys) {
        for (String k : queryKeys.keySet()) {
            BasicDBObject value = (BasicDBObject) queryKeys.get(k);
            if (k.startsWith("$")) {
                mongoEnsureIndexAddAll(indexDoc, (BasicDBList) queryKeys.get(k));
            } else {
                for (String kk : value.keySet()) {
                    indexDoc.put(kk, 1);
                }
            }
        }
    }

    private DBObject mogoDelete(BasicDBObject query) {
        mongoEnsureIndex(query, null);
        DBObject eventObject = getMongoDBConnection().getCollection(getCollectionName()).findAndRemove(query);

        return eventObject;
    }

    protected String getCollectionName() {
        return "mongodb_association";
    }

    public MongoDBConnection getMongoDBConnection() {
        return mongoDBConnection;
    }

    public void setMongoDBConnection(MongoDBConnection mongoDBConnection) {
        this.mongoDBConnection = mongoDBConnection;
    }
}
