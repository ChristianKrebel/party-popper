package com.partypopper.app.database.model;


import com.partypopper.app.database.util.Identifiable;

import lombok.Data;

@Data
public class Organizer extends Identifiable {

    private String name;
    private String description;
    private String website;
    private String adress;
    private String phone;
}