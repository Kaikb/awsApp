package project15.awsapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Button to activate Display pages
        btn = (Button) findViewById(R.id.button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Toast.makeText(MainActivity.this, "Start AWS Rekognition",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, DisplayMessageActivity.class);
                startActivity(intent);
            }
        });
    }
}
