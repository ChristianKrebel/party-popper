package com.partypopper.app.database.repository;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.partypopper.app.database.util.Identifiable;

import java.util.ArrayList;
import java.util.List;

public class SimpleMapper<TEntity extends Identifiable> {

    private final Class<TEntity> entityClass;

    public SimpleMapper(Class<TEntity> entityClass) {
        this.entityClass = entityClass;
    }

    public Task<TEntity> mapEntity(Task<DocumentSnapshot> task) {
        return task.continueWith(new Continuation<DocumentSnapshot, TEntity>() {
            @Override
            public TEntity then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                DocumentSnapshot documentSnapshot = task.getResult();
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    TEntity entity = documentSnapshot.toObject(entityClass);
                    if(entity != null) {
                        entity.setEntityKey(documentSnapshot.getId());
                    }
                    return entity;
                } else {
                    return entityClass.newInstance();
                }
            }
        });
    }

    public Task<List<TEntity>> mapEntities(Task<QuerySnapshot> task) {
        return task.continueWith(new Continuation<QuerySnapshot, List<TEntity>>() {
            @Override
            public List<TEntity> then(@NonNull Task<QuerySnapshot> task) throws Exception {
                QuerySnapshot querySnapshot = task.getResult();
                if(querySnapshot != null && !querySnapshot.isEmpty()) {
                    List<TEntity> entities = new ArrayList<>();
                    for(DocumentSnapshot snap : querySnapshot.getDocuments()) {
                        TEntity entity = snap.toObject(entityClass);
                        if(entity != null) {
                            entity.setEntityKey(snap.getId());
                            entities.add(entity);
                        }
                    }
                    return entities;
                } else {
                    return new ArrayList<>();
                }
            }
        });
    }
}
