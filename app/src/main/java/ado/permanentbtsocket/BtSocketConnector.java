package ado.permanentbtsocket;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * Created by Ado on 12/07/15.
 *
 * - connect to well-known BT socket.
 * - after 10 seconds, disconnects from it
 * - wait 15 minutes and connect to it again, and so on
 *
 * Assumptions:
 * 1. devices are already paired
 * 2. Bt is on for both devices
 * 3. they already know the service UUID
 * 4. they both support secure socket (BT2.1+)
 */
public class BtSocketConnector {

    private static final String TAG = "BtSocketConnector";

    private BluetoothAdapter mAdapter;
    private BluetoothDevice mRemoteDevice;

    private BluetoothSocket mClientSocket;

    private final ScheduledExecutorService mScheduler;
    private ScheduledFuture<?> mScheduledAction;
    private static final int INTERVAL_IN_MINS = 1;
    private static final long WAIT_TIME_AFTER_CONN_IN_MSEC = 10000;

    private boolean mStarted = false;

    private ConnectorListener mListener;

    private Handler mUIHandler = new Handler(Looper.getMainLooper());

    BtSocketConnector(final ConnectorListener listener) {
        mListener = listener;
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!BluetoothAdapter.checkBluetoothAddress(BtConstants.REMOTE_SERVER_BT_ADDRESS)) {
            throw new IllegalArgumentException();
        }
        mRemoteDevice = mAdapter.getRemoteDevice(BtConstants.REMOTE_SERVER_BT_ADDRESS);
        mScheduler = Executors.newSingleThreadScheduledExecutor();
    }

    public void start() {
        if(!mStarted) {
            mStarted = true;
            mScheduledAction = mScheduler.scheduleAtFixedRate(new ConnectExecutor(), 0, INTERVAL_IN_MINS, TimeUnit.MINUTES);
        }
    }

    public void stop() {
        if(mStarted) {
            mScheduledAction.cancel(true);
            if(mClientSocket != null) {
                if(mClientSocket.isConnected()) {
                    try {
                        mClientSocket.close();
                    } catch (IOException e) {
                        Log.e(TAG, "stop() error closing the client socket");
                    }
                }
                mClientSocket = null;
            }
            mStarted = false;
        }

    }

    public boolean isStarted() {
        return mStarted;
    }

    private class ConnectExecutor implements Runnable {

        @Override
        public void run() {
            //create one if null
            if(mClientSocket == null) {
                try {
                    mClientSocket = mRemoteDevice.createRfcommSocketToServiceRecord(BtConstants.REMOTE_SERVER_UUID);
                } catch(IOException e) {
                    throw new IllegalStateException("can't create a client socket. BT down?");
                }
            }

            //connect
            try {
                Log.d(TAG, "ConnectExecutor.run(), trying to connect...");
                mClientSocket.connect();
                Log.d(TAG, "ConnectExecutor.run(), connected!");
                mUIHandler.post(new Runnable() {
                    public void run() {
                        mListener.onConnectionSuccessful();
                    }
                });
                //wait time (10 secs)
                try {
                    Thread.sleep(WAIT_TIME_AFTER_CONN_IN_MSEC);
                } catch (InterruptedException e) {}
            } catch (IOException e) {
                Log.e(TAG, "ConnectExecutor.run(), connection error, IOException");
                Log.e(TAG, "exception:", e);
            } finally {
                //closing the client socket
                try {
                    if(!mClientSocket.isConnected()) {
                        mUIHandler.post(new Runnable() {
                            public void run() {
                                mListener.onConnectionError();
                            }
                        });
                    }
                    mClientSocket.close();
                    Log.d(TAG, "ConnectExecutor.run(), closed socket");
                } catch (IOException e) {
                    Log.e(TAG, "ConnectExecutor.run() error closing the client socket");
                } finally {
                    mClientSocket = null;
                }
            }
        }
    }
}
