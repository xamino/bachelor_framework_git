package eu.imagine.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import eu.imagine.R;

/**
 * Created with IntelliJ IDEA.
 * User: tamino
 * Date: 10/11/13
 * Time: 1:35 PM
 */
public class StartActivity extends Activity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    private Button b_start;
    private Switch b_debugLog, b_debugFrameLog, b_dupMarkers;

    private Bundle options;
    private final String TAG = "StartActivity";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);

        options = new Bundle();

        b_start = (Button) findViewById(R.id.start);
        b_start.setOnClickListener(this);
        b_debugLog = (Switch) findViewById(R.id.debugLog);
        b_debugLog.setOnCheckedChangeListener(this);
        b_debugFrameLog = (Switch) findViewById(R.id.frameDebug);
        b_debugFrameLog.setOnCheckedChangeListener(this);
        b_debugFrameLog.setEnabled(false);
        b_dupMarkers = (Switch) findViewById(R.id.dupMarkers);
        b_dupMarkers.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                Intent in = new Intent();
                in.setClass(getApplicationContext(), ImagineActivity.class);
                in.putExtras(options);
                startActivity(in);
                break;
            default:
                Log.e(TAG, "Unknown button clicked!");
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.debugLog:
                options.putBoolean("debugLog", isChecked);
                b_debugFrameLog.setEnabled(isChecked);
                break;
            case R.id.frameDebug:
                options.putBoolean("frameDebug", isChecked);
                break;
            case R.id.dupMarkers:
                options.putBoolean("dupMarkers", isChecked);
            default:
                Log.e(TAG, "Unknown compoundbutton clicked!");
        }
    }
}