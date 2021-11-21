/**
 * PURPOSE: view model class to save test activity UI data
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.view;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class TestViewModel extends ViewModel
{
    //Class-field
    private String testDate;
    private boolean[] finishDownload;
    private Integer totalScore;
    private String[] questions;
    private Integer[] timeToSolve;
    private Integer[] remainingTime;
    private Integer[] userChoices;
    private Integer currChoice;
    private Integer[] correctAns;
    private List<Integer[]> options;
    private List<List<Integer>> optionLayouts;
    private Integer[] currViewAnsLayout;
    private MutableLiveData<Integer> currQuestion;

    //Constants
    private static final int LENGTH = 10;

    //Accessor
    public String getTestDate() {
        return testDate;
    }

    public boolean[] getFinishDownload() {
        if(finishDownload == null)
        {
            finishDownload = new boolean[LENGTH];
        }
        return finishDownload;
    }

    public int getTotalScore()
    {
        if(totalScore == null)
        {
            totalScore = 0;
        }
        return totalScore;
    }

    public String[] getQuestions()
    {
        if(questions == null)
        {
            questions = new String[LENGTH];
        }
        return questions;
    }

    public Integer[] getTimeToSolve() {
        if(timeToSolve == null)
        {
            timeToSolve = new Integer[LENGTH];
        }
        return timeToSolve;
    }

    public Integer[] getRemainingTime()
    {
        if(remainingTime == null)
        {
            remainingTime = new Integer[LENGTH];
        }
        return remainingTime;
    }

    public Integer[] getUserChoices()
    {
        if(userChoices == null)
        {
            userChoices = new Integer[LENGTH];
        }
        return userChoices;
    }

    public Integer getCurrChoice()
    {
        return currChoice;
    }

    public Integer[] getCorrectAns() {
        if(correctAns == null)
        {
            correctAns = new Integer[LENGTH];
        }
        return correctAns;
    }

    public List<Integer[]> getOptions() {
        if(options == null)
        {
            options = new ArrayList<>();
        }
        return options;
    }

    public List<List<Integer>> getOptionLayouts()
    {
        if(optionLayouts == null)
        {
            optionLayouts = new ArrayList<>();
        }
        return optionLayouts;
    }

    public Integer[] getCurrViewAnsLayout()
    {
        if(currViewAnsLayout == null)
        {
            currViewAnsLayout = new Integer[LENGTH];
        }
        return currViewAnsLayout;
    }

    public MutableLiveData<Integer> getCurrQuestion()
    {
        if(currQuestion == null)
        {
            currQuestion = new MutableLiveData<>(-1);
        }
        return currQuestion;
    }

    //Mutator

    public void setTestDate(String testDate)
    {
        this.testDate = testDate;
    }

    public void setTotalScore(int totalScore)
    {
        this.totalScore = totalScore;
    }

    public void setCurrChoice(Integer currChoice)
    {
        this.currChoice = currChoice;
    }

    public void addOption(Integer[] option)
    {
        getOptions().add(option);
    }

    public void addOptionLayout(List<Integer> optionLayout)
    {
        getOptionLayouts().add(optionLayout);
    }

    public void setCurrQuestion(int currQuestion)
    {
        getCurrQuestion().setValue(currQuestion);
    }
}
