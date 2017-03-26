package com.topandnewapps.newyearzipperlock.service;

import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.StrictMode;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.topandnewapps.newyearzipperlock.Lockscreen;
import com.topandnewapps.newyearzipperlock.LockscreenActivity;
import com.topandnewapps.newyearzipperlock.LockscreenUtil;
import com.topandnewapps.newyearzipperlock.activity_zipper;


/**
 * Created by mugku on 15. 5. 20..
 */
public class LockscreenService extends Service {
    private final String TAG = "LockscreenService";
    //    public static final String LOCKSCREENSERVICE_FIRST_START = "LOCKSCREENSERVICE_FIRST_START";
    private int mServiceStartId = 0;
    private Context mContext = null;


    private BroadcastReceiver mLockscreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (null != context) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    SharedPreferences sharedPreferences = getSharedPreferences("myapp", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("changeservice",1);
                    editor.commit();
                    Intent startLockscreenIntent = new Intent(mContext, LockscreenActivity.class);
                    startLockscreenIntent.putExtra("intenttype","screenoff");
                    startLockscreenIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(startLockscreenIntent);
                    TelephonyManager tManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    boolean isPhoneIdle = tManager.getCallState() == TelephonyManager.CALL_STATE_IDLE;
                    if (isPhoneIdle) {
                        startLockscreenActivity();
                    }
                }
            }
        }
    };

    private void stateRecever(boolean isStartRecever) {
        if (isStartRecever) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mLockscreenReceiver, filter);
        } else {
            if (null != mLockscreenReceiver) {
                unregisterReceiver(mLockscreenReceiver);
            }
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        System.out.println("onTaskRemoved called");
        super.onTaskRemoved(rootIntent);
        Intent intent=new Intent(LockscreenService.this,LockscreenService.class);
        startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mServiceStartId = startId;
        stateRecever(true);
        Intent bundleIntet = intent;
        if (null != bundleIntet) {

        } else {
            Log.d(TAG, TAG + " onStartCommand intent NOT existed");
        }
        setLockGuard();
        return LockscreenService.START_STICKY;
    }


    private void setLockGuard() {
        initKeyguardService();
        if (!LockscreenUtil.getInstance(mContext).isStandardKeyguardState()) {
            setStandardKeyguardState(false);
        } else {
            setStandardKeyguardState(true);
        }
    }

    private KeyguardManager mKeyManager = null;
    private KeyguardManager.KeyguardLock mKeyLock = null;

    private void initKeyguardService() {
        if (null != mKeyManager) {
            mKeyManager = null;
        }
        mKeyManager =(KeyguardManager)getSystemService(mContext.KEYGUARD_SERVICE);
        if (null != mKeyManager) {
            if (null != mKeyLock) {
                mKeyLock = null;
            }
            mKeyLock = mKeyManager.newKeyguardLock(mContext.KEYGUARD_SERVICE);
        }
    }

    private void setStandardKeyguardState(boolean isStart) {
        if (isStart) {
            if(null != mKeyLock){
                mKeyLock.reenableKeyguard();
            }
        }
        else {

            if(null != mKeyManager){
                mKeyLock.disableKeyguard();
            }
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        stateRecever(false);
        setStandardKeyguardState(true);
    }

    private void startLockscreenActivity() {
        Intent startLockscreenActIntent = new Intent(mContext, LockscreenActivity.class);
        startLockscreenActIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startLockscreenActIntent);
    }


}
