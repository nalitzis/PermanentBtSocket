package ado.permanentbtsocket;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;


public class ClientUI extends ActionBarActivity implements ConnectorListener {

    private Button mConnectButton;

    private BtSocketConnector mSocketConnector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_ui);
        mConnectButton = (Button) findViewById(R.id.connect);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mConnectButton.setText(R.string.stop);
                mSocketConnector.start();
            }
        });

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

    }

    @Override
    public void onConnectionError() {

    }
}
