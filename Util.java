package com.example.augus.comanda;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class Util {

    public static String getStringByEditText(EditText edit) {
        if (edit == null) {
            return "";
        }
        return getString(edit.getText());
    }

    public static String getString(Object obj) {
        if (obj != null) {
            String text = obj.toString();
            if (text != null) {
                return text;
            }
        }
        return "";
    }

    public static void showAlert(Context context, String message ) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static <T extends Object> T getValueByExtra(Intent intent, String key, Class<T> type) {
        Bundle extras = intent.getExtras();

        if (extras == null) {
            return null;
        }

        switch (Types.parseType(type.getCanonicalName())) {
            case INTEGER:
                Integer value = extras.getInt(key);
                if (value == null) {
                    return null;
                }
                return (T) value;
            case BYTEARRAY:
                return (T) extras.getByteArray(key);
            case STRING:
            default:
                return (T) extras.get(key).toString();
        }
    }

    private enum Types {
        INTEGER(Integer.class.getCanonicalName()),
        BYTEARRAY(Byte[].class.getCanonicalName()),
        STRING(String.class.getCanonicalName());

        String canonicalName;

        Types(String canonicalName) {
            this.canonicalName = canonicalName;
        }

        public static Types parseType(String canonicalName) {
            for (Types typeEnum : Types.values()) {
                if (typeEnum.canonicalName.equals(canonicalName)) {
                    return typeEnum;
                }
            }
            return null;
        }
    }

    static class Request {
        static class Action {
            public static final int CAMERA = 1;
        }
    }

    static class Permission {
        public static final int CAMERA = 1;

        private static boolean allow(int[] grantResults) {
            return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
        }

        public static boolean hasPermission(String permissionId, int request, Activity activity) {
            int permission = ContextCompat.checkSelfPermission(activity, permissionId);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{permissionId}, request);
                return false;
            }
            return true;
        }
    }
    public static class BitmapUtil {

        public static byte[] getBytes(Bitmap bitmap) {
            if (bitmap == null) {
                return null;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
            return stream.toByteArray();
        }

        public static Bitmap getImage(byte[] image) {
            if (image == null) {
                return null;
            }
            return BitmapFactory.decodeByteArray(image, 0, image.length);
        }
    }
}
