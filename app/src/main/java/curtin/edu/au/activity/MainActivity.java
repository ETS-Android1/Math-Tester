/**
 * PURPOSE: shows the navigation panel of the app and list of registered students
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import curtin.edu.au.R;
import curtin.edu.au.fragment.StudentSelectorFragment;

public class MainActivity extends AppCompatActivity {
    //Declare variables
    private Button addBtn, searchBtn, testBtn, historyBtn;
    private EditText inputSearch;
    private final FragmentManager fm = getSupportFragmentManager();
    private StudentSelectorFragment fragS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //attach view to control variables
        addBtn = (Button) findViewById(R.id.addStudent);
        searchBtn = (Button) findViewById(R.id.searchStudent);
        inputSearch = (EditText) findViewById(R.id.inputSearchStudent);
        testBtn = (Button) findViewById(R.id.testBtn);
        historyBtn = (Button) findViewById(R.id.testHistoryBtn);
        fragS = (StudentSelectorFragment) fm.findFragmentById(R.id.studentSelector);

        //shows the list of students matched with searched name
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //replace by the fragment selector view with filtered student list.
                fragS = new StudentSelectorFragment(inputSearch.getText().toString(), true);
                fm.beginTransaction().replace(R.id.studentSelector, fragS).commit();
            }
        });

        //register a new student
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(RegisterStudentActivity.getIntent(MainActivity.this));
            }
        });

        //Starts a test
        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(SelectTestActivity.getIntent(MainActivity.this));
            }
        });

        //view student's test record
        historyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(HistoryActivity.getIntent(MainActivity.this));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        //recreate the navigation panel with updated views & reset previous view's input.
        inputSearch.setText("");
        fragS = new StudentSelectorFragment(null, true);
        fm.beginTransaction().replace(R.id.studentSelector, fragS).commit();
    }
}