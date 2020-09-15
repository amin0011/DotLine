package project.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.MediaPlayer;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ir.mitrade.dotline.R;
import project.app.G;
import project.app.Settings;
import project.helper.FileHelper;
import project.helper.MathHelper;

import static project.app.G.context;


public class GameView extends View {
  private static final int EDGE_LEFT = 0;
  private static final int EDGE_RIGHT = 1;
  private static final int EDGE_TOP = 2;
  private static final int EDGE_BOTTOM = 3;

  private static final int TYPE_CPU = 0;
  private static final int TYPE_PLAYER = 1;

  private Paint paintBox;
  private Paint paintDot;
  private Paint paintScoreBorder;
  private Paint paintScoreEffect;
  private Paint paintText;
  private Paint paintLine;

  private int boxWidth;
  private int boxHeight;

  private int screenWidth;
  private int screenWidthHalf;
  private int screenHeight;

  private int offsetX;
  private int offsetY;

  private float touchX;
  private float touchY;

  private ArrayList<Move> availableMoves = new ArrayList<>();

  private float[] hepticRadius = new float[]{0, 0};
  private boolean isLockedForRendering = false;
  private float drawingAlpha = 0;

  private Bitmap bluePencil;
  private Bitmap redPencil;

  private State state = new State();
  private Options options = new Options();

  private float physicTotalTime = 0;

  private ArrayList<MediaPlayer> pencilPlayer = new ArrayList<>();

  private static class Theme {
    private static final int[] PLAYER_COLORS = new int[]{Color.parseColor("#4444ff"), Color.parseColor("#ff4444")};
    private static final int BACKGROUND_COLOR = Color.parseColor("#0d0b38"); // color 222222

    private static final int SPACE_BETWEEN_DOTS = 150;
    private static final int DOT_RADIUS = 15;
  }


  private class State {
    private ArrayList<Action> actions = new ArrayList<>();

    private int playerIndex = 1;
    private int[] playerScores = new int[]{0, 0};
    private boolean isGameOver = false;
  }


  private class Options {
    protected int cols;
    protected int rows;

    private String[] playerNames = new String[]{"PLAYER 1", "PLAYER 2"};
    private int[] playerTypes = new int[]{TYPE_PLAYER, TYPE_PLAYER};

    private boolean highPerformance = Settings.isEnableHighPerformance();
  }


  private class ScreenPosition {
    public int x;
    public int y;

    public ScreenPosition(int x, int y) {
      this.x = x;
      this.y = y;
    }
  }


  private class Diff {
    public Point point;
    public Float diff;

    public Diff(Point point, float diff) {
      this.point = point;
      this.diff = diff;
    }
  }


  private class Point {
    public int i;
    public int j;

    public Point(int i, int j) {
      this.i = i;
      this.j = j;
    }
  }


  private class Box {
    public int i;
    public int j;
    public int playerIndex;

    public Box(int i, int j) {
      this.i = i;
      this.j = j;
    }
  }


  private class Action {
    public int playerIndex;
    public ArrayList<Box> boxes = new ArrayList<>();
    public Move move;

    public Action(Move move, int playerIndex) {
      this.move = move;
      this.playerIndex = playerIndex;
    }
  }


  private class Move {
    public int i1;
    public int j1;
    public int i2;
    public int j2;

    public Move(int i1, int j1, int i2, int j2) {
      this.i1 = i1;
      this.j1 = j1;
      this.i2 = i2;
      this.j2 = j2;
    }

    @Override
    public boolean equals(Object object) {
      if (!(object instanceof Move)) {
        return false;
      }

      Move move = (Move) object;
      return i1 == move.i1 && j1 == move.j1 && i2 == move.i2 && j2 == move.j2;
    }
  }


  public GameView(Context context) {
    super(context);
    initialize();
  }


  public GameView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    initialize();
  }


  public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize();
  }


  private void initialize() {
    if (isInEditMode()) {
      return;
    }

    initializeSounds();

    if (!options.highPerformance) {
      initializeBitmaps();
      mainLoop();
    }

    initializePaints();
  }


  private void initializeSounds() {
    pencilPlayer.add(MediaPlayer.create(G.context, R.raw.pencil_1));
    pencilPlayer.add(MediaPlayer.create(G.context, R.raw.pencil_2));
    pencilPlayer.add(MediaPlayer.create(G.context, R.raw.pencil_3));
  }


  private void initializeBitmaps() {
    bluePencil = BitmapFactory.decodeResource(G.resources, R.drawable.blue_pencil);
    redPencil = BitmapFactory.decodeResource(G.resources, R.drawable.red_pencil);
  }


  private void initializePaints() {
    paintDot = new Paint();
    paintDot.setColor(Color.WHITE);
    paintDot.setStyle(Paint.Style.FILL);
    paintDot.setAntiAlias(true);

    paintScoreBorder = new Paint();
    paintScoreBorder.setColor(Color.WHITE);
    paintScoreBorder.setStyle(Paint.Style.STROKE);
    paintScoreBorder.setStrokeWidth(10);
    paintScoreBorder.setAntiAlias(true);

    paintScoreEffect = new Paint();
    paintScoreEffect.setColor(Color.WHITE);
    paintScoreEffect.setStyle(Paint.Style.STROKE);
    paintScoreEffect.setStrokeWidth(4);
    paintScoreEffect.setAntiAlias(true);

    paintBox = new Paint();
    paintBox.setColor(Color.WHITE);
    paintBox.setStyle(Paint.Style.FILL);
    paintBox.setAntiAlias(true);

    paintLine = new Paint();
    paintLine.setColor(Color.parseColor("#4444ff"));
    paintLine.setStyle(Paint.Style.FILL);
    paintLine.setStrokeWidth(10);
    paintLine.setAntiAlias(true);

    paintText = new Paint();
    paintText.setColor(Color.WHITE);
    paintText.setStyle(Paint.Style.FILL);
    paintText.setAntiAlias(true);
    paintText.setTextSize(30);
    paintText.setTextAlign(Paint.Align.CENTER);
  }


  private void initializeMetrics() {
    boxWidth = (options.cols - 1) * Theme.SPACE_BETWEEN_DOTS;
    boxHeight = (options.rows - 1) * Theme.SPACE_BETWEEN_DOTS;

    screenWidth = G.displayMetrics.widthPixels;
    screenHeight = G.displayMetrics.heightPixels;

    screenWidthHalf = screenWidth / 2;

    offsetX = (screenWidth - boxWidth) / 2;
    offsetY = (screenHeight - boxHeight) / 2;
  }


  public void resetGame() {
    options.cols = Settings.getCols();
    options.rows = Settings.getRows();

    initializeMetrics();

    state.playerScores[0] = 0;
    state.playerScores[1] = 0;
    state.playerIndex = 1;
    state.isGameOver = false;

    state.actions.clear();
    populateMoves();
    refresh();

    if (isCpuTurn()) {
      playNext();
    }

    refresh();
  }


  public void startGame(boolean isMultiplayer) {
    if (isMultiplayer) {
      options.playerTypes[1] = TYPE_PLAYER;
      options.playerNames[1] = "PLAYER 2";
    } else {
      options.playerTypes[1] = TYPE_CPU;
      options.playerNames[1] = "CPU";
    }

    resetGame();
  }


  private void mainLoop() {
    Thread main = new Thread(new Runnable() {
      @Override
      public void run() {
        long physicLastTime = System.currentTimeMillis();
        long renderLastTime = System.currentTimeMillis();

        while (true) {
          long physicElapsedTime = System.currentTimeMillis() - physicLastTime;
          if (physicElapsedTime > 30) {
            physicTotalTime += physicElapsedTime;
            updatePhysic(physicElapsedTime);
            physicLastTime = System.currentTimeMillis();
          }


          long renderElapsedTime = System.currentTimeMillis() - renderLastTime;
          if (renderElapsedTime > 15) {
            renderGame();
            renderLastTime = System.currentTimeMillis();
          }

          try {
            Thread.sleep(15);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      }
    });

    main.start();
  }


  private void updateHepticRadius(long elapsedTime) {
    for (int i = 0; i < hepticRadius.length; i++) {
      hepticRadius[i] = ((physicTotalTime / 15f + i * 20) % 40) + 50;
    }
  }


  private void updateDrawingAlpha(long elapsedTime) {
    if (drawingAlpha > 1 && drawingAlpha < 2) {
      drawingAlpha += elapsedTime * 0.007f;
    } else {
      drawingAlpha += elapsedTime * 0.003f;
    }
    if (drawingAlpha >= 3) {
      isLockedForRendering = false;
      drawingAlpha = 0;
    }
  }


  private void updatePhysic(long elapsedTime) {
    updateHepticRadius(elapsedTime);
    updateDrawingAlpha(elapsedTime);
  }


  private void renderGame() {
    postInvalidate();
  }


  private void addToAvailableMoves(Move move) {
    boolean isInAction = false;
    for (Action action : state.actions) {
      if (action.move.equals(move)) {
        isInAction = true;
        break;
      }
    }

    if (!isInAction) {
      availableMoves.add(move);
    }
  }

  private void populateMoves() {
    availableMoves.clear();

    for (int i = 0; i < options.cols - 1; i++) {
      for (int j = 0; j < options.rows; j++) {
        addToAvailableMoves(new Move(i, j, i + 1, j));
      }
    }

    for (int i = 0; i < options.cols; i++) {
      for (int j = 0; j < options.rows - 1; j++) {
        addToAvailableMoves(new Move(i, j, i, j + 1));
      }
    }
  }


  private void refresh() {
    if (isGameFinished()) {
      state.isGameOver = true;
    }

    invalidate();
  }


  public void saveGame() {
    JSONObject save = new JSONObject();
    JSONObject optionsJson = new JSONObject();
    JSONObject stateJson = new JSONObject();
    JSONArray actionsJson = new JSONArray();
    try {
      optionsJson.put("cols", options.cols);
      optionsJson.put("rows", options.rows);
      optionsJson.put("opponentType", getPlayerType(2));
      save.put("options", optionsJson);

      stateJson.put("playerIndex", state.playerIndex);
      stateJson.put("actions", actionsJson);

      for (Action action : state.actions) {
        JSONObject jsonAction = new JSONObject();
        JSONObject jsonMove = new JSONObject();
        JSONArray jsonBoxes = new JSONArray();

        jsonMove.put("i1", action.move.i1);
        jsonMove.put("j1", action.move.j1);
        jsonMove.put("i2", action.move.i2);
        jsonMove.put("j2", action.move.j2);

        for (Box box : action.boxes) {
          JSONObject boxJson = new JSONObject();
          boxJson.put("i", box.i);
          boxJson.put("j", box.j);
          boxJson.put("playerIndex", box.playerIndex);

          jsonBoxes.put(boxJson);
        }

        jsonAction.put("move", jsonMove);
        jsonAction.put("boxes", jsonBoxes);
        jsonAction.put("playerIndex", action.playerIndex);

        actionsJson.put(jsonAction);
      }

      save.put("state", stateJson);
    } catch (JSONException e) {
      e.printStackTrace();
    }

    FileHelper.saveToFile(G.APP_DIR + "/save.dat", save.toString());
  }


  public void loadGame() {
    String savedGame = FileHelper.loadFromFile(G.APP_DIR + "/save.dat");

    try {
      JSONObject save = new JSONObject(savedGame);
      JSONObject optionsJson = save.getJSONObject("options");

      options.cols = optionsJson.getInt("cols");
      options.rows = optionsJson.getInt("rows");
      int opponentType = optionsJson.getInt("opponentType");
      options.playerTypes[1] = opponentType;

      if (opponentType == TYPE_CPU) {
        startGame(false);
      } else {
        startGame(true);
      }

      JSONObject stateJson = save.getJSONObject("state");
      state.playerIndex = stateJson.getInt("playerIndex");

      JSONArray actionsJson = stateJson.getJSONArray("actions");
      for (int i = 0; i < actionsJson.length(); i++) {
        JSONObject jsonAction = actionsJson.getJSONObject(i);
        JSONObject moveJson = jsonAction.getJSONObject("move");
        Action action = new Action(
          new Move(
            moveJson.getInt("i1"),
            moveJson.getInt("j1"),
            moveJson.getInt("i2"),
            moveJson.getInt("j2")
          ),
          jsonAction.getInt("playerIndex"));

        state.actions.add(action);

        JSONArray boxesJson = jsonAction.getJSONArray("boxes");
        for (int boxIndex = 0; boxIndex < boxesJson.length(); boxIndex++) {
          JSONObject boxJson = boxesJson.getJSONObject(boxIndex);
          Box box = new Box(boxJson.getInt("i"), boxJson.getInt("j"));
          box.playerIndex = boxJson.getInt("playerIndex");
          increasePlayerScore(box.playerIndex);
          action.boxes.add(box);
        }
      }

      populateMoves();

      refresh();
    } catch (JSONException e) {
      e.printStackTrace();
    }
  }


  private boolean isGameFinished() {
    return availableMoves.size() == 0;
  }


  private int getPlayerColor(int playerIndex) {
    return Theme.PLAYER_COLORS[playerIndex - 1];
  }


  private int getPlayerScore(int playerIndex) {
    return state.playerScores[playerIndex - 1];
  }


  private int getPlayerType(int playerIndex) {
    return options.playerTypes[playerIndex - 1];
  }


  private void increasePlayerScore(int playerIndex) {
    state.playerScores[playerIndex - 1]++;
  }


  private String getPlayerName(int playerIndex) {
    return options.playerNames[playerIndex - 1];
  }


  private boolean isCpuTurn() {
    return getPlayerType(state.playerIndex) == TYPE_CPU;
  }


  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    if (isInEditMode()) {
      return;
    }

  try {
    drawBackground(canvas);
    drawConnectedLines(canvas);
    drawDots(canvas);
    drawBoxes(canvas);
    drawScores(canvas);
    animatePencil(canvas);
  } catch (Exception e){
    e.getStackTrace();
  }

    if (state.isGameOver) {
      drawFinishMessage(canvas);
    }
  }


  private void drawPencil(Canvas canvas, int playerIndex, int x, int y) {
    float scale = 0.5f;

    Bitmap bitmap;
    if (playerIndex == 1) {
      bitmap = bluePencil;
    } else {
      bitmap = redPencil;
    }

    x -= (int) (bitmap.getWidth() * scale);

    Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
    RectF dstRect = new RectF(x, y, x + bitmap.getWidth() * scale, y + bitmap.getHeight() * scale);

    canvas.drawBitmap(bitmap, srcRect, dstRect, paintDot);
  }


  private ScreenPosition getPointPoisition(int i, int j) {
    int x = offsetX + (i * Theme.SPACE_BETWEEN_DOTS);
    int y = offsetY + ((options.rows - 1 - j) * Theme.SPACE_BETWEEN_DOTS);

    return new ScreenPosition(x, y);
  }


  private void drawBackground(Canvas canvas) {
    canvas.drawColor(Theme.BACKGROUND_COLOR);
  }


  private void drawConnectedLines(Canvas canvas) {
    if (!options.highPerformance && isLockedForRendering) {
      for (int i = 0; i < state.actions.size() - 1; i++) {
        Action action = state.actions.get(i);
        drawAction(canvas, action);
      }

      animateLastAction(canvas);
    } else {
      for (Action line : state.actions) {
        drawAction(canvas, line);
      }
    }
  }


  private ScreenPosition[] getLastLinePosition() {

      Action lastAction = state.actions.get(state.actions.size() - 1);
      ScreenPosition p1 = getPointPoisition(lastAction.move.i1, lastAction.move.j1);
      ScreenPosition p2 = getPointPoisition(lastAction.move.i2, lastAction.move.j2);
      paintLine.setColor(getPlayerColor(lastAction.playerIndex));

      ScreenPosition[] output = new ScreenPosition[2];
      output[0] = new ScreenPosition(0, 0);
      output[1] = new ScreenPosition(0, 0);

      if (drawingAlpha >= 2) {
        output[0].x = p1.x;
        output[0].y = p1.y;
        output[1].x = p2.x;
        output[1].y = p2.y;
        return output;
      }

      if (p1.x == p2.x) {
        //vertical
        int y2 = (int) (p1.y - Theme.SPACE_BETWEEN_DOTS * (drawingAlpha - 1));
        output[0].x = p1.x;
        output[0].y = p1.y;
        output[1].x = p2.x;
        output[1].y = y2;
        return output;
      } else {
        //horizontal
        int x2 = (int) (p1.x + Theme.SPACE_BETWEEN_DOTS * (drawingAlpha - 1));
        output[0].x = p1.x;
        output[0].y = p1.y;
        output[1].x = x2;
        output[1].y = p2.y;
        return output;
      }

  }


  private ScreenPosition[] getPencilPosition() {
    Action lastAction = state.actions.get(state.actions.size() - 1);
    ScreenPosition p1 = getPointPoisition(lastAction.move.i1, lastAction.move.j1);
    ScreenPosition p2 = getPointPoisition(lastAction.move.i2, lastAction.move.j2);

    ScreenPosition[] output = new ScreenPosition[2];
    output[0] = new ScreenPosition(0, 0);
    output[1] = new ScreenPosition(0, 0);

    if (drawingAlpha < 1) {
      int x0 = 0;
      int y0 = screenHeight;

      int diffX = p1.x - x0;
      int diffY = p1.y - y0;

      output[1].x = (int) (x0 + diffX * drawingAlpha);
      output[1].y = (int) (y0 + diffY * drawingAlpha);
      return output;
    }

    if (drawingAlpha >= 2) {
      int x0 = screenWidth;
      int y0 = screenHeight;

      int diffX = x0 - p2.x;
      int diffY = y0 - p2.y;

      output[1].x = (int) (p2.x + diffX * (drawingAlpha - 2));
      output[1].y = (int) (p2.y + diffY * (drawingAlpha - 2));
      return output;
    }

    if (p1.x == p2.x) {
      //vertical
      int y2 = (int) (p1.y - Theme.SPACE_BETWEEN_DOTS * (drawingAlpha - 1));
      output[0].x = p1.x;
      output[0].y = p1.y;
      output[1].x = p2.x;
      output[1].y = y2;
      return output;
    } else {
      //horizontal
      int x2 = (int) (p1.x + Theme.SPACE_BETWEEN_DOTS * (drawingAlpha - 1));
      output[0].x = p1.x;
      output[0].y = p1.y;
      output[1].x = x2;
      output[1].y = p2.y;
      return output;
    }
  }


  private void animateLastAction(Canvas canvas) {
    if (drawingAlpha > 1 && drawingAlpha <= 3) {
      try {
        ScreenPosition[] positions = getLastLinePosition();
        canvas.drawLine(positions[0].x, positions[0].y, positions[1].x, positions[1].y, paintLine);
      } catch (Exception e){
        e.getStackTrace();
      }
    }
  }


  private void animatePencil(Canvas canvas) {
    if (!options.highPerformance && isLockedForRendering) {
      ScreenPosition[] positions = getPencilPosition();

      Action lastAction = state.actions.get(state.actions.size() - 1);
      drawPencil(canvas, lastAction.playerIndex, positions[1].x, positions[1].y);
    }
  }


  private void drawHeptic(Canvas canvas, int x, int y) {
    for (float radius : hepticRadius) {
      float alpha = 2.5f * (100 - radius - 10);
      paintScoreEffect.setAlpha((int) alpha);
      canvas.drawCircle(x, y, radius, paintScoreEffect);
    }
  }


  private void drawAction(Canvas canvas, Action action) {
    ScreenPosition p1 = getPointPoisition(action.move.i1, action.move.j1);
    ScreenPosition p2 = getPointPoisition(action.move.i2, action.move.j2);
    paintLine.setColor(getPlayerColor(action.playerIndex));
    canvas.drawLine(p1.x, p1.y, p2.x, p2.y, paintLine);
  }


  private void drawBoxes(Canvas canvas) {
    int index = 0;
    for (Action action : state.actions) {
      if (!options.highPerformance && isLockedForRendering) {
        if (index == state.actions.size() - 1 && drawingAlpha <= 2) {
          break;
        }
      }

      for (Box box : action.boxes) {
        paintBox.setColor(getPlayerColor(box.playerIndex));
        ScreenPosition boxPos = getPointPoisition(box.i, box.j);
        canvas.drawCircle(
          boxPos.x + Theme.SPACE_BETWEEN_DOTS / 2,
          boxPos.y - Theme.SPACE_BETWEEN_DOTS / 2,
          30, paintBox);
      }
      index++;
    }
  }


  private void drawDots(Canvas canvas) {
    for (int i = 0; i < options.cols; i++) {
      for (int j = 0; j < options.rows; j++) {
        ScreenPosition point = getPointPoisition(i, j);
        canvas.drawCircle(point.x, point.y, Theme.DOT_RADIUS, paintDot);
      }
    }
  }


  private void drawPlayerScore(Canvas canvas, int playerIndex, int x, int y) {
    paintBox.setColor(getPlayerColor(playerIndex));
    canvas.drawCircle(x, y, 50, paintBox);
    canvas.drawText("" + getPlayerScore(playerIndex), x, y + 10, paintText);
    canvas.drawText(getPlayerName(playerIndex), x, y + 100, paintText);

    if (playerIndex == state.playerIndex) {
      if (!options.highPerformance) {
        drawHeptic(canvas, x, y);
      } else {
        paintScoreBorder.setColor(Color.WHITE);
        canvas.drawCircle(x, y, 60, paintScoreBorder);
      }
    } else {
      paintScoreBorder.setColor(Color.parseColor("#444444"));
      canvas.drawCircle(x, y, 60, paintScoreBorder);
    }
  }


  private void drawScores(Canvas canvas) {
    drawPlayerScore(canvas, 1, screenWidthHalf - 100, 100);
    drawPlayerScore(canvas, 2, screenWidthHalf + 100, 100);
  }


  private void drawFinishMessage(Canvas canvas) {
    canvas.drawText(getGameFinishMessage(), screenWidthHalf, getHeight() - 100, paintText);
  }


  private String getGameFinishMessage() {
    String message;
    if (getPlayerScore(1) == getPlayerScore(2)) {
      message = context.getString(R.string.gameDraw);
    } else if (getPlayerScore(1) > getPlayerScore(2)) {
      message = getPlayerName(1) + " " + context.getString(R.string.playerWonGame);
    } else {
      message = getPlayerName(2) + " " + context.getString(R.string.playerWonGame);
    }

    return message;
  }


  @Override
  public boolean onTouchEvent(MotionEvent event) {
    if (state.isGameOver) {
      return true;
    }

    if (isCpuTurn()) {
      return true;
    }

    if (isLockedForRendering) {
      return true;
    }

    touchX = event.getX();
    touchY = event.getY();

    ArrayList<Diff> diffs = getDiffsByOrder();

    Diff diff1 = diffs.get(0);
    Diff diff2 = diffs.get(1);

    if (diff1.diff > Theme.SPACE_BETWEEN_DOTS) {
      return true;
    }

    if (diff2.diff > Theme.SPACE_BETWEEN_DOTS) {
      return true;
    }

    connectLine(diff1.point, diff2.point);
    refresh();

    return super.onTouchEvent(event);
  }


  private ArrayList<Diff> getDiffsByOrder() {
    ArrayList<Diff> diffs = new ArrayList<>();

    for (int i = 0; i < options.cols; i++) {
      for (int j = 0; j < options.rows; j++) {
        ScreenPosition position = getPointPoisition(i, j);
        float diff = MathHelper.computeDiff(touchX, touchY, position.x, position.y);
        diffs.add(new Diff(new Point(i, j), diff));
      }
    }

    Collections.sort(diffs, new Comparator<Diff>() {
        @Override
        public int compare(Diff o1, Diff o2) {
          return o1.diff.compareTo(o2.diff);
        }
      }
    );

    return diffs;
  }


  private boolean connectLine(Point point1, Point point2) {
    Point firstPoint;
    Point secondPoint;

    Box box1;
    Box box2 = null;

    if (point1.i == point2.i) {
      // vertical
      if (point1.j < point2.j) {
        firstPoint = point1;
        secondPoint = point2;
      } else {
        firstPoint = point2;
        secondPoint = point1;
      }

      box1 = new Box(firstPoint.i, firstPoint.j);

      if (firstPoint.i > 0) {
        box2 = new Box(firstPoint.i - 1, firstPoint.j);
      }
    } else {
      // horizontal
      if (point1.i < point2.i) {
        firstPoint = point1;
        secondPoint = point2;
      } else {
        firstPoint = point2;
        secondPoint = point1;
      }

      box1 = new Box(firstPoint.i, firstPoint.j);

      if (firstPoint.j > 0) {
        box2 = new Box(firstPoint.i, firstPoint.j - 1);
      }
    }

    // if this line is already connected
    for (Action action : state.actions) {
      Move testMove = new Move(firstPoint.i, firstPoint.j, secondPoint.i, secondPoint.j);
      if (action.move.equals(testMove)) {
        return false;
      }
    }

    // add line to list of connected actions
    Action action = new Action(new Move(firstPoint.i, firstPoint.j, secondPoint.i, secondPoint.j), state.playerIndex);
    state.actions.add(action);

    if (Settings.isEnableSfx()) {
      if (options.highPerformance) {
        playRandomPencilSfx();
      } else {
        G.handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            playRandomPencilSfx();
          }
        }, 300);
      }
    }

    if (!options.highPerformance) {
      isLockedForRendering = true;
      drawingAlpha = 0;
    }

    for (int index = availableMoves.size() - 1; index >= 0; index--) {
      Move move = availableMoves.get(index);
      if (move.equals(action.move)) {
        availableMoves.remove(move);
        break;
      }
    }

    // check if player get award
    boolean wonBox1 = checkBox(box1);
    boolean wonBox2 = false;

    if (box2 != null) {
      wonBox2 = checkBox(box2);
    }

    boolean mustSwitchSide = !wonBox1 && !wonBox2;

    // if switching side required
    if (mustSwitchSide) {
      switchSide();
      return true;
    }

    playNext();
    return true;
  }


  private void playRandomPencilSfx() {
    int soundIndex = MathHelper.getRandom(0, 2);
    pencilPlayer.get(soundIndex).start();
  }


  private void switchSide() {
    if (state.playerIndex == 1) {
      state.playerIndex = 2;
    } else {
      state.playerIndex = 1;
    }

    playNext();
  }


  private void playNext() {
    int delayTime = 0;
    if (Settings.isEnableHighPerformance()) {
      delayTime = 100;
    } else {
      delayTime = 1000;
    }

    if (isCpuTurn()) {
      G.handler.postDelayed(new Runnable() {
        @Override
        public void run() {
          ai();
          refresh();
        }
      }, delayTime);
    }
  }


  private void ai() {
    if (isGameFinished()) {
      return;
    }

    if (fill3sidesBoxes()) {
      return;
    }

    ArrayList<Move> unsafeMoves = detectUnsafeMoves();

    if (makeRandomSafeMove(unsafeMoves)) {
      return;
    }

    makeRandomMove();
  }


  private ArrayList<Move> detectUnsafeMoves() {
    ArrayList<Move> unsafeMoves = new ArrayList<>();

    for (int i = 0; i <= options.cols - 2; i++) {
      for (int j = 0; j <= options.rows - 2; j++) {
        ArrayList<Integer> freeSides = new ArrayList<>();

        if (hasLeft(i, j)) {
          freeSides.add(EDGE_LEFT);
        }

        if (hasRight(i, j)) {
          freeSides.add(EDGE_RIGHT);
        }

        if (hasTop(i, j)) {
          freeSides.add(EDGE_TOP);
        }

        if (hasBottom(i, j)) {
          freeSides.add(EDGE_BOTTOM);
        }

        if (freeSides.size() == 2) {
          if (freeSides.contains(EDGE_LEFT) && freeSides.contains(EDGE_RIGHT)) {
            //top, bottom
            unsafeMoves.add(new Move(i, j + 1, i + 1, j + 1));
            unsafeMoves.add(new Move(i, j, i + 1, j));
          }

          if (freeSides.contains(EDGE_LEFT) && freeSides.contains(EDGE_TOP)) {
            //right, bottom
            unsafeMoves.add(new Move(i + 1, j, i + 1, j + 1));
            unsafeMoves.add(new Move(i, j, i + 1, j));
          }

          if (freeSides.contains(EDGE_LEFT) && freeSides.contains(EDGE_BOTTOM)) {
            //right, top
            unsafeMoves.add(new Move(i + 1, j, i + 1, j + 1));
            unsafeMoves.add(new Move(i, j + 1, i + 1, j + 1));
          }

          if (freeSides.contains(EDGE_RIGHT) && freeSides.contains(EDGE_TOP)) {
            //left, bottom.
            unsafeMoves.add(new Move(i, j, i, j + 1));
            unsafeMoves.add(new Move(i, j, i + 1, j));
          }

          if (freeSides.contains(EDGE_RIGHT) && freeSides.contains(EDGE_BOTTOM)) {
            //left, top
            unsafeMoves.add(new Move(i, j, i, j + 1));
            unsafeMoves.add(new Move(i, j + 1, i + 1, j + 1));
          }

          if (freeSides.contains(EDGE_TOP) && freeSides.contains(EDGE_BOTTOM)) {
            //left, right
            unsafeMoves.add(new Move(i, j, i, j + 1));
            unsafeMoves.add(new Move(i + 1, j, i + 1, j + 1));
          }
        }
      }
    }

    return unsafeMoves;
  }


  private boolean makeRandomSafeMove(ArrayList<Move> unsafeMoves) {
    ArrayList<Move> safeMoves = new ArrayList<>();

    for (Move move : availableMoves) {
      boolean isSafeMove = true;
      for (Move testMove : unsafeMoves) {
        if (testMove.equals(move)) {
          isSafeMove = false;
          break;
        }
      }

      if (isSafeMove) {
        safeMoves.add(move);
      }
    }

    if (safeMoves.size() == 0) {
      return false;
    }

    int moveIndex = MathHelper.getRandom(0, safeMoves.size() - 1);
    Move move = safeMoves.get(moveIndex);

    connectLine(new Point(move.i1, move.j1), new Point(move.i2, move.j2));
    return true;
  }


  private boolean makeRandomMove() {
    int moveIndex = MathHelper.getRandom(0, availableMoves.size() - 1);
    Move move = availableMoves.get(moveIndex);
    connectLine(new Point(move.i1, move.j1), new Point(move.i2, move.j2));

    return true;
  }


  private boolean fill3sidesBoxes() {
    for (int i = 0; i <= options.cols - 2; i++) {
      for (int j = 0; j <= options.rows - 2; j++) {
        int sides = 0;
        int freeSide = -1;

        if (hasBottom(i, j)) {
          sides++;
        } else {
          freeSide = EDGE_BOTTOM;
        }

        if (hasRight(i, j)) {
          sides++;
        } else {
          freeSide = EDGE_RIGHT;
        }

        if (hasLeft(i, j)) {
          sides++;
        } else {
          freeSide = EDGE_LEFT;
        }

        if (hasTop(i, j)) {
          sides++;
        } else {
          freeSide = EDGE_TOP;
        }

        if (sides == 3) {
          switch (freeSide) {
            case EDGE_BOTTOM:
              connectBottom(i, j);
              return true;
            case EDGE_RIGHT:
              connectRight(i, j);
              return true;
            case EDGE_LEFT:
              connectLeft(i, j);
              return true;
            case EDGE_TOP:
              connectTop(i, j);
              return true;
          }
        }
      }
    }

    return false;
  }


  private boolean connectLeft(int i, int j) {
    return connectLine(new Point(i, j), new Point(i, j + 1));
  }


  private boolean connectRight(int i, int j) {
    return connectLine(new Point(i + 1, j), new Point(i + 1, j + 1));
  }


  private boolean connectTop(int i, int j) {
    return connectLine(new Point(i, j + 1), new Point(i + 1, j + 1));
  }


  private boolean connectBottom(int i, int j) {
    return connectLine(new Point(i, j), new Point(i + 1, j));
  }


  private boolean hasLeft(int i, int j) {
    for (Action action : state.actions) {
      Move testMove = new Move(i, j, i, j + 1);
      if (action.move.equals(testMove)) {
        return true;
      }
    }

    return false;
  }


  private boolean hasRight(int i, int j) {
    for (Action action : state.actions) {
      Move testMove = new Move(i + 1, j, i + 1, j + 1);
      if (action.move.equals(testMove)) {
        return true;
      }
    }

    return false;
  }


  private boolean hasTop(int i, int j) {
    for (Action action : state.actions) {
      Move testMove = new Move(i, j + 1, i + 1, j + 1);
      if (action.move.equals(testMove)) {
        return true;
      }
    }

    return false;
  }


  private boolean hasBottom(int i, int j) {
    for (Action action : state.actions) {
      Move testMove = new Move(i, j, i + 1, j);
      if (action.move.equals(testMove)) {
        return true;
      }
    }

    return false;
  }


  private boolean checkBox(Box box) {
    int i = box.i;
    int j = box.j;

    boolean hasLeft = hasLeft(i, j);
    boolean hasRight = hasRight(i, j);
    boolean hasTop = hasTop(i, j);
    boolean hasBottom = hasBottom(i, j);

    boolean isFullConnected = hasLeft && hasRight && hasTop && hasBottom;
    if (isFullConnected) {
      box.playerIndex = state.playerIndex;
      state.actions.get(state.actions.size() - 1).boxes.add(box);

      increasePlayerScore(box.playerIndex);
      return true;
    }

    return false;
  }
}