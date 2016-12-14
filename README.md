# MediaLib
> A wrapper around the android's Camera 1 api and MediaRecorder

### How to use

**Create a media configuration**

```
Configuration config = new ConfigurationBuilder()
    // width and height of video
    // final width and height may not be same as passed here but will be close
    .setVideoDimensions(640, 320)
    .setAudioVideoEncodingBitRates(300000, 8000) // bit rate encoding values (affects quality)
    .whichCamera(TalviewVideo.REAR_CAMERA) // .whichCamera(TalviewVideo.FRONT_CAMERA
    .previewDisplayOrientation(0) // orientation in degrees for preview frames
    .videoOrientation(0) // orientation in degrees for recorded video.
    // frame rate of the video
    // (final recorded frame rate may not be same as passed here but will be close)
    .setVideoFrameRate(8)
    .setAudioChannels(1) // the number channels to record the audio into.
    .setAudioSamplingRate(8000) // refers to the audio quality
    .build()
```

**Create an instance of TalviewVideo using the Configuration**

    TalviewVideo tVideo = TalviewMedia.createFromConfig(config);

**First attach a Surface to TalviewVideo instance**

    tVideo.setCameraPreviewSurface(cameraPreviewSurface);

**Consequently you can attach a face detection listener for api > 14**
> Note: The listener is only called when only the preview is running and recording is not.

    tVideo.setFaceDetectionListener(listener);

**Whenever your ready call startCameraPreview**

    tVideo.startCameraPreview();

**If you want to record, call startRecording(File)**

    tVideo.startRecording(myOutputFile);

Then call `stopRecording()` to stop recording, this method returns the file to which the video was stored to.

**Once done, do the clean up!**
Call `close()` if you just want to release the Camera for other applications and re-use it again once you're app is back.

Call `destroy()` if you want to do a full clean up (i.e release the camera and detach surfacecallbacks from your preview surface), the instance of `TalviewVideo` is not guranteed to work expectedly after you call `destroy()` (might work if you re-attach the surface)

### Beta Feature

You can call the `void prepareAndStartPlaying(File, MediaPlayerCallback, SurfaceHolder)`  method to perform a play back of an mp4 video file.

```
/**
     * Note: This feature is in beta
     * Use this method to asynchronously play an mp4 video on the surface attached to the SurfaceHolder.
     * @param fileToPlay The mp4 video file to play
     * @param mediaPlayerCallback A callback instance to let you know when the player started finished etc.
     * @param display The holder of the surface on which to render the video.
     */
```

### Running tests

I have included a Hardware test functionality in the sample app, also written an UI espresso test, run this on your phone/device farms to test the reliability of this library.
