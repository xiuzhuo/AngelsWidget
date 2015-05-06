package angel.zhuoxiu.widget.color;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.preference.CheckBoxPreference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


public class ColorPickerPreference extends CheckBoxPreference implements ColorPickerConstant {

    String tag = this.getClass().getSimpleName();
    Dialog dialog = null;
    View checkboxView;
    View colorBox;
    int color = Color.TRANSPARENT;
    ColorPickerDialog.Builder builder;
    ColorPickerListener colorPickerListener;

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        builder = new ColorPickerDialog.Builder(context);
        builder.setColors(defaultColors);
        builder.setCancelable(true);
        builder.setColorPickerListener(new ColorPickerListener() {
            @Override
            public void onColorPicked(int index, int color) {
                setColor(color);
                dialog.dismiss();
                if (colorPickerListener != null) {
                    colorPickerListener.onColorPicked(index, color);
                }
            }
        });

    }

    public void setColorPickerListener(ColorPickerListener colorPickerListener) {
        this.colorPickerListener = colorPickerListener;
    }

    public void setColors(int... colors) {
        builder.setColors(colors);
    }

    @Override
    protected void onClick() {
        dialog = builder.show();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        checkboxView = view.findViewById(Resources.getSystem().getIdentifier("checkbox", "id", "android"));
        checkboxView.setVisibility(View.INVISIBLE);
        ((ViewGroup)checkboxView.getParent()).removeView(checkboxView);
        FrameLayout frameLayout = new FrameLayout(view.getContext());
        frameLayout.setLayoutParams(checkboxView.getLayoutParams());
        frameLayout.addView(checkboxView);
        ((ViewGroup) view).addView(frameLayout);
        colorBox = frameLayout;
        colorBox.setBackgroundColor(color);
    }

    public void setColor(int color) {
        this.color = color;
        if (colorBox != null) {
            colorBox.setBackgroundColor(color);
        }
    }

    public int getColor() {
        return color;
    }
}
