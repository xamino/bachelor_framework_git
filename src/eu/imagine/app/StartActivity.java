package eu.imagine.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import eu.imagine.R;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 10/11/13
 * Time: 1:35 PM
 */
public class StartActivity extends Activity implements View.OnClickListener {

    private Button start;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                Intent in = new Intent();
                in.setClass(getApplicationContext(), MyActivity.class);
                startActivity(in);
        }
    }
}