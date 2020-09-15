package project.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;



import ir.mitrade.dotline.R;
import project.app.EnhancedActivity;
import project.app.G;


public class MainActivity extends EnhancedActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    // eeee

    requestWritePermission();

    Button btn_resume = (Button) findViewById(R.id.btn_resume);
    Button btn_singlePlayer = (Button) findViewById(R.id.btn_singlePlayer);
    Button btn_multiPlayer = (Button) findViewById(R.id.btn_multiPlayer);
    Button btn_options = (Button) findViewById(R.id.btn_options);
    Button btn_about = (Button) findViewById(R.id.btn_about);

    btn_resume.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        resumeGame();
      }
    });

    btn_singlePlayer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openGame(false);
      }
    });

    btn_multiPlayer.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        openGame(true);
      }
    });

    btn_options.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
        MainActivity.this.startActivity(intent);
      }
    });

    btn_about.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        MainActivity.this.startActivity(intent);
      }
    });
  }


  private void requestWritePermission() {
    boolean hasPermission = (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    G.hasWriteAccess = hasPermission;
    G.createDirectory();
    if (!hasPermission) {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
    }
  }


  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    switch (requestCode) {
      case 123: {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
          G.hasWriteAccess = true;
          G.createDirectory();
        } else {
          Toast.makeText(this, "Write to external storage required for loading & saving game", Toast.LENGTH_LONG).show();
        }
      }
    }
  }


  private void openGame(boolean isMultiplayer) {
    Intent intent = new Intent(MainActivity.this, GameActivity.class);
    intent.putExtra("isMultiplayer", isMultiplayer);
    MainActivity.this.startActivity(intent);
  }

  private void resumeGame() {
    Intent intent = new Intent(MainActivity.this, GameActivity.class);
    intent.putExtra("resume", true);
    MainActivity.this.startActivity(intent);
  }
}
