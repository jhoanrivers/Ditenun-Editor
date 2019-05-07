package com.asksira.imagepickersheetdemo.activity;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.asksira.bsimagepicker.BSImagePicker;
import com.asksira.imagepickersheetdemo.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ViewTarget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Sadum5Activity extends AppCompatActivity{


    private ImageView ivImage1,imgbg, ivImage2, imgview6, imgview7, imgview8,imgview9,imgview10,imgview11,imgview12,imgview13,imgview14,imgview15,imgview16,imgview17,imgview18,imgview19,cancelimg,undoimg,redoimg,saveimg;

    Button btnLoadImage;
    TextView textSource;
    ImageView imageMotif;
    SeekBar hueBar, satBar, valBar;
    TextView hueText, satText, valText;

    final int RQS_IMAGE1 = 1;

    Uri source;
    Bitmap bitmapMaster;
    Canvas canvasMaster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sadum5);

        initial();
        touchAndDrag();

        btnLoadImage = (Button) findViewById(R.id.motif_image);
        textSource = (TextView) findViewById(R.id.sourceuri);
        imageMotif = (ImageView) findViewById(R.id.iv_image2);

        btnLoadImage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, RQS_IMAGE1);
            }
        });


        hueBar = (SeekBar) findViewById(R.id.huebar);
        satBar = (SeekBar) findViewById(R.id.satbar);
        valBar = (SeekBar) findViewById(R.id.valbar);

        //Saat belum ada foto
        hueBar.setVisibility(View.GONE);
        satBar.setVisibility(View.GONE);
        valBar.setVisibility(View.GONE);

        hueBar.setOnSeekBarChangeListener(seekBarChangeListener);
        satBar.setOnSeekBarChangeListener(seekBarChangeListener);
        valBar.setOnSeekBarChangeListener(seekBarChangeListener);


        cancelimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // exit function

                AlertDialog.Builder builder = new AlertDialog.Builder(Sadum5Activity.this);
                builder.setTitle("Batalkan Desain");
                builder.setMessage("Anda yakin ingin membatalkan desain anda?")
                        .setCancelable(false)
                        .setPositiveButton("Ya", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                finish();
                            }
                        })
                        .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });

        undoimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // undo function


            }

        });

        redoimg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //redo function


            }
        });



    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case RQS_IMAGE1:
                    source = data.getData();

                    try {
                        bitmapMaster = BitmapFactory.decodeStream(getContentResolver().openInputStream(source));


                        // Reset HSV value
                        hueBar.setVisibility(View.VISIBLE);
                        satBar.setVisibility(View.VISIBLE);
                        valBar.setVisibility(View.VISIBLE);

                        hueBar.setProgress(256);
                        satBar.setProgress(256);
                        valBar.setProgress(256);

                        loadBitmapHSV();


                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }



                    break;
            }
        }
    }


    SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            loadBitmapHSV();
        }
    };

    private void loadBitmapHSV() {
        if (bitmapMaster != null) {

            int progressHue = hueBar.getProgress() - 256;
            int progressSat = satBar.getProgress() - 256;
            int progressVal = valBar.getProgress() - 256;

            /*
             * Hue (0 .. 360) Saturation (0...1) Value (0...1)
             */

            float hue = (float) progressHue * 360 / 256;
            float sat = (float) progressSat / 256;
            float val = (float) progressVal / 256;

            imageMotif.setImageBitmap(updateHSV(bitmapMaster, hue, sat, val));

        }
    }


    private Bitmap updateHSV(Bitmap src, float settingHue, float settingSat,
                             float settingVal) {

        int w = src.getWidth();
        int h = src.getHeight();
        int[] mapSrcColor = new int[w * h];
        int[] mapDestColor = new int[w * h];

        float[] pixelHSV = new float[3];

        src.getPixels(mapSrcColor, 0, w, 0, 0, w, h);

        int index = 0;
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {

                // Convert from Color to HSV
                Color.colorToHSV(mapSrcColor[index], pixelHSV);

                // Adjust HSV
                pixelHSV[0] = pixelHSV[0] + settingHue;
                if (pixelHSV[0] < 0.0f) {
                    pixelHSV[0] = 0.0f;
                } else if (pixelHSV[0] > 360.0f) {
                    pixelHSV[0] = 360.0f;
                }

                pixelHSV[1] = pixelHSV[1] + settingSat;
                if (pixelHSV[1] < 0.0f) {
                    pixelHSV[1] = 0.0f;
                } else if (pixelHSV[1] > 1.0f) {
                    pixelHSV[1] = 1.0f;
                }

                pixelHSV[2] = pixelHSV[2] + settingVal;
                if (pixelHSV[2] < 0.0f) {
                    pixelHSV[2] = 0.0f;
                } else if (pixelHSV[2] > 1.0f) {
                    pixelHSV[2] = 1.0f;
                }

                // Convert back from HSV to Color
                mapDestColor[index] = Color.HSVToColor(pixelHSV);

                index++;
            }
        }

        return Bitmap.createBitmap(mapDestColor, w, h, Bitmap.Config.ARGB_8888);

    }



    public void touchAndDrag(){
        ivImage2.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); ivImage2.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview6.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview6.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview7.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview7.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview8.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview8.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview9.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview9.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview10.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview10.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview11.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview11.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview12.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview12.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview13.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview13.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview14.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview14.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview15.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview15.setOnDragListener(new Sadum5Activity.ChoiceDragListener());
        imgview16.setOnTouchListener(new Sadum5Activity.ChoiceTouchListener()); imgview16.setOnDragListener(new Sadum5Activity.ChoiceDragListener());

    }

    private void initial() {

        imgbg =findViewById(R.id.img_bg);
        ivImage2 = findViewById(R.id.iv_image2);
        //ivImage3 = findViewById(R.id.iv_image3);
        imgview6 = findViewById(R.id.imageView6);
        imgview7 = findViewById(R.id.imageView7);
        imgview8 = findViewById(R.id.imageView8);
        imgview9 = findViewById(R.id.imageView9);
        imgview10 = findViewById(R.id.imageView10);
        imgview11 = findViewById(R.id.imageView11);
        imgview12 = findViewById(R.id.imageView12);
        imgview13 = findViewById(R.id.imageView13);
        imgview14 = findViewById(R.id.imageView14);
        imgview15 = findViewById(R.id.imageView15);
        imgview16 = findViewById(R.id.imageView16);

        cancelimg = findViewById(R.id.cancel_imgview);
        undoimg = findViewById(R.id.undo_imgview);
        redoimg = findViewById(R.id.redo_imgview);
        saveimg = findViewById(R.id.save_imgview);


    }


    private class ChoiceDragListener implements View.OnDragListener{

        @Override
        public boolean onDrag(View view, DragEvent dragEvent) {

            switch (dragEvent.getAction()){
                case DragEvent.ACTION_DRAG_STARTED:

                    //Saat gambar di drag
                    btnLoadImage.setVisibility(View.GONE);
                    hueBar.setVisibility(View.GONE);
                    satBar.setVisibility(View.GONE);
                    valBar.setVisibility(View.GONE);
                    cancelimg.setVisibility(View.GONE);
                    redoimg.setVisibility(View.GONE);
                    undoimg.setVisibility(View.GONE);
                    saveimg.setVisibility(View.GONE);
                    imageMotif.setVisibility(View.GONE);

                    break;
                case DragEvent.ACTION_DRAG_ENTERED:
                    break;
                case DragEvent.ACTION_DRAG_EXITED:
                    break;
                case DragEvent.ACTION_DROP:
                    ImageView v = (ImageView) dragEvent.getLocalState();
                    ((ImageView)view).setImageDrawable(ivImage2.getDrawable());
                    //((ImageView)v).setImageDrawable(null);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    btnLoadImage.setVisibility(View.VISIBLE);
                    hueBar.setVisibility(View.VISIBLE);
                    satBar.setVisibility(View.VISIBLE);
                    valBar.setVisibility(View.VISIBLE);
                    cancelimg.setVisibility(View.VISIBLE);
                    redoimg.setVisibility(View.VISIBLE);
                    undoimg.setVisibility(View.VISIBLE);
                    saveimg.setVisibility(View.VISIBLE);
                    imageMotif.setVisibility(View.VISIBLE);

                    break;
            }
            return true;
        }
    }

    public final class ChoiceTouchListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            if ((motionEvent.getAction() == motionEvent.ACTION_DOWN) && ((ImageView) view).getDrawable() != null) {
                ClipData clipData = ClipData.newPlainText("", "");
                View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
                view.startDrag(clipData, shadowBuilder, view, 0);
                return true;
            } else {
                // Disinilah event change color muncul.
                //Toast.makeText(SadumActivity.this, "Clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        }
    }



    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
        else
            showSystemUI();
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
// except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitByBackKey();
            //moveTaskToBack(false);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    //Saat force back ditekan
    protected void exitByBackKey() {

        AlertDialog alertbox = new AlertDialog.Builder(this)
                .setMessage("Anda ingin keluar dan membatalkan desain?")
                .setPositiveButton("Ya", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {

                        finish();
                        //close();
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {

                    // do something when the button is clicked
                    public void onClick(DialogInterface arg0, int arg1) {
                    }
                })
                .show();
    }
}