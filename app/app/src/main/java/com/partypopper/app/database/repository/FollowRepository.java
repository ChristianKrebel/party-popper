package com.partypopper.app.database.repository;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.partypopper.app.database.util.DocumentHelper;

import java.util.List;

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

    public Task<HttpsCallableResult> followOrganizer(String id) {
        Log.i(TAG, "followOrganizer " + id);

        return mFunctions.getHttpsCallable("followOrg").call(id);
    }

    public Task<HttpsCallableResult> unfollowOrganizer(String id) {
        Log.i(TAG, "unfollowOrganizer " + id);

        return mFunctions.getHttpsCallable("unfollowOrg").call(id);
    }

    public Task<List<String>> getFollowing() {
        Log.i(TAG, "getFollowing");

        return DocumentHelper.getStringList(followingReference, "following");
    }




    /*private final DocumentReference followingReference;

    private FollowRepository() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getUid();

        this.followingReference = db
                .collection("users")
                .document(uid)
                .collection("following")
                .document("1");
    }

    public Task<Void> followOrganizer(String id) {
        Log.i(TAG, "followOrganizer " + id);

        return followingReference.update("following", FieldValue.arrayUnion(id));
    }

    public Task<Void> unfollowOrganizer(String id) {
        Log.i(TAG, "unfollowOrganizer " + id);

        return followingReference.update("following", FieldValue.arrayRemove(id));
    }

    public Task<List<String>> getFollowing() {
        Log.i(TAG, "getFollowing");

        return DocumentHelper.getStringList(followingReference, "following");
    }*/

    public static FollowRepository getInstance() {
        if(instance == null) {
            instance = new FollowRepository();
        }
        return instance;
    }
}
