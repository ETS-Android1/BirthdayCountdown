/*
 * *
 *  * Created by Vladimir Belov on 07.03.2022, 22:54
 *  * Copyright (c) 2018 - 2022. All rights reserved.
 *  * Last modified 07.03.2022, 20:52
 *
 */

package org.vovka.birthdaycountdown;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import java.util.List;

// На 1 событие масштабируемый
public class Widget2x2 extends AppWidgetProvider {

    private static void updateAppWidget(@NonNull Context context, @NonNull AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        try {

            ContactsEvents eventsData = ContactsEvents.getInstance();
            if (eventsData.getContext() == null) eventsData.setContext(context);
            eventsData.getPreferences();
            eventsData.setLocale(true);

            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            final AppWidgetProviderInfo appWidgetInfo = AppWidgetManager.getInstance(context).getAppWidgetInfo(appWidgetId);
            if (appWidgetInfo == null) return;
            String widgetType = appWidgetInfo.provider.getShortClassName();
            List<String> widgetPref = eventsData.getWidgetPreference(appWidgetId, widgetType);
            RemoteViews views = getRemoteViews(context);

            if (newOptions != null && eventsData.preferences_debug_on) {

                final Resources resources = context.getResources();
                final DisplayMetrics displayMetrics = resources.getDisplayMetrics();
                final float density = displayMetrics.density;

                ToastExpander.showText(context, Build.VERSION.SDK_INT < Build.VERSION_CODES.S ?
                        Widget5x1.class.getName() +
                                "\n appWidgetId=" + appWidgetId +
                                "\n screen: " + displayMetrics.heightPixels + "x" + displayMetrics.widthPixels + " (density " + density + ")" +
                                "\n layout=" + resources.getResourceEntryName(views.getLayoutId()) +
                                "\n minWidth=" + minWidth + ", minHeight=" + minHeight +
                                "\n widgetPref=" + widgetPref
                        : "id "+  appWidgetId + ":\n" + widgetPref
                );
            }

            new WidgetUpdater(context, ContactsEvents.getInstance(), views, 1, minWidth, minHeight, appWidgetId).invokePhotoEventsUpdate();
            appWidgetManager.updateAppWidget(appWidgetId, views);

        } catch (Exception e) {
            e.printStackTrace();
            ToastExpander.showText(context, Constants.WIDGET_2_X_2_UPDATE_APP_WIDGET_ERROR + e);
        }

    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            updateAppWidget(context, appWidgetManager, appWidgetId, null);

        }
    }

    @Override
    public void onDeleted (Context context, int[] appWidgetIds) {
        ContactsEvents eventsData = ContactsEvents.getInstance();

        for (int appWidgetId : appWidgetIds) {

            eventsData.removeWidgetPreference(appWidgetId);

        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {

        try {

            updateAppWidget(context, appWidgetManager, appWidgetId, newOptions);
            super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);

        } catch (Exception e) {
            e.printStackTrace();
            ToastExpander.showText(context, Constants.WIDGET_2_X_2_ON_APP_WIDGET_OPTIONS_CHANGED_ERROR + e);
        }
    }

    static private RemoteViews getRemoteViews(@NonNull Context context) {

        return new RemoteViews(context.getPackageName(), R.layout.widget2x2);

    }

}

