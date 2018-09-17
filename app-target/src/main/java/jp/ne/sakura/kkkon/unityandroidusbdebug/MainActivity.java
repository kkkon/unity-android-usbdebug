package jp.ne.sakura.kkkon.unityandroidusbdebug;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import jp.ne.sakura.kkkon.lib.DebugLog;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    LinearLayout    mLayout = null;
    TextView        mTextView = null;
    Button          mButtonStart = null;
    Button          mButtonStop = null;
    ThreadMonitor   mThreadMonitor = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout( this );
        layout.setOrientation( LinearLayout.VERTICAL );

        layout.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT ));

        {
            TextView text = new TextView( this );
            text.setTextSize( 24 );
            text.setText( "" );

            mTextView = text;
            layout.addView( text );
        }

        {
            Button button = new Button(this);
            button.setText("listen start");

            mButtonStart = button;
            layout.addView(button);

            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DebugLog.d(TAG, "threadStart");
                    MulticastReceiver.threadStart();
                    MulticastTunnel.threadStart();
                    threadMonitorStop();
                    threadMonitorStart();
                    mTextView.setText("");
                    mButtonStop.setEnabled(true);
                }
            });

        }

        {
            Button button = new Button(this);
            button.setText("listen stop");
            button.setEnabled(false);

            mButtonStop = button;
            layout.addView(button);

            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DebugLog.d(TAG, "threadStop");
                    MulticastTunnel.threadStop();
                    MulticastReceiver.threadStop();
                    threadMonitorStop();
                    mButtonStart.setEnabled(true);
                    mButtonStop.setEnabled(false);
                }
            });

        }

        mLayout = layout;
        setContentView( layout );
    }

    @Override
    protected void onDestroy()
    {
        MulticastTunnel.threadStop();
        MulticastReceiver.threadStop();

        if ( null == mThreadMonitor )
        {
            mThreadMonitor.interrupt();
            try
            {
                mThreadMonitor.join();
            }
            catch ( InterruptedException e )
            {

            }
            mThreadMonitor = null;
        }

        if ( null != mButtonStop )
        {
            mButtonStop.setOnClickListener( null );
            mButtonStop = null;
        }
        if ( null != mButtonStart )
        {
            mButtonStart.setOnClickListener( null );
            mButtonStart = null;
        }

        {
            final ViewGroup viewParent = (ViewGroup) mLayout.getParent();
            viewParent.removeAllViews();
        }

        super.onDestroy();
    }

    protected void threadMonitorStop()
    {
        if ( null != mThreadMonitor )
        {
            mThreadMonitor.interrupt();
            try
            {
                mThreadMonitor.join();
            }
            catch ( InterruptedException e )
            {

            }
            mThreadMonitor = null;
        }
    }

    protected void threadMonitorStart()
    {
        if ( null == mThreadMonitor )
        {
            mThreadMonitor = new ThreadMonitor();
            mThreadMonitor.start();
        }
    }

    private class ThreadMonitor extends Thread
    {

        @Override
        public void run()
        {
            for ( ; ; )
            {
                try
                {
                    Thread.sleep(30 * 1000);
                    if ( MulticastReceiver.isThreadRecvSocketError() )
                    {
                        MainActivity.this.runOnUiThread( new Runnable() {
                                @Override
                                public void run()
                                {
                                    MainActivity.this.mTextView.setText("Occured Socket Error!!");
                                }
                        });

                        break;
                    }
                }
                catch ( InterruptedException e )
                {
                    break;
                }
            }
        }
    }

}
