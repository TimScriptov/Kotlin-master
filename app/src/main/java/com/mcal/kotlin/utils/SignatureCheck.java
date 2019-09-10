package com.mcal.kotlin.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

public class SignatureCheck {

    static {
        System.loadLibrary("startandroid");
    }

    // проверяет подпись приложения
    public static boolean verifySignature(Context c) {
        return Utils.reverseString(getSignature(c)).endsWith(".RSA");
    }

    public static String getSignature(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

            Signature[] signatures = packageInfo.signatures;

            return signatures[0].toCharsString();

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    public native int getSomeValue();

    public native void init(Context ctx);
}
