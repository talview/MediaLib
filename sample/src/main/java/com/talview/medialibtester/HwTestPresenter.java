package com.talview.medialibtester;

import android.graphics.Rect;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.VideoView;

import com.talview.medialib.video.MediaPlayerCallback;
import com.talview.medialib.video.Video;

/**
 * A presenter that defines the methods for presenting data in {@link HwTestActivity} properly.
 */
interface HwTestPresenter extends Camera.FaceDetectionListener, MediaPlayerCallback {

    void takeView(HwTestView view);

    Rect resizeRectToViewBounds(Rect rect, View v);

    void startRecordingAfter5Secs(Video video);

    void recordForRecordDuration(final Video video);

    void onSkipFaceDetection(Video video);

    void playVideo(Video video, SurfaceHolder display);

    void playVideo(final VideoView videoView);

    void destroy();
}
