const functions = require("firebase-functions");
const admin = require("firebase-admin");

const serviceAccount = require("./serviceAccount.json");

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://party-popper-1770a.firebaseio.com"
});

module.exports = {
  ...require("./userCreate.js"),
  ...require("./organizer.js"),
  ...require("./follower.js"),
  ...require("./events.js")
};
