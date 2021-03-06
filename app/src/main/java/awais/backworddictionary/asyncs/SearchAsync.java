package awais.backworddictionary.asyncs;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import awais.backworddictionary.BuildConfig;
import awais.backworddictionary.custom.WordItem;
import awais.backworddictionary.helpers.Utils;
import awais.backworddictionary.interfaces.MainCheck;

public class SearchAsync extends AsyncTask<String, Void, ArrayList<WordItem>> {
    private MainCheck mainCheck;

    public SearchAsync(final MainCheck mainCheck) {
        this.mainCheck = mainCheck;
    }

    @Override
    protected ArrayList<WordItem> doInBackground(final String... params) {
        String query;
        try {
            query = URLEncoder.encode(params[0], "UTF-8");
        } catch (Exception e) {
            query = params[0].replaceAll("\\s", "+").replaceAll(" ", "+")
                    .replaceAll("#", "%23").replaceAll("@", "%40")
                    .replaceAll("&", "%26");
        }

        ArrayList<WordItem> arrayList = new ArrayList<>(0);

        try {
            final String response = Utils.getResponse("http://api.datamuse.com/sug?s=" + query);

            if (response != null) {
                final JSONArray jsonArray = new JSONArray(response);
                arrayList = new ArrayList<>(jsonArray.length());

                for (int i = 0; i < jsonArray.length(); ++i) {
                    final JSONObject jsonObject = jsonArray.getJSONObject(i);
                    arrayList.add(new WordItem(jsonObject.getString("word"), 0, null, null));
                }
            }
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.e("AWAISKING_APP", "", e);
        }

        return arrayList;
    }

    @Override
    protected void onCancelled() {
        mainCheck.afterSearch(null);
    }

    @Override
    protected void onCancelled(final ArrayList<WordItem> wordItems) {
        mainCheck = null;
    }

    @Override
    protected void onPostExecute(final ArrayList<WordItem> result) {
        if (mainCheck != null) mainCheck.afterSearch(result);
    }
}
