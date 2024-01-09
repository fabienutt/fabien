package com.example.fabien;



import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.GestureDetector;
import android.view.WindowManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.content.ContextCompat;

public class Slider extends View {
    private float v_courante;
    private int min_Slider=0;
    private int max_Slider=100;
    private int value_color;
    private int cursor_color;
    private int center_color;
    private int bar_color;
    private int grad_color;
    private float bar_width;
    private SliderChangeListener mSliderChangeListener;
    private boolean enabled;
    private int disabled_Color;
    private int side_Color;

    public float getBar_length() {
        return bar_length;
    }

    public void setBar_length(float bar_length) {
        this.bar_length = bar_length;
    }

    private float bar_length;
    private float cursor_diameter;
    private Paint mCursorPaint;
    private Paint mgradPaint;
    private Paint mValueBarPaint;
    private Paint mBarPaint;
    private Paint mSidePaint;
    public float getV_courante() {
        return v_courante;
    }

    public void setV_courante(int v_courante) {
        this.v_courante = v_courante;
    }

    public float getMin_Slider() {
        return min_Slider;
    }

    public void setMin_Slider(int min_Slider) {
        this.min_Slider = min_Slider;
    }

    public float getMax_Slider() {
        return max_Slider;
    }

    public void setMax_Slider(int max_Slider) {
        this.max_Slider = max_Slider;
    }

    public void setSliderChangeListener(SliderChangeListener mSliderChangeListener) {
        this.mSliderChangeListener = mSliderChangeListener;
    }
    public int getValue_color() {
        return value_color;
    }

    public void setValue_color(int value_color) {
        this.value_color = value_color;
    }

    public int getCursor_color() {
        return cursor_color;
    }

    public void setCursor_color(int cursor_color) {
        this.cursor_color = cursor_color;
    }

    public int getBar_color() {
        return bar_color;
    }

    public void setBar_color(int bar_color) {
        this.bar_color = bar_color;
    }

    public float getBar_width() {
        return bar_width;
    }

    public void setBar_width(float bar_width) {
        this.bar_width = bar_width;
    }

    public float getCursor_diameter() {
        return cursor_diameter;
    }

    public void setCursor_diameter(float cursor_diameter) {
        this.cursor_diameter = cursor_diameter;
    }
    public final static int MIN_BAR_LENGTH=160;
    public final static int MIN_CURSOR_DIAMETER=60;
    public final static float BAR_WIDTH_DEFAULT=20;
    public final static float BAR_LENGTH_DEFAULT=250;
    public final static float CURSOR_DIAMETER_DEFAULT=60;

    private float valueToRatio(float value){
        float res=(value-min_Slider)/(max_Slider-min_Slider);
        return res;
    }
    private float ratioToValue(float ratio){
        float res=ratio*(max_Slider-min_Slider)+min_Slider;
        return res;
    }
    private Point toPos(int value) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float angle = valueToRatio(value) * 360;
        angle += 180; // Inversion de l'angle

        double angleRadians = Math.toRadians(angle);

        int x = (int) (centerX + Math.cos(angleRadians) * (bar_length / 2));
        int y = (int) (centerY - Math.sin(angleRadians) * (bar_length / 2));

        return new Point(x, y);
    }


    private float toValue(Point point){
        float res=1-(point.y-getPaddingTop()-cursor_diameter/2)/bar_length;
        if(res>1){
            res=1;
        }
        if (res < 0) {
            res=0;
        }

        return ratioToValue(res);
    }

    public void init(Context context, AttributeSet attrs){
        mValueBarPaint = new Paint();
        mBarPaint = new Paint();
        mCursorPaint = new Paint();
        mSidePaint = new Paint();
        mgradPaint = new Paint();

        // Remplacez les lignes suivantes par les couleurs définies dans vos ressources
        bar_color = ContextCompat.getColor(context, R.color.circle);
        cursor_color = ContextCompat.getColor(context, R.color.sndcircle);
        value_color = ContextCompat.getColor(context, R.color.sndcircle);
        disabled_Color = ContextCompat.getColor(context, R.color.center);
        center_color= ContextCompat.getColor(context, R.color.center);
        side_Color= ContextCompat.getColor(context, R.color.side);
        grad_color = ContextCompat.getColor(context, R.color.grad);

        bar_length = dp_to_pixels(BAR_LENGTH_DEFAULT);
        bar_width = dp_to_pixels(BAR_WIDTH_DEFAULT);
        cursor_diameter = dp_to_pixels(CURSOR_DIAMETER_DEFAULT);

        mBarPaint.setStrokeWidth(bar_width);
        mCursorPaint.setStrokeWidth(bar_width);
        mCursorPaint.setStyle(Paint.Style.FILL);
        mSidePaint.setStyle(Paint.Style.FILL);
        mSidePaint.setColor(side_Color);

        mBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mValueBarPaint.setStrokeCap(Paint.Cap.ROUND);

        if (enabled) {
            mBarPaint.setColor(bar_color);
            mValueBarPaint.setColor(value_color);
            mCursorPaint.setColor(cursor_color);
        } else { //revoir la condition enabled
            mBarPaint.setColor(bar_color);
            mValueBarPaint.setColor(value_color);
            mCursorPaint.setColor(disabled_Color);
        }

        setMinimumHeight((int) dp_to_pixels(MIN_BAR_LENGTH + MIN_CURSOR_DIAMETER) + getPaddingTop() + getPaddingBottom());
        setMinimumWidth((int) dp_to_pixels(MIN_CURSOR_DIAMETER) + getPaddingLeft() + getPaddingRight());



    }


    private void adaptDims() {
        float pt=getPaddingTop();
        float pl=getPaddingLeft();
        float pr= getPaddingRight();
        float pb=getPaddingBottom();
        float minSliderWidth=getMinimumWidth()-pr-pl;
        float minSliderHeight=getMinimumWidth()-pt-pb;
        //plus petit curseur excede la largeur du View: suppression des padings et reduction du curseur
        if(minSliderWidth>=getWidth()){
            cursor_diameter=getWidth();
            pr=0;pl=0;
        }
        if(minSliderHeight>=getHeight()){
            cursor_diameter=getHeight();
            pt=0;pb=0;
        }
        //le plus petit curseur +le padding excede la largeur du View : reduction des paddings
        else if(minSliderWidth+pl+pr>=getWidth()){
            cursor_diameter=minSliderWidth;
            float ratio=(getWidth()-minSliderWidth)/(pl+pr);
            pl*=ratio;pr*=ratio;
        }
        else if (Math.max(cursor_diameter,bar_length)+pl+pr>=getHeight()){
            cursor_diameter=getHeight()-pt-pb;
        }
        else if(minSliderHeight+pt+pb>=getHeight()){
            cursor_diameter=minSliderHeight;
            float ratio=(getHeight()-minSliderHeight)/(pt+pb);
            pt*=ratio;pb*=ratio;
        }
        else if (Math.max(cursor_diameter,bar_length)+pt+pb>=getHeight()){
            cursor_diameter=getHeight()-pt-pb;
        }

        setPadding((int) pl,(int)pt,(int)pr,(int)pb);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int suggestedHeight, suggestedWidth;
        int width, height;

        suggestedHeight=(int) Math.max(getMinimumHeight(),cursor_diameter+bar_length+getPaddingTop()+getPaddingBottom());
        suggestedWidth=(int) Math.max(getMinimumWidth(),cursor_diameter+bar_width+ getPaddingLeft()+getPaddingRight());

        width=resolveSize(suggestedWidth,widthMeasureSpec);
        height=resolveSize(suggestedHeight,heightMeasureSpec);
        setMeasuredDimension(width,height);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        adaptDims();
        Point p1, p2, p3;
        float startAngle =180;  // Angle de départ
        p1 = toPos(min_Slider);
        p2 = toPos(max_Slider);
        p3 = toPos((int) -v_courante+100);
        float sweepAngle = calculateSweepAngle(p1, p3);  // Angle d'arc basé sur la position du curseur
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float barWidth = getWidth() / 3.18f;
        float barHeight = getHeight() / 1.24f;
        // Dessine le cercle de fond
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, bar_length / 1.8f, mSidePaint);
        // Dessine le cercle de fond
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, bar_length / 2f, mBarPaint);
        // Dessine la barre circulaire
        canvas.drawArc(centerX - bar_length / 2f, centerY- bar_length / 2f, centerX + bar_length / 2f, centerY + bar_length / 2f, startAngle, sweepAngle, true, mValueBarPaint);
        // Dessine le curseur circulaire
        //canvas.drawCircle(p3.x, p3.y, cursor_diameter / 2, mCursorPaint);
        // Dessine le cercle de centre
        canvas.drawCircle(getWidth() / 2f, getHeight() / 2f, bar_length / 4f, mCursorPaint);


        // Dessine les graduations
        float angleStep = 360 / 8; // Angle entre chaque graduation (360 degrés divisé par le nombre de graduations)
        for (int i = 0; i <= 360; i += angleStep) {
            float angleRadians = (float) Math.toRadians(i);
            float angleRadians1 = (float) Math.toRadians(i+22.5);
            float startX = centerX + (bar_length / 3) * (float) Math.cos(angleRadians);
            float startY = centerY - (bar_length / 3) * (float) Math.sin(angleRadians);
            float endX, endY;
            endX = centerX + (bar_length / 2 - 22) * (float) Math.cos(angleRadians); // Ajustez la longueur des tirets ici
            endY = centerY - (bar_length / 2 - 22) * (float) Math.sin(angleRadians); // Ajustez la longueur des tirets ici
            mgradPaint.setStrokeWidth(8); // Ajustez la largeur des graduations ici
            mgradPaint.setStrokeCap(Paint.Cap.ROUND);
            mgradPaint.setColor(ContextCompat.getColor(getContext(), R.color.grad)); // Utilisez la couleur gris foncé définie dans vos ressources
            canvas.drawLine(startX, startY, endX, endY, mgradPaint);
        }
        angleStep = 360 /8 ;
        for (int i = 360/16; i <= 360; i += angleStep) {
            float angleRadians = (float) Math.toRadians(i);
            float angleRadians1 = (float) Math.toRadians(i+22.5);
            float startX = (float) (centerX + (bar_length / 2.8) * (float) Math.cos(angleRadians));
            float startY = (float) (centerY - (bar_length / 2.8) * (float) Math.sin(angleRadians));
            float endX, endY;
            endX = centerX + (bar_length / 2 - 37) * (float) Math.cos(angleRadians); // Ajustez la longueur des tirets ici
            endY = centerY - (bar_length / 2 - 37) * (float) Math.sin(angleRadians); // Ajustez la longueur des tirets ici
            mgradPaint.setStrokeWidth(6); // Ajustez la largeur des graduations ici
            mgradPaint.setStrokeCap(Paint.Cap.ROUND);
            mgradPaint.setColor(ContextCompat.getColor(getContext(), R.color.grad)); // Utilisez la couleur gris foncé définie dans vos ressources
            canvas.drawLine(startX, startY, endX, endY, mgradPaint);
        }
    }


    private float calculateSweepAngle(Point startPoint, Point endPoint) {
        float startAngle = (float) Math.toDegrees(Math.atan2(startPoint.y - getHeight() / 2f, startPoint.x - getWidth() / 2f));
        float endAngle = (float) Math.toDegrees(Math.atan2(endPoint.y - getHeight() / 2f, endPoint.x - getWidth() / 2f));

        float sweepAngle = endAngle - startAngle;

        if (sweepAngle < 0) {
            sweepAngle += 360;
        }

        return sweepAngle;
    }




    private boolean clic=false;
    @Override
    public boolean postDelayed(Runnable action, long delayMillis) {
        return super.postDelayed(action, delayMillis);
    }

    private static final String STATE_VCOURANTE = "state_v_courante";
    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("superState", super.onSaveInstanceState());
        bundle.putFloat(STATE_VCOURANTE, v_courante);
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            v_courante = bundle.getFloat(STATE_VCOURANTE, 0.0f);
            super.onRestoreInstanceState(bundle.getParcelable("superState"));
            return;
        }
        super.onRestoreInstanceState(state);
    }

    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_TIME_DELTA = 300; // Intervalle de temps en millisecondes pour considérer deux clics comme un double clic

    private boolean mDoubleClick = false;
    private boolean mDisableMove = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {

            case MotionEvent.ACTION_MOVE:
                float x = event.getX();
                float y = event.getY();
                if (!mDisableMove) {
                    float newValue = 100 - toValueOnArc(new Point((int) x, (int) y));
                    if (newValue <70 && v_courante>80) {
                        if (v_courante>newValue) {
                            v_courante = 100;
                            newValue = 100 - toValueOnArc(new Point((int) x, (int) y));
                        }
                        else{
                            v_courante=newValue;
                        }
                    }
                    else if (newValue >30 && v_courante<20){
                        if (v_courante<newValue){
                            v_courante=0;
                            newValue=100 - toValueOnArc(new Point((int) x, (int) y));}
                        else{
                            v_courante=newValue;
                        }
                    }
                    else{
                        v_courante=newValue;
                    }
                    if (mSliderChangeListener != null) {
                        mSliderChangeListener.onChange(v_courante);
                    }
                    invalidate();

                }
                break;
            case MotionEvent.ACTION_DOWN:

                if (mDoubleClick) {
                    mDisableMove = true;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDisableMove = false;
                        }
                    }, 200);
                    v_courante = min_Slider;
                    if (mSliderChangeListener != null) mSliderChangeListener.onDoubleClick(v_courante);
                    invalidate();
                } else {
                    mDoubleClick = true;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mDoubleClick = false;
                        }
                    }, 500);
                }


                invalidate();
                break;
            default:

        }
        return true;
    }

    private float toValueOnArc(Point point) {
        float angle = calculateAngle(point);
        float ratio = angle / 360;

        if (ratio > 1) {
            ratio = 1;
        }

        if (ratio < 0) {
            ratio = 0;
        }

        return ratioToValue(ratio);
    }



    private float calculateAngle(Point point) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;

        float dx = point.x - centerX;
        float dy = point.y - centerY;

        double angleRadians = Math.atan2(dy, dx);
        float angleDegrees = (float) Math.toDegrees(angleRadians);

        angleDegrees = 180-angleDegrees; // Inversion de l'angle

        if (angleDegrees < 0) {
            angleDegrees += 360;
        }

        return angleDegrees;
    }



    public interface SliderChangeListener {
        void onChange(float value);

        void onDoubleClick(float value);
    }

    private float dp_to_pixels(float dp){
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics());
    }
    public  Slider (Context context){
        super(context);
        init(context,null);

    }
    public  Slider (Context context, AttributeSet attrs){
        super(context,attrs);
        init(context,attrs);
    }



}

