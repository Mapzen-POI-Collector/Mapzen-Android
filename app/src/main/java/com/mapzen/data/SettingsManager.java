/*Copyright (c) 2011-2012, Cloudmade
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies,
either expressed or implied, of the FreeBSD Project.
*/

package com.mapzen.data;

import android.content.SharedPreferences;

/**
 * Wrapper class for SharedPreferences which allows to put and get data in 1 call.
 * @author vgrigoruk
 *
 */
public class SettingsManager {

    private static final String TAG = SettingsManager.class.getSimpleName();

    private static final SettingsManager _instance = new SettingsManager();
    SharedPreferences mSettings;

    private SettingsManager() {};

    public static SettingsManager getInstance() {
        return _instance;
    }

    public void init(SharedPreferences settings) {
        mSettings = settings;
    }

    public String getString(String key, String defValue) {
        return mSettings.getString(key, defValue);
    }

    public Integer getInt(String key, Integer defValue) {
        return mSettings.getInt(key, defValue);
    }

    public Long getLong(String key, Long defValue) {
        return mSettings.getLong(key, defValue);
    }

    public Boolean getBoolean(String key, Boolean defValue) {
        return mSettings.getBoolean(key, defValue);
    }

    public Float getFloat(String key, Float defValue) {
        return mSettings.getFloat(key, defValue);
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = mSettings.edit();
        if (value != null)
            editor.putString(key, value);
        else
            editor.remove(key);
        editor.commit();
    }

    public void putLong(String key, Long value) {
        SharedPreferences.Editor editor = mSettings.edit();
        if (value != null)
            editor.putLong(key, value);
        else
            editor.remove(key);
        editor.commit();
    }

    public void putBoolean(String key, Boolean value) {
        SharedPreferences.Editor editor = mSettings.edit();
        if (value != null)
            editor.putBoolean(key, value);
        else
            editor.remove(key);
        editor.commit();
    }

    public void putInt(String key, Integer value) {
        SharedPreferences.Editor editor = mSettings.edit();
        if (value != null)
            editor.putInt(key, value);
        else
            editor.remove(key);
        editor.commit();
    }

    public void putFloat(String key, Float value) {
        SharedPreferences.Editor editor = mSettings.edit();
        if (value != null)
            editor.putFloat(key, value);
        else
            editor.remove(key);
        editor.commit();
    }
}
