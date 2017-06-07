package com.bitso.helpers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bitso.BitsoBook;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

public class Helpers {
    private static final String PATH = "src/test/java/JSONFiles/";
    public static final String dateTimeFormatterZOffset = ("yyyy-MM-dd'T'HH:mm:ssZZZ");
    public static final String dateTimeFormatterXOffset = ("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static DatatypeFactory dtf;

    static {
        try {
            dtf = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException ex) {
            System.out.println("FATAL: Cannot instantiate DatatypeFactory");
        }
    }

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

    public static final String fieldPrinter(Object object, Class<?> genericType) {
        StringBuilder sb = new StringBuilder();
        sb.append("==============");
        Method[] methods = genericType.getDeclaredMethods();
        for (Method method : methods) {
            String methodName = method.getName();
            if (methodName.startsWith("get")) {
                try {
                    Object methodExecutionResult = method.invoke(object);
                    sb.append('\n');
                    sb.append(methodName);
                    sb.append(": ");
                    sb.append(methodExecutionResult);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
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

    public static JSONArray parseJsonArray(String json) {
        if (json == null) {
            return null;
        }
        try {
            return new JSONArray(json);
        } catch (JSONException e) {
            System.err.println("Unable to parse json array: " + json);
            e.printStackTrace();
        }
        return null;
    }

    public static int getInt(JSONObject o, String key) {
        if (o.has(key)) {
            return o.getInt(key);
        } else {
            System.err.println("No " + key + ": " + o);
            Helpers.printStackTrace();
        }
        return -1;
    }

    public static String getString(JSONObject o, String key) {
        if (o.has(key)) {
            return o.getString(key);
        } else {
            System.err.println("No " + key + ": " + o);
            Helpers.printStackTrace();
        }
        return null;
    }

    public static BigDecimal getBD(JSONObject o, String key) {
        if (o.has(key)) {
            String value = o.isNull(key) ? "null" : o.getString(key);
            value = (value.equals("null") || value.length() == 0) ? "0" : value.trim();
            return new BigDecimal(value);
        } else {
            System.err.println("No " + key + ": " + o);
            Helpers.printStackTrace();
        }
        return null;
    }

    public static Integer getInteger(JSONObject o, String key) {
        if (o.has(key)) {
            return o.getInt(key);
        } else {
            System.err.println("No " + key + ": " + o);
            Helpers.printStackTrace();
        }
        return null;
    }

    public static Date getZonedDatetime(JSONObject o, String key) {
        if (o.has(key)) {
            final String date = o.getString(key);
            try {
                return new SimpleDateFormat(dateTimeFormatterZOffset).parse(date);
            } catch (ParseException e) {
                try {
                    return new SimpleDateFormat(dateTimeFormatterXOffset).parse(date);
                } catch (ParseException e2) {
                    try {
                        return dtf.newXMLGregorianCalendar(date).toGregorianCalendar().getTime();
                    } catch (IllegalArgumentException e3) {
                        Helpers.printStackTrace();
                    }
                }
            }
        } else {
            System.err.println("No " + key + ": " + o);
            Helpers.printStackTrace();
        }
        return null;
    }

    public static String[] parseJSONArray(JSONArray arrray) {
        int totalElements = arrray.length();
        String[] elements = new String[totalElements];
        for (int i = 0; i < totalElements; i++) {
            elements[i] = arrray.getString(i);
        }
        return elements;
    }

    public static JSONObject getJSONFromFile(String fileName) {
        String jsonString = getJSONString(fileName);
        if (jsonString == null) {
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

    public static BitsoBook getBook(String book) {
        try {
            return BitsoBook.valueOf(book.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }
}
