/**
 * PURPOSE: presents a scrollable list of online images
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import curtin.edu.au.utility.DecodeEncodeImage;
import curtin.edu.au.R;
import curtin.edu.au.view.StudentViewModel;

public class ImageFragment extends Fragment {
    //Declare vars
    private String selectedImageStr;
    private List<String> listUrlStr;
    private List<Bitmap> imgRvList;
    private List<DownloadImage> taskList;
    private int selectedPosition;

    private RecyclerView rv;
    private ImageAdapter adapter;
    private StudentViewModel studentVM;

    //Constant
    private static final int COLUMN_NUMBER = 3;

    //return the selected image
    public String getSelectedImageStr()
    {
        return selectedImageStr;
    }

    //Default Constructor
    public ImageFragment()
    {
        //Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //initialize values
        selectedImageStr = null;
        taskList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        //attach view to control vars
        rv = view.findViewById(R.id.gridList);

        //create view model to save data throughout configuration changes
        studentVM = new ViewModelProvider(requireActivity()).get(StudentViewModel.class);
        //get the saved data state when configuration changes or initialize new data for creation
        listUrlStr = studentVM.getUrlList().getValue();
        imgRvList = studentVM.getImgRvList();

        //list of recycler views set-up
        rv.setLayoutManager(new GridLayoutManager(getActivity(), COLUMN_NUMBER));
        adapter = new ImageAdapter();
        rv.setAdapter(adapter);

        //observe changes in selected image position from recycler view
        studentVM.getSelectedRvPost().observe(getViewLifecycleOwner(), integer -> {
            selectedPosition = integer;
            //notify the recycler view changes to selected image
            adapter.notifyItemChanged(selectedPosition);
        });

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

        //Stop any currently running async tasks
        for(DownloadImage task: taskList)
        {
            task.cancel(true);
        }
    }

    //indicates the current viewed entry in the recycler view.
    private class ImageViewHolder extends RecyclerView.ViewHolder
    {
        //Declare variables
        private final ImageView imageView;
        private final ProgressBar progressBar;

        //Constructor
        public ImageViewHolder(LayoutInflater inflater, ViewGroup parent)
        {
            super(inflater.inflate(R.layout.image_entry, parent, false));

            //attach each cell view's elements to control variables
            progressBar = itemView.findViewById(R.id.imageProgress);
            imageView = itemView.findViewById(R.id.image);
        }

        //attach the cell position for each view's entry element.
        public void bind(int position)
        {
            //for each image, a downloading async task starts and updates the UI
            if(position >= imgRvList.size())
            {
                //shows progress bar initially until the image is downloaded
                progressBar.setVisibility(View.VISIBLE);

                taskList.add(new DownloadImage(imageView, progressBar));
                taskList.get(taskList.size() - 1).execute(listUrlStr.get(position));
            }
            else
            {
                progressBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                imageView.setImageBitmap(imgRvList.get(position));
            }

            //set filter and background for selection
            if(selectedPosition == position)
            {
                itemView.setBackgroundColor(Color.GRAY);
            }
            else
            {
                itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            //when an image is selected
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    //when the image is shown, assign selected image to the user's click
                    if(imageView.getVisibility() == View.VISIBLE)
                    {
                        Bitmap chosenImg = ((BitmapDrawable)imageView.getDrawable()).getBitmap();

                        //set to the activity's view model
                        studentVM.setImageLive(chosenImg);

                        //notify other view-holders to configure background color
                        adapter.notifyItemChanged(selectedPosition);
                        studentVM.setSelectedRvPost(getLayoutPosition());

                        //save to encoded string
                        selectedImageStr = DecodeEncodeImage.encodeImage(chosenImg);
                    }
                }
            });
        }
    }

    //consecutively recycle the view of each image to the view-holder.
    private class ImageAdapter extends RecyclerView.Adapter<ImageViewHolder>
    {
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new ImageViewHolder(inflater, parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            holder.bind(position);
        }

        @Override
        public int getItemCount()
        {
            return listUrlStr.size();
        }
    }

    //asynchronously download image on the background
    private class DownloadImage extends AsyncTask<String, String, Bitmap>
    {
        //Declare vars
        private final ImageView imageView;
        private final ProgressBar progressBar;
        private HttpsURLConnection conn = null;
        private Bitmap image;

        //Constructor
        public DownloadImage(ImageView imageView, ProgressBar progressBar)
        {
            this.imageView = imageView;
            this.progressBar = progressBar;
        }

        @Override
        protected Bitmap doInBackground(String... values) {
            return pictureRetrievalTask(values[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            //set to UI once the image is ready
            progressBar.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageBitmap(bitmap);

            //save image to view-model
            studentVM.addToRvList(bitmap);
        }

        @Override
        protected void onProgressUpdate(String... values)
        {
            //If the method is called with "Error" message at first index, notify the user. Otherwise update the progress bar
            if(values[0].equals("Error"))
            {
                Toast.makeText(getActivity(), values[1], Toast.LENGTH_LONG).show();
            }
            else
            {
                progressBar.setProgress(Integer.parseInt(values[1]));
            }
        }

        private Bitmap pictureRetrievalTask(String imageURLStr)
        {
            try
            {
                if (imageURLStr != null)
                {
                    //download the image from the URL
                    image = getImageFromUrl(imageURLStr);
                }
                else
                {
                    publishProgress("Error", "No corresponding image found");
                }
            }
            catch (IOException e)
            {
                publishProgress("Error", "Error in downloading the data");
            }
            finally
            {
                if(conn != null)
                {
                    conn.disconnect();
                }
            }

            return image;
        }

        //download image bitmap
        private Bitmap getImageFromUrl(String imageUrlStr) throws IOException
        {
            Bitmap image = null;

            URL imageURL = new URL(Uri.parse(imageUrlStr).buildUpon().build().toString());
            conn = (HttpsURLConnection) imageURL.openConnection();

            if(conn == null)
            {
                publishProgress("Error", "Check internet");
            }
            else if(conn.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                publishProgress("Error", "Problem with connection, responding with code " + conn.getResponseCode());
            }
            else {
                image = downloadToBitmap(conn);
                if(image == null)
                {
                    publishProgress("Error", "No corresponding image found");
                }
                conn.disconnect();
            }

            return image;
        }

        //download image from input stream to bitmap
        private Bitmap downloadToBitmap(HttpURLConnection conn) throws IOException
        {
            InputStream inputStream = conn.getInputStream();
            byte[] byteData = getByteArrayFromInputStreamForImage(inputStream);

            return BitmapFactory.decodeByteArray(byteData,0,byteData.length);
        }

        //read from the connection's input stream to byte
        private byte[] getByteArrayFromInputStreamForImage(InputStream inputStream) throws IOException
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096];
            int progress = 0;

            // download the data
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
                progress = progress+nRead;
                publishProgress("Progress", String.valueOf(progress));
            }

            return buffer.toByteArray();
        }
    }
}