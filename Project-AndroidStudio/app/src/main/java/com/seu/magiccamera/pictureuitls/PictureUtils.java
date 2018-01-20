package com.seu.magiccamera.pictureuitls;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObservable;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

/**
 * Created by caotaiqiang on 20/01/2018.
 */

public class PictureUtils {
    /*
    ** below API19
     */

    public static String getFilePath_below19(Context context, Uri uri) {
        String[] proj = {MediaStore.Images.Media.DATA};

        Cursor cursor = context.getContentResolver().query(uri, proj, null, null, null);

        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);

        cursor.moveToFirst();

        String path = cursor.getString(column_index);

        return path;
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static String getPath_above19(final Context context, final Uri uri) {
        final boolean isO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;

        //Provider
        if (isO && DocumentsContract.isDocumentUri(context, uri)) {
            // external
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            } else if (isDownloadDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null,null);
            } else if(isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /*
    ** Get the value of the data column for this Uri. This is useful for
    ** MediaStore Uris, and other file-based ContentProviders
    **
    **@param context The context
    **@param uri     The Uri to query
    **@param selection Filter used in the query
    **@param selectionArgs  Selection arguments used in the query
    **@return The value of the _data column, which is typically a file path
    **
     */
    public static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /*
    ** @param uri The Uri to check
    ** @return whrther the Uri authority is ExternalStorageProvider
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /*
    ** @param uri The Uri to check
    ** @return whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /*
    ** @param uri The Uri to check
    ** @return whether the Uri authority is MeidaProvider
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /*
    ** @param uri the Uri to check
    ** @return whether the Uri authority is Google Photos
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /*
    * 将照片加到相册
     */
    public static void galleryAddPic(String mPublicPhotoPath, Context context) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mPublicPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }

    /*
    * 创建临时图片存储路径
     */
    public static File createPublicImageFile() throws IOException {
        File appDir = new File(Environment.getExternalStorageDirectory() + "/photodemo");
        if (!appDir.exists()) {
            appDir.mkdir();
        }

        String fileName = System.currentTimeMillis() + ".jpg";
        File file = new File(appDir, fileName);
        return file;
    }

    /*
    * 图片压缩
     */
    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width  = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float)reqHeight);
            final int widthRatio = Math.round((float) width / (float)reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;
    }


    /*
    * 根据路径获得图片并压缩返回bitmap用于显示
     */
    public static Bitmap getSmallBitmap(String filePath, int reqWidth, int reqHeigth) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //只返回图片的大小信息
        BitmapFactory.decodeFile(filePath, options);
        //Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeigth);
        //decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }
}
