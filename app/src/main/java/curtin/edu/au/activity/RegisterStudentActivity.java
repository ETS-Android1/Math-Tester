/**
 * PURPOSE: add new student(s) to the app database
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import curtin.edu.au.R;
import curtin.edu.au.model.Student;
import curtin.edu.au.utility.DecodeEncodeImage;

public class RegisterStudentActivity extends StudentActivity
{
    //Declare vars
    private TextView title;
    private Button confirmBtn;

    //Return intent access to this activity.
    public static Intent getIntent(Context c)
    {
        return new Intent(c, RegisterStudentActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        title = (TextView) findViewById(R.id.title);
        confirmBtn = (Button) findViewById(R.id.addStudentBtn);

        //set-up UI view
        title.setText("Register Student(s)");
        confirmBtn.setVisibility(View.VISIBLE);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hasEmptyField())
                {
                    Toast.makeText(RegisterStudentActivity.this, "There's at least an important field(s) left blank! Please check again", Toast.LENGTH_LONG).show();
                }
                else
                {
                    Student student = new Student(inputFirstName.getText().toString(), inputLastName.getText().toString(), selectedImageStr);

                    //check if the student duplicates with other students in the list.
                    boolean duplicate = false;
                    for(Student currStudent: studentList)
                    {
                        if(currStudent.getFirstName().equals(student.getFirstName()) && currStudent.getLastName().equals(student.getLastName()))
                        {
                            duplicate = true;
                        }
                    }

                    //if the user is unique
                    if(!duplicate)
                    {
                        for (int i = 0; i < inputPhones.length; i++) {
                            if (!inputPhones[i].getText().toString().isEmpty()) {
                                student.addPhone(inputPhones[i].getText().toString());
                            }
                        }

                        for (int i = 0; i < inputEmails.length; i++) {
                            if (!inputEmails[i].getText().toString().isEmpty()) {
                                student.addEmail(inputEmails[i].getText().toString());
                            }
                        }

                        //add to the list of student and database
                        studentList.add(student);
                        studentDb.add(student);

                        Toast.makeText(RegisterStudentActivity.this, "Successfully register a student", Toast.LENGTH_LONG).show();

                        //clear all user input fields
                        clear();
                    }
                    else
                    {
                        Toast.makeText(RegisterStudentActivity.this, "Duplicate with other student's name registered in the system! Please try again", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    //clear view's input and set values once added to database
    private void clear()
    {
        //clear
        inputFirstName.setText("");
        inputLastName.setText("");
        for (EditText inputPhone : inputPhones) {
            inputPhone.setText("");
        }
        for (EditText inputEmail : inputEmails) {
            inputEmail.setText("");
        }
        searchBar.setVisibility(View.GONE);
        onlineFrame.setVisibility(View.GONE);
        profilePic.setVisibility(View.GONE);
        profilePic.setImageBitmap(null);

        selectedImageStr = null;
        id = null;
        listUrlStr = null;
    }
}