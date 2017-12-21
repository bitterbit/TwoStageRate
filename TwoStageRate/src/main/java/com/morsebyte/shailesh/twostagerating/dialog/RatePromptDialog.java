package com.morsebyte.shailesh.twostagerating.dialog;


/**
 * Created by Shailesh on 2/8/16.
 */
public class RatePromptDialog {

    public String ratePromptTitle = "How would you rate our app?";
    public String rateSubmit = "Submit";
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
