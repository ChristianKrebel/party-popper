const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");

exports.onEventCreate = functions.firestore
  .document("events/{eventId}")
  .onCreate((change, context) => {
    const eventId = context.params.eventId;
    const data = change.data();


    try {
        const promises = [];

        const organizer = await db.collection("organizer").doc(data.organizer).get();
        const follower = await db.collection("organizer").doc(data.organizer).collection("follower").get();

        console.log("a " + organizer.data());
        console.log("b " + follower.data());
        console.log("c " + promises);
        
    } catch (err) {
        console.log(err);
        throw new functions.https.HttpsError(
          "internal",
          "An internal Error occured"
        );
      }
    


    return true;
  });
