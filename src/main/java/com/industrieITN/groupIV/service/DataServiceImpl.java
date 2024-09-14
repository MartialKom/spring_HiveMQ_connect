package com.industrieITN.groupIV.service;

import com.industrieITN.groupIV.models.HistoricalDataRequest;
import com.mongodb.MongoBulkWriteException;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.apache.logging.log4j.LogManager;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class DataServiceImpl implements IDataService{

    @Value("${mongodb.database.collection}")
    String dbCollection;

    @Value("${spring.data.mongodb.database}")
    String dbName;
    private static org.apache.logging.log4j.Logger logger = LogManager.getLogger(DataServiceImpl.class);


    @Value("${blg.request.startTime.fieldName}")
    String requestStartTimeFieldName;

    private final MongoClient mongoClient;

    private final MongoTemplate mongoTemplate;

    @Autowired
    public DataServiceImpl(MongoClient mongoClient, MongoTemplate mongoTemplate) {
        this.mongoClient = mongoClient;
        this.mongoTemplate = mongoTemplate;
    }


    @Override
    public String processFileDataContent(Object m) throws IOException, ParseException {
        if (mongoTemplate.collectionExists(dbCollection)) {
            System.out.println("data " + m);
            List<Document> docs = generateMongoDocs(List.of(m.toString()));
            int count = insertInto(dbCollection, docs);
            return count+"";
        } else {
            logger.info("data {}", m);
            logger.error("--------> Erreur lors de la connexion à la collection. La collection");
            return "Erreur lors de la connexion à la collection. La collection";
        }
    }

    private List<Document> generateMongoDocs(List<String> lines) throws ParseException {
        List<Document> tempDocs = new ArrayList<>();
        List<Document> docs = new ArrayList<>();
        for (String json : lines) {
            tempDocs.add(Document.parse(json));
        }

        for (Document json : tempDocs) {
            if(json.containsKey("date")) {
                logger.info(json.get("date").toString());
                DateFormat dateField_dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                TimeZone time_zone
                        = TimeZone.getTimeZone("GMT+1");
                dateField_dateFormat.setTimeZone(time_zone);
                Date date = dateField_dateFormat.parse(json.get("date").toString());
                json.put("FORMATED_"+requestStartTimeFieldName, date);
                docs.add(json);
            }
        }

        return docs;
    }

    public int insertInto(String collection, List<Document> mongoDocs) {
        try {
            if (!mongoDocs.isEmpty()) {
                Collection<Document> inserts = mongoTemplate.insert(mongoDocs, collection);
                return inserts.size();
            }
        } catch (DataIntegrityViolationException e) {
            logger.error("DataIntegrityViolationException {}", e.getMessage());

            if (e.getCause() instanceof MongoBulkWriteException) {
                return ((MongoBulkWriteException) e.getCause())
                        .getWriteResult()
                        .getInsertedCount();
            }
            return 0;
        }
        return 0;
    }

    //Get all saved data
    @Override
    public List<Document> getHistoricalDatas(HistoricalDataRequest request) throws ParseException {
        logger.info(request.toString());
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(dbCollection);

        List<Document> results = new ArrayList<>();

        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext()) {
                results.add(cursor.next());
            }
        }
        DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC+1"));
        Date startDate = formatter.parse(request.getFrom()+" 00:00:00");
        Date endDate = formatter.parse(request.getTo()+" 23:59:59");
        Document filterD = null;
        Document filterHist = null;

        // Add filter on date attribute
        filterHist = new Document("FORMATED_"+requestStartTimeFieldName, new Document("$exists", true));
        filterD = new Document("FORMATED_"+requestStartTimeFieldName, new Document("$gte", startDate)
                .append("$lte", endDate));
        Document filter = new Document("$and",List.of(filterD, filterHist));
        Document projection = Document.parse(Projections.include(request.getFields()).toBsonDocument().toJson());
        results = collection
                .find(filter)
                .projection(projection)
                .into(new ArrayList<>());
        logger.info("count {}", results.size());
        return results;
    }


    //Get the last data saved on the database
    @Override
    public Document getLast() {
        MongoDatabase database = mongoClient.getDatabase(dbName);
        MongoCollection<Document> collection = database.getCollection(dbCollection);

        Document document = new Document();
        try (MongoCursor<Document> cursor = collection.find().iterator()) {
            while (cursor.hasNext())
                document = cursor.next();
        }

        return document;
    }
}
