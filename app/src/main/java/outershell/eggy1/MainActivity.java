package outershell.eggy1;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {

    //Log
    private static final String TAG = "LogMainActivity";
    //sharedPreferences
    public static final String EGGY_PREFS = "EggySharedPreferences";

    ImageView eggImage;
    TextView appNameText;
    TextView timeDisplay;
    TextView helpText;
    boolean timerHasStarted = false;
    String timeDisplayText = "";
    String helpTextString = "";
    String helpTextString2 = "";
    eggyCountdownTimer countDownTimer;
    long startTime = 10000; // for testing
    long interval = 100; // UI update frequency. 1000 doesn't show 00:00.
    int min, sec;
    Animation eggAnimation;
    SharedPreferences sharedPrefs;
    final Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI views
        eggImage = (ImageView) findViewById(R.id.eggImage);
        timeDisplay = (TextView) findViewById(R.id.timeDisplay);
        helpText = (TextView) findViewById(R.id.helpText);
        appNameText = (TextView) findViewById(R.id.appnameText);
        Typeface oatmealFont = Typeface.createFromAsset(getAssets(), "fonts/oatmeal.ttf");
        appNameText.setTypeface(oatmealFont);

        //load sharedPreferences; set time
        sharedPrefs = getSharedPreferences(EGGY_PREFS, MODE_PRIVATE);
        startTime = sharedPrefs.getLong("startTime", TimeUnit.MINUTES.toMillis(6));

        //initiate UI
        setDisplayText(startTime);
        setHelpText();

        //play/stop button
        eggImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!timerHasStarted) {
                    Log.d(TAG, "Timer has started");
                    eggImage.setImageResource(R.drawable.egg_300px);
                    timerHasStarted = true;
                    countDownTimer = new eggyCountdownTimer(startTime, interval);
                    countDownTimer.start();
                    //animate egg
                    eggAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.egg_anim);
                    eggImage.startAnimation(eggAnimation);
                } else {
                    eggImage.clearAnimation();
                    Log.d(TAG, "Timer has stopped");
                    timerHasStarted = false;
                    countDownTimer.cancel();
                    setDisplayText(startTime);
                }
            }
        });

        //set time
        timeDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment newFragment = new TimePickerFragment();
                newFragment.show(getSupportFragmentManager(), "timePicker");
            }
        });
    }

    public void setHelpText() {

        min = (int) TimeUnit.MILLISECONDS.toMinutes(startTime);
        sec = (int) (TimeUnit.MILLISECONDS.toSeconds(startTime) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(startTime)));

        helpTextString = "Set time: %1$s";
        helpTextString2 = String.format("%02d:%02d", min, sec);
        helpText.setText(String.format(helpTextString, helpTextString2));
    }

    public class eggyCountdownTimer extends CountDownTimer {

        public eggyCountdownTimer(long startTime, long interval) {
            super(startTime, interval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            Log.d(TAG, String.valueOf(millisUntilFinished) + " Min: " + min + " Sec:" + sec);
            setDisplayText(millisUntilFinished);
//          http://stackoverflow.com/questions/9723106/get-activity-instance
        }

        @Override
        public void onFinish() {

            eggImage.clearAnimation();
            setDisplayText(startTime);
            timerHasStarted = false;
            eggImage.setImageResource(R.drawable.egg_done_300px);
            Log.d(TAG, "Countdown finished");
            alertDone();
        }
    }

    public void alertDone() {
        //sound alarm
        Uri alarm = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), alarm);
        r.play();

        //custom alertDialog
        final Dialog doneDialog = new Dialog (context);
        doneDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        doneDialog.setContentView(R.layout.custom_dialog);
        Button dialogButton = (Button) doneDialog.findViewById(R.id.dialog_button);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                r.stop();
                doneDialog.dismiss();
            }
        });
        doneDialog.show();

        //widen the alertDialog
        //Grab the window of the dialog, and change the width
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = doneDialog.getWindow();
        lp.copyFrom(window.getAttributes());
        //This makes the dialog take up the full width
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    //help method for: onTick, onFinish, onStop
    public void setDisplayText(long milliseconds) {

        min = (int) TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        sec = (int) (TimeUnit.MILLISECONDS.toSeconds(milliseconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds)));

        timeDisplayText = String.format("%02d:%02d", min, sec);
        timeDisplay.setText(timeDisplayText);
    }

    public static class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            //return super.onCreateDialog(savedInstanceState);
            return new TimePickerDialog(getActivity(), 3, this, 0,0, true);
        }

        @Override
        public void onTimeSet(TimePicker view, int minutes, int secs) {

            minutes = (int) TimeUnit.MINUTES.toMillis(minutes);
            secs = (int) TimeUnit.SECONDS.toMillis(secs);
            ((MainActivity) getActivity()).setDisplayText(minutes+secs);
            ((MainActivity) getActivity()).startTime = minutes+secs;
            ((MainActivity) getActivity()).setHelpText();
        }
    }

    //save set time
    @Override
    protected void onStop() {
        super.onStop();
        sharedPrefs = getSharedPreferences(EGGY_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putLong("startTime", startTime);
        editor.apply();
    }
}
