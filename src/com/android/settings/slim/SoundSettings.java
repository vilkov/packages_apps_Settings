/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.slim;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.SystemProperties;

import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.temasek.SeekBarPreference;
import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.logging.MetricsLogger;

public class SoundSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "SoundSettings";

    private static final int DLG_SAFE_HEADSET_VOLUME = 0;
    private static final int DLG_CAMERA_SOUND = 1;

    private static final String KEY_SAFE_HEADSET_VOLUME = "safe_headset_volume";
    private static final String PREF_LESS_NOTIFICATION_SOUNDS = "less_notification_sounds";
    private static final String KEY_CAMERA_SOUNDS = "camera_sounds";
    private static final String PROP_CAMERA_SOUND = "persist.sys.camera-sound";
    private static final String PREF_TRANSPARENT_VOLUME_DIALOG = "transparent_volume_dialog";
    private static final String PREF_VOLUME_DIALOG_STROKE = "volume_dialog_stroke";
    private static final String PREF_VOLUME_DIALOG_STROKE_COLOR = "volume_dialog_stroke_color";
    private static final String PREF_VOLUME_DIALOG_STROKE_THICKNESS = "volume_dialog_stroke_thickness";
    private static final String PREF_VOLUME_DIALOG_CORNER_RADIUS = "volume_dialog_corner_radius";
    private static final String PREF_VOLUME_DIALOG_STROKE_DASH_WIDTH = "volume_dialog_dash_width";
    private static final String PREF_VOLUME_DIALOG_STROKE_DASH_GAP = "volume_dialog_dash_gap";

    private SwitchPreference mSafeHeadsetVolume;
    private ListPreference mAnnoyingNotifications;
    private SwitchPreference mCameraSounds;
    private SeekBarPreference mVolumeDialogAlpha;

    private ListPreference mVolumeDialogStroke;
    private ColorPickerPreference mVolumeDialogStrokeColor;
    private SeekBarPreference mVolumeDialogStrokeThickness;
    private SeekBarPreference mVolumeDialogCornerRadius;
    private SeekBarPreferenceCham mVolumeDialogDashWidth;
    private SeekBarPreferenceCham mVolumeDialogDashGap;

    static final int DEFAULT_VOLUME_DIALOG_STROKE_COLOR = 0xFF80CBC4;

    @Override
    protected int getMetricsCategory() {
        return MetricsLogger.APPLICATION;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.slim_sound_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mSafeHeadsetVolume = (SwitchPreference) findPreference(KEY_SAFE_HEADSET_VOLUME);
        mSafeHeadsetVolume.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.SAFE_HEADSET_VOLUME, 1) != 0);
        mSafeHeadsetVolume.setOnPreferenceChangeListener(this);

        mAnnoyingNotifications = (ListPreference) findPreference(PREF_LESS_NOTIFICATION_SOUNDS);
        int notificationThreshold = Settings.System.getInt(getContentResolver(),
                Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD,
                0);
        mAnnoyingNotifications.setValue(Integer.toString(notificationThreshold));
        mAnnoyingNotifications.setOnPreferenceChangeListener(this);

        mCameraSounds = (SwitchPreference) findPreference(KEY_CAMERA_SOUNDS);
        mCameraSounds.setChecked(SystemProperties.getBoolean(PROP_CAMERA_SOUND, true));
        mCameraSounds.setOnPreferenceChangeListener(this);

        // Volume dialog alpha
        mVolumeDialogAlpha =
                (SeekBarPreference) prefSet.findPreference(PREF_TRANSPARENT_VOLUME_DIALOG);
        int volumeDialogAlpha = Settings.System.getInt(resolver,
                Settings.System.TRANSPARENT_VOLUME_DIALOG, 255);
        mVolumeDialogAlpha.setValue(volumeDialogAlpha / 1);
        mVolumeDialogAlpha.setOnPreferenceChangeListener(this);

        // Volume dialog stroke
        mVolumeDialogStroke =
                (ListPreference) findPreference(PREF_VOLUME_DIALOG_STROKE);
        int volumeDialogStroke = Settings.System.getIntForUser(resolver,
                Settings.System.VOLUME_DIALOG_STROKE, 0,
                UserHandle.USER_CURRENT);
	mVolumeDialogStroke.setValue(String.valueOf(volumeDialogStroke));
	mVolumeDialogStroke.setSummary(mVolumeDialogStroke.getEntry());
	mVolumeDialogStroke.setOnPreferenceChangeListener(this);

        // Volume dialog stroke color
        mVolumeDialogStrokeColor =
                (ColorPickerPreference) findPreference(PREF_VOLUME_DIALOG_STROKE_COLOR);
        mVolumeDialogStrokeColor.setOnPreferenceChangeListener(this);
        int intColor = Settings.System.getInt(resolver,
                Settings.System.VOLUME_DIALOG_STROKE_COLOR, DEFAULT_VOLUME_DIALOG_STROKE_COLOR);
        String hexColor = String.format("#%08x", (0xFF80CBC4 & intColor));
        mVolumeDialogStrokeColor.setSummary(hexColor);
        mVolumeDialogStrokeColor.setNewPreviewColor(intColor);

        // Volume dialog stroke thickness
        mVolumeDialogStrokeThickness =
                (SeekBarPreference) findPreference(PREF_VOLUME_DIALOG_STROKE_THICKNESS);
        int volumeDialogStrokeThickness = Settings.System.getInt(resolver,
                Settings.System.VOLUME_DIALOG_STROKE_THICKNESS, 4);
        mVolumeDialogStrokeThickness.setValue(volumeDialogStrokeThickness / 1);
        mVolumeDialogStrokeThickness.setOnPreferenceChangeListener(this);

        // Volume dialog corner radius
        mVolumeDialogCornerRadius =
                (SeekBarPreference) findPreference(PREF_VOLUME_DIALOG_CORNER_RADIUS);
        int volumeDialogCornerRadius = Settings.System.getInt(resolver,
                Settings.System.VOLUME_DIALOG_CORNER_RADIUS, 10);
        mVolumeDialogCornerRadius.setValue(volumeDialogCornerRadius / 1);
        mVolumeDialogCornerRadius.setOnPreferenceChangeListener(this);

        // Volume dialog dash width
        mVolumeDialogDashWidth =
                (SeekBarPreference) findPreference(PREF_VOLUME_DIALOG_STROKE_DASH_WIDTH);
        int volumeDialogDashWidth = Settings.System.getInt(resolver,
                Settings.System.VOLUME_DIALOG_STROKE_DASH_WIDTH, 0);
        if (volumeDialogDashWidth != 0) {
            mVolumeDialogDashWidth.setValue(volumeDialogDashWidth / 1);
        } else {
            mVolumeDialogDashWidth.setValue(0);
        }
        mVolumeDialogDashWidth.setOnPreferenceChangeListener(this);

        // Volume dialog dash gap
        mVolumeDialogDashGap =
                (SeekBarPreference) findPreference(PREF_VOLUME_DIALOG_STROKE_DASH_GAP);
        int volumeDialogDashGap = Settings.System.getInt(resolver,
                Settings.System.VOLUME_DIALOG_STROKE_DASH_GAP, 10);
        mVolumeDialogDashGap.setValue(volumeDialogDashGap / 1);
        mVolumeDialogDashGap.setOnPreferenceChangeListener(this);

        VolumeDialogSettingsDisabler(volumeDialogStroke);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SAFE_HEADSET_VOLUME.equals(key)) {
            if ((Boolean) objValue) {
                Settings.System.putInt(getContentResolver(),
                        Settings.System.SAFE_HEADSET_VOLUME, 1);
            } else {
                showDialogInner(DLG_SAFE_HEADSET_VOLUME);
            }
        }
        if (PREF_LESS_NOTIFICATION_SOUNDS.equals(key)) {
            final int val = Integer.valueOf((String) objValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.MUTE_ANNOYING_NOTIFICATIONS_THRESHOLD, val);
        }
        if (KEY_CAMERA_SOUNDS.equals(key)) {
           if ((Boolean) objValue) {
               SystemProperties.set(PROP_CAMERA_SOUND, "1");
           } else {
               showDialogInner(DLG_CAMERA_SOUND);
           }
        }
        if (preference == mVolumeDialogAlpha) {
           int alpha = (Integer) objValue;
           Settings.System.putInt(getContentResolver(),
                   Settings.System.TRANSPARENT_VOLUME_DIALOG, alpha * 1);
           return true;
        } else if (preference == mVolumeDialogStroke) {
           int volumeDialogStroke = Integer.parseInt((String) objValue);
           int index = mVolumeDialogStroke.findIndexOfValue((String) newValue);
           Settings.System.putIntForUser(getContentResolver(), Settings.System.
                   VOLUME_DIALOG_STROKE, volumeDialogStroke, UserHandle.USER_CURRENT);
           mVolumeDialogStroke.setSummary(mVolumeDialogStroke.getEntries()[index]);
           VolumeDialogSettingsDisabler(volumeDialogStroke);
           return true;
        } else if (preference == mVolumeDialogStrokeColor) {
           String hex = ColorPickerPreference.convertToARGB(
                   Integer.valueOf(String.valueOf(objValue)));
           preference.setSummary(hex);
           int intHex = ColorPickerPreference.convertToColorInt(hex);
           Settings.System.putInt(getContentResolver(),
                   Settings.System.VOLUME_DIALOG_STROKE_COLOR, intHex);
           return true;
        } else if (preference == mVolumeDialogStrokeThickness) {
           int val = (Integer) objValue;
           Settings.System.putInt(getContentResolver(),
                   Settings.System.VOLUME_DIALOG_STROKE_THICKNESS, val * 1);
           return true;
        } else if (preference == mVolumeDialogCornerRadius) {
           int val = (Integer) objValue;
           Settings.System.putInt(getContentResolver(),
                   Settings.System.VOLUME_DIALOG_CORNER_RADIUS, val * 1);
           return true;
        } else if (preference == mVolumeDialogDashWidth) {
           int val = (Integer) objValue;
           Settings.System.putInt(getContentResolver(),
                   Settings.System.VOLUME_DIALOG_STROKE_DASH_WIDTH, val * 1);
           return true;
        } else if (preference == mVolumeDialogDashGap) {
           int val = (Integer) objValue;
           Settings.System.putInt(getContentResolver(),
                   Settings.System.VOLUME_DIALOG_STROKE_DASH_GAP, val * 1);
           return true;
	}
        return false;
    }

    private void VolumeDialogSettingsDisabler(int volumeDialogStroke) {
        if (volumeDialogStroke == 0) {
            mVolumeDialogStrokeColor.setEnabled(false);
            mVolumeDialogStrokeThickness.setEnabled(false);
            mVolumeDialogDashWidth.setEnabled(false);
            mVolumeDialogDashGap.setEnabled(false);
        } else if (volumeDialogStroke == 1) {
            mVolumeDialogStrokeColor.setEnabled(false);
            mVolumeDialogStrokeThickness.setEnabled(true);
            mVolumeDialogDashWidth.setEnabled(true);
            mVolumeDialogDashGap.setEnabled(true);
        } else {
            mVolumeDialogStrokeColor.setEnabled(true);
            mVolumeDialogStrokeThickness.setEnabled(true);
            mVolumeDialogDashWidth.setEnabled(true);
            mVolumeDialogDashGap.setEnabled(true);
        }
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        SoundSettings getOwner() {
            return (SoundSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_SAFE_HEADSET_VOLUME:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.safe_headset_volume_warning_dialog_text)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getOwner().getContentResolver(),
                                    Settings.System.SAFE_HEADSET_VOLUME, 0);

                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create();
                case DLG_CAMERA_SOUND:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.attention)
                    .setMessage(R.string.camera_sound_warning_dialog_text)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SystemProperties.set(PROP_CAMERA_SOUND, "0");
                        }
                    })
                    .setNegativeButton(R.string.dlg_cancel,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_SAFE_HEADSET_VOLUME:
                    getOwner().mSafeHeadsetVolume.setChecked(true);
                    break;
                case DLG_CAMERA_SOUND:
                    getOwner().mCameraSounds.setChecked(true);
                    break;
            }
        }
    }

}
