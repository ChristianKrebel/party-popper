package com.partypopper.app.database.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.partypopper.app.database.util.DocumentHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowRepository {

    private static final String TAG = "FollowRepository";

    private static FollowRepository instance;

    private final FirebaseFunctions mFunctions;
    private final DocumentReference followingReference;

    private FollowRepository() {
        this.mFunctions = FirebaseFunctions.getInstance();
        this.followingReference = FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .collection("following")
                .document("1");
    }

    public Task<HttpsCallableResult> followOrganizer(String organizerId) {
        Log.i(TAG, "followOrganizer " + organizerId);

        Map<String, Object> data = new HashMap<>();
        data.put("organizerId", organizerId);

        return mFunctions.getHttpsCallable("followOrganizer").call(data);
    }

    public Task<HttpsCallableResult> unfollowOrganizer(String organizerId) {
        Log.i(TAG, "unfollowOrganizer " + organizerId);

        Map<String, Object> data = new HashMap<>();
        data.put("organizerId", organizerId);

        return mFunctions.getHttpsCallable("unfollowOrganizer").call(data);
    }

    public Task<HttpsCallableResult> blockOrganizer(String organizerId) {
        Log.i(TAG, "blockOrganizer " + organizerId);

        Map<String, Object> data = new HashMap<>();
        data.put("organizerId", organizerId);

        return mFunctions.getHttpsCallable("blockOrganizer").call(data);
    }

    public Task<HttpsCallableResult> unblockOrganizer(String organizerId) {
        Log.i(TAG, "unblockOrganizer " + organizerId);

        Map<String, Object> data = new HashMap<>();
        data.put("organizerId", organizerId);

        return mFunctions.getHttpsCallable("unblockOrganizer").call(data);
    }

    public Task<HttpsCallableResult> rateOrganizer(String organizerId, float stars) {
        Log.i(TAG, "rateOrganizer " + organizerId);

        Map<String, Object> data = new HashMap<>();
        data.put("organizerId", organizerId);
        data.put("stars", stars);
        data.put("message", "");

        return mFunctions.getHttpsCallable("rateOrganizer").call(data);
    }

    public Task<List<String>> getFollowing() {
        Log.i(TAG, "getFollowing");

        return DocumentHelper.getStringList(followingReference, "following");
    }

    public static FollowRepository getInstance() {
        if(instance == null) {
            instance = new FollowRepository();
        }
        return instance;
    }
}
