package project.app;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;


import java.io.File;
import java.util.ArrayList;

import ir.mitrade.dotline.R;

public class G extends Application {
  public static final String SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
  public static final String HOME_DIR = SDCARD + "/mitrade";
  public static final String APP_DIR = HOME_DIR + "/dot_n_boxes";

  public static Context context;
  public static Resources resources;
  public static DisplayMetrics displayMetrics;
  public static Handler handler;
  public static boolean hasWriteAccess;
  public static ArrayList<Activity> currentActivities = new ArrayList<>();

  public static MediaPlayer musicPlayer;

  @Override
  public void onCreate() {
    super.onCreate();

    context = getApplicationContext();
    resources = context.getResources();
    displayMetrics = resources.getDisplayMetrics();
    handler = new Handler();

    musicPlayer = MediaPlayer.create(context, R.raw.music);
    float volume = Settings.getMusicVolume();
    musicPlayer.setVolume(volume, volume);
  }

  public static void createDirectory() {
    if (hasWriteAccess) {
      File file = new File(APP_DIR);
      file.mkdirs();
    }
  }
}
