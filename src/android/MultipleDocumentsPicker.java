package com.akeo.cordova.plugin;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Log;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MultipleDocumentsPicker extends CordovaPlugin {
  private CallbackContext callback;
  @Override
  public boolean execute(String action, JSONArray args,
    final CallbackContext callbackContext) {
      /* Verify that the user sent a 'pick' action */
      if (!action.equals("pick")) {
        callbackContext.error("\"" + action + "\" is not a recognized action.");
        return false;
      }

      Integer type;
      this.callback = callbackContext;
      try {
          JSONObject options = args.getJSONObject(0);
          type = options.getInt("type");
          chooseFile(callbackContext, type);
          return true;
      } catch (JSONException e) {
          callbackContext.error("Error encountered: " + e.getMessage());
          return false;
      }
  }

   public void chooseFile (CallbackContext callbackContext, Integer type) {
       Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
       if(type == 1) {
        intent.setType("image/*");
       } else {
        intent.setType("*/*");
       }
       intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
       Intent chooser = Intent.createChooser(intent, "Select File");
       cordova.startActivityForResult(this, chooser, type);
   }

 @Override
	public void onActivityResult (int requestCode, int resultCode, Intent data) {
        JSONArray results = new JSONArray();
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            ClipData clipData = null;

            if (data != null) {
                uri = data.getData();
                clipData = data.getClipData();
            }
            if (uri != null) {
                results.put(getMetadata(uri));
            } else if (clipData != null && clipData.getItemCount() > 0) {
                final int length = clipData.getItemCount();
                for (int i = 0; i < length; ++i) {
                    ClipData.Item item = clipData.getItemAt(i);
                    results.put(getMetadata(item.getUri()));
                }
            }
            this.callback.success(results.toString());
        } else {
            this.callback.error("Execute failed");
        }
 }

 private Object getMetadata(Uri uri) {
     try {
         JSONObject result = new JSONObject();

         result.put("uri", uri.toString());
         // TODO vonovak - FIELD_FILE_COPY_URI is implemented on iOS only (copyTo) settings
         result.put("fileCopyUri", uri.toString());

         ContentResolver contentResolver = this.cordova.getActivity().getContentResolver();

         result.put("type", contentResolver.getType(uri));
         Cursor cursor = contentResolver.query(uri, null, null, null, null, null);
         if (cursor != null && cursor.moveToFirst()) {
             int displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
             if (!cursor.isNull(displayNameIndex)) {
                 result.put("name", cursor.getString(displayNameIndex));
             }

             if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                 int mimeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE);
                 if (!cursor.isNull(mimeIndex)) {
                     result.put("type", cursor.getString(mimeIndex));
                 }
             }

             int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
             if (!cursor.isNull(sizeIndex)) {
                 result.put("size", cursor.getInt(sizeIndex));
             }
         }
         return result;
     } catch (JSONException err) {
         return "Error";
     }
 }
}