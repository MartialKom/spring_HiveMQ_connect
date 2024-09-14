package com.industrieITN.groupIV.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalDataRequest {
    private List<String> fields;
    private String from;
    private String to;
}
