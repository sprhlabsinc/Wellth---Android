package com.wellth.View.Activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.jsibbold.zoomage.ZoomageView;
import com.wellth.R;

import java.io.File;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class GalleryActivity extends AppCompatActivity {

    private ZoomageView img_attachment;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        img_attachment = (ZoomageView) findViewById(R.id.img_attachment);

        File postFile = (File) getIntent().getSerializableExtra("image");
        Glide.with(getApplicationContext()).load(postFile).into(img_attachment);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        return true;
    }
}
