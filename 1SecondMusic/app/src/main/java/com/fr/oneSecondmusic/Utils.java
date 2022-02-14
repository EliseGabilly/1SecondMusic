package com.fr.oneSecondmusic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<String> getListOfItemFromJSONArray(JSONArray array, String item){
        List<String> res = new ArrayList<>();
        for(int n = 0; n < array.length(); n++) {
            try {
                JSONObject object = array.getJSONObject(n);
                res.add(object.getString(item));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    public static void setImgViewFromURL(ImageView imageView, String url){
        new DownLoadImageTask(imageView).execute(url);
    }

    private static class DownLoadImageTask extends AsyncTask<String,Void,Bitmap> {
        ImageView imageView;

        public DownLoadImageTask(ImageView imageView){
            this.imageView = imageView;
        }

        /**
         * Override this method to perform a computation on a background thread.
         * @param urls Params... params
         * @return bitmap
         */
        protected Bitmap doInBackground(String...urls){
            String urlOfImage = urls[0];
            Bitmap logo = null;
            if(urlOfImage==null){
                return null;
            }
            try{
                InputStream is = new URL(urlOfImage).openStream();
                logo = BitmapFactory.decodeStream(is);
            }catch(Exception e){ // Catch the download exception
                e.printStackTrace();
            }
            return logo;
        }

        /*
            onPostExecute(Result result)
                Runs on the UI thread after doInBackground(Params...).
         */
        protected void onPostExecute(Bitmap result){
            imageView.setImageBitmap(result);
        }
    }
}
