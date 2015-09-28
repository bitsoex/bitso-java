package com.bitso.helpers;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

public class Helpers {

    private static final List<Field> getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));
        if (type.getSuperclass() != null) {
            fields = getAllFields(fields, type.getSuperclass());
        }
        return fields;
    }

    public static final String fieldPrinter(Object obj) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============");
        List<Field> fields = getAllFields(new ArrayList<Field>(), obj.getClass());
        for (Field f : fields) {
            try {
                Object o = f.get(obj);
                sb.append('\n');
                sb.append(f.getName());
                sb.append(": ");
                sb.append(o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        sb.append("\n==============\n");
        return sb.toString();
    }

    public static final void printStackTrace(PrintStream out) {
        StringBuilder sb = new StringBuilder();
        sb.append("Printing Stack Trace\n");
        for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
            sb.append('\t');
            sb.append(ste);
            sb.append('\n');
        }
        out.print(sb);
    }

    public static final void printStackTrace() {
        printStackTrace(System.err);
    }

    public static JSONObject parseJson(String json) {
        if (json == null) {
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (JSONException e) {
            System.err.println("Unable to parse json: " + json);
            e.printStackTrace();
        }
        return null;
    }
}
