package edu.wpi.jlyu.sceneformfurniture;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.ux.ArFragment;

public class MainActivity extends AppCompatActivity {

    private ArFragment fragment;

    private String selectedObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (ArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);

        InitializeGallery();

        fragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                    Log.d("model", selectedObject);
                }
        );
    }

    private void InitializeGallery() {
        LinearLayout gallery = findViewById(R.id.gallery_layout);

        ImageView chair = new ImageView( this );
        chair.setImageResource(R.drawable.chair_thumb);
        chair.setContentDescription("chair");
        chair.setOnClickListener(view -> {selectedObject = "chair";});
        gallery.addView(chair);

        ImageView lamp = new ImageView( this );
        lamp.setImageResource(R.drawable.lamp_thumb);
        lamp.setContentDescription("lamp");
        lamp.setOnClickListener(view -> {selectedObject = "lamp";});
        gallery.addView(lamp);

        ImageView sofa = new ImageView( this );
        sofa.setImageResource(R.drawable.sofa_thumb);
        sofa.setContentDescription("sofa");
        sofa.setOnClickListener(view -> {selectedObject = "sofa";});
        gallery.addView(sofa);

        ImageView table = new ImageView( this );
        table.setImageResource(R.drawable.table_thumb);
        table.setContentDescription("table");
        table.setOnClickListener(view -> {selectedObject = "table";});
        gallery.addView(table);
    }
}
