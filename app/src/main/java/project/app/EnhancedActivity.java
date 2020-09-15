package project.app;

import android.support.v7.app.AppCompatActivity;

import static project.app.G.currentActivities;

public class EnhancedActivity extends AppCompatActivity {

  @Override
  protected void onPause() {
    super.onPause();
    currentActivities.remove(this);
    refreshMediaPlayer();
  }

  @Override
  protected void onResume() {
    super.onResume();
    currentActivities.add(this);
    refreshMediaPlayer();
  }

  private void refreshMediaPlayer() {
    if (!Settings.isEnableMusic()) {
      return;
    }

    G.handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (currentActivities.size() == 0) {
          G.musicPlayer.pause();
        } else {
          if (!G.musicPlayer.isPlaying()) {
            G.musicPlayer.start();
          }
        }
      }
    }, 100);
  }
}
