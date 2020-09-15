package project.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;


import ir.mitrade.dotline.R;
import project.activity.GameActivity;
import project.app.Settings;

public class OptionDialog extends Dialog {

  private GameActivity activity;

  public OptionDialog(GameActivity activity) {
    super(activity);
    this.activity = activity;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requestWindowFeature(Window.FEATURE_NO_TITLE);
    setContentView(R.layout.dialog_option);

    getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

    Button btn_ok = (Button) findViewById(R.id.btn_ok);
    final EditText edt_cols = (EditText) findViewById(R.id.edt_cols);
    final EditText edt_rows = (EditText) findViewById(R.id.edt_rows);

    edt_cols.setText("" + Settings.getCols());
    edt_rows.setText("" + Settings.getRows());

    btn_ok.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        int cols = Integer.parseInt(edt_cols.getText().toString());
        int rows = Integer.parseInt(edt_rows.getText().toString());

          Settings.setRows(rows);
          Settings.setCols(cols);

        activity.getGameView().resetGame();
        dismiss();
      }
    });
  }
}