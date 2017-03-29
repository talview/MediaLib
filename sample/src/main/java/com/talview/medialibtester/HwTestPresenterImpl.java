package com.talview.medialibtester;

import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.VideoView;

import com.talview.medialib.video.MediaPlayerException;
import com.talview.medialib.video.Video;

import java.io.File;
import java.io.IOException;

/**
 * A {@link HwTestPresenter} implementation.
 */
class HwTestPresenterImpl implements HwTestPresenter {
    private static final int FACE_DETECT_DURATION = 100;
    private static final int RECORD_DURATION = 5000;
    private Rect emptyRect = new Rect(0, 0, 0, 0);
    private boolean isFaceDetected = false;
    private FileUtils fileUtils;
    private File recordedFile;
    private boolean isPlayerSuccessful = true;
    private boolean playbackSuccessful = false;
    private HwTestView view;

    public HwTestPresenterImpl(FileUtils fileUtils) {
        this.fileUtils = fileUtils;
    }

    @Override
    public void takeView(HwTestView view) {
        this.view = view;
    }

    @Override
    public void onFaceDetection(Camera.Face[] faces, Camera camera) {
        if (view != null) {
            if (faces.length > 0) {
                isFaceDetected = true;
                view.publishFaceRect(faces[0].rect);
            } else {
                view.publishFaceRect(emptyRect);
            }
        }
    }

    @Override
    public Rect resizeRectToViewBounds(Rect rect, View v) {
        int vWidth = v.getWidth();
        int vHeight = v.getHeight();
        int l = rect.left;
        int t = rect.top;
        int r = rect.right;
        int b = rect.bottom;
        if (l == 0 && t == 0 && r == 0 && b == 0) {
            return rect;
        }
        int left = (l + 1000) * vWidth / 2000;
        int top = (t + 1000) * vHeight / 2000;
        int right = (r + 1000) * vWidth / 2000;
        int bottom = (b + 1000) * vHeight / 2000;
        return new Rect(left, top, right, bottom);
    }

    @Override
    public void startRecordingAfter5Secs(final Video video) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view == null) {
                    return;
                }
                if (!isFaceDetected) {
                    view.publishFaceDetectionFailed();
                    return;
                }
                recordForRecordDuration(video);
            }
        }, FACE_DETECT_DURATION);
    }

    @Override
    public void recordForRecordDuration(final Video video) {
        startRecording(video);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (view != null) {
                    try {
                        recordedFile = video.stopRecording();
                        view.publishRecordingFinished(recordedFile);
                    } catch (IOException e) {
                        view.publishError(HwTestView.ErrorCode.ERROR_GETTING_DATA_FROM_RECORDER);
                    }
                }
            }
        }, RECORD_DURATION);
    }

    private void startRecording(Video video) {
        try {
            recordedFile = fileUtils.createNewMp4FileWithRandomName();
            video.startRecording(recordedFile);
            view.publishRecordingStarted();
        } catch (IOException e) {
            view.publishError(HwTestView.ErrorCode.ERROR_STARTING_RECORDER);
        }
    }

    @Override
    public void onSkipFaceDetection(final Video video) {
        recordForRecordDuration(video);
    }

    @Override
    public void playVideo(final Video video, final SurfaceHolder display) {
        video.prepareAndStartPlaying(recordedFile, HwTestPresenterImpl.this, display);
    }

    @Override
    public void playVideo(final VideoView videoView) {
        if (recordedFile.length() < 16) {
            onError(new MediaPlayerException(MediaPlayerException.ERROR_UNABLE_TO_READ_FILE));
            return;
        }
        videoView.setVideoPath(recordedFile.getAbsolutePath());
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                onFinishedPlaying();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                HwTestPresenterImpl.this.onError(new MediaPlayerException(what));
                return false;
            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoView.start();
                onStart();
            }
        });
        videoView.requestFocus();
    }

    @Override
    public void destroy() {
        if (recordedFile != null && recordedFile.length() > 0) {
            if (recordedFile.delete())
                recordedFile = null;
        }
        view = null;
    }

    @Override
    public void onError(MediaPlayerException e) {
        isPlayerSuccessful = false;
        playbackSuccessful = false;
        view.publishPlayerError(e.getWhat());
    }

    @Override
    public void onFinishedPlaying() {
        if (isPlayerSuccessful) {
            playbackSuccessful = true;
            view.publishPlayerFinished();
        }
    }

    @Override
    public void onStart() {
        view.publishPlayerStarted();
    }
}
