/**
 * PURPOSE: edit or remove the selected student's information
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import curtin.edu.au.R;
import curtin.edu.au.database.TestDatabase;
import curtin.edu.au.model.Student;
import curtin.edu.au.utility.DecodeEncodeImage;

public class EditStudentActivity extends StudentActivity
{
    //Declare vars
    private Button editBtn, deleteBtn;
    private TextView title;
    private LinearLayout editLayout;

    private TestDatabase testDb;
    private Student selectedStudent;
    private String selectedFName, selectedLName;

    //Return intent access to this activity.
    public static Intent getIntent(Context c, String firstName, String lastName)
    {
        Intent intent = new Intent(c, EditStudentActivity.class);
        intent.putExtra("selected_student_first_name", firstName);
        intent.putExtra("selected_student_last_name", lastName);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //get the intent extra data
        selectedFName = getIntent().getStringExtra("selected_student_first_name");
        selectedLName = getIntent().getStringExtra("selected_student_last_name");

        //get the selected student
        for(Student student: studentList)
        {
            if(student.getFirstName().equals(selectedFName) && student.getLastName().equals(selectedLName))
            {
                selectedStudent = student;
            }
        }

        //load the test database
        testDb = new TestDatabase(EditStudentActivity.this);

        //attach view to control variables
        title = (TextView) findViewById(R.id.title);
        editLayout = (LinearLayout) findViewById(R.id.editLayout);
        editBtn = (Button) findViewById(R.id.editBtn);
        deleteBtn = (Button) findViewById(R.id.deleteBtn);

        //set-up UI view with the selected student info
        title.setText("Edit Student");
        editLayout.setVisibility(View.VISIBLE);

        inputFirstName.setText(selectedStudent.getFirstName());
        inputLastName.setText(selectedStudent.getLastName());

        List<String> phoneList = selectedStudent.getPhoneList();
        for(int i = 0; i < phoneList.size(); i++)
        {
            if(i != 0)
            {
                layoutPhones[i].setVisibility(View.VISIBLE);
            }
            inputPhones[i].setText(phoneList.get(i));
        }

        List<String> emailList = selectedStudent.getEmailList();
        for(int i = 0; i < emailList.size(); i++)
        {
            if(i != 0)
            {
                layoutEmails[i].setVisibility(View.VISIBLE);
            }
            inputEmails[i].setText(emailList.get(i));
        }

        profilePic.setVisibility(View.VISIBLE);
        profilePic.setImageBitmap(DecodeEncodeImage.decodeImage(selectedStudent.getProfilePic()));

        //confirm updating the current viewed student
        editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(hasEmptyField())
                {
                    Toast.makeText(EditStudentActivity.this, "There's at least an important field(s) left blank! Please check again", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Student student = new Student(inputFirstName.getText().toString(), inputLastName.getText().toString(), selectedImageStr);

                    //check if the newly created student duplicates with other students in the list.
                    boolean duplicate = false;
                    for(Student currStudent: studentList)
                    {
                        if (!currStudent.equals(selectedStudent)
                                && currStudent.getFirstName().equals(student.getFirstName())
                                && currStudent.getLastName().equals(student.getLastName()))
                        {
                            duplicate = true;
                            //early terminate loop once a duplicate is found
                            break;
                        }
                    }

                    //if the user is unique
                    if(!duplicate)
                    {
                        for (android.widget.EditText inputPhone : inputPhones) {
                            if (!inputPhone.getText().toString().isEmpty()) {
                                student.addPhone(inputPhone.getText().toString());
                            }
                        }

                        for (android.widget.EditText inputEmail : inputEmails) {
                            if (!inputEmail.getText().toString().isEmpty()) {
                                student.addEmail(inputEmail.getText().toString());
                            }
                        }

                        //update the list of student and database
                        studentDb.edit(student, selectedStudent);
                        studentList = studentDb.getStudentList();

                        Toast.makeText(EditStudentActivity.this, "Successfully edit the student", Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(EditStudentActivity.this, "Duplicate with other student's name registered in the system! Please try again", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        //confirm deleting the current viewed student
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove from student database and all the test records associated with the student
                studentDb.remove(selectedFName, selectedLName);
                testDb.remove(selectedFName + " " + selectedLName);

                Toast.makeText(EditStudentActivity.this, "Successfully remove the student", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }
}