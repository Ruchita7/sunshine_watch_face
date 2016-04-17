/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.sunshine.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.graphics.Palette;
import android.support.wearable.watchface.CanvasWatchFaceService;
import android.support.wearable.watchface.WatchFaceStyle;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowInsets;

import java.lang.ref.WeakReference;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Digital watch face with seconds. In ambient mode, the seconds aren't displayed. On devices with
 * low-bit ambient mode, the text is drawn without anti-aliasing in ambient mode.
 */
public class SunshineDigitalWatchFace extends CanvasWatchFaceService {
    private static final Typeface NORMAL_TYPEFACE =
            Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);

    /**
     * Update rate in milliseconds for interactive mode. We update once a second since seconds are
     * displayed in interactive mode.
     */
    private static final long INTERACTIVE_UPDATE_RATE_MS = TimeUnit.SECONDS.toMillis(1);

    /**
     * Handler message id for updating the time periodically in interactive mode.
     */
    private static final int MSG_UPDATE_TIME = 0;

    public static final String DATA_EVENT = "DATA_EVENT";
    public static final String DATA_KEY = "DATA_KEY";
    private Rect mPeekCardBounds = new Rect();

    public static final String LOG_TAG = SunshineDigitalWatchFace.class.getSimpleName();

    @Override
    public Engine onCreateEngine() {
        return new Engine();
    }

    private static class EngineHandler extends Handler {
        private final WeakReference<SunshineDigitalWatchFace.Engine> mWeakReference;

        public EngineHandler(SunshineDigitalWatchFace.Engine reference) {
            mWeakReference = new WeakReference<>(reference);
        }

        @Override
        public void handleMessage(Message msg) {
            SunshineDigitalWatchFace.Engine engine = mWeakReference.get();
            if (engine != null) {
                switch (msg.what) {
                    case MSG_UPDATE_TIME:
                        engine.handleUpdateTimeMessage();
                        break;
                }
            }
        }
    }

    private class Engine extends CanvasWatchFaceService.Engine {
        final Handler mUpdateTimeHandler = new EngineHandler(this);
        boolean mRegisteredTimeZoneReceiver = false;
        private Paint mBackgroundPaint;
        private Paint mTextPaint;
        private boolean mAmbient;
        private Time mTime;
        MessageReceiver messageReceiver;

        private Bitmap mBackgroundBitmap;
        private Bitmap mGrayBackgroundBitmap;
        private int mWatchTimeColor;
        private boolean mBurnInProtection;

        private Paint mDatePaint;
        private Paint mHighTempPaint;
        private Paint mLowTempPaint;
        private Paint mLinePaint;
        private int mWeatherId;
        private String mDdate;
        private String mHigh;
        private String mLow;

        private Bitmap mBgClearBitmap;
        private Bitmap mBgRainBitmap;
        private Bitmap mBgCloudyBitmap;
        private Bitmap mBgDrizzleBitmap;
        private Bitmap mBgFogBitmap;
        private Bitmap mBgMostlyClearBitmap;
        private Bitmap mBgSnowBitmap;
        private Bitmap mBgStormBitmap;
        private Bitmap mBgThunderstormBitmap;

        final BroadcastReceiver mTimeZoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mTime.clear(intent.getStringExtra("time-zone"));
                mTime.setToNow();
            }
        };
        int mTapCount;

        float mXOffset;
        float mYOffset;

        /**
         * Whether the display supports fewer bits for each color in ambient mode. When true, we
         * disable anti-aliasing in ambient mode.
         */
        boolean mLowBitAmbient;

        @Override
        public void onCreate(SurfaceHolder holder) {
            super.onCreate(holder);

            setWatchFaceStyle(new WatchFaceStyle.Builder(SunshineDigitalWatchFace.this)
                    .setCardPeekMode(WatchFaceStyle.PEEK_MODE_VARIABLE)
                    .setBackgroundVisibility(WatchFaceStyle.BACKGROUND_VISIBILITY_INTERRUPTIVE)
                    .setShowSystemUiTime(false)
                    .setAcceptsTapEvents(true)
                    .build());
            Resources resources = SunshineDigitalWatchFace.this.getResources();
            mYOffset = resources.getDimension(R.dimen.digital_y_offset);
            mWatchTimeColor = Color.WHITE;
            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(resources.getColor(R.color.background));
            mDatePaint = new Paint();
            mDatePaint = createTextPaint(mWatchTimeColor);
            mHighTempPaint = new Paint();
            mHighTempPaint = createTextPaint(mWatchTimeColor);
            mLowTempPaint = new Paint();
            mLowTempPaint = createTextPaint(mWatchTimeColor);

            mTextPaint = new Paint();
            //   mTextPaint = createTextPaint(resources.getColor(R.color.digital_text));
            mTextPaint = createTextPaint(mWatchTimeColor);
            mLinePaint = createTextPaint(mWatchTimeColor);
            mTime = new Time();

            messageReceiver = new MessageReceiver();
            IntentFilter filter = new IntentFilter(DATA_EVENT);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(messageReceiver, filter);

            mBackgroundPaint = new Paint();
            mBackgroundPaint.setColor(Color.BLUE);
            mBackgroundBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_mostly_clear);

            //Bitmaps for each weather condition image
            mBgClearBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_clear);
            mBgCloudyBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_cloudy);
            mBgDrizzleBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_drizzle);
            mBgFogBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_fog);
            mBgMostlyClearBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_mostly_clear);
            mBgRainBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_rain);
            mBgSnowBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_snow);
            mBgStormBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_storm);
            mBgThunderstormBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_thunderstorm);
        }

        //Class to receive broadcast intent from DataLayerListenerService
        private class MessageReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.hasExtra(DATA_KEY)) {
                    Bundle bundle = intent.getBundleExtra(DATA_KEY);
                    mDdate = bundle.getString("date");
                    mHigh = bundle.getString("high");
                    mLow = bundle.getString("low");
                    mWeatherId = bundle.getInt("weatherId");
                }
            }
        }


        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            super.onSurfaceChanged(holder, format, width, height);
            float scale = ((float) width) / (float) mBackgroundBitmap.getWidth();

            mBackgroundBitmap = Bitmap.createScaledBitmap(mBackgroundBitmap,
                    (int) (mBackgroundBitmap.getWidth() * scale),
                    (int) (mBackgroundBitmap.getHeight() * scale), true);
            if (!mBurnInProtection && !mLowBitAmbient) {
                initGrayBackgroundBitmap();
            }
        }

        private void initGrayBackgroundBitmap() {
            mGrayBackgroundBitmap = Bitmap.createBitmap(
                    mBackgroundBitmap.getWidth(),
                    mBackgroundBitmap.getHeight(),
                    Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(mGrayBackgroundBitmap);
            Paint grayPaint = new Paint();
            ColorMatrix colorMatrix = new ColorMatrix();
            colorMatrix.setSaturation(0);
            ColorMatrixColorFilter filter = new ColorMatrixColorFilter(colorMatrix);
            grayPaint.setColorFilter(filter);
            canvas.drawBitmap(mBackgroundBitmap, 0, 0, grayPaint);
        }

        @Override
        public void onDestroy() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            super.onDestroy();
        }

        private Paint createTextPaint(int textColor) {
            Paint paint = new Paint();
            paint.setColor(textColor);
            paint.setTypeface(NORMAL_TYPEFACE);
            paint.setAntiAlias(true);
            return paint;
        }

        @Override
        public void onVisibilityChanged(boolean visible) {
            super.onVisibilityChanged(visible);

            if (visible) {
                registerReceiver();

                // Update time zone in case it changed while we weren't visible.
                mTime.clear(TimeZone.getDefault().getID());
                mTime.setToNow();
            } else {
                unregisterReceiver();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        private void registerReceiver() {
            if (mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = true;
            IntentFilter filter = new IntentFilter(Intent.ACTION_TIMEZONE_CHANGED);
            SunshineDigitalWatchFace.this.registerReceiver(mTimeZoneReceiver, filter);
        }

        private void unregisterReceiver() {
            if (!mRegisteredTimeZoneReceiver) {
                return;
            }
            mRegisteredTimeZoneReceiver = false;
            SunshineDigitalWatchFace.this.unregisterReceiver(mTimeZoneReceiver);
        }

        @Override
        public void onApplyWindowInsets(WindowInsets insets) {
            super.onApplyWindowInsets(insets);

            // Load resources that have alternate values for round watches.
            Resources resources = SunshineDigitalWatchFace.this.getResources();
            boolean isRound = insets.isRound();
            mXOffset = resources.getDimension(isRound
                    ? R.dimen.digital_x_offset_round : R.dimen.digital_x_offset);
            float textSize = resources.getDimension(isRound
                    ? R.dimen.digital_text_size_round : R.dimen.digital_text_size);

            mTextPaint.setTextSize(textSize);
            mDatePaint.setTextSize(resources.getDimension(R.dimen.analog_date_text_size));
            mHighTempPaint.setTextSize(resources.getDimension(R.dimen.analog_temp_text_size));
            mLowTempPaint.setTextSize(resources.getDimension(R.dimen.analog_temp_text_size));
        }

        @Override
        public void onPropertiesChanged(Bundle properties) {
            super.onPropertiesChanged(properties);
            mLowBitAmbient = properties.getBoolean(PROPERTY_LOW_BIT_AMBIENT, false);
            mBurnInProtection = properties.getBoolean(PROPERTY_BURN_IN_PROTECTION, false);
        }

        @Override
        public void onTimeTick() {
            super.onTimeTick();
            invalidate();
        }

        @Override
        public void onAmbientModeChanged(boolean inAmbientMode) {
            super.onAmbientModeChanged(inAmbientMode);
            if (mAmbient != inAmbientMode) {
                mAmbient = inAmbientMode;
                if (mLowBitAmbient) {
                    mTextPaint.setAntiAlias(!inAmbientMode);
                    mDatePaint.setAntiAlias(!inAmbientMode);
                    mHighTempPaint.setAntiAlias(!inAmbientMode);
                    mLowTempPaint.setAntiAlias(!inAmbientMode);
                    mLinePaint.setAntiAlias(!inAmbientMode);
                }
                if (mAmbient) {
                    getBitmapWeatherCondition();
                    initGrayBackgroundBitmap();
                }
                invalidate();
            }

            // Whether the timer should be running depends on whether we're visible (as well as
            // whether we're in ambient mode), so we may need to start or stop the timer.
            updateTimer();
        }

        /**
         * Captures tap event (and tap type) and toggles the background color if the user finishes
         * a tap.
         */
        @Override
        public void onTapCommand(int tapType, int x, int y, long eventTime) {
            Resources resources = SunshineDigitalWatchFace.this.getResources();
            switch (tapType) {
                case TAP_TYPE_TOUCH:
                    // The user has started touching the screen.
                    break;
                case TAP_TYPE_TOUCH_CANCEL:
                    // The user has started a different gesture or otherwise cancelled the tap.
                    break;
                case TAP_TYPE_TAP:
                    // The user has completed the tap gesture.
                    mTapCount++;
                    mBackgroundPaint.setColor(resources.getColor(mTapCount % 2 == 0 ?
                            R.color.background : R.color.background2));
                    break;
            }
            invalidate();
        }

        @Override
        public void onDraw(Canvas canvas, Rect bounds) {

            getBitmapWeatherCondition();
            if (mAmbient && (mLowBitAmbient || mBurnInProtection)) {
                canvas.drawColor(Color.BLACK);
            } else if (mAmbient) {
                canvas.drawBitmap(mGrayBackgroundBitmap, 0, 0, mBackgroundPaint);
            } else {
                canvas.drawBitmap(mBackgroundBitmap, 0, 0, mBackgroundPaint);
            }


            float x = mXOffset;
            float y = mYOffset;
            // Draw H:MM in ambient mode or H:MM:SS in interactive mode.
            mTime.setToNow();
            String text = mAmbient
                    ? String.format("%d:%02d", mTime.hour, mTime.minute)
                    : String.format("%d:%02d:%02d", mTime.hour, mTime.minute, mTime.second);
            canvas.drawText(text, x, y, mTextPaint);

            //check if broadcast is received
            if (mDdate != null) {
                y = y + getResources().getDimension(R.dimen.digital_line_height);
                canvas.drawText(mDdate, x, y, mDatePaint);
                y += mDatePaint.getTextSize();
                canvas.drawLine(x, y, x + mDatePaint.measureText(mDdate), y, mLinePaint);
                y += getResources().getDimension(R.dimen.line_width);
                canvas.drawText(mHigh, x, y, mHighTempPaint);
                x += mHighTempPaint.measureText(mHigh) + getResources().getDimension(R.dimen.temp_space_width);
                canvas.drawText(mLow, x, y, mLowTempPaint);
                x += mLowTempPaint.measureText(mLow) + getResources().getDimension(R.dimen.temp_space_width);
                canvas.drawText(getString(SunshineUtility.getWeatherConditionResId(mWeatherId)), x, y, mHighTempPaint);
            }
            if (mAmbient) {
                canvas.drawRect(mPeekCardBounds, mBackgroundPaint);
            }
        }

        /**
         * Starts the {@link #mUpdateTimeHandler} timer if it should be running and isn't currently
         * or stops it if it shouldn't be running but currently is.
         */
        private void updateTimer() {
            mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
            if (shouldTimerBeRunning()) {
                mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
            }
        }

        /**
         * Returns whether the {@link #mUpdateTimeHandler} timer should be running. The timer should
         * only run when we're visible and in interactive mode.
         */
        private boolean shouldTimerBeRunning() {
            return isVisible() && !isInAmbientMode();
        }

        /**
         * Handle updating the time periodically in interactive mode.
         */
        private void handleUpdateTimeMessage() {
            invalidate();
            if (shouldTimerBeRunning()) {
                long timeMs = System.currentTimeMillis();
                long delayMs = INTERACTIVE_UPDATE_RATE_MS
                        - (timeMs % INTERACTIVE_UPDATE_RATE_MS);
                mUpdateTimeHandler.sendEmptyMessageDelayed(MSG_UPDATE_TIME, delayMs);
            }
        }

        @Override
        public void onPeekCardPositionUpdate(Rect rect) {
            super.onPeekCardPositionUpdate(rect);
            mPeekCardBounds.set(rect);
        }

        /**
         * Find associated bitmap based on weather condition id
         */
        private void getBitmapWeatherCondition() {
            if (mWeatherId > 0) {
                if (mWeatherId >= 200 && mWeatherId <= 232) {
                    mBackgroundBitmap = mBgStormBitmap;
                } else if (mWeatherId >= 300 && mWeatherId <= 321) {
                    mBackgroundBitmap = mBgDrizzleBitmap;
                } else if (mWeatherId >= 500 && mWeatherId <= 504) {
                    mBackgroundBitmap = mBgRainBitmap;
                } else if (mWeatherId == 511) {
                    mBackgroundBitmap = mBgSnowBitmap;
                } else if (mWeatherId >= 520 && mWeatherId <= 531) {
                    mBackgroundBitmap = mBgRainBitmap;
                } else if (mWeatherId >= 600 && mWeatherId <= 622) {
                    mBackgroundBitmap = mBgSnowBitmap;
                } else if (mWeatherId >= 701 && mWeatherId <= 761) {
                    mBackgroundBitmap = mBgFogBitmap;
                } else if (mWeatherId == 761 || mWeatherId == 781) {
                    mBackgroundBitmap = mBgThunderstormBitmap;
                } else if (mWeatherId == 800) {
                    mBackgroundBitmap = mBgClearBitmap;
                } else if (mWeatherId == 801) {
                    mBackgroundBitmap = mBgMostlyClearBitmap;
                } else if (mWeatherId >= 802 && mWeatherId <= 804) {
                    mBackgroundBitmap = mBgCloudyBitmap;
                }
            }

        }
    }
}
