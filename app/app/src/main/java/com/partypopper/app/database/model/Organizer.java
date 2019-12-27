package com.partypopper.app.database.model;


import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.partypopper.app.database.util.Identifiable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
public class Organizer extends Identifiable {

    private String name;
    private String description;
    private String website;
    private String adress;
    private String phone;
}
