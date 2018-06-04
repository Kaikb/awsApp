package project15.awsapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class OutputAcitivty extends AppCompatActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.output_display);

        Intent update = getIntent();
        Bundle b = update.getExtras();
        EditText editText1 = (EditText) findViewById(R.id.textOutputView);
        editText1.setFocusable(false);
        editText1.setFocusableInTouchMode(false);
        if (b != null) {
            editText1.setText("Recognition Result:"+"\n");
            for (String key: b.keySet()) {
                //String j = (String) b.get("mytext");
                //editText1.setText(j);
                String j = (String) b.get(key);
                editText1.append(j);
                editText1.append("\n");
            }
        }
        else editText1.setText("Sorry, no plant is recognized in the picture");
        Button btBack = findViewById(R.id.backbutton);
        btBack.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OutputAcitivty.this, DisplayMessageActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }
}

