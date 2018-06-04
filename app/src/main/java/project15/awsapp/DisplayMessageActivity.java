package project15.awsapp;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

// AWS library for S3 bucket and method for upload file
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

//Permission for load image file
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.*;


@RequiresApi(api = VERSION_CODES.M)
public class DisplayMessageActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String EXTRA_MESSAGE = "project15.awsapp.MESSAGE";
    //track Choosing Image Intent
    private static final int CHOOSING_IMAGE_REQUEST = 2;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private TextView tvFileName;
    private ImageView imageView;
    private Uri fileUri;
    private Bitmap bitmap;

    String realPath = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_message);

        //Permission check for mobile to read and write folder/image
        if (!checkPermission()) {
            openActivity();
        } else {
            if (checkPermission()) {
                requestPermissionAndContinue();
            } else {
                openActivity();
            }
        }
        //Connecting to AWS server to allow S3 use
        AWSMobileClient.getInstance().initialize(this).execute();

        imageView = (ImageView) findViewById(R.id.img_file);
        tvFileName = (TextView) findViewById(R.id.tv_file_name);
        tvFileName.setText("");

        findViewById(R.id.btn_choose_file).setOnClickListener(this);
    }

    // Check Permission for storage
    private boolean checkPermission() {

        return ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                ;
    }

    private void requestPermissionAndContinue() {
        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)
                    && ActivityCompat.shouldShowRequestPermissionRationale(this, READ_EXTERNAL_STORAGE)) {
                AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
                alertBuilder.setCancelable(true);
                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @RequiresApi(api = VERSION_CODES.M)
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(DisplayMessageActivity.this, new String[]{WRITE_EXTERNAL_STORAGE
                                ,  READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                    }
                });
                AlertDialog alert = alertBuilder.create();
                alert.show();
                Log.e("", "permission denied, show dialog");
            } else {
                ActivityCompat.requestPermissions(DisplayMessageActivity.this, new String[]{WRITE_EXTERNAL_STORAGE,
                        READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        } else {
            openActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.length > 0 && grantResults.length > 0) {

                boolean flag = true;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false;
                    }
                }
                if (flag) {
                    openActivity();
                } else {
                    finish();
                }

            } else {
                finish();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void openActivity() {
        //add your further process after giving permission or to download images from remote server.
    }


    // uploading file to S3
    private void uploadFile() {

            TransferUtility transferUtility =
                    TransferUtility.builder()
                            .context(getApplicationContext())
                            .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                            .s3Client(new AmazonS3Client(AWSMobileClient.getInstance().getCredentialsProvider()))
                            .build();

            // Read path and upload as "photo.jpg" file on S3 bucket
            TransferObserver uploadObserver =
                    transferUtility.upload("photo.jpg", new File(realPath));

            // Check status of transfering file
            uploadObserver.setTransferListener(new TransferListener() {

                @Override
                public void onStateChanged(int id, TransferState state) {
                    if (TransferState.COMPLETED == state) {
                        Toast.makeText(getApplicationContext(), "Upload Completed!", Toast.LENGTH_SHORT).show();

                        // if uploading is completed, automatically activate intent to load the output page
                        Intent intent = new Intent(DisplayMessageActivity.this, OutputAcitivty.class);
                        EditText editText = (EditText) findViewById(R.id.editText);
                        String message = editText.getText().toString();
                        intent.putExtra(EXTRA_MESSAGE, message);
                        startActivity(intent);
                        Log.i("Kathy", "Thread ID = " + Thread.currentThread().getId()); Log.i("Kathy", "before StartService");
                        Intent intentOne = new Intent(DisplayMessageActivity.this, MyService.class); startService(intentOne);
                        Intent intentFour = new Intent(DisplayMessageActivity.this, MyService.class); stopService(intentFour);
                    } else if (TransferState.FAILED == state) {
                        Toast.makeText(getApplicationContext(), "Upload Failed!", Toast.LENGTH_SHORT).show();
                    }
                }

                //Uploading progress text
                @Override
                public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                    float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                    int percentDone = (int) percentDonef;

                    tvFileName.setText("ID:" + id + "|bytesCurrent: " + bytesCurrent + "|bytesTotal: " + bytesTotal + "|" + percentDone + "%");
                }

                @Override
                public void onError(int id, Exception ex) {
                    ex.printStackTrace();
                }
            });
    }


    @Override
    public void onClick(View view) {
        int i = view.getId();

        if (i == R.id.btn_choose_file) {
            showChoosingFile();
        }
    }

    private void showChoosingFile() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image"), CHOOSING_IMAGE_REQUEST);
    }

    //Converting Uri to realPath depends on SDK version
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (bitmap != null) {
            bitmap.recycle();
        }

        if (requestCode == 1) {
            fileUri = data.getData();
            if (fileUri != null) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), fileUri);
                    imageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        if(resultCode == Activity.RESULT_OK && data != null){

            // SDK < API11
            if (Build.VERSION.SDK_INT < 11)
                try {
                    realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(
                            DisplayMessageActivity.this,
                            data.getData());
                    setTextViews(Build.VERSION.SDK_INT, data.getData()
                            . getPath(), realPath);
                } catch (Exception e) {
                    e.printStackTrace();
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver()
                            . query(selectedImage, filePathColumn, null,
                                    null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor
                            . getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();
                    bitmap = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(bitmap);
                }
                // SDK >= 11 && SDK < 19
            else if (Build.VERSION.SDK_INT < 19)
                try {
                    realPath = RealPathUtil.getRealPathFromURI_API11to18(
                            DisplayMessageActivity.this,
                            data.getData());
                    setTextViews(Build.VERSION.SDK_INT, data.getData()
                            . getPath(), realPath);
                } catch (Exception e1) {
                    e1.printStackTrace();
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    Cursor cursor = getContentResolver()
                            . query(selectedImage, filePathColumn, null,
                                    null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor
                            . getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();
                    bitmap = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(bitmap);
                }
                // SDK > 19 (Android 4.4)
            else {
                try {
                realPath = RealPathUtil.getRealPathFromURI_API19(DisplayMessageActivity.this, data.getData());
                setTextViews(Build.VERSION.SDK_INT, data.getData().getPath(), realPath);
                } catch (Exception e) {
                    Uri selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    Cursor cursor = getContentResolver()
                            .query(selectedImage, filePathColumn, null,
                                    null, null);
                    cursor.moveToFirst();
                    int columnIndex = cursor
                            .getColumnIndex(filePathColumn[0]);
                    String filePath = cursor.getString(columnIndex);
                    cursor.close();
                    bitmap = BitmapFactory.decodeFile(filePath);
                    imageView.setImageBitmap(bitmap);
                }
            }
        }
    }
    private void setTextViews(int sdk, String uriPath,String realPath){

        Uri uriFromPath = Uri.fromFile(new File(realPath));

        // you have two ways to display selected image

        // ( 1 ) imageView.setImageURI(uriFromPath);
        // ( 2 ) imageView.setImageBitmap(bitmap);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(uriFromPath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);

        Log.d("HMKCODE", "Build.VERSION.SDK_INT:"+sdk);
        Log.d("HMKCODE", "URI Path:"+uriPath);
        Log.d("HMKCODE", "Real Path: "+realPath);
        uploadFile();
    }

}





