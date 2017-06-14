package online.devliving.rxfirebasesample;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;

import online.devliving.rxfirebasesample.helpers.FirebaseHelper;
import online.devliving.rxfirebasesample.models.User;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Mehedi Hasan Khan <mehedi.mailing@gmail.com> on 6/12/17.
 */

public class ProfileActivity extends BaseActivity {
    final int PICK_IMAGE = 109;
    final int CROP_IMAGE = 111;
    final int REQ_STORAGE_PERMISSION = 113;

    ImageView propicView;
    TextView userName, userEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        propicView = (ImageView) findViewById(R.id.user_pic);
        userName = (TextView) findViewById(R.id.user_name);
        userEmail = (TextView) findViewById(R.id.user_email);

        propicView.setOnClickListener(v -> onImageClicked());

        reloadInfo();
    }

    void reloadInfo(){
        showProgressDialog("loading...");

        FirebaseHelper.getUser()
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(user -> updateView(user),
                        error -> {
                            hideProgressDialog();
                            showToast("Error: " + error.getMessage());
                            finish();
                        },
                        () -> hideProgressDialog());
    }

    void onImageClicked(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    "Required for storing cropped image",
                    REQ_STORAGE_PERMISSION);
        }
        else showImagePicker();
    }

    void showImagePicker(){
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");

        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

        startActivityForResult(chooserIntent, PICK_IMAGE);
    }

    void updateView(User user){
        userName.setText(user.username);
        userEmail.setText(user.email);

        if(user.photo_url != null) {
            Glide.with(this)
                    .load(user.photo_url)
                    .placeholder(R.drawable.ic_action_account_circle_40)
                    .centerCrop()
                    .into(propicView);
        }
    }

    void changePropic(Uri imageUri){
        showProgressDialog("uploading...");
        FirebaseHelper.changeProfilePhoto(imageUri)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(done -> {
                    hideProgressDialog();
                    reloadInfo();
                }, error -> {
                    error.printStackTrace();
                    hideProgressDialog();
                },
                () -> hideProgressDialog());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQ_STORAGE_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                showImagePicker();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE)
        {
            if(resultCode == RESULT_OK){
                Uri pickedImage = data.getData();

                File outputFile = new File(getExternalCacheDir(), "propic.jpg");
                if(!outputFile.exists()){
                    try {
                        outputFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                        showToast("Error: " + e.getMessage());
                        return;
                    }
                }

                Uri croppedImage = Uri.fromFile(outputFile);
                UCrop.Options options = new UCrop.Options();
                options.setCompressionFormat(Bitmap.CompressFormat.JPEG);

                UCrop.of(pickedImage, croppedImage)
                        .withOptions(options)
                        .start(this, CROP_IMAGE);

            }
        }
        else if(requestCode == CROP_IMAGE){
            if(resultCode == RESULT_OK){
                Uri croppedImage = UCrop.getOutput(data);
                if(croppedImage != null) changePropic(croppedImage);
            }
        }
    }
}
