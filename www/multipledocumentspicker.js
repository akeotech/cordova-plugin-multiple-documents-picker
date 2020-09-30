// Empty constructor
function MultipleDocumentsPicker() {}

// The function that passes work along to native shells
MultipleDocumentsPicker.prototype.pick = function(type, successCallback, errorCallback) {
  var options = {};
  options.type = type;
  cordova.exec(successCallback, errorCallback, 'MultipleDocumentsPicker', 'pick', [options]);
}

// Installation constructor that binds MultipleDocumentsPicker to window
MultipleDocumentsPicker.install = function() {
  if (!window.plugins) {
    window.plugins = {};
  }
  window.plugins.multipleDocumentsPicker = new MultipleDocumentsPicker();
  return window.plugins.multipleDocumentsPicker;
};
cordova.addConstructor(MultipleDocumentsPicker.install);