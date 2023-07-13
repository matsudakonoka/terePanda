package com.cnpc.epai.core.workscene.commom;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FileReader {

    public static List<String> readFromTXT(String fileName) {
        List<String> result = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try (InputStream in = classLoader.getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String str = null;
            while ((str = reader.readLine()) != null) {
                str = str.trim();
                if (str.startsWith("acti")) {
                    result.add(str);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
