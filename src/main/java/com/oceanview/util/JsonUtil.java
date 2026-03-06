package com.oceanview.util;

import org.json.JSONObject;
import org.json.JSONArray;

public class JsonUtil {

    public static String successResponse(String message) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("message", message);
        return json.toString();
    }

    public static String successResponse(String message, Object data) {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("message", message);
        json.put("data", data);
        return json.toString();
    }

    public static String errorResponse(String message) {
        JSONObject json = new JSONObject();
        json.put("success", false);
        json.put("message", message);
        return json.toString();
    }

    public static JSONObject parseBody(String body) {
        try {
            return new JSONObject(body);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}