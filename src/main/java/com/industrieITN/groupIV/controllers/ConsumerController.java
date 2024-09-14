package com.industrieITN.groupIV.controllers;

import com.industrieITN.groupIV.models.HistoricalDataRequest;
import com.industrieITN.groupIV.service.IDataService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

@RestController
@RequestMapping("/esp")
@CrossOrigin("*")
public class ConsumerController {

    @Autowired
    private IDataService dataService;


    @GetMapping("/getLast")
    public Document getTheLast(){
        return dataService.getLast();
    }

    @PostMapping("/getHistoricalDatas")
    public List<Document> getHistoricalDatas(@RequestBody HistoricalDataRequest request) throws ParseException {
        request.getFields().add("date");
        return dataService.getHistoricalDatas(request);
    }

}
