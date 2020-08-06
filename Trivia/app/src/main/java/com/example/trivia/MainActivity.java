package com.example.trivia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trivia.controller.NewController;
import com.example.trivia.data.AnswerListAsyncResponse;
import com.example.trivia.data.QuestionBank;
import com.example.trivia.model.Question;
import com.example.trivia.util.Prefs;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String MESSAGE_ID = "HighestScore_pref";
    private TextView questionTextView, counterTextView, currentScoreTextView, highestScoreTextView;
    private ImageButton backButton, nextButton ;
    private Button trueButton, falseButton;
    public int questionIndexCount = 0 , currentScore = 0, highestScore;
    public boolean[] visitedQuestion;
    public boolean correct;
    public Prefs pref;
    private List<Question> questionArrayList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = new Prefs(MainActivity.this);

        MainActivity.this.setTitle("Welcome To");


        questionTextView = findViewById(R.id.question_textView);
        counterTextView = findViewById(R.id.count_text);
        backButton = findViewById(R.id.back_button);
        nextButton = findViewById(R.id.next_button);
        trueButton = findViewById(R.id.true_button);
        falseButton = findViewById(R.id.false_button);
        currentScoreTextView = findViewById(R.id.currrent_score);
        highestScoreTextView = findViewById(R.id.highest_score);

        backButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        trueButton.setOnClickListener(this);
        falseButton.setOnClickListener(this);


        highestScore = pref.getHighScore();
        questionArrayList = new QuestionBank().getQuestions(new AnswerListAsyncResponse() {
            @Override
            public void processFinished(ArrayList<Question> questionArrayLists) {
                visitedQuestion = new boolean[questionArrayList.size()+1];
                updateDisplay();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_button:
                if(questionIndexCount>0)
                    questionIndexCount--;
                break;
            case R.id.next_button:
                if(questionIndexCount < questionArrayList.size()-1)
                    questionIndexCount++;
                break;
            case R.id.true_button:
                checkAnswer(true);
                break;
            case R.id.false_button:
                checkAnswer(false);
                break;
        }
        updateDisplay();
        Log.d("index", "onClick: " + questionIndexCount);
    }

    private void checkAnswer(boolean chose) {
        boolean ans = questionArrayList.get(questionIndexCount).isAnswer();
        if(chose == ans) {

            correct = true;
            Log.d("index", "before: " + questionIndexCount);
            shakeAnimation();
            Log.d("index", "after: " + questionIndexCount);
            if(visitedQuestion[questionIndexCount] == false) {
                currentScore++;
            }
            //Log.d("score", "checkAnswer: "+currentScore+" "+highestScore);
            highestScore = pref.getHighScore();
            if(currentScore > highestScore) {
                highestScore = currentScore;
            }
            pref.saveHighScore(highestScore);
            //Log.d("score", "checkAnswer: "+currentScore+" "+highestScore);
            visitedQuestion[questionIndexCount]=true;
        }
        else {
            correct = false;
            shakeAnimation();
            visitedQuestion[questionIndexCount]=true;
            shakePhone();//For 500 millisecond
        }
    }

    private void shakePhone() {
        if (Build.VERSION.SDK_INT >= 26) {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            ((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(500);
        }
    }

    private void updateDisplay(){
        counterTextView.setText(questionIndexCount+1+" of "+questionArrayList.size());
        questionTextView.setText(questionArrayList.get(questionIndexCount).getQuestion());
        currentScoreTextView.setText(MessageFormat.format("Score : {0} ", currentScore));
        highestScoreTextView.setText(MessageFormat.format("Highest Score : {0}", highestScore));
    }

    public void shakeAnimation(){
        Animation shake = AnimationUtils.loadAnimation(MainActivity.this,
                R.anim.shake_animation);

        final CardView cardView = findViewById(R.id.cardView);
        cardView.setAnimation(shake);


        shake.setAnimationListener(new Animation.AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                if(correct == true)
                    cardView.setCardBackgroundColor(Color.GREEN);
                else
                    cardView.setCardBackgroundColor(Color.RED);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                cardView.setCardBackgroundColor(Color.WHITE);
                if(correct == true){
                    if(questionIndexCount < questionArrayList.size()-1)
                        questionIndexCount++;
                    updateDisplay();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }
}
