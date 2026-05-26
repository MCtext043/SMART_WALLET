package com.example.smartwallet.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.lang.reflect.Type;

public class JsonUtils {
    
    private static final Gson GSON = new GsonBuilder()
            .setLenient()
            .setPrettyPrinting()
            .create();
    
    public static String toJson(Object object) {
        try {
            return GSON.toJson(object);
        } catch (Exception e) {
            Logger.e("JsonUtils", "Error converting object to JSON", e);
            return "{}";
        }
    }
    
    public static <T> T fromJson(String json, Class<T> classOfT) {
        try {
            return GSON.fromJson(json, classOfT);
        } catch (JsonSyntaxException e) {
            Logger.e("JsonUtils", "Error parsing JSON to " + classOfT.getSimpleName(), e);
            return null;
        }
    }
    
    public static <T> T fromJson(String json, Type typeOfT) {
        try {
            return GSON.fromJson(json, typeOfT);
        } catch (JsonSyntaxException e) {
            Logger.e("JsonUtils", "Error parsing JSON to " + typeOfT.getTypeName(), e);
            return null;
        }
    }
    
    public static boolean isValidJson(String json) {
        try {
            GSON.fromJson(json, Object.class);
            return true;
        } catch (JsonSyntaxException e) {
            return false;
        }
    }
    
    public static String prettyPrint(String json) {
        try {
            Object object = GSON.fromJson(json, Object.class);
            return GSON.toJson(object);
        } catch (JsonSyntaxException e) {
            Logger.e("JsonUtils", "Error pretty printing JSON", e);
            return json;
        }
    }
    
    public static String minify(String json) {
        try {
            Object object = GSON.fromJson(json, Object.class);
            return GSON.toJson(object);
        } catch (JsonSyntaxException e) {
            Logger.e("JsonUtils", "Error minifying JSON", e);
            return json;
        }
    }
}
