package com.example.demo.dto;

import java.util.List;

public class SourceMapRequest {
    private List<SourceItem> items;

    public List<SourceItem> getItems() {
        return items;
    }

    public void setItems(List<SourceItem> items) {
        this.items = items;
    }
}