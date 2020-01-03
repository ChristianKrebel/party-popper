const functions = require("firebase-functions");
const admin = require("firebase-admin");

const serviceAccount = require("./serviceAccount.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://party-popper-1770a.firebaseio.com"
});

module.exports = Object.assign(
  require("./userCreate"),
  require("./organizer"),
  require("./follower"),
  require("./events"),
  require("./rateOrganizer")
);
