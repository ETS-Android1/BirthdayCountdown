/*
 * *
 *  * Created by Vladimir Belov on 07.03.2022, 22:54
 *  * Copyright (c) 2018 - 2022. All rights reserved.
 *  * Last modified 07.03.2022, 19:16
 *
 */

package org.vovka.birthdaycountdown;

import static org.vovka.birthdaycountdown.Constants.ACTION_LAUNCH;
import static org.vovka.birthdaycountdown.Constants.DATETIME_DD_MM_YYYY_HH_MM;
import static org.vovka.birthdaycountdown.Constants.PARAM_APP_WIDGET_ID;
import static org.vovka.birthdaycountdown.Constants.REGEX_PLUS;
import static org.vovka.birthdaycountdown.Constants.STRING_0;
import static org.vovka.birthdaycountdown.Constants.STRING_1;
import static org.vovka.birthdaycountdown.Constants.STRING_10;
import static org.vovka.birthdaycountdown.Constants.STRING_2;
import static org.vovka.birthdaycountdown.Constants.STRING_3;
import static org.vovka.birthdaycountdown.Constants.STRING_4;
import static org.vovka.birthdaycountdown.Constants.STRING_5;
import static org.vovka.birthdaycountdown.Constants.STRING_6;
import static org.vovka.birthdaycountdown.Constants.STRING_7;
import static org.vovka.birthdaycountdown.Constants.STRING_8;
import static org.vovka.birthdaycountdown.Constants.STRING_9;
import static org.vovka.birthdaycountdown.Constants.STRING_EMPTY;
import static org.vovka.birthdaycountdown.Constants.STRING_EOT;
import static org.vovka.birthdaycountdown.Constants.STRING_ID;
import static org.vovka.birthdaycountdown.Constants.STRING_STORAGE_CONTACTS;
import static org.vovka.birthdaycountdown.Constants.TIME_FORCE_UPDATE;
import static org.vovka.birthdaycountdown.Constants.Type_5K;
import static org.vovka.birthdaycountdown.Constants.Type_BirthDay;
import static org.vovka.birthdaycountdown.Constants.Type_CalendarEvent;
import static org.vovka.birthdaycountdown.Constants.Type_FileEvent;
import static org.vovka.birthdaycountdown.Constants.WIDGET_EVENTS_MAX;
import static org.vovka.birthdaycountdown.Constants.WIDGET_EVENT_INFO;
import static org.vovka.birthdaycountdown.Constants.WIDGET_ICON_EVENT_TYPE;
import static org.vovka.birthdaycountdown.Constants.WIDGET_ICON_FAV;
import static org.vovka.birthdaycountdown.Constants.WIDGET_ICON_SILENCED;
import static org.vovka.birthdaycountdown.Constants.WIDGET_ICON_ZODIAC;
import static org.vovka.birthdaycountdown.Constants.WIDGET_ICON_ZODIAC_YEAR;
import static org.vovka.birthdaycountdown.Constants.WIDGET_IMAGE_VIEW;
import static org.vovka.birthdaycountdown.Constants.WIDGET_IMAGE_VIEW_CENTERED;
import static org.vovka.birthdaycountdown.Constants.WIDGET_IMAGE_VIEW_START;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_SIZE_BIG;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_SIZE_SMALL;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_SIZE_TINY;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_VIEW;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_VIEW_2_ND;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_VIEW_2_ND_CENTERED;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_VIEW_AGE;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_VIEW_CENTERED;
import static org.vovka.birthdaycountdown.Constants.WIDGET_TEXT_VIEW_DISTANCE;
import static org.vovka.birthdaycountdown.Constants.WIDGET_UPDATER_DRAW_EVENT_ERROR;
import static org.vovka.birthdaycountdown.Constants.WIDGET_UPDATER_INVOKE_ERROR;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_contactID;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_eventCaption;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_eventDateText;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_eventLabel;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_eventStorage;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_eventSubType;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_eventType;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_nickname;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_organization;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_personFullName;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_personFullNameAlt;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_starred;
import static org.vovka.birthdaycountdown.ContactsEvents.Position_title;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.provider.ContactsContract;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class WidgetUpdater {
    final private Context context;
    final private ContactsEvents eventsData;
    final private RemoteViews views;
    final private int eventsCount;
    final private int width;
    final private int height;
    private final int widgetId;
    private Resources resources;
    private String packageName;
    private double fontMagnify;

    private int colorDefault;
    private int colorEventToday;
    private int colorEventSoon;
    private int colorEventFar;

    private int eventsDisplayed;
    private int eventsToShow;
    private int eventsToSkip;
    private List<String> widgetPref;
    private List<String> widgetPref_eventInfo = new ArrayList<>();

    final int PendingIntentImmutable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;

    WidgetUpdater(@NonNull Context context, @NonNull ContactsEvents eventsData, @NonNull RemoteViews views, int eventsCount, int width, int height, int widgetId) {
        this.context = context;
        this.eventsData = eventsData;
        this.views = views;
        this.eventsCount = eventsCount > WIDGET_EVENTS_MAX ? WIDGET_EVENTS_MAX : eventsCount > 0 ? eventsCount : 1;
        this.width = width;
        this.height = height;
        this.widgetId = widgetId;
    }

    void invokePhotoEventsUpdate() {
        //По нажатию на виджет открываем основное окно
        //http://flowovertop.blogspot.com/2013/04/android-widget-with-button-click-to.html
        Intent intentView = new Intent(context, MainActivity.class);
        intentView.setAction(ACTION_LAUNCH);
        views.setOnClickPendingIntent(R.id.appwidget_main, PendingIntent.getActivity(context, 0, intentView, PendingIntentImmutable | PendingIntent.FLAG_UPDATE_CURRENT));

        //Получаем данные
        if (eventsData.isEmptyEventList() || System.currentTimeMillis() - eventsData.statLastComputeDates > TIME_FORCE_UPDATE + eventsData.statTimeComputeDates) {
            if (eventsData.getContext() == null) eventsData.setContext(context);
            if (eventsData.getEvents(context)) eventsData.computeDates();
        }

        //Отрисовываем события
        try {

            //Скрываем все события
            resources = context.getResources();
            packageName = context.getPackageName();
            for (int e = 0; e < this.eventsCount; e++) {
                views.setViewVisibility(resources.getIdentifier(WIDGET_EVENT_INFO + e, STRING_ID, packageName), View.INVISIBLE);
            }

            //Получаем настройки отображения виджета
            final AppWidgetProviderInfo appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(widgetId);
            if (appWidgetInfo == null) return;
            String widgetType = appWidgetInfo.provider.getShortClassName();
            widgetPref = eventsData.getWidgetPreference(widgetId, widgetType);

            int startingIndex = 1;
            try {
                if (widgetPref.size() > 0) startingIndex = Integer.parseInt(widgetPref.get(0));
            } catch (Exception e) {/**/}

            if (widgetPref.size() > 4 && !widgetPref.get(4).isEmpty()) {
                widgetPref_eventInfo = Arrays.asList(widgetPref.get(4).split(REGEX_PLUS));
            }

            if (eventsData.isEmptyEventList() || eventsData.eventList.size() < startingIndex) {

                views.setTextViewText(R.id.appwidget_text, context.getString(R.string.msg_no_events));
                views.setViewVisibility(R.id.appwidget_text, View.VISIBLE);
                return;

            }

            eventsToShow = Math.min(this.eventsCount, eventsData.eventList.size());

            //Увеличение шрифтов в зависимости от размеров окна
            fontMagnify = 1;
            int cells = getCellsForSize(Math.min(width, height));
            if (widgetPref.size() > 1 && !widgetPref.get(1).equals(STRING_0)) {
                switch (widgetPref.get(1)) {
                    case STRING_1:
                        fontMagnify = cells * 0.5;
                        break;
                    case STRING_2:
                        fontMagnify = cells * 0.65;
                        break;
                    case STRING_3:
                        fontMagnify = cells * 0.75;
                        break;
                    case STRING_4:
                        fontMagnify = cells * 0.85;
                        break;
                    case STRING_5:
                        fontMagnify = cells * 1.0;
                        break;
                    case STRING_6:
                        fontMagnify = cells * 1.2;
                        break;
                    case STRING_7:
                        fontMagnify = cells * 1.5;
                        break;
                    case STRING_8:
                        fontMagnify = cells * 1.75;
                        break;
                    case STRING_9:
                        fontMagnify = cells * 2.0;
                        break;
                }
            } else {
                fontMagnify = 1 + 1.0 * (cells - 1);
            }

            colorDefault = eventsData.preferences_widgets_color_default;
            colorEventToday = eventsData.preferences_widgets_color_eventtoday;
            colorEventSoon = eventsData.preferences_widgets_color_eventsoon;
            colorEventFar = eventsData.preferences_widgets_color_eventfar;

            //Отрисовываем информацию о событиях
            eventsDisplayed = 0;
            eventsToSkip = startingIndex - 1;

            int i = 0;
            while (i < eventsData.eventList.size() & eventsDisplayed <= eventsToShow) {
                drawPhotoEvent(i++);
            }

            if (eventsDisplayed == 0) { //вообще ничего не нашли

                views.setTextViewText(R.id.appwidget_text, context.getString(R.string.msg_no_events));
                views.setViewVisibility(R.id.appwidget_text, View.VISIBLE);
                Intent intentConfig = new Intent(context, WidgetConfigureActivity.class);
                intentConfig.setAction(ACTION_LAUNCH);
                intentConfig.putExtra(PARAM_APP_WIDGET_ID, widgetId);
                views.setOnClickPendingIntent(R.id.appwidget_text, PendingIntent.getActivity(context, widgetId, intentConfig, PendingIntentImmutable));

            } else {

                views.setViewVisibility(R.id.appwidget_text, View.GONE);

            }

            //Цвет подложки
            int colorWidgetBackground = 0;
            if (widgetPref.size() > 5 && !widgetPref.get(5).isEmpty()) {
                try {
                    colorWidgetBackground = Color.parseColor(widgetPref.get(5));
                } catch (Exception e) { /* */}
            }
            if (colorWidgetBackground == 0) {
                colorWidgetBackground = ContextCompat.getColor(context, R.color.pref_Widgets_Color_WidgetBackground_default);
            }
            views.setInt(R.id.events,"setBackgroundColor", colorWidgetBackground);

            //https://stackoverflow.com/questions/12523005/how-set-background-drawable-programmatically-in-android
            //Если события есть - рисуем бордюр, иначе - прозрачность
            if (eventsToShow > 0 && (widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_Border)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_Border))) {
                views.setInt(R.id.appwidget_main,"setBackgroundResource", R.drawable.layout_bg);
            } else {
                views.setInt(R.id.appwidget_main,"setBackgroundResource", 0);
            }

            if (eventsData.preferences_debug_on) {
                List<String> widgetPref = eventsData.getWidgetPreference(widgetId, widgetType);
                views.setTextViewText(R.id.info, (width > 70 ? context.getString(R.string.widget_msg_updated) : STRING_EMPTY) + new SimpleDateFormat(DATETIME_DD_MM_YYYY_HH_MM, resources.getConfiguration().locale).format(new Date(Calendar.getInstance().getTimeInMillis())));
                views.setViewVisibility(R.id.info, View.VISIBLE);
            } else {
                views.setTextViewText(R.id.info, STRING_EMPTY);
                views.setViewVisibility(R.id.info, View.INVISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            if (eventsData.preferences_debug_on) Toast.makeText(context, WIDGET_UPDATER_INVOKE_ERROR + e, Toast.LENGTH_LONG).show();
        }
    }

    private void drawPhotoEvent(int i) {
        //Отрисовка одного события
        try {

            String event = eventsData.eventList.get(i);
            String[] singleEventArray = event.split(STRING_EOT, -1);

            boolean isVisibleEvent = false;
            boolean useEventListPrefs = true;

            final String eventSubType = singleEventArray[Position_eventSubType];
            final String eventKey = eventsData.getEventKey(singleEventArray);

            if  (eventSubType.equals(ContactsEvents.eventTypesIDs.get(Type_CalendarEvent)) ||
                    eventSubType.equals(ContactsEvents.eventTypesIDs.get(Type_FileEvent))) { //пропускаем события календарей и из файлов
                useEventListPrefs = false;
            } else if (widgetPref.size() > 3 && !widgetPref.get(3).isEmpty()) {
                List<String> eventsPrefList =  Arrays.asList(widgetPref.get(3).split(REGEX_PLUS));
                if (eventsPrefList.size() > 0) {
                    useEventListPrefs = false;
                    isVisibleEvent = eventsPrefList.contains(singleEventArray[Position_eventType]) &&
                            (eventsData.getHiddenEventsCount() == 0 || !eventsData.checkIsHiddenEvent(eventKey));
                }
            }
            if (useEventListPrefs) isVisibleEvent = eventsData.preferences_list_event_types.contains(singleEventArray[Position_eventType]) &&
                    (eventsData.getHiddenEventsCount() == 0 || !eventsData.checkIsHiddenEvent(eventKey));

            if (!isVisibleEvent) {
                return;
            } else if (eventsToSkip > 0) {
                eventsToSkip--;
                return;
            }

            Person person = new Person(context, event);
            //if (eventsData.preferences_debug_on) Toast.makeText(context, eventsDisplayed + " " + event, Toast.LENGTH_LONG).show();
            int visibleCell = 1; //2 - top left, 3 - top center, 5 - bottom left, 7 - bottom center
            String rowValue;

            //Под фото
            int id_widget_Caption_left = resources.getIdentifier(WIDGET_TEXT_VIEW + eventsDisplayed, STRING_ID, packageName);
            int id_widget_Caption_centered = resources.getIdentifier(WIDGET_TEXT_VIEW_CENTERED + eventsDisplayed, STRING_ID, packageName);

            views.setViewVisibility(id_widget_Caption_left, View.INVISIBLE);
            views.setViewVisibility(id_widget_Caption_centered, View.INVISIBLE);

            views.setTextViewTextSize(id_widget_Caption_left, TypedValue.COMPLEX_UNIT_SP, (float) (WIDGET_TEXT_SIZE_TINY * fontMagnify));
            views.setTextViewTextSize(id_widget_Caption_centered, TypedValue.COMPLEX_UNIT_SP, (float) (WIDGET_TEXT_SIZE_TINY * fontMagnify));

            switch (eventsData.preferences_widgets_bottom_info) {
                case STRING_1: //Фамилия Имя Отчество
                    rowValue = singleEventArray[Position_personFullNameAlt];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_left, rowValue);
                        views.setViewVisibility(id_widget_Caption_left, View.VISIBLE);
                        visibleCell *= 2;
                    }
                    break;
                case STRING_2: //Дата события
                    rowValue = eventsData.getDateFormated(singleEventArray[Position_eventDateText], ContactsEvents.FormatDate.WithYear);
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption_centered, View.VISIBLE);
                        visibleCell *= 3;
                    }
                    break;
                case STRING_3: //Фамилия И.О.
                    rowValue =
                        singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ? eventsData.getContactFullNameShort(ContactsEvents.parseToLong(singleEventArray[Position_contactID])) :
                        person.getFullNameShort();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption_centered, View.VISIBLE);
                        visibleCell *= 3;
                    }
                    break;
                case STRING_4: //Имя Отчество Фамилия
                    rowValue = singleEventArray[Position_personFullName];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_left, rowValue);
                        views.setViewVisibility(id_widget_Caption_left, View.VISIBLE);
                        visibleCell *= 2;
                    }
                    break;
                case STRING_5: //Имя
                    rowValue =
                        singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ? eventsData.getContactData(ContactsEvents.parseToLong(singleEventArray[Position_contactID]), ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME) :
                        person.getFirstName();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption_centered, View.VISIBLE);
                        visibleCell *= 3;
                    }
                    break;
                case STRING_6: //Фамилия
                    rowValue =
                        singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ? eventsData.getContactData(ContactsEvents.parseToLong(singleEventArray[Position_contactID]), ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME) :
                        person.getSecondName();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption_centered, View.VISIBLE);
                        visibleCell *= 3;
                    }
                    break;
                case STRING_7: //Псевдоним (Имя, если отсутствует)
                    rowValue =
                        singleEventArray[Position_nickname].trim().length() > 0 ? singleEventArray[Position_nickname] :
                        singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ? eventsData.getContactData(ContactsEvents.parseToLong(singleEventArray[Position_contactID]), ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME) :
                        person.getFirstName();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption_centered, View.VISIBLE);
                        visibleCell *= 3;
                    }
                    break;
                case STRING_8: //Тип события
                    rowValue = singleEventArray[Position_eventCaption];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_left, rowValue);
                        views.setViewVisibility(id_widget_Caption_left, View.VISIBLE);
                        visibleCell *= 2;
                    }
                    break;
                case STRING_9: //Наименование события
                    rowValue =
                        singleEventArray[Position_eventLabel].trim().isEmpty() ? singleEventArray[Position_eventCaption] :
                        singleEventArray[Position_eventLabel];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_left, rowValue);
                        views.setViewVisibility(id_widget_Caption_left, View.VISIBLE);
                        visibleCell *= 2;
                    }
                    break;
                case STRING_10: //Организация (Должность, если отсутствует)
                    rowValue = singleEventArray[Position_organization].trim().length() > 0 ? singleEventArray[Position_organization] : singleEventArray[Position_title];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption_centered, View.VISIBLE);
                        visibleCell *= 3;
                    }
                    break;
            }

            //Под фото (верхний ряд)
            int id_widget_Caption2nd_left = resources.getIdentifier(WIDGET_TEXT_VIEW_2_ND + eventsDisplayed, STRING_ID, packageName);
            int id_widget_Caption2nd_centered = resources.getIdentifier(WIDGET_TEXT_VIEW_2_ND_CENTERED + eventsDisplayed, STRING_ID, packageName);

            views.setViewVisibility(id_widget_Caption2nd_left, View.INVISIBLE);
            views.setViewVisibility(id_widget_Caption2nd_centered, View.INVISIBLE);

            views.setTextViewTextSize(id_widget_Caption2nd_left, TypedValue.COMPLEX_UNIT_SP, (float) (WIDGET_TEXT_SIZE_TINY * fontMagnify));
            views.setTextViewTextSize(id_widget_Caption2nd_centered, TypedValue.COMPLEX_UNIT_SP, (float) (WIDGET_TEXT_SIZE_TINY * fontMagnify));

            switch (eventsData.preferences_widgets_bottom_info_2nd) {
                case STRING_1: //Фамилия Имя Отчество
                    rowValue = singleEventArray[Position_personFullNameAlt];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_left, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_left, View.VISIBLE);
                        visibleCell *= 5;
                    }
                    break;
                case STRING_2: //Дата события
                    rowValue = eventsData.getDateFormated(singleEventArray[Position_eventDateText], ContactsEvents.FormatDate.WithYear);
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_centered, View.VISIBLE);
                        visibleCell *= 7;
                    }
                    break;
                case STRING_3: //Фамилия И.О.
                    rowValue =
                            singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ? eventsData.getContactFullNameShort(ContactsEvents.parseToLong(singleEventArray[Position_contactID])) :
                            person.getFullNameShort();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_centered, View.VISIBLE);
                        visibleCell*=7;
                    }
                    break;
                case STRING_4: //Имя Отчество Фамилия
                    rowValue = singleEventArray[Position_personFullName];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_left, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_left, View.VISIBLE);
                        visibleCell *= 5;
                    }
                    break;
                case STRING_5: //Имя
                    rowValue =
                            singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ?
                            eventsData.getContactData(ContactsEvents.parseToLong(singleEventArray[Position_contactID]), ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME) :
                            person.getFirstName();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_centered, View.VISIBLE);
                        visibleCell *= 7;
                    }
                    break;
                case STRING_6: //Фамилия
                     rowValue =
                            singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ?
                            eventsData.getContactData(ContactsEvents.parseToLong(singleEventArray[Position_contactID]), ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME) :
                            person.getSecondName();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_centered, View.VISIBLE);
                        visibleCell *= 7;
                    }
                    break;
                case STRING_7: //Псевдоним (Имя, если отсутствует)
                    rowValue =
                            singleEventArray[Position_nickname].trim().length() > 0 ? singleEventArray[Position_nickname] :
                            singleEventArray[Position_eventStorage].equals(STRING_STORAGE_CONTACTS) ? eventsData.getContactData(ContactsEvents.parseToLong(singleEventArray[Position_contactID]), ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME) :
                            person.getFirstName();
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_centered, View.VISIBLE);
                        visibleCell *= 7;
                    }
                    break;
                case STRING_8: //Тип события
                    rowValue = singleEventArray[Position_eventCaption];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_left, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_left, View.VISIBLE);
                        visibleCell *= 5;
                    }
                    break;
                case STRING_9: //Наименование события
                    rowValue =
                            singleEventArray[Position_eventLabel].trim().isEmpty() ? singleEventArray[Position_eventCaption] :
                            singleEventArray[Position_eventLabel];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_left, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_left, View.VISIBLE);
                        visibleCell*=5;
                    }
                    break;
                case STRING_10: //Организация (Должность, если отсутствует)
                    rowValue = singleEventArray[Position_organization].trim().length() > 0 ? singleEventArray[Position_organization] : singleEventArray[Position_title];
                    if (!rowValue.trim().isEmpty()) {
                        views.setTextViewText(id_widget_Caption2nd_centered, rowValue);
                        views.setViewVisibility(id_widget_Caption2nd_centered, View.VISIBLE);
                        visibleCell *= 7;
                    }
                    break;
            }

            //Фото

            int roundingFactor = 0;
            if (widgetPref != null && widgetPref.size() > 6) {
                switch (widgetPref.get(6)) {
                    case STRING_1: roundingFactor = 2; break;
                    case STRING_2: roundingFactor = 3; break;
                    case STRING_3: roundingFactor = 4; break;
                    case STRING_4: roundingFactor = 9; break;
                }
            }

            //eventsData.temp_int = roundingFactor;
            Bitmap photo = eventsData.getContactPhoto(event, widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_Photo)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_Photo), true, true, roundingFactor);
            if (photo != null) {

                //https://stackoverflow.com/questions/2459916/how-to-make-an-imageview-with-rounded-corners
                //https://stackoverflow.com/questions/7895118/android-remoteviews-how-to-set-scaletype-of-an-imageview-inside-a-widget
                int id_widget_Photo = resources.getIdentifier(WIDGET_IMAGE_VIEW + eventsDisplayed, STRING_ID, packageName);
                int id_widget_Photo_Centered = resources.getIdentifier(WIDGET_IMAGE_VIEW_CENTERED + eventsDisplayed, STRING_ID, packageName);
                int id_widget_Photo_Start = resources.getIdentifier(WIDGET_IMAGE_VIEW_START + eventsDisplayed, STRING_ID, packageName);
                int id_Photo;
                if (roundingFactor < 1) {
                    views.setViewVisibility(id_widget_Photo, View.VISIBLE);
                    views.setViewVisibility(id_widget_Photo_Centered, View.GONE);
                    views.setViewVisibility(id_widget_Photo_Start, View.GONE);
                    id_Photo = id_widget_Photo;
                } else if (roundingFactor > 8) {
                    views.setViewVisibility(id_widget_Photo, View.GONE);
                    views.setViewVisibility(id_widget_Photo_Centered, View.VISIBLE);
                    views.setViewVisibility(id_widget_Photo_Start, View.GONE);
                    id_Photo = id_widget_Photo_Centered;
                } else {
                    views.setViewVisibility(id_widget_Photo, View.GONE);
                    views.setViewVisibility(id_widget_Photo_Centered, View.GONE);
                    views.setViewVisibility(id_widget_Photo_Start, View.VISIBLE);
                    id_Photo = id_widget_Photo_Start;
                }

                //необходимо уменьшать, потому что вот: https://stackoverflow.com/questions/13494898/remoteviews-for-widget-update-exceeds-max-bitmap-memory-usage-error
                final int dstWidth = eventsToShow > 1 ? (4 * width / eventsToShow) : (2 * width);
                final int dstHeight = eventsToShow > 1 ? (4 * photo.getHeight() * width) / (photo.getWidth() * eventsToShow) : (2 * photo.getHeight() * width / photo.getWidth());
                if (dstHeight > 0 && dstWidth > 0) {
                    Bitmap photo_small = Bitmap.createScaledBitmap(photo, dstWidth, dstHeight, true);
                    views.setImageViewBitmap(id_Photo, photo_small);
                } else {
                    Bitmap photo_icon = eventsData.getContactPhoto(event, false, true, true, roundingFactor);
                    views.setImageViewBitmap(id_Photo, photo_icon);

                }
                //photo.recycle(); //https://stackoverflow.com/questions/38784302/cant-parcel-a-recycled-bitmap

            }

            //Иконка события
            int id_widget_EventIcon = resources.getIdentifier(WIDGET_ICON_EVENT_TYPE + eventsDisplayed, STRING_ID, packageName);

            if (widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_EventIcon)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_EventIcon)) {

                int eventIcon;
                try {
                    eventIcon = Integer.parseInt(singleEventArray[ContactsEvents.Position_eventIcon]);
                } catch (NumberFormatException e) {
                    eventIcon = 0;
                }
                if (eventIcon != 0) {
                    views.setImageViewResource(id_widget_EventIcon, eventIcon);
                } else {
                    views.setImageViewResource(id_widget_EventIcon, android.R.color.transparent);
                }

                views.setViewVisibility(id_widget_EventIcon, View.VISIBLE);

            } else {

                views.setViewVisibility(id_widget_EventIcon, View.GONE);

            }

            //Иконка знака зодиака
            //https://emojipedia.org/microsoft/windows-10-may-2019-update/aquarius/
            String strZodiacInfo = STRING_EMPTY;
            int id_widget_ZodiacIcon = resources.getIdentifier(WIDGET_ICON_ZODIAC + eventsDisplayed, STRING_ID, packageName);

            if (widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_ZodiacSign)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_ZodiacSign)) {

                if (eventSubType.equals(ContactsEvents.eventTypesIDs.get(Type_BirthDay)) || eventSubType.equals(ContactsEvents.eventTypesIDs.get(Type_5K))) {

                    strZodiacInfo = eventsData.getZodiacInfo(ContactsEvents.ZodiacInfo.SIGN, singleEventArray[Position_eventDateText]); //нам нужна только иконка

                } else if (eventsData.set_events_birthdays.containsKey(singleEventArray[Position_contactID])) {

                    Locale locale_en = new Locale(Constants.LANG_EN);
                    SimpleDateFormat sdfYear = new SimpleDateFormat(Constants.DATE_DD_MM_YYYY, locale_en);
                    final Date birthDate = eventsData.set_events_birthdays.get(singleEventArray[Position_contactID]);
                    if (birthDate != null) {
                        strZodiacInfo = eventsData.getZodiacInfo(ContactsEvents.ZodiacInfo.SIGN, sdfYear.format(birthDate));
                    }

                }
            }

            if (!strZodiacInfo.isEmpty()) {

                views.setTextViewText(id_widget_ZodiacIcon, strZodiacInfo);
                views.setViewVisibility(id_widget_ZodiacIcon, View.VISIBLE);

            } else{

                views.setViewVisibility(id_widget_ZodiacIcon, View.GONE);

            }

            //Иконка зодиакального года
            String strZodiacYearInfo = STRING_EMPTY;
            int id_widget_ZodiacYearIcon = resources.getIdentifier(WIDGET_ICON_ZODIAC_YEAR + eventsDisplayed, STRING_ID, packageName);

            if (widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_ZodiacYear)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_ZodiacYear)) {

            //if (eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_ZodiacYear)) {

                if (eventSubType.equals(ContactsEvents.eventTypesIDs.get(Type_BirthDay)) || eventSubType.equals(ContactsEvents.eventTypesIDs.get(Type_5K))) {

                    strZodiacYearInfo = eventsData.getZodiacInfo(ContactsEvents.ZodiacInfo.YEAR, singleEventArray[Position_eventDateText]); //нам нужна только иконка

                } else if (eventsData.set_events_birthdays.containsKey(singleEventArray[Position_contactID])) {

                    Locale locale_en = new Locale(Constants.LANG_EN);
                    SimpleDateFormat sdfYear = new SimpleDateFormat(Constants.DATE_DD_MM_YYYY, locale_en);
                    final Date birthDate = eventsData.set_events_birthdays.get(singleEventArray[Position_contactID]);
                    if (birthDate != null) {
                        strZodiacYearInfo = eventsData.getZodiacInfo(ContactsEvents.ZodiacInfo.YEAR, sdfYear.format(birthDate));
                    }

                }

            }

            if (!strZodiacYearInfo.isEmpty()) {

                views.setTextViewText(id_widget_ZodiacYearIcon, strZodiacYearInfo);
                views.setViewVisibility(id_widget_ZodiacYearIcon, View.VISIBLE);

            } else{

                views.setViewVisibility(id_widget_ZodiacYearIcon, View.GONE);

            }

            //Иконка фаворита
            int id_widget_FavIcon = resources.getIdentifier(WIDGET_ICON_FAV + eventsDisplayed, STRING_ID, packageName);
            if ((widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_FavoritesIcon)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_FavoritesIcon)) && singleEventArray[Position_starred].equals(STRING_1)) {

                views.setViewVisibility(id_widget_FavIcon, View.VISIBLE);

            } else {

                views.setViewVisibility(id_widget_FavIcon, View.GONE);

            }

            //Иконка события без уведомления
            int id_widget_SilencedIcon = resources.getIdentifier(WIDGET_ICON_SILENCED + eventsDisplayed, STRING_ID, packageName);
            if ((widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_SilencedIcon)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_SilencedIcon)) && eventsData.checkIsSilencedEvent(eventKey)) {

                views.setTextViewText(id_widget_SilencedIcon, "\uD83D\uDEAB"); //https://emojipedia.org/prohibited/
                views.setViewVisibility(id_widget_SilencedIcon, View.VISIBLE);

            } else {

                views.setViewVisibility(id_widget_SilencedIcon, View.GONE);

            }

            //Цвета по-умолчанию
            int id_widget_Age = resources.getIdentifier(WIDGET_TEXT_VIEW_AGE + eventsDisplayed, STRING_ID, packageName);

            views.setTextColor(id_widget_Age, colorDefault);
            views.setTextColor(id_widget_Caption_left, colorDefault);
            views.setTextColor(id_widget_Caption_centered, colorDefault);
            views.setTextColor(id_widget_Caption2nd_left, colorDefault);
            views.setTextColor(id_widget_Caption2nd_centered, colorDefault);

            //todo: сделать цвет тени зависымым от цвета текста
            //https://stackoverflow.com/questions/44417666/change-properties-of-view-inside-remoteview
            //https://stackoverflow.com/questions/6435648/any-way-to-set-the-text-shadow-for-a-spannablestring - не работает
            // @android.view.RemotableViewMethod
            //views.setInt(id_widget_Caption2nd_centered, "setShadowColor", resources.getColor(R.color.white));

            //Сколько осталось до события
            int id_widget_Distance = resources.getIdentifier(WIDGET_TEXT_VIEW_DISTANCE + eventsDisplayed, STRING_ID, packageName);
            String eventDistance = singleEventArray[ContactsEvents.Position_eventDistance];
            int eventDistance_Days;
            try {
                eventDistance_Days = Integer.parseInt(eventDistance);
            } catch (Exception e) {
                eventDistance_Days = 365;
            }

            if (eventDistance_Days == 0) { //Сегодня

                if (colorEventToday != 0) {
                    if (person.Age > -1) {
                        views.setTextColor(id_widget_Age, colorEventToday);
                        //views.setInt(context.getResources().getIdentifier("textViewAge" + i, "id", context.getPackageName()),"setShadowColor", context.getResources().getColor(R.color.white));
                    } else {
                        //Если возраста нет и событие уже сегодня - ставим цвет для ФИО
                        views.setTextColor(id_widget_Caption_left, colorEventToday);
                        views.setTextColor(id_widget_Caption_centered, colorEventToday);
                        views.setTextColor(id_widget_Caption2nd_left, colorEventToday);
                        views.setTextColor(id_widget_Caption2nd_centered, colorEventToday);
                    }
                }
                views.setTextViewText(id_widget_Distance, STRING_EMPTY);

            } else if (eventDistance_Days >= 1 && eventDistance_Days <= eventsData.preferences_widgets_days_eventsoon) { //Скоро

                views.setTextColor(id_widget_Distance, colorEventSoon);
                views.setTextViewText(id_widget_Distance, eventDistance);
                views.setTextViewTextSize(id_widget_Distance, TypedValue.COMPLEX_UNIT_SP, (float) (WIDGET_TEXT_SIZE_BIG * fontMagnify));

            } else { //Попозже

                views.setTextColor(id_widget_Distance, eventsData.preferences_widgets_days_eventsoon != 0 ? colorEventFar : colorDefault);
                views.setTextViewText(id_widget_Distance, eventDistance);
                views.setTextViewTextSize(id_widget_Distance, TypedValue.COMPLEX_UNIT_SP, (float) ((Integer.parseInt(eventDistance) < WIDGET_TEXT_SIZE_TINY ? WIDGET_TEXT_SIZE_BIG : WIDGET_TEXT_SIZE_SMALL) * fontMagnify));

            }

            //Возраст
            if (widgetPref_eventInfo.isEmpty() ? eventsData.preferences_widgets_event_info.contains(ContactsEvents.pref_Widgets_EventInfo_Age)
                    : widgetPref_eventInfo.contains(ContactsEvents.pref_Widgets_EventInfo_Age)) {

                if (eventSubType.equals(ContactsEvents.eventTypesIDs.get(Type_5K))) {
                    views.setTextViewText(id_widget_Age, singleEventArray[ContactsEvents.Position_age_caption]);
                } else if (person.Age > -1) {
                    views.setTextViewText(id_widget_Age, Integer.toString(person.Age));
                } else {
                    views.setTextViewText(id_widget_Age, STRING_EMPTY);
                }
                views.setTextColor(resources.getIdentifier(WIDGET_TEXT_VIEW + eventsDisplayed, STRING_ID, packageName), colorDefault);
                //views.setInt(context.getResources().getIdentifier("textViewAge" + i, "id", context.getPackageName()),"setShadowColor", context.getResources().getColor(R.color.dark_gray));
                views.setTextViewTextSize(id_widget_Age, TypedValue.COMPLEX_UNIT_SP, (float) ((eventDistance_Days == 0 ? WIDGET_TEXT_SIZE_BIG : WIDGET_TEXT_SIZE_SMALL) * fontMagnify));
                views.setViewVisibility(id_widget_Age, View.VISIBLE);

            } else {

                views.setViewVisibility(id_widget_Age, View.GONE);

            }

            //Если не последнее событие - по нажатию на фото открываем событие
            if (eventsToShow > 1 && eventsDisplayed < (eventsToShow - 1)) {

                Intent intent = null;

                if (eventsData.preferences_widgets_on_click_action == 7) { //Основной список событий
                    intent = new Intent(context, MainActivity.class);
                    intent.setAction(ACTION_LAUNCH);
                } else if (eventsData.preferences_widgets_on_click_action >= 1 & eventsData.preferences_widgets_on_click_action <=4) {
                    intent = ContactsEvents.getViewActionIntent(singleEventArray, eventsData.preferences_widgets_on_click_action);
                }

                if (intent != null) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    views.setOnClickPendingIntent(resources.getIdentifier(WIDGET_EVENT_INFO + eventsDisplayed, STRING_ID, packageName), PendingIntent.getActivity(context, 0, intent, PendingIntentImmutable));
                } else { //Ничего не показываем
                    views.setOnClickPendingIntent(resources.getIdentifier(WIDGET_EVENT_INFO + eventsDisplayed, STRING_ID, packageName), null);
                }

            } else {

                Intent intentConfig = new Intent(context, WidgetConfigureActivity.class);
                intentConfig.setAction(ACTION_LAUNCH);
                intentConfig.putExtra(PARAM_APP_WIDGET_ID, widgetId);
                if (visibleCell % 2 == 0) views.setOnClickPendingIntent(resources.getIdentifier(WIDGET_TEXT_VIEW + eventsDisplayed, STRING_ID, packageName), PendingIntent.getActivity(context, widgetId, intentConfig, PendingIntentImmutable));
                if (visibleCell % 3 == 0) views.setOnClickPendingIntent(resources.getIdentifier(WIDGET_TEXT_VIEW_CENTERED + eventsDisplayed, STRING_ID, packageName), PendingIntent.getActivity(context, widgetId, intentConfig, PendingIntentImmutable));
                if (visibleCell % 5 == 0) views.setOnClickPendingIntent(resources.getIdentifier(WIDGET_TEXT_VIEW_2_ND + eventsDisplayed, STRING_ID, packageName), PendingIntent.getActivity(context, widgetId, intentConfig, PendingIntentImmutable));
                if (visibleCell % 7 == 0) views.setOnClickPendingIntent(resources.getIdentifier(WIDGET_TEXT_VIEW_2_ND_CENTERED + eventsDisplayed, STRING_ID, packageName), PendingIntent.getActivity(context, widgetId, intentConfig, PendingIntentImmutable));

            }

            //Показываем событие
            views.setViewVisibility(resources.getIdentifier(WIDGET_EVENT_INFO + eventsDisplayed, STRING_ID, packageName), View.VISIBLE);
            eventsDisplayed++;

        } catch (Exception e) {
            e.printStackTrace();
            if (eventsData.preferences_debug_on) Toast.makeText(context, WIDGET_UPDATER_DRAW_EVENT_ERROR + e, Toast.LENGTH_LONG).show();
        }
    }

    private static int getCellsForSize(int size) {
        int n = 2;
        while (70 * n - 30 < (size)) {
            ++n;
        }
        return n - 1;
    }

}
