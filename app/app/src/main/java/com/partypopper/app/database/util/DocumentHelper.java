package com.partypopper.app.database.util;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DocumentHelper {

    public static Task<List<String>> getStringList(DocumentReference documentReference, String fieldName) {
        return getList(documentReference, fieldName).continueWith(new Continuation<List<?>, List<String>>() {
            @Override
            public List<String> then(@NonNull Task<List<?>> task) throws Exception {
                List<?> list = task.getResult();

                if(list == null) {
                    return new ArrayList<>();
                }

                List<String> result = new ArrayList<>();

                for(Object object : list) {
                    if((object instanceof String) || isPrimitiveWrapper(object)) {
                        result.add(String.valueOf(object));
                    }
                }

                return result;
            }
        });
    }

    public static Task<List<?>> getList(DocumentReference documentReference, final String fieldName) {
        return documentReference.get().continueWith(new Continuation<DocumentSnapshot, List<?>>() {
            @Override
            public List<?> then(@NonNull Task<DocumentSnapshot> task) throws Exception {
                DocumentSnapshot documentSnapshot = task.getResult();
                if(documentSnapshot != null && documentSnapshot.exists()) {
                    Object o = documentSnapshot.get(fieldName);
                    return (List<?>) ((o instanceof List) ? o : null);
                }
                return null;
            }
        });
    }

    private static boolean isPrimitiveWrapper(Object input) {
        return input instanceof Integer || input instanceof Boolean ||
                input instanceof Character || input instanceof Byte ||
                input instanceof Short || input instanceof Double ||
                input instanceof Long || input instanceof Float;
    }
}
