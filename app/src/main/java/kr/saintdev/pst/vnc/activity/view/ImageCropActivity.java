package kr.saintdev.pst.vnc.activity.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.theartofdev.edmodo.cropper.CropImage;

import java.io.File;

/**
 * Created by yuuki on 18. 4. 14.
 */

public class ImageCropActivity extends AppCompatActivity {
    File image = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String path = intent.getStringExtra("image");

        this.image = new File(path);
        Uri uri = Uri.fromFile(this.image);

        CropImage.activity(uri).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK ) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            String file = null;

            Uri resultUri = result.getUri();
            file = resultUri.getPath();

            Intent intent = new Intent(this, TranslateActivity.class);
            intent.putExtra("image", file);
            startActivity(intent);
        }

        finish();
    }
}
