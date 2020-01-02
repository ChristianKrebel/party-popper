package com.partypopper.app.database.model;


import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.partypopper.app.database.util.Identifiable;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
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
}
