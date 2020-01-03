const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");

exports.signUpOrganizer = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data);
  helper.errorIfNotAuthenticated(context);

  const user = await admin.auth().getUser(context.auth.uid);
  const orgRef = db.collection("organizer").doc(user.uid);

  if (user.customClaims && user.customClaims.organizer) {
    console.log(user.uid, "is already a Orgranizer");
    return false;
  }

  data.avgRating = 0;
  data.followCount = 0;
  data.numRatings = 0;

  try {
    const promises = [];

    promises.push(orgRef.set(data));

    promises.push(
      orgRef
        .collection("follower")
        .doc("1")
        .set({ follower: [] })
    );

    await Promise.all(promises);
  } catch (err) {
    console.log(err);
    throw new functions.https.HttpsError(
      "internal",
      "An internal Error occured"
    );
  }

  await admin.auth().setCustomUserClaims(user.uid, { organizer: true });
  return true;
});
