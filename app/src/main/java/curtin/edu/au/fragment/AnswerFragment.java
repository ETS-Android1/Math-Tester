/**
 * PURPOSE: presents an option frame and allows user to choose an answer
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import curtin.edu.au.R;
import curtin.edu.au.view.TestViewModel;

public class AnswerFragment extends Fragment
{
    //Declare vars
    private Button enterBtn;
    private Button[] optionsBtn;
    private EditText inputAnswer;

    private int optionNumb, startIndex;
    private TestViewModel testVM;

    //Constant layout
    private static final int[] layoutIDs = {R.layout.option_4_entry, R.layout.option_3_entry, R.layout.option_2_entry, R.layout.short_answer};

    public AnswerFragment()
    {
        // Required empty public constructor
    }

    //Alternative Constructor
    public AnswerFragment(int optionNumb, int startIndex)
    {
        this.optionNumb = optionNumb;
        this.startIndex = startIndex;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = null;
        if(optionNumb == 4)
        {
            view = inflater.inflate(layoutIDs[0], container, false);
        }
        else if (optionNumb == 3)
        {
            view = inflater.inflate(layoutIDs[1], container, false);
        }
        else if (optionNumb == 2)
        {
            view = inflater.inflate(layoutIDs[2], container, false);
        }
        else
        {
            view = inflater.inflate(layoutIDs[3], container, false);
        }

        //create this fragment's view model
        testVM = new ViewModelProvider(requireActivity()).get(TestViewModel.class);

        //attach view to control vars
        optionsBtn = new Button[]{
            view.findViewById(R.id.option1),
            view.findViewById(R.id.option2),
            view.findViewById(R.id.option3),
            view.findViewById(R.id.option4),
        };
        enterBtn = (Button) view.findViewById(R.id.EnterText);
        inputAnswer = view.findViewById(R.id.inputAnswer);

        //set up UI for option buttons
        for(int i = 0; i < optionsBtn.length; i++)
        {
            setBtn(i);
        }

        //when the user enters a short answer's value
        if(enterBtn != null)
        {
            enterBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String shortAns = inputAnswer.getText().toString();
                    if(!shortAns.equals(""))
                    {
                        testVM.setCurrChoice(Integer.parseInt(shortAns));
                    }
                    else
                    {
                        testVM.setCurrChoice(null);
                    }

                    Toast.makeText(getActivity(), "Saved answer", Toast.LENGTH_SHORT).show();
                }
            });

            if(testVM.getCurrChoice() != null)
            {
                inputAnswer.setText(testVM.getCurrChoice().toString());
            }
        }

        return view;
    }

    private void setBtn(int index)
    {
        if(optionsBtn[index] != null)
        {
            optionsBtn[index].setText(String.valueOf(testVM.getOptions().get(testVM.getCurrQuestion().getValue() - 1)[startIndex + index]));

            //pre-set the option to highlighted gray if user has selected it before
            if(testVM.getCurrChoice() != null)
            {
                if (Integer.parseInt(optionsBtn[index].getText().toString()) == testVM.getCurrChoice()) {
                    optionsBtn[index].setClickable(false);
                    optionsBtn[index].setBackgroundColor(Color.GRAY);
                }
            }

            optionsBtn[index].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    optionsBtn[index].setClickable(false);
                    optionsBtn[index].setBackgroundColor(Color.GRAY);
                    for(int i = 0; i < optionsBtn.length; i++)
                    {
                        if(i != index && optionsBtn[i] != null)
                        {
                            optionsBtn[i].setClickable(true);
                            optionsBtn[i].setBackgroundResource(android.R.drawable.btn_default);
                        }
                    }
                    testVM.setCurrChoice(Integer.parseInt(optionsBtn[index].getText().toString()));
                    Log.d("Check curr choice", testVM.getCurrChoice().toString());
                }
            });
        }
    }
}