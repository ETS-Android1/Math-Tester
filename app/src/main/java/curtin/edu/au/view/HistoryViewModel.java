/**
 * PURPOSE: view model class to save history activity UI data
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.view;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class HistoryViewModel extends ViewModel
{
    //Class-field:
    private MutableLiveData<Integer> selectedStudentPos;

    //Accessor
    public MutableLiveData<Integer> getSelectedStudentPos()
    {
        if(selectedStudentPos == null)
        {
            selectedStudentPos = new MutableLiveData<>();
        }
        return selectedStudentPos;
    }

    //Mutator
    public void setSelectedStudentPos(int position)
    {
        getSelectedStudentPos().setValue(position);
    }
}
