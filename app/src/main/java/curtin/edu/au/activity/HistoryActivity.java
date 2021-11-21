/**
 * PURPOSE: view the test history of any registered student
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.List;

import curtin.edu.au.R;
import curtin.edu.au.database.TestDatabase;
import curtin.edu.au.fragment.HistoryFragment;
import curtin.edu.au.fragment.StudentSelectorFragment;
import curtin.edu.au.model.Student;
import curtin.edu.au.model.Test;
import curtin.edu.au.view.HistoryViewModel;

public class HistoryActivity extends AppCompatActivity
{
    //Declare vars
    private Button returnBtn, searchBtn, findBtn, sortBtn, sendBtn;
    private EditText inputSearchStudent;
    private final FragmentManager fm = getSupportFragmentManager();
    private StudentSelectorFragment fragS;
    private HistoryFragment fragH;
    private LinearLayout testLayout;

    private HistoryViewModel historyVM;
    private String selectedStudentName;
    private boolean highToLow = true;

    //Return intent access to this activity.
    public static Intent getIntent(Context c)
    {
        return new Intent(c, HistoryActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        //attach view to control variables
        returnBtn = (Button) findViewById(R.id.backBtn);
        searchBtn = (Button) findViewById(R.id.searchStudent);
        findBtn = (Button) findViewById(R.id.findBtn);
        sortBtn = (Button) findViewById(R.id.sortBtn);
        sendBtn = (Button) findViewById(R.id.sendBtn);
        inputSearchStudent = (EditText) findViewById(R.id.inputSearchStudent);
        fragS = (StudentSelectorFragment) fm.findFragmentById(R.id.studentSelector);
        fragH = (HistoryFragment) fm.findFragmentById(R.id.historyView);
        testLayout = (LinearLayout) findViewById(R.id.testLayout);

        //create the activity's view model
        historyVM = new ViewModelProvider(this).get(HistoryViewModel.class);
        //observe the selected student position in the list and update UI accordingly
        historyVM.getSelectedStudentPos().observe(this, integer -> {
            testLayout.setVisibility(View.GONE);
        });

        //set-up initial view
        testLayout.setVisibility(View.GONE);

        //link the initial student selector fragment to this activity.
        if(fragS == null)
        {
            fragS = new StudentSelectorFragment(null, false);
            fm.beginTransaction().add(R.id.studentSelector, fragS).commit();
        }

        //return to the main activity
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //shows the list of students matched with searched name
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //replace by the fragment selector view with filtered student list.
                fragS = new StudentSelectorFragment(inputSearchStudent.getText().toString(), false);
                fm.beginTransaction().replace(R.id.studentSelector, fragS).commit();

                //close the previous student's test history
                testLayout.setVisibility(View.GONE);
            }
        });

        //show the test score history of the selected student
        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fragS.getSelectedStudent() == null)
                {
                    Toast.makeText(HistoryActivity.this, "User hasn't selected a student profile to view his record!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //update with the selected student's tests
                    Student student = fragS.getSelectedStudent();
                    selectedStudentName = student.getFirstName() + " " + student.getLastName();
                    fragH = new HistoryFragment(selectedStudentName, false, false);
                    fm.beginTransaction().replace(R.id.historyView, fragH).commit();
                    testLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //sort the list of test records from highest to lowest and vice versa
        sortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //replace a newer history fragment with a sorted list
                fragH = new HistoryFragment(selectedStudentName, true, highToLow);
                fm.beginTransaction().replace(R.id.historyView, fragH).commit();
                highToLow = !highToLow;
            }
        });

        //send the entire list of selected student's test records to his email
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Student student = fragS.getSelectedStudent();
                List<Test> selectedTestList = fragH.getTestList();
                if(student != null && selectedTestList.size() != 0)
                {
                    selectedStudentName = student.getFirstName() + " " + student.getLastName();
                    StringBuilder emailContent = new StringBuilder("The list below presents all of your test records in the form of Date/Test Duration/Score:");

                    //get the content of email
                    for(Test test: selectedTestList)
                    {
                        emailContent.append("\n~")
                                .append(test.getDate())
                                .append("/")
                                .append(test.getTime())
                                .append("/")
                                .append(test.getScore())
                                .append("~");
                    }

                    //send email
                    boolean sent = false;
                    for(String email: student.getEmailList())
                    {
                        //validate the email address to correct form
                        if(!TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches())
                        {
                            sent = true;
                            sendEmail(email, emailContent.toString());
                        }
                    }

                    if(!sent)
                    {
                        Toast.makeText(HistoryActivity.this, "No valid emails retrieved from the student's detail", Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    Toast.makeText(HistoryActivity.this, "Can't send test record to invalid student or student who hasn't taken any test", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //send student's test record through email to that student
    private void sendEmail(String to, String body)
    {
        Intent intent = new Intent(Intent.ACTION_SEND);
        //set one type of application
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{to});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Student's test records");
        intent.putExtra(Intent.EXTRA_TEXT, body);

        try {
            startActivity(Intent.createChooser(intent, "Send email..."));
            Toast.makeText(HistoryActivity.this, "Successfully send the student's test record through email", Toast.LENGTH_LONG).show();
        }
        catch(ActivityNotFoundException ex)
        {
            Toast.makeText(HistoryActivity.this, "No email clients installed on the device", Toast.LENGTH_LONG).show();
        }
    }
}