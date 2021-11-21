/**
 * PURPOSE: model class to hold student's data
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.model;

import java.util.LinkedList;
import java.util.List;

public class Student
{
    //Class-fields
    private String firstName;
    private String lastName;
    private List<String> phoneList;
    private List<String> emailList;
    private String profilePic;

    //Constructor
    public Student(String firstName, String lastName, String profilePic)
    {
        setFirstName(firstName);
        setLastName(lastName);
        phoneList = new LinkedList<>();
        emailList = new LinkedList<>();
        this.profilePic = profilePic;
    }

    //Accessors
    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public List<String> getPhoneList()
    {
        return phoneList;
    }

    public List<String> getEmailList()
    {
        return emailList;
    }

    public String getProfilePic()
    {
        return profilePic;
    }

    //Mutators
    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setProfilePic(String profilePic)
    {
        this.profilePic = profilePic;
    }

    //add a new phone number if the list of numbers haven't reached 10
    public boolean addPhone(String phoneNo)
    {
        if(phoneList.size() < 10) {
            return phoneList.add(phoneNo);
        }
        else
        {
            return false;
        }
    }

    //add a new phone number if the list of email haven't reached 10
    public boolean addEmail(String email)
    {
        if(emailList.size() < 10) {
            return emailList.add(email);
        }
        else
        {
            return false;
        }
    }
}
