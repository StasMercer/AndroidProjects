package com.example.android.firebasemessenger;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;

import com.mindorks.placeholderview.annotations.Click;
import com.mindorks.placeholderview.annotations.Layout;
import com.mindorks.placeholderview.annotations.Resolve;
import com.mindorks.placeholderview.annotations.View;

/**
 * Created by Mercer on 23.02.2018.
 */

@Layout(R.layout.drawer_item)
public class DrawerMenuItem {

    public static final int DRAWER_MENU_ITEM_PROFILE = 1;
    /* public static final int DRAWER_MENU_ITEM_MESSAGE = 4;
     public static final int DRAWER_MENU_ITEM_NOTIFICATIONS = 5;*/
    public static final int DRAWER_MENU_ITEM_SETTINGS = 6;
    public static final int DRAWER_MENU_ITEM_LOGOUT = 8;

    private int mMenuPosition;
    private Context mContext;
    private DrawerCallBack mCallBack;

    @View(R.id.itemNameTxt)
    private TextView itemNameTxt;

    @View(R.id.itemIcon)
    private ImageView itemIcon;

    public DrawerMenuItem(Context context, int menuPosition, DrawerCallBack callBack) {
        mContext = context;
        mMenuPosition = menuPosition;
        mCallBack = callBack;
    }


    @Resolve
    private void onResolved() {
        switch (mMenuPosition) {
            case DRAWER_MENU_ITEM_PROFILE:
                itemNameTxt.setText(R.string.Profile);
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_account_circle_black_18dp));
                break;
           /* case DRAWER_MENU_ITEM_MESSAGE:
                itemNameTxt.setText("Messages");
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_email_black_18dp));
                break;*/
           /* case DRAWER_MENU_ITEM_NOTIFICATIONS:
                itemNameTxt.setText("Notifications");
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_notifications_black_18dp));
                break;
            case DRAWER_MENU_ITEM_SETTINGS:
                itemNameTxt.setText(R.string.Settings);
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_settings_black_18dp));
                break;
                */
            case DRAWER_MENU_ITEM_LOGOUT:
                itemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_exit_to_app_black_18dp));
                itemNameTxt.setText(R.string.Logout);
                break;
        }
    }

    @Click(R.id.mainView)
    private void onMenuItemClick() {
        switch (mMenuPosition) {
            case DRAWER_MENU_ITEM_PROFILE:
                //Toast.makeText(mContext, "Profile", Toast.LENGTH_SHORT).show();
                if (mCallBack != null) mCallBack.onProfileMenuSelected();
                break;
           /* case DRAWER_MENU_ITEM_MESSAGE:
                Toast.makeText(mContext, "Messages", Toast.LENGTH_SHORT).show();
                mCallBack.onMessagesMenuSelected();
                break;
            case DRAWER_MENU_ITEM_NOTIFICATIONS:
                Toast.makeText(mContext, "Notifications", Toast.LENGTH_SHORT).show();
                if(mCallBack != null)mCallBack.onNotificationsMenuSelected();
                break;
            case DRAWER_MENU_ITEM_SETTINGS:

                if (mCallBack != null) mCallBack.onSettingsMenuSelected();
                break;
                */
            case DRAWER_MENU_ITEM_LOGOUT:

                if (mCallBack != null) mCallBack.onLogoutMenuSelected();
                break;
        }
    }


    public interface DrawerCallBack {
        void onProfileMenuSelected();

        //in future
        /*void onMessagesMenuSelected();
        void onNotificationsMenuSelected();
        void onSettingsMenuSelected();
*/
        void onLogoutMenuSelected();
    }


}


