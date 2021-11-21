/**
 * PURPOSE: indicates a scrollable list of students.
 * AUTHOR: MINH VU
 * LAST MODIFIED DATE: 5/09/2021
 */
package curtin.edu.au.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import curtin.edu.au.utility.DecodeEncodeImage;
import curtin.edu.au.activity.EditStudentActivity;
import curtin.edu.au.R;
import curtin.edu.au.model.Student;
import curtin.edu.au.database.StudentDatabase;
import curtin.edu.au.view.HistoryViewModel;

public class StudentSelectorFragment extends Fragment
{
    //Declare variables:
    private Student selectedStudent;
    private int selectedPosition;
    private List<Student> studentList;
    private StudentDatabase studentDb;
    private String filterName;
    private boolean forEdit;

    private HistoryViewModel historyVM;
    private RecyclerView rv;
    private StudentAdapter adapter;
    private TextView emptyText;

    //return the selected instructor.
    public Student getSelectedStudent()
    {
        return selectedStudent;
    }

    //Default Constructor:
    public StudentSelectorFragment()
    {
        // Required empty public constructor
    }

    //Alternative Constructor:
    public StudentSelectorFragment(String filterName, boolean forEdit)
    {
        this.filterName = filterName;
        this.forEdit = forEdit;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //set starting selection to nothing
        selectedStudent = null;
        selectedPosition = -1;

        //load student database
        studentDb = new StudentDatabase(getActivity());
        studentDb.load();
        studentList = studentDb.getStudentList();

        //create a temporary array list of students to store students having name matched the filter search.
        if(filterName != null)
        {
            List<Student> tempList = new ArrayList<>();
            for(Student student: studentList)
            {
                String studentName = student.getFirstName() + " " + student.getLastName();
                if(studentName.contains(filterName))
                {
                    tempList.add(student);
                }
            }
            //swap the list
            studentList = tempList;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_selector, container, false);

        //attach views to control vars
        emptyText = (TextView) view.findViewById(R.id.empty);
        rv = view.findViewById(R.id.rvList);

        //create the fragment's view model
        historyVM = new ViewModelProvider(requireActivity()).get(HistoryViewModel.class);

        //set-up initial UI
        if(studentList.isEmpty())
        {
            emptyText.setVisibility(View.VISIBLE);
        }
        else {
            rv.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        }

        adapter = new StudentAdapter();
        rv.setAdapter(adapter);
        return view;
    }

    //indicates the current viewed entry in the recycler view.
    private class StudentViewHolder extends RecyclerView.ViewHolder
    {
        //Declare variables:
        private Student student;
        private TextView studentName;
        private ImageView studentImg;

        public StudentViewHolder(LayoutInflater inflater, ViewGroup parent)
        {
            super(inflater.inflate(R.layout.student_entry, parent, false));

            studentName = itemView.findViewById(R.id.name);
            studentImg = itemView.findViewById(R.id.profileImg);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    if(forEdit)
                    {
                        startActivity(EditStudentActivity.getIntent(getActivity(), student.getFirstName(), student.getLastName()));
                    }
                    else
                    {
                        selectedStudent = student;

                        //notify other view-holders to configure background color
                        adapter.notifyItemChanged(selectedPosition);
                        selectedPosition = getLayoutPosition();
                        adapter.notifyItemChanged(selectedPosition);

                        //changes saved position in history view model
                        historyVM.setSelectedStudentPos(selectedPosition);
                    }
                }
            });
        }

        //attach the source of each view's entry element.
        public void bind(Student student, boolean focused)
        {
            this.student = student;
            //update the UI.
            studentName.setText(new StringBuilder().append(student.getFirstName()).append(" ").append(student.getLastName()));
            studentImg.setImageBitmap(DecodeEncodeImage.decodeImage(student.getProfilePic()));

            //highlight selected
            if(focused)
            {
                //set cyan for selected
                itemView.setBackgroundColor(Color.CYAN);
            }
            else
            {
                //set back to default
                itemView.setBackgroundResource(R.drawable.border);
            }
        }
    }

    //adapter class to find the source of instructorList to consecutively bind to the recycled view of the view-holder.
    private class StudentAdapter extends RecyclerView.Adapter<StudentViewHolder>
    {
        @NonNull
        @Override
        public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new StudentViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull StudentViewHolder holder, int position)
        {
            holder.bind(studentList.get(position), selectedPosition == position);
        }

        @Override
        public int getItemCount() {
            return studentList.size();
        }
    }
}