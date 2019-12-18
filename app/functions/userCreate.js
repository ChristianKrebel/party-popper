const functions = require("firebase-functions");
const admin = require("firebase-admin");
const db = admin.firestore();

exports.createUser = functions.auth.user().onCreate(async user => {
  const uid = user.uid;

  const userRef = db.collection("users").doc(uid);
  const promises = [];

  promises.push(
    userRef.set({
      following: 0,
      createdAt: admin.firestore.FieldValue.serverTimestamp()
    })
  );

  promises.push(
    userRef
      .collection("following")
      .doc("1")
      .set({ following: [] })
  );

  return Promise.all(promises);
});
