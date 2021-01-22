package com.ruet_cse_1503050.ragib.androidzip.utils;

import android.net.Uri;
import android.os.Environment;
import android.os.storage.StorageVolume;

import java.io.File;

public class DeviceAccessInfo {

    private final StorageVolume storageVolume;
    private final String path;
    private final boolean isPrimary;

    private String uriStr;
    private boolean allowed;

    public DeviceAccessInfo(StorageVolume storageVolume, String path) {
        this.storageVolume = storageVolume;
        this.path = path;
        this.isPrimary = path.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath());
        this.allowed = false;
        try {
            File tmp = new File(path);
            if (tmp != null && tmp.exists() && tmp.canRead() && tmp.canWrite()) {
                this.allowed = true;
                this.uriStr = Uri.fromFile(new File(path)).toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StorageVolume getStorageVolume() {
        return storageVolume;
    }

    public String getPath() {
        return path;
    }

    public boolean isAllowed() {
        return allowed;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    public String getUriStr() {
        return uriStr;
    }

    public void setUriStr(String uriStr) {
        this.uriStr = uriStr;
    }

    public boolean isPrimary() {
        return isPrimary;
    }
}
