/*
 * *
 *  * Created by Vladimir Belov on 20.02.20 1:25
 *  * Copyright (c) 2018 - 2020. All rights reserved.
 *  * Last modified 20.02.20 1:17
 *
 */

package org.vovka.birthdaycountdown;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

public class WidgetConfigureActivity extends AppCompatActivity {

    private int widgetId = 0;
    private String widgetType;
    private ContactsEvents eventsData;

    public void onCreate(Bundle savedInstanceState) {
        try {

            super.onCreate(savedInstanceState);

            eventsData = ContactsEvents.getInstance();
            if (eventsData.context == null) eventsData.context = getApplicationContext();
            eventsData.getPreferences();
            eventsData.setLocale(true);

            //Без этого на Android 8 и 9 не меняет динамически язык
            Locale locale;
            if (eventsData.preferences_language.equals(getString(R.string.pref_Language_default))) {
                locale = new Locale(eventsData.systemLocale);
            } else {
                locale = new Locale(eventsData.preferences_language);
            }
            Resources applicationRes = getBaseContext().getResources();
            Configuration applicationConf = applicationRes.getConfiguration();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                applicationConf.setLocales(new android.os.LocaleList(locale));
            } else {
                applicationConf.setLocale(locale);
            }
            applicationRes.updateConfiguration(applicationConf, applicationRes.getDisplayMetrics());

            this.setTheme(eventsData.preferences_theme.themeMain);

            setContentView(R.layout.widget_config);

            Toolbar toolbar = findViewById(R.id.toolbar);
            toolbar.setPopupTheme(eventsData.preferences_theme.themePopup);
            toolbar.setTitle(R.string.window_widget_settings);

            //Цвет заголовка окна
            TypedArray ta = this.getTheme().obtainStyledAttributes(R.styleable.Theme);
            toolbar.setTitleTextColor(ta.getColor(R.styleable.Theme_windowTitleColor, ContextCompat.getColor(this, R.color.white)));
            setSupportActionBar(toolbar);

            //todo: цвет spinner https://stackoverflow.com/questions/9476665/how-to-change-spinner-text-size-and-text-color

            setResult(RESULT_CANCELED);

            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
            if (extras != null) widgetId = extras.getInt("appWidgetId", 0);

            //if (eventsData.preferences_debug_on) Toast.makeText(this, "widgetId=" + widgetId, Toast.LENGTH_LONG).show();

            List<String> widgetPref = eventsData.getWidgetPreference(widgetId);

            int prefStartingIndex = 1;
            try {
                if (widgetPref.size() > 0) prefStartingIndex = Integer.parseInt(widgetPref.get(0));
            } catch (Exception e1) {/**/}

            Spinner spinnerIndex = findViewById(R.id.spinnerEventShift);
            spinnerIndex.setSelection(prefStartingIndex - 1);

            widgetType = AppWidgetManager.getInstance(this).getAppWidgetInfo(widgetId).provider.getShortClassName();

            if (!widgetType.equals(".Widget2x2")) {

                //Скрываем Коэффициент масштабирования размера шрифта
                findViewById(R.id.dividerFontMagnify).setVisibility(View.GONE);
                findViewById(R.id.spinnerFontMagnifyLabel).setVisibility(View.GONE);
                findViewById(R.id.spinnerFontMagnify).setVisibility(View.GONE);
                findViewById(R.id.textViewFontMagnify).setVisibility(View.GONE);

            } else {

                //Заполняем Коэффициент масштабирования размера шрифта
                int prefMagnifyIndex = 0;
                try {
                    if (widgetPref.size() > 1) prefMagnifyIndex = Integer.parseInt(widgetPref.get(1));
                } catch (Exception e2) {/**/}

                Spinner spinnerMagnify = findViewById(R.id.spinnerFontMagnify);
                spinnerMagnify.setSelection(prefMagnifyIndex);

            }

            if (!widgetType.equals(".Widget5x1")) {

                //Скрываем количество событий
                findViewById(R.id.dividerEventsCount).setVisibility(View.GONE);
                findViewById(R.id.spinnerEventsCountLabel).setVisibility(View.GONE);
                findViewById(R.id.spinnerEventsCount).setVisibility(View.GONE);
                findViewById(R.id.textViewEventsCount).setVisibility(View.GONE);

            } else {

                //Заполняем количество событий
                int prefEventsCountIndex = 0;
                try {
                    if (widgetPref.size() > 2) prefEventsCountIndex = Integer.parseInt(widgetPref.get(2));
                } catch (Exception e2) {/**/}

                Spinner spinnerEventsCount = findViewById(R.id.spinnerEventsCount);
                spinnerEventsCount.setSelection(prefEventsCountIndex);

            }

        } catch (Exception e) {
            e.printStackTrace();
            if (eventsData.preferences_debug_on) Toast.makeText(this, Constants.WIDGET_CONFIGURE_ACTIVITY_ON_CREATE_ERROR + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unused")
    public void buttonOkOnClick(View view) {

        try {
            Spinner spinnerIndex = findViewById(R.id.spinnerEventShift);
            Spinner spinnerMagnify = findViewById(R.id.spinnerFontMagnify);
            Spinner spinnerEventsCount = findViewById(R.id.spinnerEventsCount);
            int selectedItemPosition = spinnerIndex.getSelectedItemPosition();

            //Сохраняем настройки
            if (widgetId != 0 && selectedItemPosition != -1) {

                eventsData.setWidgetPreference(widgetId,
                        spinnerIndex.getItemAtPosition(selectedItemPosition).toString() //тут именно значение в списке
                        .concat(Constants.STRING_COMMA)
                        .concat(widgetType.equals(".Widget2x2") ? String.valueOf(spinnerMagnify.getSelectedItemPosition()) : "0") //тут позиция в списке
                        .concat(Constants.STRING_COMMA)
                        .concat(widgetType.equals(".Widget5x1") ? String.valueOf(spinnerEventsCount.getSelectedItemPosition()) : "0") //тут позиция в списке
                );

            }

            Intent intent = new Intent();
            intent.putExtra("appWidgetId", widgetId);
            setResult(RESULT_OK, intent);

            //Посылаем сообщения на обновление виджетов

            eventsData.updateWidgets();

            finish();
        } catch (Exception e) {
            e.printStackTrace();
            if (eventsData.preferences_debug_on) Toast.makeText(this, Constants.WIDGET_CONFIGURE_ACTIVITY_BUTTON_OK_ON_CLICK_ERROR + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @SuppressWarnings("unused")
    public void buttonCancelOnClick(View view) {
        setResult(RESULT_CANCELED);
        finish();
    }
}
