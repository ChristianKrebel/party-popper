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

      console.log("a " + organizerDoc.data().name);
      console.log("b " + followerDoc.data().follower);

      var follower = followerDoc.data().follower;
      if (followerDoc.exists && follower !== null) {
        var promises = [];
        console.log("size " + follower);
        for (x of follower) {
          promises.push(
            db
              .collection("users")
              .doc(x)
              .get()
          );
        }

        const a = await Promise.all(promises);
        console.log(a);
        console.log("c " + promises[0]);
        console.log("x " + typeof promises[0]);
        console.log("c " + (await promises[0]).data().createdAt);
        console.log("c " + promises[0].data().createdAt);
      }
    } catch (err) {
      console.log(err);
      throw new functions.https.HttpsError(
        "internal",
        "An internal Error occured"
      );
    }

    return true;
  });
