exports.joinEvent = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.eventId);
  helper.errorIfNotAuthenticated(context);

  const uid = context.auth.uid;
  const eventDoc = db.collection("events").doc(data.eventId);
  const joinedRef = eventDoc.collection("joined");

  try {
    const follower = await followerDoc.get();
    if (follower.exists && follower.data().follower.includes(uid)) {
      const promises = [];

      // Add to Follower
      promises.push(
        followerDoc.update(
          "follower",
          admin.firestore.FieldValue.arrayRemove(uid)
        )
      );

      // Increment Follower Count
      promises.push(
        orgDoc.update("followCount", admin.firestore.FieldValue.increment(-1))
      );

      // Add to Following
      promises.push(
        followingDoc.update(
          "following",
          admin.firestore.FieldValue.arrayRemove(data.orgId)
        )
      );

      // Increment Following Count
      promises.push(
        userDoc.update("following", admin.firestore.FieldValue.increment(-1))
      );

      await Promise.all(promises);
      return true;
    }
  } catch (err) {
    console.log(err);
    throw new functions.https.HttpsError(
      "internal",
      "An internal Error occured"
    );
  }
  return false;
});
