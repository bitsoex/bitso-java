package com.bitso;

import org.json.JSONObject;

import com.bitso.helpers.Helpers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class BitsoMockJSON {
    private static final String PATH = "src/test/java/JSONFiles/";

    public static JSONObject getJSONFromFile(String fileName) {
        String jsonString = getJSONString(fileName);
        if(jsonString == null){
            return null;
        }
        return Helpers.parseJson(jsonString);
    }

    private static String getJSONString(String fileName) {
        BufferedReader br = null;
        String line = "";
        StringBuffer sb = new StringBuffer();
        try {
            FileReader fr = new FileReader(PATH + fileName);
            br = new BufferedReader(fr);
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            line = sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
            line = null;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return line;
    }
}