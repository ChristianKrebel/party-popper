package com.partypopper.app.database.model;

import com.partypopper.app.database.util.Identifiable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Organizer extends Identifiable {

    private String name;
    private String lowercaseName;
    private String description;
    private String website;
    private String address;
    private String phone;
    private float avgRating;
    private int numRatings;
    private int followCount;
    private String image;

}
