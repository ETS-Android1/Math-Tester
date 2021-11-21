/**
 * PURPOSE: establish a test record database holding the history of all tests taken
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import curtin.edu.au.database.MathSchemas.TestTable;
import curtin.edu.au.model.Test;


import java.util.ArrayList;
import java.util.List;

public class TestDatabase extends SQLiteOpenHelper
{
    //Constants:
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "Test.db";
    private static final int MAX_ENTRY = 10;

    //Class-fields:
    private SQLiteDatabase db;
    private List<Test> testList;

    //Return access to the test list.
    public List<Test> getTestList()
    {
        return testList;
    }

    public TestDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, VERSION);
        testList = new ArrayList<>();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase)
    {
        //create a table in database.
        sqLiteDatabase.execSQL("CREATE TABLE "
                + TestTable.NAME + "("
                + TestTable.Cols.STUDENT + " TEXT, "
                + TestTable.Cols.SCORE + " TEXT, "
                + TestTable.Cols.DATE + " TEXT, "
                + TestTable.Cols.TIME  + " TEXT)");
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
        Cursor cursor = db.query(TestTable.NAME, null, null, null, null, null, null);

        //Iterate over each row and retrieve the entire database content.
        try
        {
            cursor.moveToFirst();
            while(!cursor.isAfterLast())
            {
                Test test = new Test(cursor.getString(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3));
                testList.add(test);
                cursor.moveToNext();
            }
        }
        finally
        {
            cursor.close();
        }
    }

    //add a new Student.
    public void add(Test test)
    {
        db = this.getWritableDatabase();
        //add to the Student list.
        testList.add(test);

        //add to the database then.
        ContentValues cv = new ContentValues();
        cv.put(TestTable.Cols.STUDENT, test.getStudentName());
        cv.put(TestTable.Cols.SCORE, test.getScore());
        cv.put(TestTable.Cols.DATE, test.getDate());
        cv.put(TestTable.Cols.TIME, test.getTime());

        db.insert(TestTable.NAME, null, cv);
    }

    //delete a Student.
    public void remove(String studentName)
    {
        db = this.getWritableDatabase();
        List<Test> rmTestList = new ArrayList<>();

        //find the targeted tests
        for(Test test: testList)
        {
            if(test.getStudentName().equals(studentName))
            {
                rmTestList.add(test);
            }
        }

        //if found
        if(rmTestList.size() != 0)
        {
            //remove from list
            for(Test test: rmTestList)
            {
                testList.remove(test);
            }
            //remove from database then
            String[] whereValue = {studentName};
            db.delete(TestTable.NAME, TestTable.Cols.STUDENT + " = ? ", whereValue);
        }
    }
}
