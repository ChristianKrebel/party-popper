const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");

exports.joinEvent = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.eventId);
  helper.errorIfNotAuthenticated(context);

  const uid = context.auth.uid;
  const eventDoc = db.collection("events").doc(data.eventId);
  try {
    const event = await eventDoc.get();
    if (!event.exists) {
      return false;
    }

    const joinedDoc = eventDoc.collection("joined").doc(uid);
    const joined = await joinedDoc.get();
    if (!joined.exists) {
      const promises = [];

      promises.push(
        joinedDoc.set({
          uid: uid,
          joinedAt: admin.firestore.FieldValue.serverTimestamp()
        })
      );

      promises.push(
        eventDoc.update("going", admin.firestore.FieldValue.increment(1))
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

exports.leaveEvent = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.eventId);
  helper.errorIfNotAuthenticated(context);

  const uid = context.auth.uid;
  const eventDoc = db.collection("events").doc(data.eventId);
  try {
    const event = await eventDoc.get();
    if (!event.exists) {
      return false;
    }

    const joinedDoc = eventDoc.collection("joined").doc(uid);
    const joined = await joinedDoc.get();
    if (joined.exists) {
      const promises = [];

      promises.push(joinedDoc.delete());

      promises.push(
        eventDoc.update("going", admin.firestore.FieldValue.increment(-1))
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
