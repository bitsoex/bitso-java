package com.bitso.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

public class Util {

    public static List<JSONObject> readFileToJson(String nFile) {
        List<JSONObject> list = new ArrayList<JSONObject>();
        BufferedReader br = null;
        try {
            String sCurrentLine;
            File file = new File(nFile);
            br = new BufferedReader(new FileReader(file));
            while ((sCurrentLine = br.readLine()) != null) {
                JSONObject jsonObject = new JSONObject(sCurrentLine);
                list.add(jsonObject);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (br != null) br.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return list;
    }
}
