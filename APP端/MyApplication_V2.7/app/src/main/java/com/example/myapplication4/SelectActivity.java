package com.example.myapplication4;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

public class SelectActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select);
    }
    public void OnPlay(View v){
        startActivity(new Intent(SelectActivity.this,Mainactivity.class));
    }
    public void OnAbout(View v) {
        startActivity(new Intent(SelectActivity.this, userActivity.class));
    }
}
