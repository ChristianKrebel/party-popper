package com.partypopper.app.database.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.partypopper.app.database.model.BlockedOrganizer;

import java.util.List;

public class BlockedRepository {

    private static BlockedRepository instance;

    private final FirebaseFirestore db;
    private final SimpleMapper<BlockedOrganizer> blockedMapper;

    private final CollectionReference blockedRef;

    private BlockedRepository() {
        this.db = FirebaseFirestore.getInstance();
        this.blockedMapper = new SimpleMapper<>(BlockedOrganizer.class);

        blockedRef = db.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("blocked");
    }

    public Task<Boolean> hasBlocked(String orgId) {
        return blockedRef.document(orgId).get().continueWith(new Continuation<DocumentSnapshot, Boolean>() {
            @Override
            public Boolean then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                DocumentSnapshot snapshot = task.getResult();
                return snapshot != null && snapshot.exists();
            }
        });
    }

    public Task<List<BlockedOrganizer>> getBlockedOrganizers(int amount) {
        return blockedMapper.mapEntities(blockedRef.orderBy("blockedAt", Query.Direction.ASCENDING).limit(amount).get());
    }

    public static BlockedRepository getInstance() {
        if(instance == null) {
            instance = new BlockedRepository();
        }
        return instance;
    }
}
