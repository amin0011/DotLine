package project.activity;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;


import ir.mitrade.dotline.R;
import project.app.EnhancedActivity;
import project.dialog.OptionDialog;
import project.view.GameView;


public class GameActivity extends EnhancedActivity {

  private GameView gameView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_game);

    final boolean isMultiplayer = getIntent().getExtras().getBoolean("isMultiplayer");
    final boolean mustResume = getIntent().getExtras().getBoolean("resume");

    ImageButton btn_reset = (ImageButton) findViewById(R.id.btn_reset);
    ImageButton btn_option = (ImageButton) findViewById(R.id.btn_option);
    gameView = (GameView) findViewById(R.id.gameview);

    btn_reset.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        gameView.resetGame();
      }
    });

    btn_option.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        gameView.startGame(isMultiplayer);
        Dialog dialog = new OptionDialog(GameActivity.this);
        dialog.show();
      }
    });

    if (mustResume) {
      gameView.loadGame();
    } else {
      gameView.startGame(isMultiplayer);
    }
  }


  public GameView getGameView() {
    return gameView;
  }

  @Override
  protected void onPause() {
    super.onPause();
    gameView.saveGame();
  }
}
