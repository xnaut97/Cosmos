package com.github.xnaut97.cosmos.database.type;

import com.github.xnaut97.cosmos.database.DatabaseConnection;
import com.github.xnaut97.cosmos.database.mongo.MongoCollectionWrapper;
import com.github.xnaut97.cosmos.database.credentials.MongoCredentials;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Getter
public class MongoDatabase implements DatabaseConnection {

    private final Plugin plugin;
    private final MongoCredentials credentials;

    private MongoClient client;
    private com.mongodb.client.MongoDatabase database;

    public MongoDatabase(
            Plugin plugin,
            MongoCredentials credentials
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.credentials = Objects.requireNonNull(credentials, "credentials");
    }

    @Override
    public boolean isConnected() {

        try {

            if (client == null || database == null) {
                return false;
            }

            database.runCommand(new Document("ping", 1));

            return true;

        } catch (Exception ex) {
            return false;
        }
    }

    @Override
    public void connect() {

        try {

            MongoClientSettings.Builder builder = MongoClientSettings.builder()
                    .applyToConnectionPoolSettings(pool -> pool
                            .minSize(credentials.getMinPoolSize())
                            .maxSize(credentials.getMaxPoolSize()))
                    .applyToSocketSettings(socket -> socket
                            .connectTimeout((int) credentials.getConnectionTimeout(), TimeUnit.MILLISECONDS)
                            .readTimeout((int) credentials.getReadTimeout(), TimeUnit.MILLISECONDS))
                    .applyToClusterSettings(cluster -> cluster
                            .serverSelectionTimeout(credentials.getServerSelectionTimeout(), TimeUnit.MILLISECONDS));

            if (credentials.usesConnectionString()) {
                builder.applyConnectionString(new ConnectionString(credentials.getConnectionString()));
            } else {
                builder.applyToClusterSettings(cluster -> cluster.hosts(Collections.singletonList(
                        new ServerAddress(credentials.getHost(), credentials.getPort())
                )));

                if (credentials.hasCredentials()) {
                    builder.credential(MongoCredential.createCredential(
                            credentials.getUsername(),
                            credentials.getAuthDatabase(),
                            credentials.getPassword().toCharArray()
                    ));
                }
            }

            this.client = MongoClients.create(builder.build());
            this.database = client.getDatabase(credentials.getDatabase());

            database.runCommand(new Document("ping", 1));

            plugin.getLogger().info("Connected to MongoDB database.");

        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to connect MongoDB database.");
            disconnect();
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void disconnect() {

        if (client != null) {
            client.close();
            client = null;
            database = null;
        }
    }

    public boolean collectionExists(String collection) {
        ensureConnected();

        MongoIterable<String> names = database.listCollectionNames();
        for (String name : names) {
            if (name.equals(collection)) {
                return true;
            }
        }

        return false;
    }

    public void createCollection(String collection) {
        ensureConnected();

        if (!collectionExists(collection)) {
            database.createCollection(collection);
        }
    }

    public MongoCollectionWrapper collection(String collection) {
        ensureConnected();
        return new MongoCollectionWrapper(database.getCollection(collection));
    }

    public void insert(String collection, Document document) {
        collection(collection).insert(document);
    }

    public UpdateResult update(String collection, Bson filter, Bson update) {
        return collection(collection).update(filter, update);
    }

    public DeleteResult delete(String collection, Bson filter) {
        return collection(collection).delete(filter);
    }

    public Document findOne(String collection, Bson filter) {
        return collection(collection).findOne(filter);
    }

    public Document findById(String collection, Object id) {
        return findOne(collection, Filters.eq("_id", id));
    }

    public List<Document> findMany(String collection, Bson filter) {
        return collection(collection).findMany(filter);
    }

    public List<Document> findAll(String collection) {
        return collection(collection).findAll();
    }

    private void ensureConnected() {
        if (database == null) {
            throw new IllegalStateException("MongoDB database is not connected");
        }
    }
}
