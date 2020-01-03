const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");

exports.followOrganizer = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.organizerId);
  helper.errorIfNotAuthenticated(context);

  const uid = context.auth.uid;
  const orgDoc = db.collection("organizer").doc(data.organizerId);
  const followerDoc = orgDoc.collection("follower").doc("1");
  const userDoc = db.collection("users").doc(uid);
  const followingDoc = userDoc.collection("following").doc("1");

  try {
    const follower = await followerDoc.get();
    if (follower.exists && !follower.data().follower.includes(uid)) {
      const promises = [];

      // Add to Follower
      promises.push(
        followerDoc.update(
          "follower",
          admin.firestore.FieldValue.arrayUnion(uid)
        )
      );

      // Increment Follower Count
      promises.push(
        orgDoc.update("followCount", admin.firestore.FieldValue.increment(1))
      );

      // Add to Following
      promises.push(
        followingDoc.update(
          "following",
          admin.firestore.FieldValue.arrayUnion(data.organizerId)
        )
      );

      // Increment Following Count
      promises.push(
        userDoc.update("following", admin.firestore.FieldValue.increment(1))
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

exports.unfollowOrganizer = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.organizerId);
  helper.errorIfNotAuthenticated(context);

  return await unfollow(data, context);
});

exports.blockOrganizer = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.organizerId);
  helper.errorIfNotAuthenticated(context);

  try {
    const uid = context.auth.uid;
    const blockedRef = db
      .collection("users")
      .doc(uid)
      .collection("blocked")
      .doc(data.organizerId);
    const promises = [];

    promises.push(unfollow(data, context));

    promises.push(
      blockedRef.set({
        blockedAt: admin.firestore.FieldValue.serverTimestamp()
      })
    );

    await Promise.all(promises);
    return true;
  } catch (err) {
    console.log(err);
    throw new functions.https.HttpsError(
      "internal",
      "An internal Error occured"
    );
  }
});

exports.unblockOrganizer = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.organizerId);
  helper.errorIfNotAuthenticated(context);

  try {
    const uid = context.auth.uid;
    const blockedRef = db
      .collection("users")
      .doc(uid)
      .collection("blocked")
      .doc(data.organizerId);

    await blockedRef.delete();
    return true;
  } catch (err) {
    console.log(err);
    throw new functions.https.HttpsError(
      "internal",
      "An internal Error occured"
    );
  }
});

async function unfollow(data, context) {
  const uid = context.auth.uid;
  const orgDoc = db.collection("organizer").doc(data.organizerId);
  const followerDoc = orgDoc.collection("follower").doc("1");
  const userDoc = db.collection("users").doc(uid);
  const followingDoc = userDoc.collection("following").doc("1");

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
          admin.firestore.FieldValue.arrayRemove(data.organizerId)
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
}
