package project15.awsapp;

import android.app.Service;
import android.content.Intent; import android.os.IBinder;
import android.util.Log;
import com.amazonaws.auth.CognitoCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognitionClient;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;
import com.amazonaws.services.s3.AmazonS3Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class MyService extends Service {
    String photo = "photo.jpg";
    String bucket = "recognition-deployments-mobilehub-862349224";

    public MyService() {
        new Thread(networkTask).start();
    }

    Runnable networkTask = new Runnable() {
        @Override
        public void run(){
            CognitoCredentialsProvider credentialsProvider = new CognitoCredentialsProvider( "us-east-1:3e275793-b7f1-435b-addb-050cb376e39b", Regions.US_EAST_1);

            DetectLabelsRequest request = new DetectLabelsRequest()
                    .withImage(new Image().withS3Object(new S3Object().withName(photo).withBucket(bucket)))
                    .withMaxLabels(10)
                    .withMinConfidence(75F);
            AmazonRekognitionClient rekognitionClient = new AmazonRekognitionClient(credentialsProvider);
            AmazonS3Client s3Client = new AmazonS3Client(credentialsProvider);
            DetectLabelsResult result = rekognitionClient.detectLabels(request);

            List<Label> labels = result.getLabels();
            InputStream inputStream = getResources().openRawResource(R.raw.plantname);
            String plantString = getString(inputStream);
            Log.d("myLog","Detected labels for "+photo);
            Intent myIntent = new Intent(MyService.this, OutputAcitivty.class);

            NumberFormat nt = NumberFormat.getPercentInstance();
            nt.setMinimumFractionDigits(1);
            Integer labelNumber = 0;
            for(Label label:labels) {
                if (plantString.contains(label.getName())) {
                    myIntent.putExtra("mytext"+labelNumber.toString(), label.getName() + ": " + nt.format(label.getConfidence()/100).toString());
                    labelNumber += 1;
                    Log.d("in", label.getName() + ": " + label.getConfidence().toString());
                }
                else Log.d("not in", label.getName() + ": " + label.getConfidence().toString());
            }
            startActivity(myIntent);
        }
    };

    // Read data from "downloadData.txt", which is provided from USDA United States Department of Agriculture

    public String getString(InputStream inputStream) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "utf-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
