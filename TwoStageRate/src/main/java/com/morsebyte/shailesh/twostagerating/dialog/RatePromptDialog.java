package com.morsebyte.shailesh.twostagerating.dialog;

import android.app.Dialog;
import android.content.Context;
import android.preference.TwoStatePreference;
import android.view.Window;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.morsebyte.shailesh.twostagerating.AppRateDataModel;
import com.morsebyte.shailesh.twostagerating.R;

/**
 * Created by Shailesh on 2/8/16.
 */
public class RatePromptDialog {

    public static String ratePromptTitle = "How would you rate our app?";
    public static String rateSubmit = "Submit";
    public boolean dismissible = true;

    public void setDismissible(boolean dismissible)
    {
        this.dismissible = dismissible;
    }

    public boolean isDismissible()
    {
        return this.dismissible;
    }

    public void setTitle(String ratePromptTitle)
    {
        this.ratePromptTitle =ratePromptTitle;
    }

    public String getTitle( )
    {
        return this.ratePromptTitle;
    }







}
