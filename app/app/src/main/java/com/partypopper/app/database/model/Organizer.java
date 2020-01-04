package com.partypopper.app.database.model;

import com.google.firebase.firestore.GeoPoint;
import com.partypopper.app.database.util.Identifiable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Organizer extends Identifiable {

    private String name;
    private String lowercaseName;
    private String description;
    private String email;
    private String website;
    private String address;
    private String phone;
    private float avgRating;
    private int numRatings;
    private int followCount;
    private String image;
    private GeoPoint location;

}
