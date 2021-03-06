package com.qunar.qfwrapper.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Adler32;
import java.util.zip.CheckedInputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GzipTool {

    private static Logger logger = LoggerFactory.getLogger(GzipTool.class);

    public static String unZip(String log, InputStream in, String charSet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GZIPInputStream gis = null;
        try {
            gis = new GZIPInputStream(new CheckedInputStream(in, new Adler32()));
            byte[] _byte = new byte[1024];
            int len = 0;
            while ((len = gis.read(_byte)) != -1) {
                baos.write(_byte, 0, len);
            }
        } catch (IOException e) {
            logger.warn("unZip exception on " + log);
        } finally {
            baos.close();
            if (null != gis)
                gis.close();
        }
        return new String(baos.toByteArray(), charSet);
    }

    public static String unDeflater(String log, InputStream in, String charSet) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        InflaterInputStream gis = null;
        try {
            gis = new InflaterInputStream(new CheckedInputStream(in, new Adler32()), new Inflater(true));
            byte[] _byte = new byte[1024];
            int len = 0;
            while ((len = gis.read(_byte)) != -1) {
                baos.write(_byte, 0, len);
            }
        } catch (IOException e) {
            logger.warn("unDeflater exception on " + log);
        } finally {
            baos.close();
            if (null != gis)
                gis.close();
        }
        return new String(baos.toByteArray(), charSet);
    }
}
