package pmf.novak101.movietitlequizhtmlscraping;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class EndGameScreen extends AppCompatActivity {

    public void restartGame(View view){
        EndGameScreen.this.finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_end_game_screen);
        TextView score = findViewById(R.id.textView);
        Button button = findViewById(R.id.button2);

        MediaPlayer.create(getApplicationContext(), R.raw.congrats).start();

        score.setText("Congrats!");
        String reaction ="Yeyy";
        Bundle extras = getIntent().getExtras();
        int value = -1;
        if (extras != null) {
             value = extras.getInt("score");
        }
        if(value < 5)
            reaction ="Meh..";
        else
            reaction ="Amazing!";
        if(value == 10)
            reaction ="WOW!";

        score.setText(reaction+" You Scored: "+value+"/10");
    }
}