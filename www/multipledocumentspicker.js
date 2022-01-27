var exec = require('cordova/exec');

function MultipleDocumentsPicker() {}

MultipleDocumentsPicker.prototype.pick = function(type, successCallback, errorCallback) {
  var options = {};
  options.type = type;
  exec(successCallback, errorCallback, 'MultipleDocumentsPicker2', 'pick', [options]);
}

module.exports = new MultipleDocumentsPicker();