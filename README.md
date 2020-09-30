# Multiple Documents Picker

## Overview

Multiple documents picker plugin for Cordova.

Install with Cordova CLI:

	$ cordova plugin add cordova-plugin-multiple-documents-picker

Supported Platforms:

* Android

* iOS

## API

	/**
	 * Displays native prompt for user to select files.
	 *
	 * @param type (eg. 1 for images only and 2 for all type of files).
	 *
	 * @returns Promise containing selected file's data,
	 * URI, MIME type and name.
	 *
	 * If user cancels or error occurs, promise will be rejected.
	 */
	multipleDocumentsPicker.pick(type: number) : Promise<{
		type: string;
		name: string;
		uri: string;
	}>

## Example Usage

	(async () => {
		const file = await multipleDocumentsPicker.pick(2);
		console.log(file);
	})();
