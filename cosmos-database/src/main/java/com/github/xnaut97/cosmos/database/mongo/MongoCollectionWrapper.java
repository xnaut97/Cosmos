package com.github.xnaut97.cosmos.database.mongo;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MongoCollectionWrapper {

    private final MongoCollection<Document> collection;

    public MongoCollectionWrapper(MongoCollection<Document> collection) {
        this.collection = Objects.requireNonNull(collection, "collection");
    }

    public MongoCollection<Document> raw() {
        return collection;
    }

    public void insert(Document document) {
        collection.insertOne(Objects.requireNonNull(document, "document"));
    }

    public UpdateResult update(Bson filter, Bson update) {
        return collection.updateOne(
                Objects.requireNonNull(filter, "filter"),
                Objects.requireNonNull(update, "update")
        );
    }

    public DeleteResult delete(Bson filter) {
        return collection.deleteOne(Objects.requireNonNull(filter, "filter"));
    }

    public Document findOne(Bson filter) {
        return collection.find(Objects.requireNonNull(filter, "filter")).first();
    }

    public List<Document> findMany(Bson filter) {
        List<Document> documents = new ArrayList<Document>();
        FindIterable<Document> iterable = collection.find(Objects.requireNonNull(filter, "filter"));
        for (Document document : iterable) {
            documents.add(document);
        }
        return documents;
    }

    public List<Document> findAll() {
        return findMany(Filters.empty());
    }
}
