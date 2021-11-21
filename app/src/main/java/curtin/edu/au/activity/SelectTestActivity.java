/**
 * PURPOSE: pre-test panel to select a student entry to take the following test
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import curtin.edu.au.R;
import curtin.edu.au.fragment.StudentSelectorFragment;

public class SelectTestActivity extends AppCompatActivity {
    //Declare vars
    private Button testBtn, returnBtn, searchBtn;
    private EditText inputSearchStudent;
    private final FragmentManager fm = getSupportFragmentManager();
    private StudentSelectorFragment fragS;

    //Return intent access to this activity.
    public static Intent getIntent(Context c)
    {
        return new Intent(c, SelectTestActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_test);

        //attach view to control variables
        testBtn = (Button) findViewById(R.id.testBtn);
        returnBtn = (Button) findViewById(R.id.backBtn);
        searchBtn = (Button) findViewById(R.id.searchStudent);
        inputSearchStudent = (EditText) findViewById(R.id.inputSearchStudent);
        fragS = (StudentSelectorFragment) fm.findFragmentById(R.id.studentSelector);

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
            }
        });

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(fragS.getSelectedStudent() == null)
                {
                    Toast.makeText(SelectTestActivity.this, "User hasn't selected a student profile to do the test!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    startActivity(TestActivity.getIntent(SelectTestActivity.this, fragS.getSelectedStudent().getFirstName(), fragS.getSelectedStudent().getLastName()));
                }
            }
        });
    }
}