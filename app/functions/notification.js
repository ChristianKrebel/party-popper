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

      const organizerDoc = await db
        .collection("organizer")
        .doc(change.data().organizer)
        .get();
      const followerDoc = await db
        .collection("organizer")
        .doc(change.data().organizer)
        .collection("follower")
        .doc("1")
        .get();

      var follower = followerDoc.data().follower;
      if (followerDoc.exists && follower !== null) {
        var promises = [];
        var organizerName = organizerDoc.data().name;
        var organizerImage = organizerDoc.data().image;
        var eventName = change.data().name;
        for (x of follower) {
          promises.push(
            sendNotification(x, organizerName, organizerImage, eventName)
          );
        }

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

async function sendNotification(uid, organizerName, organizerImage, eventName) {
  try {
    const userRef = await db
      .collection("users")
      .doc(uid)
      .get();
    if (userRef !== null && userRef.exists) {
      const fcmToken = userRef.data().fcmToken;
      if (fcmToken !== null) {
        var message = {
          notification: {
            title: organizerName,
            body: "hat ein neues Event erstellt: \n" + eventName,
            image: organizerImage
          },
          token: fcmToken
        };

        return admin
          .messaging()
          .send(message)
          .then(response => {
            // Response is a message ID string.
            console.log("Successfully sent message:", response);
            return response;
          })
          .catch(error => {
            console.log("Error sending message:", error);
            return error;
          });
      }
    }
  } catch (err) {
    console.log(err);
    throw new functions.https.HttpsError(
      "internal",
      "An internal Error occured"
    );
  }
  return true;
}
