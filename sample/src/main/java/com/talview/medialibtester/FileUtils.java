package com.talview.medialibtester;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * A wrapper class for file utility.
 */
public class FileUtils {
    private Context app;

    public FileUtils(Context app) {
        this.app = app.getApplicationContext();
    }

    public File createNewRandomFileInExternalStorage(String extension) {
        File externalRootDir = getExternalRootFolder();
        if (externalRootDir != null) {
            return new File(externalRootDir.getAbsolutePath() + "/" + generateRandomFilename() + "." + extension);
        } else {
            return null;
        }
    }

    public File createNewMp4FileWithRandomName() {
        if (isExternalStorageAvailable())
            return createNewRandomFileInExternalStorage("mp4");
        else
            return createNewRandomFileInternal("mp4");
    }

    public File createNewFileInExternalStorage(String filename, String extension) {
        File externalRootDir = getExternalRootFolder();
        if (externalRootDir != null) {
            return new File(externalRootDir.getAbsolutePath() + "/" + filename + "." + extension);
        } else {
            return null;
        }
    }

    public File createNewRandomFileInternal(String extension) {
        File externalRootDir = getInternalRootFolder();
        if (externalRootDir != null) {
            return new File(externalRootDir.getAbsolutePath() + "/" + generateRandomFilename() + "." + extension);
        } else {
            return null;
        }
    }

    public File createNewFileInternal(String filename, String extension) {
        File externalRootDir = getInternalRootFolder();
        if (externalRootDir != null) {
            return new File(externalRootDir.getAbsolutePath() + "/" + filename + "." + extension);
        } else {
            return null;
        }
    }

    private File getExternalRootFolder() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Talview/");
            if (file.mkdirs() || file.isDirectory()) {
                return file;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public boolean isExternalStorageAvailable() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }


    private File getInternalRootFolder() {
        return app.getFilesDir();
    }

    private String generateRandomFilename() {
        return String.valueOf(System.currentTimeMillis());
    }

}
