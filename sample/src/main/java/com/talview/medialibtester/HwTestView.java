package com.talview.medialibtester;

import android.graphics.Rect;

import java.io.File;

/**
 * A view interface for communicating with the its presenter counter part {@link HwTestPresenterImpl}
 */
public interface HwTestView {
    enum ErrorCode {
        ERROR_STARTING_RECORDER, ERROR_GETTING_DATA_FROM_RECORDER
    }

    void publishFaceRect(Rect rect);

    void publishFaceDetectionFailed();

    void publishRecordingFinished(File recordedFile);

    void publishPlayerError(int what);

    void publishPlayerFinished();

    void publishPlayerStarted();

    void publishRecordingStarted();

    void startTest();

    void publishError(ErrorCode t);
}
