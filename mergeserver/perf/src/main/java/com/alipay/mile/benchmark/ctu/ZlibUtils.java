/**
 * Alipay.com Inc.
 * Copyright (c) 2004-2012 All Rights Reserved.
 */
package com.alipay.mile.benchmark.ctu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPOutputStream;

import org.apache.log4j.Logger;

/**
 * 
 * @author yuzhong.zhao
 * @version $Id: ZlibUtils.java, v 0.1 2012-11-5 ����08:24:29 yuzhong.zhao Exp $
 */
public class ZlibUtils {
    /** Ĭ���ַ��� */
    private static final String CHARSET_UTF_8 = "utf-8";

    /** Ĭ��BUFF��С */
    public static final int     BUFFER        = 512;

    /** ��־��¼�� */
    private static final Logger logger        = Logger.getLogger(ZlibUtils.class);

    /**
     * �ֽ�ѹ��
     * 
     * @param data ��ѹ���ֽ�
     * @return ѹ������ֽ�
     * @throws Exception
     */
    public static byte[] compress(byte[] data) {
        if (data == null) {
            return null;
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            long start = System.currentTimeMillis();
            compress(bais, baos);
            baos.flush();
            if (logger.isDebugEnabled()) {
                logger.debug("compress, Y, " + (System.currentTimeMillis() - start)
                             + "ms");
            }
            return baos.toByteArray();
        } catch (Exception ex) {
            logger.error("compress error", ex);
            return new byte[] {};
        } finally {
            try {
                if (baos != null)
                    baos.close();
                if (bais != null)
                    bais.close();
            } catch (Exception ex) {
                logger.error("close stream error", ex);
            }
        }
    }

    /**
     * �����ֽ�����ѹ����ĳ���
     * @param data
     * @return ����
     */
    public static int getCompressedLength(byte[] data) {
        byte[] compress = compress(data);
        return compress.length;
    }

    /**
     * �����ַ���ѹ����ĳ���
     * @param data
     * @return ����
     */
    public static int getCompressedLength(String data) {
        try {
            byte[] compress = compress(data.getBytes(CHARSET_UTF_8));
            return compress.length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * �ַ���ѹ��
     * @param source
     * @return ѹ������ַ���
     */
    public static String compress(String source) {
        try {
            byte[] sourceArray = source.getBytes(CHARSET_UTF_8);
            byte[] targetArray = compress(sourceArray);
            return new String(targetArray, CHARSET_UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /** 
     * ����ѹ�� 
     *  
     * @param is ������
     * @param os �����
     * @throws Exception 
     */
    private static void compress(InputStream is, OutputStream os) throws Exception {

        GZIPOutputStream gos = null;
        try {
            gos = new GZIPOutputStream(os);
            int count;
            byte data[] = new byte[BUFFER];
            while ((count = is.read(data, 0, BUFFER)) != -1) {
                gos.write(data, 0, count);
            }
            gos.finish();
            gos.flush();
        } finally {
            try {
                if (gos != null)
                    gos.close();
            } catch (Exception ex) {
                logger.error("close stream error", ex);
            }
        }
    }
}
