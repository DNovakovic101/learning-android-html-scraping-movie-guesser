package pmf.novak101.movietitlequizhtmlscraping;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    public class Movie {
        public String imgSrc;
        public String title;

        public Movie(String imgSrc, String title){
            this.imgSrc = imgSrc;
            this.title = title;
        }
    }

    ExecutorService executor = Executors.newFixedThreadPool(4);
    Handler handler = new Handler(Looper.getMainLooper());

    MediaPlayer mediaPlayerBackground;
    MediaPlayer clickRight;
    MediaPlayer clickWrong;
    ValueAnimator colorAnimationRight;
    ValueAnimator colorAnimationWrong;

    ArrayList<String> allTitles = new ArrayList<String>();
    ArrayList<Movie> movies = new ArrayList<Movie>();

    ArrayList<Movie> currentGameMovies = new ArrayList<Movie>();
    final int GAME_SIZE= 10;
    int currentRound = 0;

    String currentRightAnswer;
    ArrayList<String> currentAnswers = new ArrayList<String>();
    int numberOfRightAnswers = 0;

    Button option1;
    Button option2;
    Button option3;
    Button option4;
    ConstraintLayout layoutMain;
    ImageView movieImage;

    public void pickOption(View view){
        if(view.getTag() == currentRightAnswer) {
            Toast.makeText(MainActivity.this, "CORRECT!", Toast.LENGTH_SHORT).show();
            clickRight.seekTo(0);
            clickRight.start();
            colorAnimationRight.start();
            numberOfRightAnswers++;
        }
        else {
            Toast.makeText(MainActivity.this, "WRONG!", Toast.LENGTH_SHORT).show();
            clickWrong.seekTo(0);
            clickWrong.start();
            colorAnimationWrong.start();
        }
        currentRound++;
        setCurrentQuestion();
    }

    InputStream in = null;
    HttpURLConnection conn = null;
    // setImageBitmap with null == error ?
    Bitmap bitmap = null;

    public void setCurrentQuestion(){
        if(currentRound < GAME_SIZE-1) {
            currentRightAnswer = "";
            currentAnswers = new ArrayList<String>();
            executor.execute(() -> {
                try {
                    URL url = new URL(currentGameMovies.get(0).imgSrc);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    in = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(in);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                }

                handler.post(() -> {
                    movieImage.setImageBitmap(bitmap);
                    currentRightAnswer = currentGameMovies.get(0).title;
                    currentGameMovies.remove(0);
                    populateWrongButtonAnswers();
                });
            });
        }else{
            Intent intent = new Intent(getApplicationContext(), EndGameScreen.class);
            intent.putExtra("score", numberOfRightAnswers);
            startActivity(intent);
            numberOfRightAnswers = 0;
            currentRound = 0;
            populateGame();
            setCurrentQuestion();
        }
    }

    public void populateGame(){
        Collections.shuffle(movies);
        for(int i = 1; i < GAME_SIZE; i++){
            currentGameMovies.add(movies.remove(i));
        }
    }

    public void populateWrongButtonAnswers(){
        currentAnswers.add(currentRightAnswer);
        Collections.shuffle(allTitles);
        Collections.shuffle(allTitles);
        for(int i = 0 ; i < 3 ; i++){
           String temp = allTitles.get( (ThreadLocalRandom.current().nextInt(250)+1) );
           while(temp == currentRightAnswer)
               temp = allTitles.get( (ThreadLocalRandom.current().nextInt(250)+1) );
           currentAnswers.add(temp);
        }
        Collections.shuffle(currentAnswers);

        setTextAndTagForButton(option1, 0);
        setTextAndTagForButton(option2, 1);
        setTextAndTagForButton(option3, 2);
        setTextAndTagForButton(option4, 3);

    }

    public void setTextAndTagForButton(Button btn, int position){
        btn.setText(currentAnswers.get(position));
        btn.setTag(currentAnswers.get(position));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        option1 = (Button) findViewById(R.id.buttonOption1);
        option2 = (Button) findViewById(R.id.buttonOption2);
        option3 = (Button) findViewById(R.id.buttonOption3);
        option4 = (Button) findViewById(R.id.buttonOption4);

        movieImage = (ImageView) findViewById(R.id.imageViewMovie);
        layoutMain = (ConstraintLayout) findViewById(R.id.layoutMain);
        executor.execute(new Runnable() {
            @Override
            public void run() {

                Document document = null;
                try {
                    document = Jsoup.connect("https://www.imdb.com/chart/top/").get();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Elements question =  document.select("tr td > a img");
                for(Element ques:question){
                    String imgSrc = ques.attr("src");
                    String title = ques.attr("alt");
                    Movie temp = new Movie(imgSrc, title);
                    movies.add(temp);
                    allTitles.add(title);
                };

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Collections.shuffle(movies);
                        populateGame();
                        setCurrentQuestion();
                    }
                });
            }
        });

        mediaPlayerBackground = MediaPlayer.create(getApplicationContext(), R.raw.background);
        mediaPlayerBackground.setLooping(true);
        mediaPlayerBackground.start();

        clickRight = MediaPlayer.create(getApplicationContext(), R.raw.right);
        clickWrong = MediaPlayer.create(getApplicationContext(), R.raw.wrong);


        int colorFrom = ContextCompat.getColor(this, R.color.white);
        int colorToRight = ContextCompat.getColor(this, R.color.right);
        int colorToWrong = ContextCompat.getColor(this, R.color.wrong);

        colorAnimationRight = ValueAnimator.ofArgb(colorFrom, colorToRight,colorFrom);
        colorAnimationRight.setDuration(250); // milliseconds
        colorAnimationRight.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                layoutMain.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });

        colorAnimationWrong = ValueAnimator.ofArgb(colorFrom, colorToWrong,colorFrom);
        colorAnimationWrong.setDuration(250); // milliseconds
        colorAnimationWrong.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                layoutMain.setBackgroundColor((int) animator.getAnimatedValue());
            }

        });

    }
}