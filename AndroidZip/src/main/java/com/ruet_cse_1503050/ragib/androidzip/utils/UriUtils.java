package com.ruet_cse_1503050.ragib.androidzip.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class UriUtils {

    @Nullable
    public static String getDocumentFileAbsPath(@NonNull Context context, @NonNull DocumentFile file, boolean isTreeDoc) {
        String path = null;
        try {
            path = getPath(context, file.getUri(), isTreeDoc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }

    @Nullable
    private static String getAbsFilePathFromUri(@NonNull Context context, @Nullable final Uri uri, boolean isTreeDoc) {
        String path = null;
        try {
            path = getPath(context, uri, isTreeDoc);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
    
    @Nullable
    public static String getAbsFilePathFromFileURI(String uri_str) {
        File tmp_file = null;
        try {
            tmp_file = new File(new URI(uri_str));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return tmp_file != null ? tmp_file.getAbsolutePath() : null;
    }

    @Nullable
    public static String getRootPath(@NonNull Context context, @NonNull DocumentFile file, boolean isTreeDoc) {
        return ExtractVolumePath(context, file.getUri(), isTreeDoc);
    }

    @Nullable
    public static String ExtractVolumePath( Context context, @Nullable Uri uri, boolean isTreeDoc) {
        String selection = null;
        String[] selectionArgs = null;

        if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            try {
                if (isExternalStorageDocument(uri)) {
                    final String docId = isTreeDoc ? DocumentsContract.getTreeDocumentId(uri) : DocumentsContract.getDocumentId(uri);
                    final String[] id_components = docId.split(":");
                    if (id_components[0].equals("primary")) {
                        // shared internal storage
                        return Environment.getExternalStorageDirectory().getAbsolutePath();
                    } else {
                        // other external media
                        try {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                                Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                                Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                                Method getState = storageVolumeClazz.getMethod("getState");
                                Method getUuid = storageVolumeClazz.getMethod("getUuid");
                                Method getPath = storageVolumeClazz.getMethod("getPath");
                                StorageVolume[] result = (StorageVolume[]) getVolumeList.invoke(mStorageManager);
                                for (int i = 0; result != null && i < result.length; i++) {
                                    StorageVolume storageVolumeElement = result[i];
                                    boolean mounted = false;
                                    try {
                                        mounted = ((String) getState.invoke(storageVolumeElement)).equals(Environment.MEDIA_MOUNTED);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if(mounted){
                                        String uuid = (String) getUuid.invoke(storageVolumeElement);
                                        if (uuid != null && uuid.equals(id_components[0])) {
                                            String volPath = null;
                                            try {
                                                volPath = (String) getPath.invoke(storageVolumeElement);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                            if (volPath != null && !volPath.isEmpty()) {
                                                return volPath;
                                            }
                                        }
                                    }
                                }
                            } else {
                                StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                                List<StorageVolume> volumes = mStorageManager.getStorageVolumes();
                                for (int i = 0; volumes != null && i < volumes.size(); i++) {
                                    StorageVolume storageVolumeElement = volumes.get(i);
                                    if(storageVolumeElement.getState().equals(Environment.MEDIA_MOUNTED)){
                                        String uuid = storageVolumeElement.getUuid();
                                        if (uuid != null && uuid.equals(id_components[0])) {
                                            return storageVolumeElement.getDirectory().getAbsolutePath();
                                        }
                                    }
                                }
                            }
                            return null;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                } else if (isDownloadsDocument(uri)) {
                    // downloads folder
                    return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                } else if (isMediaDocument(uri)) {
                    // media directory components
                    final String docId = isTreeDoc ? DocumentsContract.getTreeDocumentId(uri) : DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("image".equals(type)) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    selection = "_id=?";
                    selectionArgs = new String[]{split[1]};
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(cursor != null){
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    public static List<DeviceAccessInfo> getStorageDevices(Context context) {

        List<DeviceAccessInfo> devices = new ArrayList<>(0);

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                Method getState = storageVolumeClazz.getMethod("getState");
                Method getUuid = storageVolumeClazz.getMethod("getUuid");
                Method getPath = storageVolumeClazz.getMethod("getPath");
                StorageVolume[] result = (StorageVolume[]) getVolumeList.invoke(mStorageManager);
                for (int i = 0; result != null && i < result.length; i++) {
                    StorageVolume storageVolumeElement = result[i];
                    boolean mounted = false;
                    try {
                        mounted = ((String) getState.invoke(storageVolumeElement)).equals(Environment.MEDIA_MOUNTED);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if(mounted){
                        String volPath = null;
                        try {
                            volPath = (String) getPath.invoke(storageVolumeElement);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (volPath != null && !volPath.isEmpty()) {
                            devices.add(new DeviceAccessInfo(storageVolumeElement, volPath));
                        }
                    }
                }
            } else {
                StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                List<StorageVolume> volumes = mStorageManager.getStorageVolumes();
                for (int i = 0; volumes != null && i < volumes.size(); i++) {
                    StorageVolume storageVolumeElement = volumes.get(i);
                    if(storageVolumeElement.getState().equals(Environment.MEDIA_MOUNTED)){
                        String uuid = storageVolumeElement.getUuid();
                        if (uuid != null) {
                            devices.add(
                                    new DeviceAccessInfo(
                                            storageVolumeElement,
                                            storageVolumeElement.getDirectory().getAbsolutePath()
                                    )
                            );
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return devices;
    }

    public static String getPath(final Context context, Uri uri, boolean isTreeDoc) throws URISyntaxException {

        String selection = null;
        String[] selectionArgs = null;

        if (DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            final String docIdT = isTreeDoc ? DocumentsContract.getTreeDocumentId(uri) : DocumentsContract.getDocumentId(uri);
            final String[] id_componentsT = docIdT.split(":");
            try {
                if (isExternalStorageDocument(uri)) {
                    final String docId = isTreeDoc ? DocumentsContract.getTreeDocumentId(uri) : DocumentsContract.getDocumentId(uri);
                    final String[] id_components = docId.split(":");
                    if (id_components[0].equals("primary")) {
                        // shared internal storage
                        if (id_components.length < 2) {
                            return Environment.getExternalStorageDirectory().getAbsolutePath();
                        } else {
                            if (id_components[1] == null || id_components[1].isEmpty()) {
                                return Environment.getExternalStorageDirectory().getAbsolutePath();
                            } else {
                                return Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + id_components[1];
                            }
                        }
                    } else {
                        // other external media
                        try {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                                StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                                Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
                                Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
                                Method getState = storageVolumeClazz.getMethod("getState");
                                Method getUuid = storageVolumeClazz.getMethod("getUuid");
                                Method getPath = storageVolumeClazz.getMethod("getPath");
                                StorageVolume[] result = (StorageVolume[]) getVolumeList.invoke(mStorageManager);
                                for (int i = 0; result != null && i < result.length; i++) {
                                    StorageVolume storageVolumeElement = result[i];
                                    boolean mounted = false;
                                    try {
                                        mounted = ((String) getState.invoke(storageVolumeElement)).equals(Environment.MEDIA_MOUNTED);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    if(mounted){
                                        String uuid = (String) getUuid.invoke(storageVolumeElement);
                                        if (uuid != null && uuid.equals(id_components[0])) {
                                            if (id_components.length < 2) {
                                                return (String) getPath.invoke(storageVolumeElement);
                                            } else if (id_components[1] == null || id_components[1].equals("")) {
                                                return (String) getPath.invoke(storageVolumeElement);
                                            } else {
                                                return (String) getPath.invoke(storageVolumeElement) + File.separator + id_components[1];
                                            }
                                        }
                                    }
                                }
                            } else {
                                StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
                                List<StorageVolume> volumes = mStorageManager.getStorageVolumes();
                                for (int i = 0; volumes != null && i < volumes.size(); i++) {
                                    StorageVolume storageVolumeElement = volumes.get(i);
                                    if(storageVolumeElement.getState().equals(Environment.MEDIA_MOUNTED)){
                                        String uuid = storageVolumeElement.getUuid();
                                        if (uuid != null && uuid.equals(id_components[0])) {
                                            return storageVolumeElement.getDirectory().getAbsolutePath() + File.separator + id_components[1];
                                        }
                                    }
                                }
                            }
                            return null;
                        } catch (Exception e) {
                            return null;
                        }
                    }
                } else if (isDownloadsDocument(uri)) {
                    // downloads folder
                    final String docId = isTreeDoc ? DocumentsContract.getTreeDocumentId(uri) : DocumentsContract.getDocumentId(uri);
                    if (docId.equals("downloads")) {
                        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
                    } else if (docId.startsWith("raw:")) {
                        return docId.split(":")[1];
                    } else {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                            String ID = null;
                            if(docId.contains(":")){
                                ID = docId.split(":")[1];
                            } else {
                                ID = docId;
                            }
                            uri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                            selection = "_id=?";
                            selectionArgs = new String[]{ID};
                        } else {
                            // already handled
                        }
                    }
                } else if (isMediaDocument(uri)) {
                    // media directory components
                    final String docId = isTreeDoc ? DocumentsContract.getTreeDocumentId(uri) : DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("image".equals(type)) {
                        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(type)) {
                        uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else if ("audio".equals(type)) {
                        uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                    }
                    selection = "_id=?";
                    selectionArgs = new String[]{split[1]};
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(cursor != null){
                cursor.close();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        
        return null;
        
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}