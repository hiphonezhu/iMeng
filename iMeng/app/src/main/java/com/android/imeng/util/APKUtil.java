package com.android.imeng.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.text.TextUtils;

/**
 * 基础工具类 [尽量减少类似Util的类存在]
 * 
 * @author hiphonezhu@gmail.com
 * @version [Android-BaseLine, 2014-8-29]
 */
public class APKUtil {
    /**
     * 获得版本号
     *
     * @return
     */
    public static int getVerCode(Context context) {
        int verCode = -1;
        try {
            verCode = context.getApplicationContext().getPackageManager()
                    .getPackageInfo(context.getApplicationContext().getPackageName(), 0).versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return verCode;
    }

    /**
     * 获得版本名称
     *
     * @return
     */
    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getApplicationContext().getPackageManager()
                    .getPackageInfo(context.getApplicationContext().getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    /**
     * 获得APP包名
     *
     * @return
     */
    public static String getPackageName(Context context) {
        return context.getApplicationContext().getPackageName();
    }

    /**
     * 获得磁盘缓存目录 [PS：应用卸载后会被自动删除]
     *
     * @param context
     * @param uniqueName
     * @return
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getApplicationContext().getExternalCacheDir().getPath();
        } else {
            cachePath = context.getApplicationContext().getFilesDir().getPath();
        }
        File dir = new File(cachePath + File.separator + uniqueName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    /**
     * 组装参数
     *
     * @param parameters
     * @return
     */
    public static String getParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        Set<String> keys = parameters.keySet();
        Iterator<String> keysIt = keys.iterator();
        while (keysIt.hasNext()) {
            String key = keysIt.next();
            if (!TextUtils.isEmpty(key)) {
                Object value = parameters.get(key);
                if (value == null) {
                    value = "";
                }
                sb.append(key + "=" + value + "&");
            }
        }
        if (sb.length() > 0) {
            return sb.substring(0, sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * dp转px
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    /**
     * 将字符串转成MD5值
     * @param string
     * @return
     */
    public static String stringToMD5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10)
                hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }

        return hex.toString();
    }

    /**
     * 保存流到文件
     * @param is
     * @param savePath
     * @throws IOException
     */
    public static void save2File(InputStream is, String savePath) throws IOException {
        File saveFile = new File(savePath);
        if (!saveFile.exists())
        {
            saveFile.createNewFile();
        }
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(saveFile);
            byte[] buffer = new byte[2048];
            int len = -1;
            while((len = is.read(buffer)) != -1)
            {
                fos.write(buffer, 0, len);
            }
            fos.flush();
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
            if (is != null)
            {
                is.close();
            }
        }
    }
}
