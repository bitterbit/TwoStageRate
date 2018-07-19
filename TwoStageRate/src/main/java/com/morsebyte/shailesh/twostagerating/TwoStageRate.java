package com.morsebyte.shailesh.twostagerating;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.morsebyte.shailesh.twostagerating.dialog.ConfirmRateDialog;
import com.morsebyte.shailesh.twostagerating.dialog.FeedbackDialog;
import com.morsebyte.shailesh.twostagerating.dialog.RatePromptDialog;

import java.util.Date;

/**
 * Created by Shailesh on 2/5/16.
 */
public class TwoStageRate {

    private static final String LAUNCH_COUNT = "TWOSTAGELAUNCHCOUNT";
    private static final String INSTALL_DAYS = "TWOSTAGEINSTALLDAYS";
    private static final String INSTALL_DATE = "TWOSTAGEINSTALLDATE";
    private static final String EVENT_COUNT = "TWOSTAGEEVENTCOUNT";
    private static final String STOP_TRACK = "TWOSTAGESTOPTRACK";

    private static final String SHARED_PREFERENCES_SHOW_ICON_KEY = "TwoStageRateShowAppIcon";
    private static final String SHARED_PREFERENCES_SHOULD_RESET_ON_DISMISS = "TwoStageRateShouldRefreshOnPrimaryDISMISS";
    private static final String SHARED_PREFERENCES_SHOULD_RESET_ON_RATING_DECLINED = "TwoStageRateShouldResetOnDecliningToRate";
    private static final String SHARED_PREFERENCES_SHOULD_RESET_ON_FEEDBACK_DECLINED = "TwoStageRateShouldResetOnDecliningForFeedBack";

    private static final String SHARED_PREFERENCES_TOTAL_LAUNCH_TIMES = "TwoStageRateTotalLaunchTimes";
    private static final String SHARED_PREFERENCES_TOTAL_EVENTS_COUNT = "TwoStageRateTotalEventCount";
    private static final String SHARED_PREFERENCES_TOTAL_INSTALL_DAYS = "TwoStageRateTotalInstallDays";


    private RatePromptDialog ratePromptDialog = new RatePromptDialog();
    private FeedbackDialog feedbackDialog = new FeedbackDialog();
    private ConfirmRateDialog confirmRateDialog = new ConfirmRateDialog();
    private Settings settings = new Settings();
    private boolean isDebug = false;
    private Context mContext;
    private float lastRating;

    private FeedbackListener feedbackListener;

    public interface FeedbackListener {
        void onRatePromptSubmit(float rating);
        void onNegativeFeedbackSubmit(float rating, String message);
        void onPositiveFeedbackSubmit(float rating);
    }

    private TwoStageRate(Context context) {
        this.mContext = context;
    }

    private static TwoStageRate instance;

    public static TwoStageRate with(Context context) {
        if (instance == null){
            instance = new TwoStageRate(context);
        }

        instance.setUpSettingsIfNotExists(context);
        instance.mContext = context;
        return instance;
    }

    /**
     * Sets up setting items if they are in preferences. Else it just sets the default values
     */
    private void setUpSettingsIfNotExists(Context context) {
        settings.setEventsTimes(Utils.getIntSystemValue(SHARED_PREFERENCES_TOTAL_EVENTS_COUNT, context, 10));
        settings.setInstallDays(Utils.getIntSystemValue(SHARED_PREFERENCES_TOTAL_INSTALL_DAYS, context, 5));
        settings.setLaunchTimes(Utils.getIntSystemValue(SHARED_PREFERENCES_TOTAL_LAUNCH_TIMES, context, 5));
    }

    /**
     * Checks if the conditions are met (anu one ) and shows prompt if yes.
     * But before it checks whether it has already shown the prompt and user has responded
     * <p>
     * Also it always shows up in Debug mode
     */
    public void showIfMeetsConditions() {

        if (!Utils.getBooleanSystemValue(STOP_TRACK, mContext)) {
            if (checkIfMeetsCondition() || isDebug) {
                showRatePromptDialog();
                Utils.setBooleanSystemValue(STOP_TRACK, true, mContext);

            } else {
                track();
            }
        }
    }

    private void track() {

    }

    private boolean checkIfMeetsCondition() {
        return isOverLaunchTimes() ||
                isOverInstallDays() || isOverEventCounts();

    }

    public void showRatePromptDialog() {
        Dialog dialog = getRatePromptDialog(mContext, ratePromptDialog, settings.getThresholdRating());
        if (dialog != null) {
            dialog.show();
        }

    }

    /**
     * Setting install date of app
     */
    public void setInstallDate() {

        if (Utils.getLongSystemValue(INSTALL_DATE, mContext) == 0) {
            //getting the current time in milliseconds, and creating a Date object from it:
            Date date = new Date(System.currentTimeMillis()); //or simply new Date();
            long millis = date.getTime();
            Utils.setLongSystemValue(INSTALL_DATE, millis, mContext);
        }
    }

    private boolean isOverLaunchTimes() {
        int launches = Utils.getIntSystemValue(LAUNCH_COUNT, mContext);
        if (launches >= settings.getLaunchTimes()) {
            return true;
        } else {
            int count = Utils.getIntSystemValue(LAUNCH_COUNT, mContext) + 1;
            Utils.setIntSystemValue(LAUNCH_COUNT, count, mContext);
            return false;
        }

    }

    private boolean isOverInstallDays() {
        if (Utils.getLongSystemValue(INSTALL_DATE, mContext) == 0) {
            setInstallDate();
            return false;
        } else {
            Date installDate = new Date(Utils.getLongSystemValue(INSTALL_DATE, mContext));
            Date currentDate = new Date(System.currentTimeMillis());
            long days = Utils.daysBetween(installDate, currentDate);
            if (days >= settings.getInstallDays()) {
                return true;
            } else {
                return false;
            }
        }

    }

    private boolean isOverEventCounts() {
        if (Utils.getIntSystemValue(EVENT_COUNT, mContext) >= settings.getEventsTimes()) {
            return true;
        } else {
            return false;
        }
    }

    public void incrementEvent() {
        int eventCount = Utils.getIntSystemValue(EVENT_COUNT, mContext) + 1;
        Utils.setIntSystemValue(EVENT_COUNT, eventCount, mContext);
    }

    public void isDebug(Boolean isDeb) {
        isDebug = isDeb;
    }

    /**
     * It would be called when user says remind me later.
     */
    private void resetTwoStage() {
        //set current date
        //getting the current time in milliseconds, and creating a Date object from it:
        Date date = new Date(System.currentTimeMillis()); //or simply new Date();
        long millis = date.getTime();
        Utils.setLongSystemValue(INSTALL_DATE, millis, mContext);
        Utils.setIntSystemValue(INSTALL_DAYS, 0, mContext);
        Utils.setIntSystemValue(EVENT_COUNT, 0, mContext);
        Utils.setIntSystemValue(LAUNCH_COUNT, 0, mContext);

        Utils.setBooleanSystemValue(STOP_TRACK, false, mContext);

    }

    private Dialog getRatePromptDialog(final Context context, final RatePromptDialog ratePromptDialog, final float threshold) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_rate_initial);
        dialog.setCancelable(this.ratePromptDialog.isDismissible());

        // set the custom dialog components - text, image and button
        TextView title = (TextView) dialog.findViewById(R.id.tvRatePromptTitle);
        title.setText(ratePromptDialog.getTitle());
        final RatingBar rbRating = (RatingBar) dialog.findViewById(R.id.rbRatePromptBar);
        ImageView ivAppIcon = (ImageView) dialog.findViewById(R.id.ivAppIcon);

        if ((Utils.getBooleanSystemValue(SHARED_PREFERENCES_SHOW_ICON_KEY, context, true))) {
            ivAppIcon.setImageResource(Utils.twoStageGetAppIconResourceId(context));
            ivAppIcon.setVisibility(View.VISIBLE);
        } else {
            ivAppIcon.setVisibility(View.GONE);
        }

        final TextView tvRatePromptSubmit = (TextView)dialog.findViewById(R.id.tvRatePromptSubmit);
        tvRatePromptSubmit.setText(ratePromptDialog.rateSubmit);

        tvRatePromptSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(feedbackListener != null){
                    feedbackListener.onRatePromptSubmit(lastRating);
                }

                if (lastRating > threshold) {
                    Dialog dialog1 = getConfirmRateDialog(context, confirmRateDialog, new PositiveFeedbackDialogListener() {
                        @Override
                        public void submit() {
                            feedbackListener.onPositiveFeedbackSubmit(lastRating);
                        }
                    });

                    Log.i("MYTAG", "last rating > threshold " + feedbackListener);

                    if (dialog1 != null) {
                        dialog1.show();
                    }
                } else {
                    Dialog dialog1 = getFeedbackDialog(context, feedbackDialog, new NegativeFeedbackDialogListener() {
                        @Override
                        public void onSubmit(String feedback) {
                            if (feedbackListener != null) {
                                feedbackListener.onNegativeFeedbackSubmit(lastRating, feedback);
                            }
                        }
                    });
                    if (dialog1 != null) {
                        dialog1.show();
                    }
                }
                dialog.dismiss();

            }
        });

        rbRating.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, final float rating, boolean fromUser) {
                tvRatePromptSubmit.setEnabled(true);
                //tvRatePromptSubmit.setTextColor(context.getResources().getColor());
                tvRatePromptSubmit.setTextColor(ContextCompat.getColor(context, R.color.rate_prompt_button_text_color));
                lastRating = rating;
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogDismissed();
            }
        });

        return dialog;
    }

    public Dialog getConfirmRateDialog(final Context context, final ConfirmRateDialog confirmRateDialog, final PositiveFeedbackDialogListener listener) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_confirm_rate);
        dialog.setCancelable(this.confirmRateDialog.isDismissible());

        // set the custom dialog components - text, image and button
        ((TextView) dialog.findViewById(R.id.tvConfirmRateTitle)).setText(confirmRateDialog.getTitle());
        ((TextView) dialog.findViewById(R.id.tvConfirmRateText)).setText(confirmRateDialog.getDescription());

        TextView deny = (TextView) dialog.findViewById(R.id.tvConfirmDeny);
        deny.setText(confirmRateDialog.getNegativeText());


        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Resetting twostage if declined and setting is done so
                if ((Utils.getBooleanSystemValue(SHARED_PREFERENCES_SHOULD_RESET_ON_RATING_DECLINED, mContext, false))) {
                    resetTwoStage();
                }
                dialog.dismiss();
            }

        });

        TextView submit = (TextView) dialog.findViewById(R.id.tvConfirmSubmit);
        submit.setText(confirmRateDialog.getPositiveText());
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.submit();
                final Intent intentToAppstore = settings.getStoreType() == Settings.StoreType.GOOGLEPLAY ?
                        IntentHelper.createIntentForGooglePlay(context) : IntentHelper.createIntentForAmazonAppstore(context);
                context.startActivity(intentToAppstore);
                dialog.dismiss();

            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogDismissed();
            }
        });


        return dialog;
    }

    public Dialog getFeedbackDialog(final Context context, final FeedbackDialog feedbackDialog, final NegativeFeedbackDialogListener listener) {
        // custom dialog
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(this.feedbackDialog.isDismissible());
        dialog.setContentView(R.layout.dialog_feedback);

        // set the custom dialog components - text, image and button
        TextView title = (TextView) dialog.findViewById(R.id.tvFeedbackTitle);
        title.setText(feedbackDialog.getTitle());
        TextView text = (TextView) dialog.findViewById(R.id.tvFeedbackText);
        text.setText(feedbackDialog.getDescription());
        TextView deny = (TextView) dialog.findViewById(R.id.tvFeedbackDeny);
        deny.setText(feedbackDialog.getNegativeText());
        final EditText etFeedback = (EditText) dialog.findViewById(R.id.etFeedback);
        TextView submit = (TextView) dialog.findViewById(R.id.tvFeedbackSubmit);
        submit.setText(feedbackDialog.getPositiveText());
        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ToDo : emit something here
                //Reseting twostage if declined and setting is done so
                if ((Utils.getBooleanSystemValue(SHARED_PREFERENCES_SHOULD_RESET_ON_FEEDBACK_DECLINED, mContext, false))) {
                    resetTwoStage();
                }
                dialog.dismiss();
            }

        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (etFeedback.getText() != null && etFeedback.getText().length() > 0) {
                    dialog.dismiss();
                    listener.onSubmit(etFeedback.getText().toString());
                } else {
                    Toast.makeText(context, feedbackDialog.feedbackPromptEmptyText, Toast.LENGTH_LONG).show();
                }
            }
        });

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                onDialogDismissed();
            }
        });


        return dialog;
    }

    /**
     * Setter and getters for rate prompt dialog
     *
     * @param ratePromptTitle
     */

    public TwoStageRate setRatePromptTitle(String ratePromptTitle) {
        this.ratePromptDialog.ratePromptTitle = ratePromptTitle;
        return this;
    }

    public TwoStageRate setRatePromptSubmitText(String submitText){
        this.ratePromptDialog.rateSubmit = submitText;
        return this;
    }

    public TwoStageRate setRatePromptDismissible(boolean dismissible) {
        this.ratePromptDialog.dismissible = dismissible;
        return this;
    }

    public TwoStageRate setFeedbackDialogTitle(String feedbackPromptTitle) {
        this.feedbackDialog.feedbackPromptTitle = feedbackPromptTitle;
        return this;
    }

    public TwoStageRate setFeedbackDialogDescription(String feedbackPromptText) {
        this.feedbackDialog.feedbackPromptText = feedbackPromptText;
        return this;
    }

    public TwoStageRate setFeedbackDialogPositiveText(String feedbackPromptPositiveText) {
        this.feedbackDialog.feedbackPromptPositiveText = feedbackPromptPositiveText;
        return this;
    }


    public TwoStageRate setFeedbackDialogNegativeText(String feedbackPromptNegativeText) {
        this.feedbackDialog.feedbackPromptNegativeText = feedbackPromptNegativeText;
        return this;
    }

    public TwoStageRate setFeedbackDialogDismissible(boolean dismissible) {
        this.feedbackDialog.dismissible = dismissible;
        return this;
    }

    public TwoStageRate setFeedbackDialogEmptyText(String text){
        this.feedbackDialog.feedbackPromptEmptyText = text;
        return this;
    }

    public TwoStageRate setListener(FeedbackListener listener){
        feedbackListener = listener;
        return this;
    }

    private void onDialogDismissed() {
        if ((Utils.getBooleanSystemValue(SHARED_PREFERENCES_SHOULD_RESET_ON_DISMISS, mContext, true))) {
            resetTwoStage();
        }
    }

    /*
     *All setters for ConfirmRateDialog
     */

    public TwoStageRate setConfirmRateDialogTitle(String confirmRateTitle) {
        this.confirmRateDialog.confirmRateTitle = confirmRateTitle;
        return this;
    }


    public TwoStageRate setConfirmRateDialogDescription(String confirmRateText) {
        this.confirmRateDialog.confirmRateText = confirmRateText;
        return this;
    }

    public TwoStageRate setConfirmRateDialogNegativeText(String confirmRateNegativeText) {
        this.confirmRateDialog.confirmRateNegativeText = confirmRateNegativeText;
        return this;
    }

    public TwoStageRate setConfirmRateDialogPositiveText(String confirmRatePositiveText) {
        this.confirmRateDialog.confirmRatePositiveText = confirmRatePositiveText;
        return this;
    }

    public TwoStageRate setConfirmRateDialogDismissible(boolean dismissible) {
        this.confirmRateDialog.dismissible = dismissible;
        return this;
    }

    /**
     * Setters for Settings
     */

    public TwoStageRate setInstallDays(int installDays) {
        Utils.setIntSystemValue(SHARED_PREFERENCES_TOTAL_INSTALL_DAYS, installDays, mContext);
        this.settings.installDays = installDays;
        return this;
    }

    public TwoStageRate setLaunchTimes(int launchTimes) {
        Utils.setIntSystemValue(SHARED_PREFERENCES_TOTAL_LAUNCH_TIMES, launchTimes, mContext);
        this.settings.launchTimes = launchTimes;
        return this;
    }

    public TwoStageRate setEventsTimes(int eventsTimes) {
        Utils.setIntSystemValue(SHARED_PREFERENCES_TOTAL_EVENTS_COUNT, eventsTimes, mContext);
        this.settings.eventsTimes = eventsTimes;
        return this;
    }


    public TwoStageRate setThresholdRating(float thresholdRating) {
        this.settings.thresholdRating = thresholdRating;
        return this;
    }

    public TwoStageRate setStoreType(Settings.StoreType storeType) {
        this.settings.storeType = storeType;
        return this;
    }

    public TwoStageRate resetOnDismiss(boolean shouldReset) {
        Utils.setBooleanSystemValue(SHARED_PREFERENCES_SHOULD_RESET_ON_DISMISS, shouldReset, mContext);
        return this;
    }

    /**
     * User gave good rating at first but declined to rate on playstore
     * @param shouldReset
     * @return
     */
    public TwoStageRate resetOnRatingDeclined(boolean shouldReset) {
        Utils.setBooleanSystemValue(SHARED_PREFERENCES_SHOULD_RESET_ON_RATING_DECLINED, shouldReset, mContext);
        return this;
    }

    public TwoStageRate resetOnFeedBackDeclined(boolean shouldReset) {
        Utils.setBooleanSystemValue(SHARED_PREFERENCES_SHOULD_RESET_ON_FEEDBACK_DECLINED, shouldReset, mContext);
        return this;
    }

    public TwoStageRate setShowAppIcon(boolean showAppIcon) {
        Utils.setBooleanSystemValue(SHARED_PREFERENCES_SHOW_ICON_KEY, showAppIcon, mContext);
        return this;
    }


}
