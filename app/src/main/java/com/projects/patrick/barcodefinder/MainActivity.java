package com.projects.patrick.barcodefinder;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewOverlay;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraSource cameraSource;
    private BarcodeDetector barcodeDetector;
    private Button buttonScan;
    private TextView textBarcode;
    private ImageView imageBarcode;
    private ViewOverlay viewOverlay;
    private SurfaceView surfaceView;
    private boolean cameraRunning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 66);
        }

        //surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        buttonScan = (Button) findViewById(R.id.buttonScan);
        textBarcode = (TextView) findViewById(R.id.textBarcode);
        imageBarcode = (ImageView) findViewById(R.id.imageView);
        viewOverlay = imageBarcode.getOverlay();

        //surfaceView.setOnClickListener(this);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ButtonClick","scan the image!");
                barcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(Barcode.QR_CODE).build();
                if(!barcodeDetector.isOperational()){
                    Log.d("BarcodeDetector", "not ready yet");
                }
                Frame frame = new Frame.Builder().setBitmap(BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.qrcode39275882)).build();
                SparseArray<Barcode> barcodeSparseArray = barcodeDetector.detect(frame);
                Log.d("DetectedBarcodes", "found: " + barcodeSparseArray.size());
                if(barcodeSparseArray.size() > 0){
                    Barcode barcode = barcodeSparseArray.valueAt(0);
                    textBarcode.setText(barcode.displayValue);
                    final Rect barcodeBoundingbox = barcode.getBoundingBox();
                    final Paint paint = new Paint();
                    paint.setColor(Color.RED);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(6f);
                    Drawable drawFrame = new Drawable() {
                        @Override
                        public void draw(@NonNull Canvas canvas) {
                            canvas.drawRect(barcodeBoundingbox, paint);
                        }

                        @Override
                        public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {

                        }

                        @Override
                        public void setColorFilter(@Nullable ColorFilter colorFilter) {

                        }

                        @Override
                        public int getOpacity() {
                            return PixelFormat.UNKNOWN;
                        }
                    };
                    viewOverlay.add(drawFrame);
                }
            }
        });
        //createCameraSource();
    }

    private void createCameraSource(){
        Context context = getApplicationContext();

        barcodeDetector = new BarcodeDetector.Builder(context).setBarcodeFormats(Barcode.QR_CODE).build();


        if(!barcodeDetector.isOperational()){
            Log.w("BarcodeDetector", "detector not ready yet");
        }

        CameraSource.Builder builder = new CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1200,700)
                .setRequestedFps(15f)
                .setAutoFocusEnabled(true);
        cameraSource = builder.build();
    }

    private void startCameraSource() throws SecurityException {
        if(cameraSource != null){
            try {
                cameraSource.start(surfaceView.getHolder());
            }catch (Exception e){
                Log.e("vCameraSource", "could not start camera");
                cameraSource.release();
                cameraSource = null;
            }
        } else {
            Log.d("vCameraSource", "is null");
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        if(cameraSource != null) {
            cameraSource.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(cameraSource != null) {
            cameraSource.release();
        }
    }

    @Override
    public void onClick(View v) {
        Log.d("Main", "start camera");
        if(!cameraRunning && cameraSource != null) {
            startCameraSource();
            cameraRunning = true;
        } else {
            cameraSource.stop();
            cameraRunning = false;
        }
    }
}
