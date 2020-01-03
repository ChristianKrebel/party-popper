const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");

exports.rateOrganizer = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.orgId);
  helper.errorIfEmpty(data.stars);
  helper.errorIfNotAuthenticated(context);

  const uid = context.auth.uid;
  const orgRef = db.collection("organizer").doc(data.orgId);
  const ratingRef = orgRef.collection("reviews").doc(uid);

  try {
    const ratingDoc = await ratingRef.get();
    const promises = [];

    promises.push(
      db.runTransaction(transaction => {
        return transaction.get(orgRef).then(orgDoc => {
          var newNumRatings = orgDoc.data().numRatings;
          var starsData = data.stars;
          var message = data.message == null ? "" : data.message;
          if (ratingDoc.exists && ratingDoc.data().stars != null) {
            starsData = starsData - ratingDoc.data().stars;
          } else {
            newNumRatings += 1;
          }

          const oldRatingTotal =
            orgDoc.data().avgRating * orgDoc.data().numRatings;
          const newAvgRating = (oldRatingTotal + starsData) / newNumRatings;

          transaction.set(ratingRef, {
            stars: data.stars,
            createdAt: admin.firestore.FieldValue.serverTimestamp(),
            message: message
          });

          return transaction.update(orgRef, {
            avgRating: newAvgRating,
            numRatings: newNumRatings
          });
        });
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

  return false;
});
