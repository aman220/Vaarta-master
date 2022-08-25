package com.example.application;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class PopupMenuCustomLayout {
    private PopupMenuCustomOnClickListener onClickListener;
    private Context context;
    private PopupWindow popupWindow;
    private int rLayoutId;
    private View popupView;

    public PopupMenuCustomLayout(Context context, int rLayoutId, PopupMenuCustomOnClickListener onClickListener) {
        this.context = context;
        this.onClickListener = onClickListener;
        this.rLayoutId = rLayoutId;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
        popupView = inflater.inflate(rLayoutId, null);
        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true;
        popupWindow = new PopupWindow(popupView, width, height, focusable);
        popupWindow.setElevation(10);

        LinearLayout linearLayout = (LinearLayout) popupView;
        for (int i = 0; i < linearLayout.getChildCount(); i++) {
            View v = linearLayout.getChildAt(i);
            v.setOnClickListener( v1 -> { onClickListener.onClick( v1.getId()); popupWindow.dismiss(); });
        }
    }
    public void setAnimationStyle( int animationStyle) {
        popupWindow.setAnimationStyle(animationStyle);
    }
    public void show() {
        popupWindow.showAtLocation( popupView, Gravity.CENTER, 0, 0);
    }

    public void show( View anchorView, int gravity, int offsetX, int offsetY) {
        popupWindow.showAsDropDown( anchorView, offsetX, -(anchorView.getHeight() + offsetY), gravity);
    }

    public void setMenuItemVisibility(int popup_menu_custom_item, boolean b) {
        ((LinearLayout)popupView).getChildAt(popup_menu_custom_item).setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setPadding(int popup_menu_custom_item,int top, int right, int bottom, int left) {
        ((LinearLayout)popupView).getChildAt(popup_menu_custom_item).setPadding(top, right, bottom, left);
    }

    public interface PopupMenuCustomOnClickListener {
        public void onClick(int menuItemId);
    }
}