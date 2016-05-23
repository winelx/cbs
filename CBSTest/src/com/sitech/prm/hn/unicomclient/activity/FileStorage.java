package com.sitech.prm.hn.unicomclient.activity;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by zero on 2015/11/9.
 * email:evil_song@126.com
 */
public class FileStorage {
    /**
     * 保存文件内容
     *
     * @param context
     * @param filename 文件名称
     * @param content  文件内容
     * @param mode     存储方式
     * @throws IOException IO异常
     */
    public void WriteFiles(Context context, String filename, String content, int mode) throws IOException {
        FileOutputStream fileOutputStream = context.openFileOutput(filename, mode);
        fileOutputStream.write(content.getBytes());
        fileOutputStream.close();
    }

    public String ReadFiles(Context context, String filename) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        FileInputStream fileInputStream = context.openFileInput(filename);
        byte[] bytes = new byte[1024];
        int length = 0;
        while ((length = fileInputStream.read(bytes)) != -1) {
            byteArrayOutputStream.write(bytes, 0, length);
        }
        String content = byteArrayOutputStream.toString();
        fileInputStream.close();
        byteArrayOutputStream.close();
        return content;
    }
}
