package cz.atha.iotoys;

import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class JoystickView extends View implements View.OnTouchListener {

    final private static int INVALID_POSITION = -1;

    private float currX = INVALID_POSITION;
    private float currY = INVALID_POSITION;

    private int ballColor = Color.BLUE;
    private JoystickListener listener;


    public JoystickView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOnTouchListener(this);
    }

    public JoystickView(Context context){
        super(context);
        setOnTouchListener(this);
    }

    // TODO getters for now, implement listener
    public float getCurrX() {
        return currX;
    }

    public void setJoystickListener(JoystickListener listener){
        this.listener = listener;
    }

    interface JoystickListener {
        boolean onJoystickPositionChanged(byte x, byte y);
    }

    public float getCurrY() {
        return currY;
    }

    public boolean onTouch(View v, MotionEvent e) {

        currX = e.getX();
        currY = e.getY();

        // when they let go of the joystick it pops back to the center
        if(e.getAction() == MotionEvent.ACTION_UP) {
            currX = getWidth() / 2;
            currY = getHeight() / 2;
        }

        // keep it within bounds
        if (currX < 0) currX = 0;
        else if (currX > getWidth()) currX = getWidth();
        if (currY < 0) currY = 0;
        else if (currY > getHeight()) currY = getHeight();

        // notify listener
        if (listener != null){

            // TODO change so this is computed whenever the view gets resized
            int maxX = getWidth() / 2;
            int maxY = getHeight() / 2;

            byte x = (byte)((currX - maxX) / maxX * 127);
            byte y = (byte)((currY - maxY) / maxY * 127);

            listener.onJoystickPositionChanged(x, y);
        }

        invalidate();

        return true;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // default position if not initialized
        if (currX == INVALID_POSITION && currY == INVALID_POSITION) {
            currX = getWidth() / 2;
            currY = getHeight() / 2;
        }

        // Create a new Paint object.
        Paint paint = new Paint();

        // Set paint color.
        paint.setColor(this.ballColor);

        // Draw a circle in the canvas.
        canvas.drawCircle(currX, currY, 35, paint);
    }

}
