package com.example.smartwallet.network.dto;

import com.google.gson.annotations.SerializedName;

public class AnalyticsData {
    @SerializedName("total_cashback")
    public double totalCashback;
    
    @SerializedName("category_spending")
    public CategorySpending[] categorySpending;
    
    @SerializedName("spending_trends")
    public SpendingTrends spendingTrends;
    
    public static class CategorySpending {
        public String category;
        public double amount;
        public double percentage;
    }
    
    public static class SpendingTrends {
        @SerializedName("food_trend")
        public String foodTrend;
        
        @SerializedName("trend_percentage")
        public double trendPercentage;
    }
}
