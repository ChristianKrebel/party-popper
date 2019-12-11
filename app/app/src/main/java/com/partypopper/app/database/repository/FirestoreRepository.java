package com.partypopper.app.database.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.partypopper.app.database.util.Identifiable;

import java.util.List;

public class FirestoreRepository<TEntity extends Identifiable> {

    private static final String TAG = "FirestoreRepository";

    private final SimpleMapper<TEntity> simpleMapper;

    private final Class<TEntity> entityClass;

    private final CollectionReference collectionReference;
    private final String collectionName;

    public FirestoreRepository(Class<TEntity> entityClass, String collectionName) {
        this.simpleMapper = new SimpleMapper<>(entityClass);
        this.collectionName = collectionName;
        this.entityClass = entityClass;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        this.collectionReference = db.collection(this.collectionName);
    }

    public Task<Boolean> exists(final String id) {
        DocumentReference documentReference = collectionReference.document(id);
        Log.i(TAG, "Checking existence of '" + id + "' in '" + collectionName + "'.");

        return documentReference.get().continueWith(new Continuation<DocumentSnapshot, Boolean>() {
            @Override
            public Boolean then(@NonNull Task<DocumentSnapshot> task) {
                return task.getResult() != null && task.getResult().exists();
            }
        });
    }

    public Task<TEntity> get(String id) {
        DocumentReference documentReference = collectionReference.document(id);
        Log.i(TAG, "Getting '" + id + "' in '" + collectionName + "'.");

        return simpleMapper.mapEntity(documentReference.get());
    }

    public Task<List<TEntity>> getAll() {
        Log.i(TAG, "Getting All Documents in '" + collectionName + "'.");

        return simpleMapper.mapEntities(collectionReference.get());
    }

    public Task<Void> create(TEntity entity, boolean useKey) {
        Log.i(TAG, "Creating " + entityClass.getSimpleName() + " in '" + collectionName + "'.");
        if(useKey) {
            final String documentName = entity.getEntityKey();
            DocumentReference documentReference = collectionReference.document(documentName);
            Log.i(TAG, "Creating '" + documentName + "' in '" + collectionName + "'.");
            return documentReference.set(entity).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "There was an error creating '" + documentName + "' in '" + collectionName + "'!", e);
                }
            });
        } else {
            return collectionReference.document().set(entity).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "There was an error creating '" + entityClass.getSimpleName()  + "' in '" + collectionName + "'!", e);
                }
            });
        }
    }

    public Task<Void> update(TEntity entity) {
        final String documentName = entity.getEntityKey();
        DocumentReference documentReference = collectionReference.document(documentName);
        Log.i(TAG, "Updating '" + documentName + "' in '" + collectionName + "'.");

        return documentReference.set(entity).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "There was an error updating '" + documentName + "' in '" + collectionName + "'.", e);
            }
        });
    }

    public Task<Void> delete(final String documentName) {
        DocumentReference documentReference = collectionReference.document(documentName);
        Log.i(TAG, "Deleting '" + documentName + "' in '" + collectionName + "'.");

        return documentReference.delete().addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "There was an error deleting '" + documentName + "' in '" + collectionName + "'.", e);
            }
        });
    }

}
