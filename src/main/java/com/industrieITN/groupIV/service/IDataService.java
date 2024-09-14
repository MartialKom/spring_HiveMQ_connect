package com.industrieITN.groupIV.service;

import com.industrieITN.groupIV.models.HistoricalDataRequest;
import org.bson.Document;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public interface IDataService {

    String processFileDataContent(Object m) throws IOException, ParseException;
    List<Document> getHistoricalDatas(HistoricalDataRequest request) throws ParseException;

    Document getLast();
}
