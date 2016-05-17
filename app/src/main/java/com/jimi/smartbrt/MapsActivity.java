package com.jimi.smartbrt;
                    
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

public class MapsActivity extends FragmentActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        Button b = (Button) findViewById(R.id.btn_request_directions);
        b.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_request_directions) {
            Intent i = new Intent(MapsActivity.this, SimpleDirectionActivity.class);
            startActivity(i);
        }
    }
}
