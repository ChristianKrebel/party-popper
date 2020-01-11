const functions = require("firebase-functions");
const admin = require("firebase-admin");

const db = admin.firestore();
const helper = require("./functions");
const geo = require("geofirex").init(admin);

exports.getGeoPoint = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.lat);
  helper.errorIfEmpty(data.lng);

  return geo.point(Number(data.lat), Number(data.lng));
});

exports.getNearbyEvents = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.lat);
  helper.errorIfEmpty(data.lng);
  helper.errorIfEmpty(data.radius);

  const center = geo.point(Number(data.lat), Number(data.lng));
  const radius = parseInt(data.radius);
  const field = "position";

  const firestoreRef = db.collection("events").where();
  const geoRef = geo.query(firestoreRef);

  const query = geoRef.within(center, radius, field);
  return await query.get();
});

exports.createEvent = functions.https.onCall(async (data, context) => {
  helper.errorIfEmpty(data.lat);
  helper.errorIfEmpty(data.lng);

  const geopoint = geo.point(Number(data.lat), Number(data.lng));

  await db
    .collection("events")
    .doc("PrKq4KBElP8bsQgSWYOQ")
    .update(geopoint);
});

exports.getNearbyOrganizer = functions.https.onCall(
  async (data, context) => {}
);
