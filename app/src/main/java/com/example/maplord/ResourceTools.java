package com.example.maplord;

import android.content.Context;

import androidx.fragment.app.Fragment;

public final class ResourceTools {
    private ResourceTools() {
    }

    public static double getDouble(Fragment fragment, int resourceId) {
        Context context = fragment.requireContext();
        String resourceValue = context.getString(resourceId);
        return Double.parseDouble(resourceValue);
    }
}
