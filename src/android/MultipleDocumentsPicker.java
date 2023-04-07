package com.akeo.cordova.plugin;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;
// Cordova-required packages
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultipleDocumentsPicker extends CordovaPlugin {
  private CallbackContext callback;
  private boolean base64;
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
        //   this.base64 = options.getInt("base64");
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
       } else if(type == 3) {
           intent.setType("*/*");
           String[] mimeTypes = {"application/pdf", "image/*", "video/*", "application/msword", "application/vnd.ms.excel", "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "application/vnd.openxmlformats-officedocument.presentationml.presentation"};
           intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
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


         String uriString = uri.toString();
         Log.d("data", "onActivityResult: uri"+uriString);

       try {
         Context context = cordova.getContext();
         InputStream in = context.getContentResolver().openInputStream(uri);
         byte[] bytes = getBytes(in);
         Log.d("data", "onActivityResult: bytes size="+bytes.length);
         Log.d("data", "onActivityResult: Base64string="+Base64.encodeToString(bytes,Base64.DEFAULT));
         String ansValue = Base64.encodeToString(bytes,Base64.DEFAULT);
         result.put("base64", ansValue);
       } catch (Exception e) {
         // TODO: handle exception
         e.printStackTrace();
         Log.d("error", "onActivityResult: " + e.toString());
       }

         result.put("uri", uri.toString());
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
  public byte[] getBytes(InputStream inputStream) throws IOException {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    int bufferSize = 1024;
    byte[] buffer = new byte[bufferSize];

    int len = 0;
    while ((len = inputStream.read(buffer)) != -1) {
      byteBuffer.write(buffer, 0, len);
    }
    return byteBuffer.toByteArray();
  }
}
