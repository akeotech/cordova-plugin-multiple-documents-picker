import UIKit
import MobileCoreServices
import Foundation

@objc(MultipleDocumentsPicker) class MultipleDocumentsPicker : CDVPlugin {

    func callPicker () {
        let types = [kUTTypePDF, kUTTypeText, kUTTypeRTF, kUTTypeSpreadsheet, kUTTypeImage]
        let documentPicker = UIDocumentPickerViewController(documentTypes: types as [String], in: .import)
        documentPicker.delegate = self
        if #available(iOS 11.0, *) {
             documentPicker.allowsMultipleSelection = true
         }
        self.present(documentPicker, animated: true, completion: nil)
	}
    @objc(pick:)
    func pick(command: CDVInvokedUrlCommand) {
        self.commandCallback = command.callbackId

        self.callPicker()
    }

    func documentWasSelected(url: url) {
        self.send("DOCUMENT_SELECTED")
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

	func sendError (_ message: String) {
		self.send(message, CDVCommandStatus_ERROR)
	}
}

extension MultipleDocumentsPicker : UIDocumentPickerDelegate {
	@available(iOS 11.0, *)
	func documentPicker (
		_ controller: UIDocumentPickerViewController,
		didPickDocumentsAt urls: [URL]
	) {
		let picker = controller as! UIDocumentPickerViewController
		if let url = urls.first {
			self.documentWasSelected(url: url)
		}
	}

	func documentPicker (
		_ controller: UIDocumentPickerViewController,
		didPickDocumentAt url: URL
	) {
		let picker = controller as! UIDocumentPickerViewController
		self.documentWasSelected(url: url)
	}

	func documentPickerWasCancelled (_ controller: UIDocumentPickerViewController) {
		self.send("RESULT_CANCELED")
	}
}