package jp.ne.sakura.kkkon.unityandroidusbdebug;

//import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import jp.ne.sakura.kkkon.lib.DebugLog;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout( this );
        layout.setOrientation( LinearLayout.VERTICAL );

        layout.setLayoutParams( new LinearLayout.LayoutParams( LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT ));

        {
            Button button = new Button(this);
            button.setText("listen start");

            layout.addView(button);

            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DebugLog.d(TAG, "threadStart");
                    MulticastReceiver.threadStart();
                    MulticastTunnel.threadStart();
                }
            });

        }

        {
            Button button = new Button(this);
            button.setText("listen stop");

            layout.addView(button);

            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    DebugLog.d(TAG, "threadStop");
                    MulticastTunnel.threadStop();
                    MulticastReceiver.threadStop();
                }
            });

        }

        setContentView( layout );
    }
}
