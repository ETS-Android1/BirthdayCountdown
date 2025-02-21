/*
 * *
 *  * Created by Vladimir Belov on 26.12.2021, 1:01
 *  * Copyright (c) 2018 - 2021. All rights reserved.
 *  * Last modified 26.12.2021, 0:22
 *
 */

package org.vovka.birthdaycountdown;

import static org.vovka.birthdaycountdown.Constants.RES_PACKAGE_ANDROID;
import static org.vovka.birthdaycountdown.Constants.STRING_ID;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class ColorPicker extends FrameLayout implements View.OnClickListener {

    private int[] mColorChoices = {};
    private int mValue = 0;
    private int mItemLayoutId = R.layout.item_color;
    private int mNumColumns = 4;
    private String mSelectDialogTitle = "";
    private int mSelectDialogIcon;
    private ColorGridAdapter mAdapter;
    private SeekBar mAlphaSeekBar;

    public ColorPicker(@NonNull Context context) {
        super(context);
        initAttrs(null, 0, 0);
    }

    public ColorPicker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs, 0, 0);
    }

    public ColorPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs, defStyleAttr, 0);
    }

    public ColorPicker(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttrs(attrs, defStyleAttr, defStyleRes);
    }

    private void initAttrs(AttributeSet attrs, int defStyleAttr, int defStyleRes) {

        LayoutInflater.from(getContext()).inflate(R.layout.picker_color, this);
        setOnClickListener(this);

        TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.ColorPreference, defStyleAttr, defStyleRes);

        try {
            mSelectDialogTitle = a.getString(R.styleable.ColorPreference_dialogTitle);
            mSelectDialogIcon = a.getResourceId(R.styleable.ColorPreference_dialogIcon, 0);
            mItemLayoutId = a.getResourceId(R.styleable.ColorPreference_itemLayout, mItemLayoutId);
            mNumColumns = a.getInteger(R.styleable.ColorPreference_numColumns, mNumColumns);
            mValue = a.getInteger(R.styleable.ColorPreference_defaultValue, ContextCompat.getColor(getContext(), R.color.pref_Widgets_Color_WidgetBackground_default));
            int choicesResId = a.getResourceId(R.styleable.ColorPreference_choices, R.array.default_color_choice_values);
            if (choicesResId > 0) {
                //https://stackoverflow.com/questions/9114587/how-can-i-save-colors-in-array-xml-and-get-its-back-to-color-array
                mColorChoices = a.getResources().getIntArray(choicesResId);
            }

            if (a.hasValue(R.styleable.ColorPreference_title)) {
                int id = getResources().getIdentifier("title", STRING_ID, RES_PACKAGE_ANDROID);
                if (id > 0) {
                    TextView view = findViewById(id);
                    if (view != null) view.setText(a.getString(R.styleable.ColorPreference_title));
                }
            }
            if (a.hasValue(R.styleable.ColorPreference_summary)) {
                int id = getResources().getIdentifier("summary", STRING_ID, RES_PACKAGE_ANDROID);
                if (id > 0) {
                    TextView view = findViewById(id);
                    if (view != null)
                        view.setText(a.getString(R.styleable.ColorPreference_summary));
                }

            }

            int id = getResources().getIdentifier("icon", STRING_ID, RES_PACKAGE_ANDROID);
            if (id > 0) {
                ImageView view = findViewById(id);
                if (view != null) {
                    if (a.hasValue(R.styleable.ColorPreference_icon)) {
                        view.setImageResource(a.getResourceId(R.styleable.ColorPreference_icon, 0));
                        view.setVisibility(View.VISIBLE);
                    } else {
                        view.setVisibility(View.GONE);
                    }
                }
            }

            int idRGB = getResources().getIdentifier("text1", STRING_ID, RES_PACKAGE_ANDROID);
            if (idRGB > 0) {
                View rgbView = findViewById(idRGB);
                if (rgbView != null) {
                    if (!ContactsEvents.getInstance().preferences_debug_on) {
                        rgbView.setVisibility(GONE);
                    } else {
                        rgbView.setOnClickListener(v -> setColorManual());
                    }
                }
            }
            setColor(mValue);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), Constants.COLOR_PICKER_INIT_ATTRS_ERROR + e, Toast.LENGTH_LONG).show();
        } finally {
            a.recycle();
        }

    }

    public void setColor(int color) {

        mValue = color;
        int id = getResources().getIdentifier("icon1", STRING_ID, RES_PACKAGE_ANDROID);
        if (id > 0) {
            View view = findViewById(id);
            if (view != null) {
                setColorViewValue(view, mValue);
            }
            if (mAlphaSeekBar != null) {
                mAlphaSeekBar.setProgress(Color.alpha(mValue));
            }
            int idRGB = getResources().getIdentifier("text1", STRING_ID, RES_PACKAGE_ANDROID);
            if (idRGB > 0) {
                TextView rgbView = findViewById(idRGB);
                if (rgbView != null) {
                    rgbView.setText(ContactsEvents.toARGBString(mValue));
                }
            }
        }
    }

    public void setColorManual() {

        TypedArray ta = getContext().getTheme().obtainStyledAttributes(R.styleable.Theme);
        final EditText textEdit = new EditText(getContext());
        textEdit.setSingleLine();
        textEdit.setText(ContactsEvents.toARGBString(mValue));
        textEdit.setTextColor(ta.getColor(R.styleable.Theme_dialogTextColor, 0));
        textEdit.setHintTextColor(ta.getColor(R.styleable.Theme_dialogHintColor, 0));
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), ContactsEvents.getInstance().preferences_theme.themeDialog))
                .setTitle(mSelectDialogTitle)
                .setIcon(R.drawable.ic_menu_paste)
                .setView(textEdit)
                .setPositiveButton(R.string.button_ok, null)
                .setNegativeButton(R.string.button_cancel, (dialog, which) -> dialog.cancel());

        AlertDialog alertToShow = builder.create();


        alertToShow.setOnShowListener(arg0 -> {
            alertToShow.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ta.getColor(R.styleable.Theme_dialogButtonColor, 0));
            alertToShow.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ta.getColor(R.styleable.Theme_dialogButtonColor, 0));

            alertToShow.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {

                int colorNew = 0;
                try {
                    colorNew = Color.parseColor(textEdit.getText().toString());
                } catch (Exception e) { /**/ }
                if (colorNew == 0) {
                    Toast.makeText(getContext(), getResources().getString(R.string.msg_color_parse_error), Toast.LENGTH_LONG).show();
                    return;
                }

                setColor(colorNew);
                alertToShow.dismiss();

            });

        });

        alertToShow.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertToShow.show();

    }

    public int getColor() {return mValue;}

    public void setAlphaSeekBar(SeekBar seekBarView) {
        mAlphaSeekBar = seekBarView;
    }

    public void onClick(View v) {

        try {

            LayoutInflater layoutInflater = LayoutInflater.from(getContext());
            View rootView = layoutInflater.inflate(R.layout.dialog_colors, null);

            mAdapter = new ColorGridAdapter(getContext());
            mAdapter.setSelectedColor(mValue);

            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getContext(), ContactsEvents.getInstance().preferences_theme.themeDialog))
                    .setTitle(mSelectDialogTitle)
                    .setIcon(mSelectDialogIcon)
                    .setView(rootView);

            AlertDialog alertToShow = builder.create();

            GridView mColorGrid = rootView.findViewById(R.id.color_grid);
            if (mColorGrid != null) {
                mColorGrid.setNumColumns(mNumColumns);
                mColorGrid.setAdapter(mAdapter);
                mColorGrid.setOnItemClickListener((listView, view, position, itemId) -> {
                    setColor(mAdapter.getItem(position));
                    alertToShow.dismiss();
                });
            }
            View mCaptionView = rootView.findViewById(R.id.caption);
            if (mCaptionView != null) {
                mCaptionView.setVisibility(View.GONE);
            }

            alertToShow.requestWindowFeature(Window.FEATURE_NO_TITLE);
            alertToShow.show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), Constants.COLOR_PICKER_ON_CLICK_ERROR + e, Toast.LENGTH_LONG).show();
        }
    }

    private void setColorViewValue(View view, int color) {

        try {
            if (view instanceof ImageView) {
                ImageView imageView = (ImageView) view;
                Resources res = imageView.getContext().getResources();
                int radius = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40, res.getDisplayMetrics());

                if (Color.alpha(color) == 0) {
                    Bitmap bm = BitmapFactory.decodeResource(res, R.drawable.transparent);
                    imageView.setImageBitmap(Bitmap.createScaledBitmap(bm, radius, radius, false));
                    bm.recycle();
                    return;
                }
                Drawable currentDrawable = imageView.getDrawable();
                GradientDrawable colorChoiceDrawable;
                if (currentDrawable instanceof GradientDrawable) {
                    colorChoiceDrawable = (GradientDrawable) currentDrawable;
                } else {
                    colorChoiceDrawable = new GradientDrawable();
                    colorChoiceDrawable.setShape(GradientDrawable.OVAL);
                }

                // Set stroke to dark version of color
                int darkenedColor = Color.rgb(
                        Color.red(color) * 192 / 256,
                        Color.green(color) * 192 / 256,
                        Color.blue(color) * 192 / 256);

                colorChoiceDrawable.setSize(radius, radius);
                colorChoiceDrawable.setColor(color);
                colorChoiceDrawable.setStroke((int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 1, res.getDisplayMetrics()), darkenedColor);
                imageView.setImageDrawable(colorChoiceDrawable);
                imageView.setContentDescription(Integer.toString(color));

            } else if (view instanceof TextView) {
                ((TextView) view).setTextColor(color);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), Constants.COLOR_PICKER_SET_COLOR_VIEW_VALUE_ERROR + e, Toast.LENGTH_LONG).show();
        }
    }

    private class ColorGridAdapter extends BaseAdapter {
        private final List<Integer> mChoices = new ArrayList<>();
        private int mSelectedColor;

        private ColorGridAdapter(Context context) {
            for (int color : mColorChoices) {
                mChoices.add(color);
            }
        }

        @Override
        public int getCount() {
            return mChoices.size();
        }

        @Override
        public Integer getItem(int position) {
            return mChoices.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mChoices.get(position);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(mItemLayoutId, container, false);
            }

            int color = getItem(position);
            setColorViewValue(convertView.findViewById(R.id.color_view), color);
            convertView.setBackgroundColor(color == mSelectedColor ? 0x6633b5e5 : 0);
            return convertView;
        }

        public void setSelectedColor(int selectedColor) {
            mSelectedColor = selectedColor;
            notifyDataSetChanged();
        }

        public int getSelectedColor() {
            return mSelectedColor;
        }

    }

}
