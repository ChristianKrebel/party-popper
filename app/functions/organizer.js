const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");

exports.signUpOrganizer = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data);
  helper.errorIfNotAuthenticated(context);

  const user = await admin.auth().getUser(context.auth.uid);
  const orgDoc = db.collection("organizer").doc(user.uid);

  if (user.customClaims && user.customClaims.organizer) {
    console.log(user.uid, "is already a Orgranizer");
    return false;
  }

  const promises = [];

  promises.push(
    admin.auth().setCustomUserClaims(user.uid, {
      organizer: true
    })
  );

  promises.push(orgDoc.set(data));

  promises.push(
    orgDoc
      .collection("follower")
      .doc("1")
      .set({ follower: [] })
  );

  await Promise.all(promises);
  return true;
});
