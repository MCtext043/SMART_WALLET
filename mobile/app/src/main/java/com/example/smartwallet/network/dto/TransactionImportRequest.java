package com.example.smartwallet.network.dto;

import java.util.List;

public class TransactionImportRequest {
    public List<TransactionImportItem> items;

    public TransactionImportRequest(List<TransactionImportItem> items) {
        this.items = items;
    }
}
