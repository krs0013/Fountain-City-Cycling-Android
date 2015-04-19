package edu.auburn.ppl.cyclecolumbus;

import android.content.Context;

import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by kennystreit on 4/14/15.
 */
public class JsonStorage {

    private String mJson;
    private Context mContext;

    public JsonStorage(Context context) {
        mJson = "";
        this.mContext = context;
    }

    public JsonStorage(String json, Context context) {
        this.mJson = json;
        this.mContext = context;
    }

    /******************************************************************************************
     * Reads the json array into string form
     * Used to see if the user has agreed or disagreed to contest agreement
     ******************************************************************************************
     * @return The full string of the json array
     * @throws IOException
     ******************************************************************************************/
    public String readJSON() throws IOException {
        StringBuffer sb = new StringBuffer();
        FileInputStream fis = mContext.openFileInput("contest_agreement.txt");

        byte[] inputBuffer = new byte[fis.available()];
        fis.read(inputBuffer);
        String contents = new String(inputBuffer);
        contents = contents.trim();

        String[] split = contents.split("##;");
        for (String s : split) {
            if (!s.equals(""))
                sb.append(s);
        }

        fis.close();
        return sb.toString();
    }

    /******************************************************************************************
     * This will write the actual object to the text file.
     * Used to save the agreement, whether the user agreed or disagreed.
     ******************************************************************************************
     * @param outer It's the outermost json object that encompasses all tabs
     ******************************************************************************************/
    public void writeJSON(JSONObject outer) {

        FileOutputStream fos;
        try {
            fos = mContext.openFileOutput("contest_agreement.txt", Context.MODE_PRIVATE);

            fos.write(outer.toString().getBytes());

            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
