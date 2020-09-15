package project.activity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;



import ir.mitrade.dotline.R;
import project.app.EnhancedActivity;
import project.app.G;
import project.app.Settings;

public class SettingsActivity extends EnhancedActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_settings);

    Switch chk_enableHighPerformance = (Switch) findViewById(R.id.chk_enableHighPerformance);
    Switch chk_enableMusic = (Switch) findViewById(R.id.chk_enableMusic);
    Switch chk_enableSfx = (Switch) findViewById(R.id.chk_enableSfx);
    SeekBar slider_musicVolume = (SeekBar) findViewById(R.id.slider_musicVolume);

    chk_enableHighPerformance.setChecked(Settings.isEnableHighPerformance());
    chk_enableMusic.setChecked(Settings.isEnableMusic());
    chk_enableSfx.setChecked(Settings.isEnableSfx());
    slider_musicVolume.setProgress((int) (Settings.getMusicVolume() * 100));

    chk_enableHighPerformance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.getSharedPreferenceEditor()
          .putBoolean("enable_high_performance", isChecked)
          .apply();
      }
    });

    chk_enableMusic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.getSharedPreferenceEditor()
          .putBoolean("enable_music", isChecked)
          .apply();

        if (!isChecked) {
          G.musicPlayer.pause();
        } else {
          G.musicPlayer.start();
        }
      }
    });

    chk_enableSfx.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        Settings.getSharedPreferenceEditor()
          .putBoolean("enable_sfx", isChecked)
          .apply();
      }
    });

    slider_musicVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override
      public void onProgressChanged(SeekBar seekBar, int value, boolean b) {
        Settings.getSharedPreferenceEditor()
          .putInt("music_volume", value)
          .apply();

        float volume = Settings.getMusicVolume();
        G.musicPlayer.setVolume(volume, volume);
      }

      @Override
      public void onStartTrackingTouch(SeekBar seekBar) {}
      @Override
      public void onStopTrackingTouch(SeekBar seekBar) {}
    });
  }
}
