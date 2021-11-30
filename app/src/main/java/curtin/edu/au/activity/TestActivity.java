/**
 * PURPOSE: download and present test content while handles the math test logic
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import curtin.edu.au.R;
import curtin.edu.au.database.TestDatabase;
import curtin.edu.au.fragment.AnswerFragment;
import curtin.edu.au.model.Test;
import curtin.edu.au.utility.DownloadTestUtils;
import curtin.edu.au.view.TestViewModel;

public class TestActivity extends AppCompatActivity {
    //Declare vars
    private Button returnBtn, nextQBtn, previousQBtn, previousAnsBtn, nextAnsBtn, finishBtn;
    private TextView studentName, totalScore, question, timeText;
    private ProgressBar countdownBar;
    private AnswerFragment fragA;
    private FragmentManager fm;

    private String selectedFName, selectedLName;
    private MyCountDownTimer timer;
    private TestDatabase testDb;
    private TestViewModel testVM;
    private int index;

    //Constants
    private static final int MAX_QUESTIONS = 10;
    private static final int MAX_OPTIONS = 10;

    //Return intent access to this activity.
    public static Intent getIntent(Context c, String fName, String lName)
    {
        Intent intent = new Intent(c, TestActivity.class);
        intent.putExtra("selected_student_first_name", fName);
        intent.putExtra("selected_student_last_name", lName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        //initialize the database
        testDb = new TestDatabase(TestActivity.this);

        //get the intent extra data
        selectedFName = getIntent().getStringExtra("selected_student_first_name");
        selectedLName = getIntent().getStringExtra("selected_student_last_name");

        //attach view to control variables
        returnBtn = (Button) findViewById(R.id.backBtn);
        nextQBtn = (Button) findViewById(R.id.nextQBtn);
        previousQBtn = (Button) findViewById(R.id.previousQBtn);
        nextAnsBtn = (Button) findViewById(R.id.nextAnsBtn);
        previousAnsBtn = (Button) findViewById(R.id.previousAnsBtn);
        finishBtn = (Button) findViewById(R.id.finishTestBtn);
        studentName = (TextView) findViewById(R.id.studentName);
        totalScore = (TextView) findViewById(R.id.score);
        question = (TextView) findViewById(R.id.question);
        timeText = (TextView) findViewById(R.id.timeText);
        countdownBar = (ProgressBar) findViewById(R.id.countdownBar);
        fm = getSupportFragmentManager();
        fragA = (AnswerFragment) fm.findFragmentById(R.id.fragment_answer);

        //create this activity's view model
        testVM = new ViewModelProvider(this).get(TestViewModel.class);
        //update the activity's UI when the user navigates to other questions
        testVM.getCurrQuestion().observe(this, integer -> {
            //update the UI only when a question has been downloaded successfully
            index = integer - 1;
            if(integer != -1)
            {
                updateUIQuestion();
            }
        });
        //get the date of the test
        if(testVM.getTestDate() == null)
        {
            Date currDate = Calendar.getInstance().getTime();
            Locale locale = new Locale("en", "AU");
            String pattern = "dd-M-yyyy hh:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern, locale);
            testVM.setTestDate(simpleDateFormat.format(currDate));
        }
        //if at least a question has been successfully downloaded, update score to UI
        if(testVM.getFinishDownload()[0])
        {
            updateScoreUI();
        }

        //set-up initial view
        studentName.setText(new StringBuilder().append(selectedFName).append(" ").append(selectedLName));

        //download all the test's questions when the test is started
        if(savedInstanceState == null)
        {
            new DownloadTest().execute();
        }

        //return to pre-test activity
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        //when next option button is clicked
        nextAnsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check if the current question's content has been successfully downloaded
                if(testVM.getFinishDownload()[index])
                {
                    testVM.getCurrViewAnsLayout()[index] += 1;
                    updateUIAns();
                }
            }
        });

        //when the previous option button is clicked
        previousAnsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for resuming index position of option to display to UI
                testVM.getCurrViewAnsLayout()[index] -= 1;
                updateUIAns();
            }
        });

        //continue to the next question
        nextQBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToNextQuestion(index);
            }
        });

        //roll back to the previous question
        previousQBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rollBackQuestion(index);
            }
        });

        //when the user wants to end the test
        finishBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishTest();
            }
        });
    }

    //generates each question's view
    private void updateUIQuestion()
    {
        //if the user haven't chosen an answer yet for the new question
        if(testVM.getUserChoices()[index] == null)
        {
            testVM.getCurrViewAnsLayout()[index] = 0;
        }

        //update navigation buttons
        updateQButton();
        //update question
        question.setText(testVM.getQuestions()[index]);
        //set currently selected answer to saved answer for the new question
        testVM.setCurrChoice(testVM.getUserChoices()[index]);
        //update answer options
        updateUIAns();
        //update time
        timeText.setText(new StringBuilder().append(testVM.getRemainingTime()[index]).append(" secs"));
        countdownBar.setMax(testVM.getTimeToSolve()[index]);
        if(timer != null)
        {
            timer.cancel();
        }
        timer = new MyCountDownTimer(testVM.getRemainingTime()[index] * 1000L, 1000); // in milliseconds
        timer.start();
    }

    //enable or disable buttons according to the current question
    private void updateQButton()
    {
        //re-enable all buttons initially
        nextQBtn.setVisibility(View.VISIBLE);
        nextQBtn.setClickable(true);
        previousQBtn.setVisibility(View.VISIBLE);
        previousQBtn.setClickable(true);
        //if current question is the last question, disable next question button
        if (index == MAX_QUESTIONS - 1)
        {
            nextQBtn.setVisibility(View.INVISIBLE);
            nextQBtn.setClickable(false);
        }
        //if current question is the first question, disable previous question button
        else if (index == 0)
        {
            previousQBtn.setVisibility(View.INVISIBLE);
            previousQBtn.setClickable(false);
        }
    }

    //save and update total score to UI
    private void updateScoreUI()
    {
        Integer ans = testVM.getCurrChoice();
        Integer savedAns = testVM.getUserChoices()[index];
        //check if there's an answer provided
        if(ans != null)
        {
            //if the current user's answer is different than previous selected answer then evaluate score
            if(!ans.equals(savedAns))
            {
                //answer corrects then mark will be awarded and deducted otherwise
                if(ans.equals(testVM.getCorrectAns()[index]))
                {
                    testVM.setTotalScore(testVM.getTotalScore() + 10);
                }
                else
                {
                    testVM.setTotalScore(testVM.getTotalScore() - 5);
                }
            }
        }

        //saved answer
        testVM.getUserChoices()[index] = ans;

        //set UI total score
        totalScore.setText(new StringBuilder("Total score: ").append(testVM.getTotalScore()));
    }

    //update each option's view
    private void updateUIAns()
    {
        //calculate the starting index position of the currently viewed first option
        int startIndex = 0;
        for(int i = 0; i < testVM.getCurrViewAnsLayout()[index]; i++)
        {
            startIndex += testVM.getOptionLayouts().get(index).get(i);
        }

        //create a new answer layout
        fragA = new AnswerFragment(testVM.getOptionLayouts().get(index).get(testVM.getCurrViewAnsLayout()[index]), startIndex);
        fm.beginTransaction().replace(R.id.fragment_answer, fragA).commit();

        //enable next or previous option's view accordingly
        checkNextAnsBtn();
        checkPrevAnsBtn();
    }

    //from the number of options available, create a sequence of option layout recursively
    private void decideLayout(List<Integer> optionLayout, int optionLeft)
    {
        if (optionLeft > 5)
        {
            optionLayout.add(4);
            decideLayout(optionLayout,optionLeft - 4);
        }
        else if (optionLeft == 5)
        {
            optionLayout.add(3);
            optionLayout.add(2);
        }
        else
        {
            optionLayout.add(optionLeft);
        }
    }

    //handles transition to next question
    private boolean moveToNextQuestion(int nextIndex)
    {
        boolean success = false;

        //loop to skip the next question(s) if its remaining time has run out
        do
        {
            nextIndex++;
            if(nextIndex != MAX_QUESTIONS && testVM.getFinishDownload()[nextIndex] && testVM.getRemainingTime()[nextIndex] != 0)
            {
                success = true;
                //early terminate loop when a suitable next question has been found
                break;
            }
        } while(nextIndex != MAX_QUESTIONS && testVM.getFinishDownload()[nextIndex] && testVM.getRemainingTime()[nextIndex] == 0);

        //if a suitable question which hasn't run out of time is found, update the UI
        if(success)
        {
            updateScoreUI();
            testVM.setCurrQuestion(nextIndex + 1);
        }

        return success;
    }

    //handles transition to previous question
    private boolean rollBackQuestion(int prevIndex)
    {
        boolean success = false;

        //loop to skip the previous question(s) if its remaining time has run out
        do
        {
            prevIndex--;
            if(prevIndex != -1 && testVM.getRemainingTime()[prevIndex] != 0)
            {
                success = true;
                //early terminate loop when a suitable next question has been found
                break;
            }
        } while(prevIndex != -1 && testVM.getRemainingTime()[prevIndex] == 0);

        //if a suitable question which hasn't run out of time is found, update the UI
        if(success)
        {
            updateScoreUI();
            testVM.setCurrQuestion(prevIndex + 1);
        }

        return success;
    }

    //check to enable or disable the next option view
    private void checkNextAnsBtn()
    {
        if(testVM.getCurrViewAnsLayout()[index] == testVM.getOptionLayouts().get(index).size() - 1)
        {
            nextAnsBtn.setVisibility(View.INVISIBLE);
            nextAnsBtn.setClickable(false);
        }
        else
        {
            nextAnsBtn.setVisibility(View.VISIBLE);
            nextAnsBtn.setClickable(true);
        }
    }

    //check to enable or disable the previous option view
    private void checkPrevAnsBtn()
    {
        if(testVM.getCurrViewAnsLayout()[index] == 0)
        {
            previousAnsBtn.setVisibility(View.INVISIBLE);
            previousAnsBtn.setClickable(false);
        }
        else
        {
            previousAnsBtn.setVisibility(View.VISIBLE);
            previousAnsBtn.setClickable(true);
        }
    }

    private void finishTest()
    {
        int timeSpent = 0;

        if(testVM.getFinishDownload()[MAX_QUESTIONS - 1])
        {
            for(int i = 0; i < MAX_QUESTIONS; i++)
            {
                timeSpent += testVM.getTimeToSolve()[i] - testVM.getRemainingTime()[i];
            }

            //evaluate last viewed question to the total score
            updateScoreUI();

            testDb.add(new Test(selectedFName + " " + selectedLName, testVM.getTotalScore(), testVM.getTestDate(), timeSpent));
            Toast.makeText(TestActivity.this, "The student has finished the test", Toast.LENGTH_SHORT).show();
            finish();
        }
        else
        {
            Toast.makeText(TestActivity.this, "Wait until all the test content are downloaded and try again!", Toast.LENGTH_SHORT).show();
        }
    }

    //asynchronously download test's questions on the background
    private class DownloadTest extends AsyncTask<Void, String, Void> {
        // Declare var
        HttpsURLConnection conn = null;

        @Override
        protected Void doInBackground(Void... voids)
        {
            for(int i = 0; i < MAX_QUESTIONS; i++)
            {
                String q = testRetrieval();
                if(q != null) {
                    publishProgress("Success", q, String.valueOf(i));
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            //if the first parameter returns an Error string, notify users
            if(!values[0].equals("Success"))
            {
                Toast.makeText(TestActivity.this, values[0], Toast.LENGTH_LONG).show();
            }
            //if not, check the content of values and store it in arrays of each question's element
            else
            {
                int tempIndex = Integer.parseInt(values[2]);

                //set-up the rest of the UI once a question is successfully downloaded
                //get each part of the test content from the Question Bank String
                try {
                    JSONObject jBase = new JSONObject(values[1]);
                    //question
                    testVM.getQuestions()[tempIndex] = "Question " + (tempIndex + 1) + ": " + jBase.getString("question");

                    //time to solve
                    testVM.getTimeToSolve()[tempIndex] = jBase.getInt("timetosolve");
                    testVM.getRemainingTime()[tempIndex] = testVM.getTimeToSolve()[tempIndex];

                    //correct answer
                    testVM.getCorrectAns()[tempIndex] = jBase.getInt("result");

                    //answer options
                    JSONArray jArray = jBase.getJSONArray("options");
                    //max of 10 options are presented to students
                    Integer[] optionArray = new Integer[Math.min(jArray.length(), MAX_OPTIONS)];
                    boolean containRes = false;
                    for (int i = 0; i < optionArray.length; i++)
                    {
                        optionArray[i] = jArray.optInt(i);
                        //if any input option matches with the result, set containRes variable to true
                        if(optionArray[i].equals(testVM.getCorrectAns()[tempIndex]))
                        {
                            containRes = true;
                        }
                    }
                    //if the provided options doesn't contain the correct result, replace a random option with the correct result
                    if(!containRes && optionArray.length != 0)
                    {
                        optionArray[(int)(Math.random() * (optionArray.length - 1))] = testVM.getCorrectAns()[tempIndex];
                    }
                    testVM.getOptions().add(optionArray);
                }
                catch (JSONException e)
                {
                    Toast.makeText(TestActivity.this, "Error in converting JSON object while downloading", Toast.LENGTH_SHORT).show();
                }

                //recursively create a layout sequence
                List<Integer> optionLayout = new ArrayList<>();
                decideLayout(optionLayout, testVM.getOptions().get(tempIndex).length);
                testVM.addOptionLayout(optionLayout);

                testVM.getFinishDownload()[tempIndex] = true;

                //set UI view for first question if the user just started the test
                if(tempIndex == 0)
                {
                    index = tempIndex;
                    updateScoreUI();
                    testVM.setCurrQuestion(1);
                }
            }
        }

        private String testRetrieval()
        {
            String questionBank = null;

            try
            {
                // create the URL of the web link and establish a connection with the web server(using the loopback address)
                String urlString = Uri.parse("https://10.0.2.2:8000/random/question/").buildUpon()
                        .appendQueryParameter("method", "thedata.getit")
                        .appendQueryParameter("api_key", "01189998819991197253")
                        .appendQueryParameter("format", "json")
                        .build().toString();
                URL url = new URL(urlString);
                conn = (HttpsURLConnection) url.openConnection();
                conn.setConnectTimeout(1000);
                conn.setReadTimeout(2000);

                // add self-signed cert for the server to trust and allow the connection
                DownloadTestUtils.addCertificate(TestActivity.this, conn);

                // check if the connection is established successfully
                if(conn == null)
                {
                    publishProgress("Check internet");
                }
                if(conn.getResponseCode() != HttpsURLConnection.HTTP_OK)
                {
                    publishProgress("Problem with connection, responding with code " + conn.getResponseCode());
                }
                else
                {
                    // start downloading data
                    questionBank = downloadToString(conn);
                }
            }
            catch(MalformedURLException e)
            {
                publishProgress("The provided URL path is invalid!");
            }
            catch(SocketTimeoutException e)
            {
                publishProgress("Connection timeout! Question Bank's destination can't be reached. Can't retrieve question!");
                //close the test when the remote server destination can't be reached
                finish();
            }
            catch (IOException e)
            {
                publishProgress("Error in downloading the data");
            }
            catch (GeneralSecurityException e)
            {
                publishProgress("An error occurs while verifying the identity of the client's certificate.");
            }
            finally
            {
                if(conn != null)
                {
                    conn.disconnect();
                }
            }

            return questionBank;
        }

        // download the question bank content as String
        private String downloadToString(HttpURLConnection conn) throws IOException
        {
            InputStream is = conn.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int bytesRead = is.read(buffer);
            while (bytesRead > 0) {
                baos.write(buffer, 0, bytesRead);
                //read the next byte chunk from buffer
                bytesRead = is.read(buffer);
            }
            baos.close();

            return baos.toString();
        }
    }

    //custom countdown timer class to shows the remaining time of a question
    private class MyCountDownTimer extends CountDownTimer
    {

        public MyCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

            int progress = (int) (millisUntilFinished/1000);

            //update view
            timeText.setText(new StringBuilder().append(progress).append(" secs"));
            testVM.getRemainingTime()[index] = progress;

            countdownBar.setProgress(countdownBar.getMax()-progress);
        }

        @Override
        public void onFinish()
        {
            Toast.makeText(TestActivity.this, "Run out of time!Next Question", Toast.LENGTH_SHORT).show();

            //move to the next or previous question if remaining time still available
            if(!moveToNextQuestion(index))
            {
                if(!rollBackQuestion(index))
                {
                    finishTest();
                }
            }
        }
    }
}