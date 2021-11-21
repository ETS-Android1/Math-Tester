/**
 * PURPOSE: establish a student database holding all the registered student information
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import curtin.edu.au.database.MathSchemas.StudentTable;
import curtin.edu.au.model.Student;

import java.util.ArrayList;
import java.util.List;

public class StudentDatabase extends SQLiteOpenHelper
{
    //Constants:
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "Student.db";
    private static final int MAX_ENTRY = 10;

    //Class-fields:
    private SQLiteDatabase db;
    private List<Student> studentList;

    //Return access to the student list.
    public List<Student> getStudentList()
    {
        return studentList;
    }

    public StudentDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, VERSION);
        studentList = new ArrayList<>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        //create a table in database.
        sqLiteDatabase.execSQL("CREATE TABLE "
                + StudentTable.NAME + "("
                + StudentTable.Cols.FNAME + " TEXT, "
                + StudentTable.Cols.LNAME + " TEXT, "
                + StudentTable.Cols.PHONE + " TEXT, "
                + StudentTable.Cols.EMAIL + " TEXT, "
                + StudentTable.Cols.PICTURE + " TEXT)");
    }

    //Not implementing onUpgrade - not necessary for the scope of this unit.
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        throw new UnsupportedOperationException("Sorry this method isn't implemented!");
    }

    //Implementing database query functions.
    //load list of students from database
    public void load()
    {
        db = this.getWritableDatabase();
        Cursor cursor = db.query(StudentTable.NAME, null, null, null, null, null, null);

        //Iterate over each row and retrieve the entire database content.
        try
        {
            cursor.moveToFirst();
            while(!cursor.isAfterLast())
            {
                Student student = new Student(cursor.getString(0), cursor.getString(1), cursor.getString(4));
                String[] phoneArr = cursor.getString(2).split(",");
                for(int i = 0; i < phoneArr.length; i++)
                {
                    student.addPhone(phoneArr[i]);
                }
                String[] emailArr = cursor.getString(3).split(",");
                for(int i = 0; i < emailArr.length; i++)
                {
                    student.addEmail(emailArr[i]);
                }
                studentList.add(student);
                cursor.moveToNext();
            }
        }
        finally
        {
            cursor.close();
        }
    }

    //add a new Student.
    public void add(Student student)
    {
        //add to the Student list.
        studentList.add(student);

        //add to the database then.
        ContentValues cv = new ContentValues();
        cv.put(StudentTable.Cols.FNAME, student.getFirstName());
        cv.put(StudentTable.Cols.LNAME, student.getLastName());

        StringBuilder phonesString = new StringBuilder();
        for(String phone: student.getPhoneList())
        {
            phonesString.append(phone).append(",");
        }
        cv.put(StudentTable.Cols.PHONE, String.valueOf(phonesString));

        StringBuilder emailsString = new StringBuilder();
        for(String email:student.getEmailList())
        {
            emailsString.append(email).append(",");
        }
        cv.put(StudentTable.Cols.EMAIL, String.valueOf(emailsString));

        cv.put(StudentTable.Cols.PICTURE, student.getProfilePic());
        db.insert(StudentTable.NAME, null, cv);
    }

    //delete a Student.
    public void remove(String firstName, String lastName)
    {
        Student rmStudent = null;

        //find the targeted student
        for(Student student: studentList)
        {
            if(student.getFirstName().equals(firstName) && student.getLastName().equals(lastName))
            {
                rmStudent = student;
            }
        }

        //if found
        if(rmStudent != null)
        {
            //remove from list
            studentList.remove(rmStudent);
            //remove from database then
            String[] whereValue = {firstName, lastName};
            db.delete(StudentTable.NAME, StudentTable.Cols.FNAME + " = ? AND " + StudentTable.Cols.LNAME + " = ?", whereValue);
        }
    }

    //edit a Student.
    public void edit(Student newStudent, Student oldStudent)
    {
        //update the list
        studentList.set(studentList.indexOf(oldStudent), newStudent);

        //update in the database then.
        ContentValues cv = new ContentValues();
        cv.put(StudentTable.Cols.FNAME, newStudent.getFirstName());
        cv.put(StudentTable.Cols.LNAME, newStudent.getLastName());

        StringBuilder phonesString = new StringBuilder();
        for(String phone: newStudent.getPhoneList())
        {
            phonesString.append(phone).append(",");
        }
        cv.put(StudentTable.Cols.PHONE, phonesString.toString());

        StringBuilder emailsString = new StringBuilder();
        for(String email: newStudent.getEmailList())
        {
            emailsString.append(email).append(",");
        }
        cv.put(StudentTable.Cols.EMAIL, emailsString.toString());

        cv.put(StudentTable.Cols.PICTURE, newStudent.getProfilePic());
        String[] whereValue = {oldStudent.getFirstName(), oldStudent.getLastName()};
        db.update(StudentTable.NAME, cv, StudentTable.Cols.FNAME + " = ? AND " + StudentTable.Cols.LNAME + " = ?", whereValue);
    }
}
