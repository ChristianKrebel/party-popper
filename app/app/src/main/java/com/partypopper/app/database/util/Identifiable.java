package com.partypopper.app.database.util;

import com.google.firebase.firestore.Exclude;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.util.Map;

public abstract class Identifiable {

    @Exclude
    private String id;

    @Exclude
    public String getEntityKey() {
        return id;
    }

    @Exclude
    public void setEntityKey(String key) {
        this.id = key;
    }




    public Map<String, Object> toMap() {
        Gson gson = new Gson();
        String json = gson.toJson(this);

        Map<String,Object> result = new Gson().fromJson(json, Map.class);

        return result;
    }
}
