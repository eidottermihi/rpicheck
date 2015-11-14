package de.eidottermihi.rpicheck.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;

import static android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID;
import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;

/**
 * @author eidottermihi
 */
public class WidgetUpdateService extends IntentService {


    public WidgetUpdateService() {
        super("WidgetUpdateService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int incomingAppWidgetId = intent.getIntExtra(EXTRA_APPWIDGET_ID,
                INVALID_APPWIDGET_ID);
        if (incomingAppWidgetId != INVALID_APPWIDGET_ID) {
            // TODO code for query and widget update
        }
    }
}
