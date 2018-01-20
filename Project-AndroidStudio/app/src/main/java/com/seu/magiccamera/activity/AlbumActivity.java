package com.seu.magiccamera.activity;

import android.Manifest;
import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.PermissionRequest;
import android.widget.Button;
import android.widget.ImageView;

import com.seu.magiccamera.MainActivity;
import com.seu.magiccamera.R;
import com.seu.magiccamera.pictureuitls.PictureUtils;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;


/**
 * Created by why8222 on 2016/3/18.
 */
public class AlbumActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_PICK_IMAGE = 222;
    Button mButtonAL = null;
    Button mButtonTP = null;
    public ImageView mImageView;
    public static final int SUCCESSCODE = 100;
    private static final int REQ_GALLERY = 333;
    private String mPublicPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        mButtonAL = (Button) findViewById(R.id.btAlbum);
        mButtonTP = (Button) findViewById(R.id.btTakeP);

        mButtonAL.setOnClickListener(this);
        mButtonTP.setOnClickListener(this);

        mImageView = (ImageView) findViewById(R.id.iv);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //获取相册中的照片
            case R.id.btAlbum:
                getImageFromAlbum();
                break;
            case R.id.btTakeP:
                showTakePicture();
                break;
            default:
                    break;
        }
    }

    /*
    ** 获取相册中的照片
     */
    public void getImageFromAlbum() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = SUCCESSCODE)
    public void doSomething() {
        //TODO: 申请权限成功后，做点儿什么？
        TakePicture();
    }

    @PermissionFail(requestCode = SUCCESSCODE)
    public void doFailThing() {
        //TODO: 申请权限失败后，作何处理？
    }

    //拍照功能
    private void showTakePicture() {
        PermissionGen.with(AlbumActivity.this)
                .addRequestCode(SUCCESSCODE)
                .permissions(
                        Manifest.permission.CAMERA,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .request();
    }


    private void TakePicture() {
        Intent takePictureIntent = new Intent(this, CameraActivity.class);
        //查看是否有拍照权限
        if (true)/*takePictureIntent.resolveActivity(getPackageManager()) != null)*/ {
            File photoFile = null;
            try {
                photoFile = PictureUtils.createPublicImageFile();
                mPublicPhotoPath = photoFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //启动拍照应用
            if (photoFile != null) {
                takePictureIntent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri photoURI = FileProvider.getUriForFile(this, "applicationId.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQ_GALLERY);
            }
        }
    }

    private Uri uri;
    String path;
    int mTargetW;
    int mTargetH;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mTargetH = mImageView.getMaxHeight();
        mTargetW = mImageView.getMaxWidth();

        switch (requestCode) {
            //take picture
            case REQ_GALLERY:
                if (requestCode != Activity.RESULT_OK) return;
                uri = Uri.parse(mPublicPhotoPath);
                path = uri.getPath();
                PictureUtils.galleryAddPic(mPublicPhotoPath, this);
                break;
            case REQUEST_CODE_PICK_IMAGE:
                if (data == null) return;
                uri = data.getData();
                int sdkVersion = Integer.valueOf(Build.VERSION.SDK_INT);
                if (sdkVersion >= 19) {
                    path = this.uri.getPath();
                    path = PictureUtils.getPath_above19(AlbumActivity.this, this.uri);
                } else {
                    path = PictureUtils.getFilePath_below19(AlbumActivity.this, this.uri);
                }
                break;
        }
        mImageView.setImageBitmap(PictureUtils.getSmallBitmap(path, mTargetW, mTargetH));
    }
}
