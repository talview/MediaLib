package com.talview.medialibtester;

import android.graphics.Rect;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.VideoView;

import com.talview.medialib.video.MediaPlayerCallback;
import com.talview.medialib.video.TalviewVideo;

/**
 * A presenter that defines the methods for presenting data in {@link HwTestActivity} properly.
 */
interface HwTestPresenter extends Camera.FaceDetectionListener, MediaPlayerCallback {

    void takeView(HwTestView view);

    Rect resizeRectToViewBounds(Rect rect, View v);

    void startRecordingAfter5Secs(TalviewVideo talviewVideo);

    void recordForRecordDuration(final TalviewVideo talviewVideo);

    void onSkipFaceDetection(TalviewVideo video);

    void playVideo(TalviewVideo talviewVideo, SurfaceHolder display);

    void playVideo(final VideoView videoView);

    void destroy();
}
