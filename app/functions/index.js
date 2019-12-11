const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.helloWorld = functions.https.onCall((data, context) => {
  return {
    text: "Hallo Welt"
  };
});

exports.signUpOrganizer = functions.https.onCall((data, context) => {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "The function must be called while authenticated."
    );
  }
  setUpOrganizer(data, context);
});

async function setUpOrganizer(data, context) {
  const user = await admin.auth().getUser(context.auth.uid);
  const store = admin.firestore();
  if (user.customClaims && user.customClaims.organizer) {
    console.log("${uid} is already a Orgranizer");
    return;
  }

  admin.auth().setCustomUserClaims(user.uid, {
    organizer: true
  });

  let setDoc = store
    .collection("organizer")
    .doc(user.uid)
    .set(data);
}
