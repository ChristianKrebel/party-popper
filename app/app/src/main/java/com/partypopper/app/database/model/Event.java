package com.partypopper.app.database.model;


import com.partypopper.app.database.util.Identifiable;

import java.util.Date;

import lombok.Data;

@Data
public class Event extends Identifiable {

    private String description;
    private Date endDate;
    private int going;
    private String name;
    private String organizer;
    private Date startDate;

}
