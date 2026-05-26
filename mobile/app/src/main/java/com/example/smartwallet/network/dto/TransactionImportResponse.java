package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TransactionImportResponse {
    public int imported;
    @SerializedName("skipped_duplicates")
    public int skippedDuplicates;
    public List<Object> errors;
}
