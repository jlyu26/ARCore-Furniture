package edu.wpi.jlyu.sceneformfurniture;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

public class MainActivity extends AppCompatActivity {

    private CustomArFragment fragment;
    private Anchor cloudAnchor;

    private enum AppAnchorState {
        NONE,
        HOSTING,
        HOSTED,
        RESOLVING,
        RESOLVED
    }

    private AppAnchorState appAnchorState = AppAnchorState.NONE;

    private SnackbarHelper snackbarHelper = new SnackbarHelper();

    private StorageManager storageManager;

    private Uri selectedObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.sceneform_fragment);
        fragment.getPlaneDiscoveryController().hide();  // Hide initial hand gesture
        fragment.getArSceneView().getScene().setOnUpdateListener(this::onUpdateFrame);

        InitializeGallery();

        Button clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCloudAnchor(null);
            }
        });

        Button resolveButton = findViewById(R.id.resolve_button);
        resolveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cloudAnchor != null) {
                    snackbarHelper.showMessageWithDismiss(getParent(), "Please clear anchor.");
                    return;
                }
                ResolveDialogFragment dialog = new ResolveDialogFragment();
                dialog.setOkListener(MainActivity.this::onResolveOKPressed);
                dialog.show(getSupportFragmentManager(), "Resolve");
            }
        });

        fragment.setOnTapArPlaneListener(
                (HitResult hitResult, Plane plane, MotionEvent motionEvent) -> {
                     if (plane.getType() != Plane.Type.HORIZONTAL_UPWARD_FACING ||
                             appAnchorState != AppAnchorState.NONE) {
                         return;
                     }

                     Anchor newAnchor = fragment.getArSceneView().getSession().hostCloudAnchor(hitResult.createAnchor());

                     setCloudAnchor(newAnchor);

                     appAnchorState = AppAnchorState.HOSTING;
                     snackbarHelper.showMessage(this, "Hosting Anchor...");

                     placeObject(fragment, cloudAnchor, selectedObject);
                }
        );

        storageManager = new StorageManager(this);
    }

    private void onResolveOKPressed(String dialogValue) {
        int shortCode = Integer.parseInt(dialogValue);
        storageManager.getCloudAnchorID(shortCode, (cloudAnchorId) -> {
            Anchor resolvedAnchor = fragment.getArSceneView().getSession().resolveCloudAnchor(cloudAnchorId);
            setCloudAnchor(resolvedAnchor);
            placeObject(fragment, cloudAnchor, selectedObject);
            snackbarHelper.showMessage(this, "Resolving Anchor...");
            appAnchorState = AppAnchorState.RESOLVING;
        });
    }

    private void setCloudAnchor (Anchor newAnchor) {
        if (cloudAnchor != null) {
            cloudAnchor.detach();
        }

        cloudAnchor = newAnchor;
        appAnchorState = AppAnchorState.NONE;
        snackbarHelper.hide(this);
    }

    private void onUpdateFrame(FrameTime frameTime) {
        checkUpdatedAnchor();
    }

    // private synchronized void checkUpdatedAnchor() {
    private void checkUpdatedAnchor() {
        if (appAnchorState != AppAnchorState.HOSTING && appAnchorState != AppAnchorState.RESOLVING) {
            return;
        }
        Anchor.CloudAnchorState cloudState = cloudAnchor.getCloudAnchorState();
        if (appAnchorState == AppAnchorState.HOSTING) {

            if (cloudState.isError()) {
                snackbarHelper.showMessageWithDismiss(this, "Error hosting anchor: "
                        + cloudState);
                appAnchorState = AppAnchorState.NONE;
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                storageManager.nextShortCode((shortCode) -> {
                    if (shortCode == null) {
                        snackbarHelper.showMessageWithDismiss(this, "Couldn't get shortcode.");
                        return;
                    }
                    storageManager.storeUsingShortCode(shortCode, cloudAnchor.getCloudAnchorId());

                    snackbarHelper.showMessageWithDismiss(this, "Anchor hosted! Cloud Short Code: "
                            + shortCode);
                });

                appAnchorState = AppAnchorState.HOSTED;
            }
        }
        else if (appAnchorState == AppAnchorState.RESOLVING) {
            if (cloudState.isError()) {
                snackbarHelper.showMessageWithDismiss(this, "Error resolving anchor: "
                        + cloudState);
                appAnchorState = AppAnchorState.NONE;
            } else if (cloudState == Anchor.CloudAnchorState.SUCCESS) {
                snackbarHelper.showMessageWithDismiss(this, "Anchor resolved!");
                appAnchorState = AppAnchorState.RESOLVED;
            }
        }
    }

    private void InitializeGallery() {
        LinearLayout gallery = findViewById(R.id.gallery_layout);

        ImageView chair = new ImageView( this );
        chair.setImageResource(R.drawable.chair_thumb);
        chair.setContentDescription("chair");
        chair.setOnClickListener(view -> {selectedObject = Uri.parse("chair.sfb");});
        gallery.addView(chair);

        ImageView goat = new ImageView( this );
        goat.setImageResource(R.drawable.goat_thumb);
        goat.setContentDescription("goat");
        goat.setOnClickListener(view -> {selectedObject = Uri.parse("goat.sfb");});
        gallery.addView(goat);

        ImageView lamp = new ImageView( this );
        lamp.setImageResource(R.drawable.lamp_thumb);
        lamp.setContentDescription("lamp");
        lamp.setOnClickListener(view -> {selectedObject = Uri.parse("lamp.sfb");});
        gallery.addView(lamp);

        ImageView sofa = new ImageView( this );
        sofa.setImageResource(R.drawable.sofa_thumb);
        sofa.setContentDescription("sofa");
        sofa.setOnClickListener(view -> {selectedObject = Uri.parse("sofa.sfb");});
        gallery.addView(sofa);

        ImageView table = new ImageView( this );
        table.setImageResource(R.drawable.table_thumb);
        table.setContentDescription("table");
        table.setOnClickListener(view -> {selectedObject = Uri.parse("table.sfb");});
        gallery.addView(table);
    }

    // placeObject() takes the position and orientation of an object to be rendered as
    // an anchor and starts async loading the object. Once the builder built the object,
    // addNodeToScene() will be called, and that will actually create the object in
    // the camera view.
    private void placeObject(ArFragment fragment, Anchor anchor, Uri model) {
        ModelRenderable.builder()
                .setSource(fragment.getContext(), model)
                .build()
                .thenAccept(renderable -> addNodeToScene(fragment, anchor, renderable))
                .exceptionally((throwable -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder( this );
                    builder.setMessage(throwable.getMessage())
                            .setTitle("Error!");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return null;
                }));
    }

    // Place the object on AR
    private void addNodeToScene(ArFragment fragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(fragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        fragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }

}
