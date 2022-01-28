import UIKit
import MobileCoreServices
import Foundation

@objc(MultipleDocumentsPicker) class MultipleDocumentsPicker : CDVPlugin {
    var commandCallback: String?
    func callPicker (type: Int) {
        var types = [String]()
        if(type == 1){
            types = ["public.image"]
        } else {
            types = ["public.item"]
        }
        let documentPicker = UIDocumentPickerViewController(documentTypes: types as [String], in: .import)
        documentPicker.delegate = self
        if #available(iOS 11.0, *) {
             documentPicker.allowsMultipleSelection = true
         }
        self.viewController.present(documentPicker, animated: true, completion: nil)
    }
    @objc(pick:)
    func pick(command: CDVInvokedUrlCommand) {
        self.commandCallback = command.callbackId
        let type = command.arguments.first as! NSDictionary
        let argType = type["type"] as! Int

        self.callPicker(type: argType)
    }
    
    func detectMimeType (_ url: URL) -> String {
        if let uti = UTTypeCreatePreferredIdentifierForTag(
            kUTTagClassFilenameExtension,
            url.pathExtension as CFString,
            nil
        )?.takeRetainedValue() {
            if let mimetype = UTTypeCopyPreferredTagWithClass(
                uti,
                kUTTagClassMIMEType
            )?.takeRetainedValue() as String? {
                return mimetype
            }
        }

        return "application/octet-stream"
    }

    func documentWasSelected(urls: [URL]) {
        var documentsArray: [[String: String]] = [[String: String]]()
        for url in urls {
            let fileData = try! Data.init(contentsOf: url)
            let base64String: String = fileData.base64EncodedString(options: NSData.Base64EncodingOptions.init(rawValue: 0))

            let result = (["uri": url.absoluteString, "base64": base64String, "name": url.lastPathComponent, "type": self.detectMimeType(url)])
            documentsArray.append(result)
        }
        do {
            if let message = try String(
                data: JSONSerialization.data(
                    withJSONObject: documentsArray,
                    options: []
                ),
                encoding: String.Encoding.utf8
            ) {
                self.send(message)
            }
            else {
                self.sendError("Serializing result failed.")
            }
        }
        catch let error {
            self.sendError(error.localizedDescription)
        }
    }

    func send (_ message: String, _ status: CDVCommandStatus = CDVCommandStatus_OK) {
        if let callbackId = self.commandCallback {
            self.commandCallback = nil

            let pluginResult = CDVPluginResult(
                status: status,
                messageAs: message
            )

            self.commandDelegate!.send(
                pluginResult,
                callbackId: callbackId
            )
        }
    }
    
    func sendError (_ message: String, _ status: CDVCommandStatus = CDVCommandStatus_ERROR) {
        if let callbackId = self.commandCallback {
            self.commandCallback = nil

            let pluginResult = CDVPluginResult(
                status: status,
                messageAs: message
            )

            self.commandDelegate!.send(
                pluginResult,
                callbackId: callbackId
            )
        }
    }
}

extension MultipleDocumentsPicker : UIDocumentPickerDelegate {
    @available(iOS 11.0, *)
    func documentPicker (
        _ controller: UIDocumentPickerViewController,
        didPickDocumentsAt urls: [URL]
    ) {
        self.documentWasSelected(urls: urls);
    }

    private func documentPicker (
        _ controller: UIDocumentPickerViewController,
        didPickDocumentAt urls: [URL]
    ) {
        self.documentWasSelected(urls: urls)
    }

    func documentPickerWasCancelled (_ controller: UIDocumentPickerViewController) {
        self.sendError("RESULT_CANCELED")
    }
}
