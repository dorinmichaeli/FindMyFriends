package com.example.findmyfriends.tools;

import android.content.Context;

public final class ResourceTools {
    private ResourceTools() {
    }

    public static double getDouble(Context context, int resourceId) {
        String resourceValue = context.getString(resourceId);
        return Double.parseDouble(resourceValue);
    }
}
