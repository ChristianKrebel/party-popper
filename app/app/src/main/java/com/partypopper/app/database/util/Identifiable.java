package com.partypopper.app.database.util;

import com.google.firebase.firestore.Exclude;

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

}
