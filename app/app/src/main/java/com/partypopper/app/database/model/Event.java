package com.partypopper.app.database.model;

import com.google.firebase.firestore.GeoPoint;
import com.partypopper.app.database.util.Identifiable;

import java.util.Date;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Event extends Identifiable {

    private String description;
    private Date endDate;
    private int going;
    private String name;
    private String lowercaseName;
    private String organizer;
    private Date startDate;
    private String image;
    private String eventUrl;
    private GeoPoint location;
}
