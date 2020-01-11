const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");

exports.onEventCreate = functions.firestore
  .document("events/{eventId}")
  .onCreate(async (change, context) => {
    const eventId = context.params.eventId;
    const data = change.data();

    try {
      console.log(change.data());
      console.log(change.data().organizer);

      const organizer = await db
        .collection("organizer")
        .doc(change.data().organizer)
        .get();
      const follower = await db
        .collection("organizer")
        .doc(change.data().organizer)
        .collection("follower")
        .doc("1")
        .get();

      console.log("a " + organizer.data());
      console.log("b " + follower.data());
    } catch (err) {
      console.log(err);
      throw new functions.https.HttpsError(
        "internal",
        "An internal Error occured"
      );
    }

    return true;
  });
