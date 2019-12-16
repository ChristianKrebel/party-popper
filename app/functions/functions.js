exports.errorIfNotAuthenticated = function(context) {
  if (!context.auth) {
    throw new functions.https.HttpsError(
      "failed-precondition",
      "The function must be called while authenticated."
    );
  }
  return true;
};

exports.errorIfEmpty = function(obj) {
  if (this.isEmpty(obj)) {
    throw new functions.https.HttpsError(
      "invalid-argument",
      "The Request Data is invalid."
    );
  }
  return true;
};

exports.isEmpty = function(str) {
  return !str || str.length === 0;
};
