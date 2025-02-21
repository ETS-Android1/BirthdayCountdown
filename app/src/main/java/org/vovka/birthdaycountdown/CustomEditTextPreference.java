/*
 * *
 *  * Created by Vladimir Belov on 26.12.2021, 1:01
 *  * Copyright (c) 2018 - 2021. All rights reserved.
 *  * Last modified 01.12.2021, 12:15
 *
 */

package org.vovka.birthdaycountdown;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.EditText;

public class CustomEditTextPreference extends EditTextPreference {

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomEditTextPreference(Context context) {
        super(context);
    }

/*    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        TypedArray ta = builder.getContext().getTheme().obtainStyledAttributes(R.styleable.Theme);
        int color = ta.getColor(R.styleable.Theme_dialogTextColor, 0);
        Toast.makeText(builder.getContext(), Color.red(color) + "," + Color.green(color) + "," + Color.blue(color), Toast.LENGTH_LONG).show();
        //builder.getContext().setTheme(ContactsEvents.getInstance().preferences_theme.themeDialog);
        super.onPrepareDialogBuilder(builder);
    }*/

    @Override
    protected void showDialog(Bundle state)
    {
        super.showDialog(state);
        final AlertDialog dialog = (AlertDialog) getDialog();
        final EditText editText = dialog.findViewById(android.R.id.edit);
        if (editText != null) {
            TypedArray ta = getContext().getTheme().obtainStyledAttributes(R.styleable.Theme);
            //https://www.thetopsites.net/article/51779376.shtml
            //в светлой теме получилось выставить editTextPreference color только таким способом
            editText.setTextColor(ta.getColor(R.styleable.Theme_dialogTextColor, 0));
            editText.setHintTextColor(ta.getColor(R.styleable.Theme_dialogHintColor, 0));
            //editText.setContentDescription(getContext().getString(R.string.widget_EditText));
            //final float scale = getContext().getResources().getDisplayMetrics().density;
            //editText.setMinimumHeight((int) (48 * scale + 0.5f));
            editText.setMinimumHeight((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getContext().getResources().getDisplayMetrics()));
        }
    }
}
