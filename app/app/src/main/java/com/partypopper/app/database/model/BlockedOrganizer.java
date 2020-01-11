package com.partypopper.app.database.model;

import com.google.firebase.Timestamp;
import com.partypopper.app.database.util.Identifiable;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlockedOrganizer extends Identifiable {

    private Timestamp blockedAt;
}
