package ado.permanentbtsocket;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


public class ClientUI extends Activity implements ConnectorListener {

    private static final String TAG = "ClientUI";

    private Button mConnectButton;

    private BtSocketConnector mSocketConnector;

    private int mCntOk = 0;
    private int mCntError = 0;

    private TextView mCntOkText;
    private TextView mCntErrorText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_ui);
        mConnectButton = (Button) findViewById(R.id.connect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean wasStarted = mSocketConnector.isStarted();
                if(wasStarted) {
                    mSocketConnector.stop();
                    mConnectButton.setText(R.string.start);
                } else {
                    mSocketConnector.start();
                    mConnectButton.setText(R.string.stop);
                }
            }
        });
        mCntOkText = (TextView)findViewById(R.id.textViewConnOk);
        mCntErrorText = (TextView)findViewById(R.id.textViewConnError);

        mSocketConnector = new BtSocketConnector(this);
    }


    @Override
    public void onDestroy() {
        if(mSocketConnector.isStarted()) {
            mSocketConnector.stop();
        }
        mSocketConnector = null;
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_client_ui, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionSuccessful() {
        mCntOk++;
        mCntOkText.setText(""+mCntOk);
    }

    @Override
    public void onConnectionError() {
        Log.d(TAG, "onConnectionError()");
        mCntError++;
        mCntErrorText.setText(""+mCntError);
    }
}
