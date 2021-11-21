/**
 * PURPOSE: an abstract parent activity to host common features of adding and editing student activities
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import curtin.edu.au.R;
import curtin.edu.au.database.StudentDatabase;
import curtin.edu.au.fragment.ImageFragment;
import curtin.edu.au.utility.DecodeEncodeImage;
import curtin.edu.au.view.StudentViewModel;
import curtin.edu.au.model.Student;

public abstract class StudentActivity extends AppCompatActivity {
    //Declare variables
    protected Button returnBtn, prefillBtn, addPhoneBtn, addEmailBtn, cameraBtn, galleryBtn, onlineBtn, searchImgBtn;
    protected EditText inputFirstName, inputLastName, inputSearchBar;
    protected LinearLayout searchBar;
    protected LinearLayout[] layoutPhones, layoutEmails;
    protected EditText[] inputPhones, inputEmails;
    protected Button[] removePhoneBtn, removeEmailBtn;
    protected ImageView profilePic;
    protected final FragmentManager fm = getSupportFragmentManager();
    protected ImageFragment fI;
    protected FrameLayout onlineFrame;

    protected String id;
    protected List<String> listUrlStr;
    protected String selectedImageStr = null;
    protected StudentDatabase studentDb;
    protected List<Student> studentList;
    protected StudentViewModel studentVM;
    protected Integer onlineVis, searchVis;
    protected int[] arrVisPhones = new int[MAX_ENTRY];
    protected int[] arrVisEmails = new int[MAX_ENTRY];

    //Constants
    protected static final int REQUEST_TAKE_PHOTO = 1;
    protected static final int REQUEST_CONTACT = 2;
    protected static final int REQUEST_READ_CONTACT_PERMISSION = 3;
    protected static final int REQUEST_PICK_PHOTO = 4;
    protected static final int MAX_ENTRY = 10;
    protected static final String API_KEY = "24130682-5e81bdacb14bb8a480da54200";
    protected static final int MAX_IMAGES = 50;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        //load student database
        studentDb = new StudentDatabase(this);
        studentDb.load();
        studentList = studentDb.getStudentList();

        //attach view to control variables
        returnBtn = (Button) findViewById(R.id.backBtn);
        prefillBtn = (Button) findViewById(R.id.prefillBtn);
        addPhoneBtn = (Button) findViewById(R.id.addPhoneBtn);
        addEmailBtn = (Button) findViewById(R.id.addEmailBtn);
        cameraBtn = (Button) findViewById(R.id.livePhotoBtn);
        galleryBtn = (Button) findViewById(R.id.storagePhotoBtn);
        onlineBtn = (Button) findViewById(R.id.onlinePhotoBtn);
        searchImgBtn = (Button) findViewById(R.id.searchImgBtn);
        inputFirstName = (EditText) findViewById(R.id.inputFirstName);
        inputLastName = (EditText) findViewById(R.id.inputLastName);
        inputSearchBar = (EditText) findViewById(R.id.inputImageSearch);
        profilePic = (ImageView) findViewById(R.id.profileImg);
        onlineFrame = (FrameLayout) findViewById(R.id.imageSelector);
        searchBar = (LinearLayout) findViewById(R.id.searchBar);
        inputSearchBar = (EditText) findViewById(R.id.inputImageSearch);
        fI = (ImageFragment) fm.findFragmentById(R.id.imageSelector);

        //initialize array to hold the sequence of phones & email elements(10 each)
        layoutPhones = new LinearLayout[]{
                (LinearLayout) findViewById(R.id.layoutPhone1),
                (LinearLayout) findViewById(R.id.layoutPhone2),
                (LinearLayout) findViewById(R.id.layoutPhone3),
                (LinearLayout) findViewById(R.id.layoutPhone4),
                (LinearLayout) findViewById(R.id.layoutPhone5),
                (LinearLayout) findViewById(R.id.layoutPhone6),
                (LinearLayout) findViewById(R.id.layoutPhone7),
                (LinearLayout) findViewById(R.id.layoutPhone8),
                (LinearLayout) findViewById(R.id.layoutPhone9),
                (LinearLayout) findViewById(R.id.layoutPhone10)
        };
        layoutEmails = new LinearLayout[]{
                (LinearLayout) findViewById(R.id.layoutEmail1),
                (LinearLayout) findViewById(R.id.layoutEmail2),
                (LinearLayout) findViewById(R.id.layoutEmail3),
                (LinearLayout) findViewById(R.id.layoutEmail4),
                (LinearLayout) findViewById(R.id.layoutEmail5),
                (LinearLayout) findViewById(R.id.layoutEmail6),
                (LinearLayout) findViewById(R.id.layoutEmail7),
                (LinearLayout) findViewById(R.id.layoutEmail8),
                (LinearLayout) findViewById(R.id.layoutEmail9),
                (LinearLayout) findViewById(R.id.layoutEmail10)
        };
        inputPhones = new EditText[]{
                (EditText) findViewById(R.id.inputPhone1),
                (EditText) findViewById(R.id.inputPhone2),
                (EditText) findViewById(R.id.inputPhone3),
                (EditText) findViewById(R.id.inputPhone4),
                (EditText) findViewById(R.id.inputPhone5),
                (EditText) findViewById(R.id.inputPhone6),
                (EditText) findViewById(R.id.inputPhone7),
                (EditText) findViewById(R.id.inputPhone8),
                (EditText) findViewById(R.id.inputPhone9),
                (EditText) findViewById(R.id.inputPhone10)
        };
        inputEmails = new EditText[]{
                (EditText) findViewById(R.id.inputEmail1),
                (EditText) findViewById(R.id.inputEmail2),
                (EditText) findViewById(R.id.inputEmail3),
                (EditText) findViewById(R.id.inputEmail4),
                (EditText) findViewById(R.id.inputEmail5),
                (EditText) findViewById(R.id.inputEmail6),
                (EditText) findViewById(R.id.inputEmail7),
                (EditText) findViewById(R.id.inputEmail8),
                (EditText) findViewById(R.id.inputEmail9),
                (EditText) findViewById(R.id.inputEmail10)
        };
        removePhoneBtn = new Button[]{
                (Button) findViewById(R.id.removePhoneBtn2),
                (Button) findViewById(R.id.removePhoneBtn3),
                (Button) findViewById(R.id.removePhoneBtn4),
                (Button) findViewById(R.id.removePhoneBtn5),
                (Button) findViewById(R.id.removePhoneBtn6),
                (Button) findViewById(R.id.removePhoneBtn7),
                (Button) findViewById(R.id.removePhoneBtn8),
                (Button) findViewById(R.id.removePhoneBtn9),
                (Button) findViewById(R.id.removePhoneBtn10)
        };
        removeEmailBtn = new Button[]{
                (Button) findViewById(R.id.removeEmailBtn2),
                (Button) findViewById(R.id.removeEmailBtn3),
                (Button) findViewById(R.id.removeEmailBtn4),
                (Button) findViewById(R.id.removeEmailBtn5),
                (Button) findViewById(R.id.removeEmailBtn6),
                (Button) findViewById(R.id.removeEmailBtn7),
                (Button) findViewById(R.id.removeEmailBtn8),
                (Button) findViewById(R.id.removeEmailBtn9),
                (Button) findViewById(R.id.removeEmailBtn10)
        };

        //create image view model
        studentVM = new ViewModelProvider(this).get(StudentViewModel.class);
        //observe the selected image and update to UI when change occurs
        studentVM.getImageLive().observe(this, bitmap -> {
            if(studentVM.getShow())
            {
                profilePic.setVisibility(View.VISIBLE);
                profilePic.setImageBitmap(bitmap);
            }
            else
            {
                profilePic.setVisibility(View.GONE);
            }
        });
        //observe the list of Urls and replace new image fragments on changes
        studentVM.getUrlList().observe(this, strings -> {
            //reset the previous saved data from the fragment if exist
            studentVM.setImgRvList(null);

            onlineFrame.setVisibility(View.VISIBLE);
            // link and replace Image fragment to this activity once all the related URLs finished downloading for every search
            fI = new ImageFragment();
            fm.beginTransaction().replace(R.id.imageSelector, fI).commit();

            if(strings.isEmpty())
            {
                Toast.makeText(StudentActivity.this, "No searched images found from pixabay", Toast.LENGTH_SHORT).show();
            }
        });

        //return to the main navigation panel activity
        returnBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //enable an extra phone entry
        addPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 1; i < layoutPhones.length; i++)
                {
                    if(layoutPhones[i].getVisibility() == View.GONE && layoutPhones[i-1].getVisibility() == View.VISIBLE)
                    {
                        layoutPhones[i].setVisibility(View.VISIBLE);
                        //use break to stop adding the following entries once one has been made visible
                        break;
                    }
                }
            }
        });

        //remove a specific phone entry
        for(int i = 0; i < 9; i++)
        {
            final int y = i+1;
            removePhoneBtn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //clear the input entry
                    inputPhones[y].setText("");
                    //remove the entry's view
                    layoutPhones[y].setVisibility(View.GONE);
                }
            });
        }

        //enable an extra input email entry
        addEmailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(int i = 1; i < layoutEmails.length; i++)
                {
                    if(layoutEmails[i].getVisibility() == View.GONE && layoutEmails[i-1].getVisibility() == View.VISIBLE)
                    {
                        layoutEmails[i].setVisibility(View.VISIBLE);
                        //use break to stop adding the following entries once one has been made visible
                        break;
                    }
                }
            }
        });

        //remove a specific email entry
        for(int i = 0; i < 9; i++)
        {
            final int y = i+1;
            removeEmailBtn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //clear the input entry
                    inputEmails[y].setText("");
                    //remove the entry's view
                    layoutEmails[y].setVisibility(View.GONE);
                }
            });
        }

        //pick a contact from the phone's contact list to prefill student registration
        prefillBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContactClicked();
            }
        });

        //take a photo from camera app as profile pic
        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //close other camera's views and erase selected image
                searchBar.setVisibility(View.GONE);
                onlineFrame.setVisibility(View.GONE);
                studentVM.setImageLive(null);

                takePhoto();
            }
        });

        //pick a photo from the external drive/gallery as profile pic
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //close other camera's views and erase selected image
                searchBar.setVisibility(View.GONE);
                onlineFrame.setVisibility(View.GONE);
                studentVM.setImageLive(null);

                pickPhoto();
            }
        });

        //enable online search for profile pictures
        onlineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //close other camera's views and erase selected image
                searchBar.setVisibility(View.VISIBLE);
                profilePic.setVisibility(View.GONE);
                studentVM.setImageLive(null);
            }
        });

        //search list of related images to keyword
        searchImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                //reset previous image selection and recycler view's selected item for new search
                profilePic.setVisibility(View.GONE);
                if(studentVM.getSelectedRvPost().getValue() != -1)
                {
                    studentVM.setSelectedRvPost(-1);
                }
                //download all the related image URLs to the search result
                new DownloadURL().execute(inputSearchBar.getText().toString());
            }
        });
    }

    //check if there's any important field(s) left empty
    protected boolean hasEmptyField()
    {
        boolean hasAPhone = false;
        boolean hasAnEmail = false;

        for(int i = 0; i < inputPhones.length; i++)
        {
            if(!inputPhones[i].getText().toString().isEmpty())
            {
                hasAPhone = true;
            }
        }

        for(int i = 0; i < inputEmails.length; i++)
        {
            if(!inputEmails[i].getText().toString().isEmpty())
            {
                hasAnEmail = true;
            }
        }

        //if the image view isn't null
        if(profilePic.getDrawable() != null)
        {
            //set the selected image to this view's encoded image
            Bitmap image = ((BitmapDrawable)profilePic.getDrawable()).getBitmap();
            selectedImageStr = DecodeEncodeImage.encodeImage(image);
        }

        return (inputFirstName.getText().toString().isEmpty()
                || inputLastName.getText().toString().isEmpty()
                || !hasAPhone || !hasAnEmail || selectedImageStr == null);
    }

    //invoke other app that allows user to pick a contact from the contact list.
    private void pickContactClicked()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setData(ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    //invoke other app that can take a photo and return that photo.
    private void takePhoto()
    {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_TAKE_PHOTO);
    }

    //invoke other app that can pick a photo from the phone's external storage
    private void pickPhoto()
    {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_PICK_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_TAKE_PHOTO)
        {
            Bitmap image = (Bitmap) data.getExtras().get("data");
            studentVM.setImageLive(image);
        }
        else if(resultCode == Activity.RESULT_OK && requestCode == REQUEST_PICK_PHOTO)
        {
            Uri imageUri = data.getData();
            try
            {
                Bitmap image = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                studentVM.setImageLive(image);
            }
            catch (IOException e)
            {
                Toast.makeText(StudentActivity.this, "There's an error occur in locating the image's location", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CONTACT) {
            //retrieve the chosen contact's ID.
            Uri contactUri = data.getData();

            String[] queryFields = new String[]{
                    ContactsContract.Contacts._ID,
            };

            //query wanted attributes from the database
            Cursor c = getContentResolver().query(
                    contactUri, queryFields, null, null, null);

            try
            {
                if(c.getCount() > 0)
                {
                    c.moveToFirst();
                    id = c.getString(0);
                }
            }
            finally
            {
                c.close();
            }

            //asking for permission to read the contact detail at run-time if first-time access.
            if(ContextCompat.checkSelfPermission(StudentActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(StudentActivity.this, new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_READ_CONTACT_PERMISSION);
            }
            else
            {
                //retrieve name(s)
                contactUri = ContactsContract.Data.CONTENT_URI;
                queryFields = new String[]{
                        ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                        ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME
                };
                String whereClause = ContactsContract.Data.RAW_CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?";
                String[] whereValues = new String[]{id, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE};

                c = getContentResolver().query(
                        contactUri, queryFields, whereClause, whereValues, null);

                try
                {
                    if(c.getCount() > 0)
                    {
                        c.moveToFirst();
                        inputFirstName.setText(c.getString(0));
                        inputLastName.setText(c.getString(1));
                    }
                }
                finally
                {
                    c.close();
                }

                //get the phone numbers
                contactUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                queryFields = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                whereClause = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
                whereValues = new String[]{id};

                c = getContentResolver().query(
                        contactUri, queryFields, whereClause, whereValues, null);

                try
                {
                    if(c.getCount() > 0)
                    {
                        c.moveToFirst();
                        for(int i = 0; i < MAX_ENTRY; i++)
                        {
                            layoutPhones[i].setVisibility(View.VISIBLE);
                            inputPhones[i].setText(c.getString(0));
                            if(!c.moveToNext())
                            {
                                //terminate loop early as soon as no more phone entries retrieved
                                break;
                            }
                        }
                    }
                }
                finally
                {
                    c.close();
                }

                //get email addresses.
                contactUri = ContactsContract.CommonDataKinds.Email.CONTENT_URI;
                queryFields = new String[]{ContactsContract.CommonDataKinds.Email.ADDRESS};
                whereClause = ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?";
                whereValues = new String[]{id};

                c = getContentResolver().query(
                        contactUri, queryFields, whereClause, whereValues, null);

                try
                {
                    if(c.getCount() > 0)
                    {
                        c.moveToFirst();
                        for(int i = 0; i < MAX_ENTRY; i++)
                        {
                            layoutEmails[i].setVisibility(View.VISIBLE);
                            inputEmails[i].setText(c.getString(0));
                            if(!c.moveToNext())
                            {
                                //terminate loop early as soon as no more phone entries retrieved
                                break;
                            }
                        }
                    }
                }
                finally {
                    c.close();
                }
            }
        }
    }

    //asynchronously download set of related image URLs on the background
    private class DownloadURL extends AsyncTask<String, String, Void>
    {
        //Declare vars
        private HttpsURLConnection conn;

        @Override
        protected Void doInBackground(String... values) {
            getImageUrls(values[0]);
            return null;
        }

        @Override
        protected void onPreExecute()
        {
            listUrlStr = new LinkedList<>();
        }

        @Override
        protected void onProgressUpdate(String... messages) {
            if(messages[0].equals("Add"))
            {
                listUrlStr.add(messages[1]);
            }
            else {
                Toast.makeText(StudentActivity.this, messages[0], Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onPostExecute(Void unused)
        {
            studentVM.getUrlList().postValue(listUrlStr);
        }

        private void getImageUrls(String searchKey)
        {
            try
            {
                // create the URL version of the web link and establish a connection with the web server
                String urlString = Uri.parse("https://pixabay.com/api/").buildUpon()
                        .appendQueryParameter("key", API_KEY)
                        .appendQueryParameter("q", searchKey)
                        .build().toString();

                URL url = new URL(urlString);
                conn = (HttpsURLConnection) url.openConnection();

                // check if the connection is established successfully
                if(conn == null)
                {
                    publishProgress("Check the internet");
                }
                else if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
                {
                    Log.d("Check internet", "GOT HERE");
                    publishProgress("Problem with connection, responding with code " + conn.getResponseCode());
                }
                else {
                    //download the set of related image URLs
                    String relatedURLs = downloadToString(conn);
                    //get and store each image URL from JSON object
                    JSONObject jBase = new JSONObject(relatedURLs);
                    JSONArray jHits = jBase.getJSONArray("hits");
                    if(jHits.length() > 0)
                    {
                        //add each image url to the list of URLs up to 50 images
                        for(int i = 0; i < (Math.min(jHits.length(), MAX_IMAGES)); i++)
                        {
                            JSONObject jHitsItem = jHits.getJSONObject(i);
                            publishProgress("Add", jHitsItem.getString("largeImageURL"));
                        }
                    }
                }
            }
            catch(JSONException f)
            {
                publishProgress("Error in converting JSON object while downloading");
            }
            catch (IOException e)
            {
                publishProgress("Error in downloading the data");
            }
            finally
            {
                if(conn != null)
                {
                    conn.disconnect();
                }
            }
        }

        //download the set of related image URLs
        private String downloadToString(HttpURLConnection conn) throws IOException
        {
            String data = null;
            InputStream inputStream = conn.getInputStream();
            byte[] byteData = getByteArrayFromInputStream(inputStream);
            data = new String(byteData, StandardCharsets.UTF_8);

            return data;
        }

        //read URL from the input stream to bytes
        private byte[] getByteArrayFromInputStream(InputStream inputStream) throws IOException
        {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[4096];

            // download the data
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            return buffer.toByteArray();
        }
    }

    //set up the UI views
    protected void updateUI()
    {
        for(int i = 0; i < MAX_ENTRY; i++)
        {
            layoutPhones[i].setVisibility(arrVisPhones[i]);
            layoutEmails[i].setVisibility(arrVisEmails[i]);
        }
        searchBar.setVisibility(searchVis);
        onlineFrame.setVisibility(onlineVis);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //save all the current state's input data for current activity reload
        //for phone & email
        for(int i = 0; i < MAX_ENTRY; i++)
        {
            arrVisPhones[i] = layoutPhones[i].getVisibility();
            arrVisEmails[i] = layoutEmails[i].getVisibility();
        }
        outState.putSerializable("PHONE_VISIBILITIES", arrVisPhones);
        outState.putSerializable("EMAIL_VISIBILITIES", arrVisEmails);
        //for online frame
        outState.putInt("SEARCH_VISIBILITY", searchBar.getVisibility());
        outState.putInt("ONLINE_VISIBILITY", onlineFrame.getVisibility());
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        //repopulate the activity's views from saved bundle
        //for phone & email views
        arrVisPhones = (int[]) savedInstanceState.getSerializable("PHONE_VISIBILITIES");
        arrVisEmails = (int[]) savedInstanceState.getSerializable("EMAIL_VISIBILITIES");
        //for online image frame
        searchVis = savedInstanceState.getInt("SEARCH_VISIBILITY");
        //if the image has been downloaded
        onlineVis = savedInstanceState.getInt("ONLINE_VISIBILITY");
        //set to view
        updateUI();
    }
}