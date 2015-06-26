package dragon.comm;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by lin.cheng on 6/17/15.
 */
public class Utils {
    public static String readRawContentFromFile(String path) throws Exception {
        FileReader fr = null;
        BufferedReader rdr = null;
        try {
            String ret = "";
            fr = new FileReader(path);
            rdr = new BufferedReader(fr);
            String line;
            while ((line = rdr.readLine()) != null) {
                ret += line + "\n";
            }
            return ret;
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (Exception e) {
                }
                fr = null;
            }
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (Exception e) {
                }
                rdr = null;
            }
        }
    }

    public static String[] parseHeaderAndData(String src, final List<String[]> data, boolean noHeader){
        if(StringUtils.isBlank(src)) return null;
        if(data == null){
            Logger.getLogger(Utils.class.getName()).log(Level.WARNING, "Invalid call, data parameter cannot be null.");
            return null;
        }

        InputStream in = null;
        Reader reader = null;
        CSVReader csvReader = null;
        String[] headers = null;

        try {
            byte[] bytes = src.getBytes("UTF-8");
            in = new ByteArrayInputStream(bytes);
            reader = new InputStreamReader(in, "UTF-8");

            csvReader = new CSVReader(reader);
            String[] lineItems;
            int cnt = 0;
            while ((lineItems = csvReader.readNext()) != null) {
                if(noHeader){
                    if(headers == null){
                        headers = new String[lineItems.length];
                        for(int i=0; i<lineItems.length; i++){
                            headers[i] = String.valueOf(i);
                        }
                    }
                    data.add(lineItems);
                } else{
                    if (lineItems.length < 1 || (lineItems.length == 1 && StringUtils.isEmpty(lineItems[0]))) {
                        if(cnt == 0){
                            Logger.getLogger(Utils.class.getName()).log(Level.WARNING, "Invalid header(1st line).");
                            return null;
                        } else{
                            continue;
                        }
                    }
                    if(cnt == 0){//headers
                        headers = lineItems;
                    } else if (headers != null && lineItems.length == headers.length) {//data
                        data.add(lineItems);
                    } else{
                        Logger.getLogger(Utils.class.getName()).log(Level.WARNING, "Column number not match in line {0}", cnt);
                    }
                }
                cnt ++;
            }
            return headers;
        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            if (in != null) {
                try {
                    in.close();
                    in = null;
                } catch (IOException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                    reader = null;
                } catch (IOException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            if (csvReader != null) {
                try {
                    csvReader.close();
                    csvReader = null;
                } catch (IOException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
