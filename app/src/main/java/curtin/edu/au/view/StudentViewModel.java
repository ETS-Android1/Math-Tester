/**
 * PURPOSE: view model class to save student activity UI data
 * AUTHOR: Minh Vu
 * LAST MODIFIED DATE: 20/11/2021
 */
package curtin.edu.au.view;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class StudentViewModel extends ViewModel
{
    //Class-field:
    private MutableLiveData<Bitmap> imageLive;
    private MutableLiveData<Integer> selectedRvPost;
    private boolean show;
    private List<Bitmap> imgRvList;
    private MutableLiveData<List<String>> urlList;

    //Accessor
    public MutableLiveData<Bitmap> getImageLive()
    {
        if(imageLive == null)
        {
            imageLive = new MutableLiveData<>();
        }
        return imageLive;
    }

    public MutableLiveData<Integer> getSelectedRvPost()
    {
        if(selectedRvPost == null)
        {
            selectedRvPost = new MutableLiveData<>(-1);
        }
        return selectedRvPost;
    }

    public boolean getShow()
    {
        return show;
    }

    public List<Bitmap> getImgRvList()
    {
        if(imgRvList == null)
        {
            imgRvList = new ArrayList<>();
        }
        return imgRvList;
    }

    public MutableLiveData<List<String>> getUrlList()
    {
        if(urlList == null)
        {
            urlList = new MutableLiveData<>();
        }
        return urlList;
    }

    //Mutator
    public void setImageLive(Bitmap image)
    {
        if(image == null)
        {
            setShow(false);
        }
        else
        {
            setShow(true);
        }
        imageLive.setValue(image);
    }

    public void setSelectedRvPost(int position)
    {
        selectedRvPost.setValue(position);
    }

    public void setShow(boolean show)
    {
        this.show = show;
    }

    public void setImgRvList(List<Bitmap> imgRvList)
    {
        this.imgRvList = imgRvList;
    }

    public void setUrlList(List<String> urlList)
    {
        this.urlList.setValue(urlList);
    }

    public void addToRvList(Bitmap image)
    {
        imgRvList.add(image);
    }
}
