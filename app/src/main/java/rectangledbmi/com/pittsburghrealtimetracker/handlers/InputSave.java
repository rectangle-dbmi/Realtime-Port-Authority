package rectangledbmi.com.pittsburghrealtimetracker.handlers;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import rectangledbmi.com.pittsburghrealtimetracker.patapi.PortAuthorityAPI;

/**
 * Inspired by http://stackoverflow.com/questions/8986376/how-to-download-xml-file-from-server-and-save-it-in-sd-card
 *
 * Created by epicstar on 1/12/15.
 */
public class InputSave {
    private Context context;

    public InputSave(Context context) {
        this.context = context;
    }

    public void saveFile(String selectedRoute) {
        try {
            File file = new File(context.getFilesDir(), "/lineinfo/" + selectedRoute + ".xml");
            if(!file.exists()) {
                URL url = PortAuthorityAPI.getPatterns(selectedRoute);
                URLConnection conexion = url.openConnection();
                conexion.connect();
                int lengthOfFile = conexion.getContentLength();
                InputStream is = url.openStream();

                FileOutputStream fos = new FileOutputStream(file);
                byte data[] = new byte[1024];
                int count = 0;
                long total = 0;
                int progress = 0;
                while ((count = is.read(data)) != -1) {
                    total += count;
                    int progress_temp = (int) total * 100 / lengthOfFile;
                    if (progress_temp % 10 == 0 && progress != progress_temp) {
                        progress = progress_temp;
                    }
                    fos.write(data, 0, count);
                }
                is.close();
                fos.close();
                Log.d("save_file", selectedRoute + " attempted file saved...");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
