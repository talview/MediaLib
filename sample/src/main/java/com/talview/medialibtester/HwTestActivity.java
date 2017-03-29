package com.talview.medialibtester;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.talview.medialib.Media;
import com.talview.medialib.config.ConfigurationBuilder;
import com.talview.medialib.video.MediaPlayerException;
import com.talview.medialib.video.Video;

import java.io.File;
import java.io.IOException;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class HwTestActivity extends AppCompatActivity implements HwTestView {
    private Button testCameraBtn, detectingBtn, playbackBtn, playingBtn, recordingBtn, testRepeat;

    Video video;

    private SurfaceView cameraPreviewSurface;
    private SurfaceView transparentView;
    private VideoView playerSurface;
    private Canvas faceDetectCanvas;
    private SurfaceHolder holderTransparent;
    AlertDialog faceDetectErrorDialog;
    private FrameLayout frameHwTestView;

    HwTestPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hw_test);
        injectMembers();

        cameraPreviewSurface = (SurfaceView) findViewById(R.id.frame_hw_camera_preview);
        video.setCameraPreviewSurface(cameraPreviewSurface);
        video.setFaceDetectionListener(presenter);

        playerSurface = (VideoView) findViewById(R.id.surface_for_player);
        transparentView = (SurfaceView) findViewById(R.id.holder_transparent);
        assert transparentView != null;
        holderTransparent = transparentView.getHolder();
        holderTransparent.setFormat(PixelFormat.TRANSPARENT);
        testCameraBtn = (Button) findViewById(R.id.btn_test_camera);
        playbackBtn = (Button) findViewById(R.id.btn_test_playback);
        playingBtn = (Button) findViewById(R.id.btn_test_playing);
        detectingBtn = (Button) findViewById(R.id.btn_test_detecting);
        recordingBtn = (Button) findViewById(R.id.btn_test_recording);
        testRepeat = (Button) findViewById(R.id.btn_test_repeat);

        frameHwTestView = (FrameLayout) findViewById(R.id.layout_hw_test_camera);
        setVisibilityOfBtn(BTN_TEST_CAMERA);

        testCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HwTestActivityPermissionsDispatcher
                        .onTestStartButtonClickedWithCheck(HwTestActivity.this);
            }
        });

        playbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerSurface.setVisibility(View.VISIBLE);
                cameraPreviewSurface.setVisibility(View.INVISIBLE);
                presenter.playVideo(playerSurface);
            }
        });

        testRepeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                testRepeat.setVisibility(View.GONE);
                reset();
            }
        });
    }

    @NeedsPermission({
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.WAKE_LOCK
    })
    public void onTestStartButtonClicked() {
        //noinspection ConstantConditions
        frameHwTestView.setVisibility(View.VISIBLE);
        startTest();
    }

    @OnPermissionDenied({
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.WAKE_LOCK
    })
    void showDeniedForHwTestPermissions() {
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        HwTestActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void startTest() {
        setVisibilityOfBtn(BTN_DETECTING_FACE);
        try {
            video.startCameraPreview();
        } catch (IOException e) {
            // TODO: add crashlytics log here.
            return;
        }
        presenter.startRecordingAfter5Secs(video);
        transparentView.setVisibility(View.VISIBLE);
    }

    @Override
    public void publishError(ErrorCode t) {
        switch (t) {
            case ERROR_GETTING_DATA_FROM_RECORDER:
                break;
            case ERROR_STARTING_RECORDER:
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.takeView(null);
        reset();
    }

    private void reset() {
        try {
            video.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (faceDetectErrorDialog != null) {
            if (faceDetectErrorDialog.isShowing())
                faceDetectErrorDialog.dismiss();
            faceDetectErrorDialog = null;
        }
        setVisibilityOfBtn(BTN_TEST_CAMERA);
        testRepeat.setVisibility(View.GONE);
        playerSurface.setVisibility(View.INVISIBLE);
        cameraPreviewSurface.setVisibility(View.VISIBLE);
        frameHwTestView.setVisibility(View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.takeView(this);
    }


    private void openTalviewWebsite() {
        // TODO: launch talview website
    }

    private void openContactSupportActivity() {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:support@talview.com"));
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException ex) {
        }
    }

    private static final int BTN_TEST_CAMERA = 0;
    private static final int BTN_DETECTING_FACE = 1;
    private static final int BTN_PLAYING = 2;
    private static final int BTN_PLAYBACK = 3;
    private static final int BTN_RECORDING = 4;
    private static final int BTN_CONTINUE = 5;

    private void setVisibilityOfBtn(int which) {
        testCameraBtn.setVisibility(View.GONE);
        detectingBtn.setVisibility(View.GONE);
        playingBtn.setVisibility(View.GONE);
        playbackBtn.setVisibility(View.GONE);
        recordingBtn.setVisibility(View.GONE);
        testRepeat.setVisibility(View.GONE);
        switch (which) {
            case 0:
                testCameraBtn.setVisibility(View.VISIBLE);
                break;
            case 1:
                detectingBtn.setVisibility(View.VISIBLE);
                break;
            case 2:
                playingBtn.setVisibility(View.VISIBLE);
                break;
            case 3:
                playbackBtn.setVisibility(View.VISIBLE);
                break;
            case 4:
                recordingBtn.setVisibility(View.VISIBLE);
                break;
            case 5:
                testRepeat.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void publishFaceRect(Rect rect) {
        Rect resizedRect = presenter.resizeRectToViewBounds(rect, transparentView);
        drawFocusRect(resizedRect.left, resizedRect.top,
                resizedRect.right, resizedRect.bottom, Color.GREEN);
    }

    @Override
    public void publishFaceDetectionFailed() {
        showAlertForFailingFaceDetection();
    }

    @Override
    public void publishRecordingFinished(File recordedFile) {
        setVisibilityOfBtn(BTN_PLAYBACK);
    }

    @Override
    public void publishRecordingStarted() {
        setVisibilityOfBtn(BTN_RECORDING);
        transparentView.setVisibility(View.INVISIBLE);
    }

    @Override
    public void publishPlayerError(int what) {
        setVisibilityOfBtn(BTN_TEST_CAMERA);
        switch (what) {
            case MediaPlayerException.ERROR_UNABLE_TO_SET_FILE:
                break;
            case MediaPlayerException.ERROR_UNABLE_TO_READ_FILE:
        }
    }

    @Override
    public void publishPlayerFinished() {
        setVisibilityOfBtn(BTN_CONTINUE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.destroy();
        video.destroy();
        video = null;
    }

    @Override
    public void publishPlayerStarted() {
        setVisibilityOfBtn(BTN_PLAYING);
    }

    private void drawFocusRect(float RectLeft, float RectTop, float RectRight, float RectBottom, int color) {
        faceDetectCanvas = holderTransparent.lockCanvas();
        if (faceDetectCanvas != null) {
            faceDetectCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            //border's properties
            Paint paint = new Paint();
            paint.setStyle(Paint.Style.STROKE);
            paint.setColor(color);
            paint.setStrokeWidth(3);
            faceDetectCanvas.drawRect(RectLeft, RectTop, RectRight, RectBottom, paint);
            holderTransparent.unlockCanvasAndPost(faceDetectCanvas);
        }
    }

    private void showAlertForFailingFaceDetection() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.no_face_detected));
        dialogBuilder.setMessage(R.string.retry_or_continue_face_detect);
        dialogBuilder.setPositiveButton(R.string.hwtest_facedetectionerroralert_skip, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                presenter.onSkipFaceDetection(video);
            }
        });
        dialogBuilder.setNegativeButton(R.string.retry, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                reset();
                testCameraBtn.performClick();
            }
        });
        dialogBuilder.setCancelable(false);
        faceDetectErrorDialog = dialogBuilder.create();
        faceDetectErrorDialog.show();
    }

    protected final void injectMembers() {
        video = Media.createFromConfig(new ConfigurationBuilder()
                .setVideoDimensions(640, 320)
                .setAudioVideoEncodingBitRates(300000, 8000)
                .setVideoFrameRate(8).setAudioChannels(1).setAudioSamplingRate(8000).build());
        presenter = new HwTestPresenterImpl(new FileUtils(this));
    }
}
