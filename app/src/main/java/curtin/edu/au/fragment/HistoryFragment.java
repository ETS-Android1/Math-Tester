/**
 * PURPOSE: presents test records of the selected student
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import curtin.edu.au.R;
import curtin.edu.au.model.Test;
import curtin.edu.au.database.TestDatabase;

public class HistoryFragment extends Fragment {
    //Declare vars
    private List<Test> testList;
    private TestDatabase testDb;
    private String studentName;
    private boolean sort, highToLow;

    private RecyclerView rv;
    private TextView noTest;
    private HistoryAdapter adapter;

    //get selected test list
    public List<Test> getTestList()
    {
        return testList;
    }

    public HistoryFragment() {
        // Required empty public constructor
        sort = false;
        highToLow = false;
        studentName = null;
    }

    //Alternative Constructor:
    public HistoryFragment(String studentName, boolean sort, boolean highToLow)
    {
        this.studentName = studentName;
        this.sort = sort;
        this.highToLow = highToLow;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load test database
        testDb = new TestDatabase(getActivity());
        testDb.load();
        testList = testDb.getTestList();

        //filter list entries
        if(studentName != null)
        {
            //if a student was selected, list then only contains that student's record
            List<Test> tempList = new ArrayList<>();
            for(Test test: testList)
            {
                if(test.getStudentName().equals(studentName))
                {
                    tempList.add(test);
                }
            }
            testList = tempList;

            //if needs to sort
            if(sort)
            {
                //sort using algorithm by test score
                testList = insertSortLTH(testList.toArray(new Test[0]));
                if (highToLow)
                {
                    //reverse the insertion order
                    Collections.reverse(testList);
                }
            }
        }
    }

    //sorting function from high to low
    private List<Test> insertSortLTH(Test[] array) {
        for (int i = 1; i < array.length; i++) {
            Test current = array[i];
            int j = i - 1;
            while (j >= 0 && current.getScore() < array[j].getScore())
            {
                array[j + 1] = array[j];
                j--;
            }
            //exit here so j is either -1 or it's at the first element where score of current  >= score array[j]
            array[j + 1] = current;
        }

        //convert the array to ArrayList
        return Arrays.asList(array);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        //attach views to control vars
        rv = view.findViewById(R.id.historyRv);
        noTest = view.findViewById(R.id.emptyTest);

        //set-up initial views
        if(testList.isEmpty())
        {
            rv.setVisibility(View.GONE);
        }
        else
        {
            noTest.setVisibility(View.GONE);
            rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
            adapter = new HistoryAdapter();
            rv.setAdapter(adapter);
        }

        return view;
    }

    private class HistoryViewHolder extends RecyclerView.ViewHolder
    {
        //Declare vars
        private final TextView date;
        private final TextView duration;
        private final TextView score;

        //Constructor
        public HistoryViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.history_entry, parent, false));

            //attach view to control vars
            date = itemView.findViewById(R.id.dateTxt);
            duration = itemView.findViewById(R.id.durationTxt);
            score = itemView.findViewById(R.id.scoreTxt);
        }

        //attach the source of each view-holder
        public void bind(Test test)
        {
            //update view
            date.setText(test.getDate());
            duration.setText(new StringBuilder().append(test.getTime()).append(" seconds"));
            score.setText(String.valueOf(test.getScore()));
        }
    }

    private class HistoryAdapter extends RecyclerView.Adapter<HistoryViewHolder>
    {
        @NonNull
        @Override
        public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new HistoryViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
            holder.bind(testList.get(position));
        }

        @Override
        public int getItemCount() {
            return testList.size();
        }
    }
}