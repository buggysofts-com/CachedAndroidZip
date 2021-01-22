package com.ruet_cse_1503050.ragib.androidzip.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public final class CommonUtils {

    public static byte[] ReadStream(InputStream stream, boolean close_stream) {

        byte[] return_data = null;

        BufferedInputStream bin = null;
        ByteArrayOutputStream bout = null;

        try {

            bin = new BufferedInputStream(stream);
            bout = new ByteArrayOutputStream();

            int readNum;
            byte[] data = new byte[8192];
            while ((readNum = bin.read(data)) >= 0) {
                bout.write(data, 0, readNum);
            }

            return_data = bout.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (bout != null) {
                bout.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (close_stream) {
            try {
                if (bin != null) {
                    bin.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return return_data;

    }

}
