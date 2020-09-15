package project.app;

import android.content.SharedPreferences;

import static android.content.Context.MODE_PRIVATE;

public class Settings {
  public static SharedPreferences.Editor getSharedPreferenceEditor() {
    return G.context.getSharedPreferences("options", MODE_PRIVATE).edit();
  }

  public static SharedPreferences getSharedPreference() {
    return G.context.getSharedPreferences("options", MODE_PRIVATE);
  }

  public static boolean isEnableMusic() {
    return getSharedPreference().getBoolean("enable_music", false);
  }

  public static boolean isEnableSfx() {
    return getSharedPreference().getBoolean("enable_sfx", false);
  }

  public static boolean isEnableHighPerformance() {
    return getSharedPreference().getBoolean("enable_high_performance", false);
  }

  public static int getCols() {
    return getSharedPreference().getInt("gridCols", 4);
  }

  public static void setCols(int value) {
    getSharedPreferenceEditor().putInt("gridCols", value).apply();
  }

  public static int getRows() {
    return getSharedPreference().getInt("gridRows", 4);
  }

  public static void setRows(int value) {
    getSharedPreferenceEditor().putInt("gridRows", value).apply();
  }

  public static float getMusicVolume() {
    int value = getSharedPreference().getInt("music_volume", 80);
    return value / 100.0f;
  }
}
