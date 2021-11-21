/**
 * PURPOSE: model class to hold test's data
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.model;

public class Test
{
    //Class-fields
    private String studentName;
    private int score;
    private String date;
    private int time;

    //Constructor
    public Test(String studentName, int score, String date, int time)
    {
        this.studentName = studentName;
        this.score = score;
        this.date = date;
        this.time = time;
    }

    //Accessor
    public String getStudentName()
    {
        return studentName;
    }

    public int getScore()
    {
        return score;
    }

    public int getTime()
    {
        return time;
    }

    public String getDate()
    {
        return date;
    }

    //Mutator
    public void setStudentName(String studentName)
    {
        this.studentName = studentName;
    }

    public void setScore(int score)
    {
        this.score = score;
    }

    public void setDate(String date)
    {
        this.date = date;
    }

    public void setTime(int time)
    {
        this.time = time;
    }
}
