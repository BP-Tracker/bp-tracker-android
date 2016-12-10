package com.bptracker.service;


import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.bptracker.firmware.core.BptApi;
import com.bptracker.firmware.core.Function;
import com.bptracker.util.IntentUtil;

import io.particle.android.sdk.utils.TLog;

/**
 *
 * <p>Supports binding an passing in intents
 *
 * <p>Requires intents extras: {@link IntentUtil#EXTRA_FUNCTION}
 * <p>The results are returned in a LocalBroadcast action {@link IntentUtil#ACTION_FUNCTION_RESULT}
 */
public class RunFunctionService extends Service {

    private final IBinder mBinder = new RunFunctionBinder();

    public class RunFunctionBinder extends Binder {
        RunFunctionService getService(){
            return RunFunctionService.this;
        }
    }


    /**
     * Tries to run the function passed in the Intent ignoring errors and the result
     * @param intent
     * @param flags
     * @param startId
     * @return
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        _log.v("onStartCommand called");

        Function function = intent.getParcelableExtra(IntentUtil.EXTRA_FUNCTION);

        if (function == null) {
            _log.e("Required extra EXTRA_FUNCTION is missing from intent");
            stopSelf(startId);
            return START_STICKY;
        }

        BptApi.call(function, new FunctionCallback(this, startId));
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        _log.v("onDestroy called");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //TODO: useful?
    public Uri callFunction(Function function, BptApi.ResultCallback callback){
        return BptApi.call(function, callback);
    }

    private static class FunctionCallback implements BptApi.ResultCallback {
        private int mStartId;
        private int mFunctionResult;
        private String mEventResult;
        private Service mService;

        public FunctionCallback(Service service, int startId) {
            mStartId = startId;
            mService = service;
            mFunctionResult = -1; //TODO: error prone
            mEventResult = null;
        }

        private void broadcastIntent(Function f, String error){
            Intent intent = new Intent(IntentUtil.ACTION_FUNCTION_RESULT);
            intent.putExtra(IntentUtil.EXTRA_FUNCTION, f);

            if(error == null){
                if(f.doReceiveEvents()){
                    intent.putExtra(IntentUtil.EXTRA_FUNCTION_EVENT_RESULT, mEventResult);
                }

                intent.putExtra(IntentUtil.EXTRA_FUNCTION_RESULT, mFunctionResult);
            }else{
                intent.putExtra(IntentUtil.EXTRA_ERROR, error);
            }

            boolean r = LocalBroadcastManager.getInstance(mService).sendBroadcast(intent);
            _log.v("Broadcasting ACTION_FUNCTION_RESULT intent " + r);
        }

        @Override
        public void onFunctionError(Function function, String reason) {
            _log.v("onFunctionError");

            mService.stopSelf(mStartId);
        }

        @Override
        public void onFunctionResult(Function function, int result, String eventResult) {
            _log.v("onFunctionResult: " + function.getUri() + " " + function.doReceiveEvents()
                + " " + result + " " + eventResult);

            mFunctionResult = result;
            mEventResult = eventResult;

            broadcastIntent(function, null);
            mService.stopSelf(mStartId);
        }

        @Override
        public void onFunctionTimeout(Function function, int source) {
            _log.v("onFunctionTimeout");

            broadcastIntent(function, "timeout occurred");
            mService.stopSelf(mStartId);
        }
    }


    private static final TLog _log = TLog.get(RunFunctionService.class);
}
