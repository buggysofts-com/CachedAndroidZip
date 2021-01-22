package com.ruet_cse_1503050.ragib.androidzip;

import android.content.Context;

import androidx.documentfile.provider.DocumentFile;

import com.ruet_cse_1503050.ragib.androidzip.utils.CommonUtils;
import com.ruet_cse_1503050.ragib.androidzip.utils.UriUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class AndroidZip {

    private final Context context;

    // For normal files, use a standard ZipFile
    private File rawSrc;
    private ZipFile zipFile;

    // For DocumentFile, cache entries for faster access
    private DocumentFile docSrc;
    private Map<String, ZipEntry> entryMap;

    public AndroidZip(Context context, File raw_source) throws Exception {

        this.context = context;

        this.rawSrc = raw_source;
        this.docSrc = null;

        this.zipFile = new ZipFile(raw_source);

        this.entryMap = null;

    }

    public AndroidZip(Context context, DocumentFile doc_source) throws Exception {

        this.context = context;

        File raw_source = new File(UriUtils.getDocumentFileAbsPath(context, doc_source, false));
        if (raw_source != null && raw_source.exists() & raw_source.canRead()) {
            ZipFile tmpZip = null;
            try {
                tmpZip = new ZipFile(raw_source);
            } catch (Exception e) {
            }
            if (tmpZip != null) {

                this.rawSrc = raw_source;
                this.docSrc = null;

                this.zipFile = tmpZip;

                this.entryMap = null;

            } else {

                this.rawSrc = raw_source;
                this.docSrc = doc_source;

                this.zipFile = null;

                this.entryMap = new HashMap<>(0);

            }
        } else {

            this.rawSrc = raw_source;
            this.docSrc = doc_source;

            this.zipFile = null;

            this.entryMap = new HashMap<>(0);

        }

    }

    public AndroidZip(Context context, DocumentFile doc_source, File raw_source) throws Exception {

        this.context = context;

        if (raw_source != null && raw_source.exists() & raw_source.canRead()) {
            ZipFile tmpZip = null;
            try {
                tmpZip = new ZipFile(raw_source);
            } catch (Exception e) {
            }
            if (tmpZip != null) {

                this.rawSrc = raw_source;
                this.docSrc = null;

                this.zipFile = tmpZip;

                this.entryMap = null;

            } else {

                this.rawSrc = raw_source;
                this.docSrc = doc_source;

                this.zipFile = null;

                this.entryMap = new HashMap<>(0);

            }
        } else {

            this.rawSrc = raw_source;
            this.docSrc = doc_source;

            this.zipFile = null;

            this.entryMap = new HashMap<>(0);

        }

    }

    /**
     * Returs the <b>ZipEntry</b> with name <i><b>name</b></i>, or null if not found or an exception was occurred.
     *
     * @param name Path/Name of the entry within the ZipFile.
     */
    public ZipEntry getEntry(String name) {

        if (zipFile != null) {

            // zip file is not null, return from standard ZIpFile API
            return zipFile.getEntry(name);

        } else {

            ZipEntry entry = entryMap.get(name);

            if (entry == null) {

                ZipInputStream zipInputStream = null;
                try {
                    zipInputStream = new ZipInputStream(
                            new BufferedInputStream(
                                    context.getContentResolver().openInputStream(docSrc.getUri())
                            )
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (zipInputStream != null) {
                    try {
                        while ((entry = zipInputStream.getNextEntry()) != null) {
                            String entryName = entry.getName();
                            entryMap.put(entryName, entry);
                            if (entryName.equals(name)) {
                                return entry;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        zipInputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            } else {

                // already available in the entry map
                return entry;

            }

        }

        return null;

    }

    public InputStream getInputStream(String name) {

        if (zipFile != null) {

            // zip file is not null, return from standard ZIpFile API
            try {
                return zipFile.getInputStream(getEntry(name));
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            ZipInputStream zipInputStream = null;
            try {
                zipInputStream = new ZipInputStream(
                        new BufferedInputStream(
                                context.getContentResolver().openInputStream(docSrc.getUri())
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (zipInputStream != null) {
                try {
                    ZipEntry entry = null;
                    while ((entry = zipInputStream.getNextEntry()) != null) {
                        String entryName = entry.getName();
                        entryMap.put(entryName, entry);
                        if (entryName.equals(name)) {
                            return zipInputStream;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    zipInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        return null;
    }

    public byte[] getEntryData(String name) {

        byte[] data = null;

        InputStream is = null;
        try {
            is = getInputStream(name);
            if (is != null) {
                data = CommonUtils.ReadStream(is, true);
            } else {
                data = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return data;
    }

    public List<ZipEntry> getEntries() {

        List<ZipEntry> entry_list = new ArrayList<>(0);

        if (zipFile != null) {

            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                entry_list.add(entries.nextElement());
            }

        } else {

            ZipInputStream zipInputStream = null;
            try {
                zipInputStream = new ZipInputStream(
                        new BufferedInputStream(
                                context.getContentResolver().openInputStream(docSrc.getUri())
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (zipInputStream != null) {
                try {
                    ZipEntry entry = null;
                    while ((entry = zipInputStream.getNextEntry()) != null) {
                        String entryName = entry.getName();
                        entryMap.put(entryName, entry);
                        entry_list.add(entry);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    entry_list = new ArrayList<>(0);
                }
                try {
                    zipInputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        return entry_list;
    }

    public boolean ExtractEntry(String entry_name, File dest) {

        boolean ret = true;
        BufferedInputStream is = null;
        BufferedOutputStream os = null;

        try {
            is = new BufferedInputStream(getInputStream(entry_name));
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (is != null) {
            byte[] data = new byte[8192];
            int readnum = 0;
            try {
                os = new BufferedOutputStream(new FileOutputStream(dest));
                while ((readnum = is.read(data)) >= 0) {
                    os.write(data, 0, readnum);
                }
            } catch (Exception e) {
                e.printStackTrace();
                ret = false;
            }
        } else {
            ret = false;
        }

        try {
            if (os != null) {
                os.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (is != null) {
                is.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;

    }

}
