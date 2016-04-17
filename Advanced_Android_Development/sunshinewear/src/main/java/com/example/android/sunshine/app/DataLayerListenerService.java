package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

/**
 * Class implementing WearableListenerService for receiving weather data item from SunshineSyncAdapter
 */
public class DataLayerListenerService extends WearableListenerService {

    GoogleApiClient mGoogleApiClient;
    public static final String TODAY_WEATHER_KEY = "/today-weather";
    final String LOG_TAG = DataLayerListenerService.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();
    }

    /**
     * @param dataEvents
     */
    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        if (!mGoogleApiClient.isConnected() || !mGoogleApiClient.isConnecting()) {
            ConnectionResult connectionResult = mGoogleApiClient
                    .blockingConnect(30, TimeUnit.SECONDS);
            if (!connectionResult.isSuccess()) {
                Log.e(LOG_TAG, "DataLayerListenerService failed to connect to GoogleApiClient, "
                        + "error code: " + connectionResult.getErrorCode());
                return;
            }
        }
        Bundle weatherBundle = null;

        for (DataEvent event : dataEvents) {
            DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
            String path = event.getDataItem().getUri().getPath();

            if (path.equals(TODAY_WEATHER_KEY)) {
                weatherBundle = new Bundle();
                weatherBundle.putString("date", dataMap.getString("date"));
                weatherBundle.putInt("weatherId", dataMap.getInt("conditionId"));
                weatherBundle.putString("high", dataMap.getString("high"));
                weatherBundle.putString("low", dataMap.getString("low"));
                break;
            }
        }

        if (weatherBundle != null) {
            Intent messageIntent = new Intent(SunshineDigitalWatchFace.DATA_EVENT);
            messageIntent.putExtra(SunshineDigitalWatchFace.DATA_KEY, weatherBundle);
            //send broadcast to SunshineDigitalWatchFace
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(messageIntent);
        }
    }
}
