package angel.zhuoxiu.widget.color;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by zxui on 29/04/15.
 */
class ColorPickerCell extends View {
    public ColorPickerCell(Context context) {
        super(context);
    }

    public ColorPickerCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPickerCell(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(0, widthMeasureSpec), getDefaultSize(0, heightMeasureSpec));
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        Log.i("ColorPickerCell", "width=" + width + " height=" + height);
        int size = Math.max(width, height);
        widthMeasureSpec = heightMeasureSpec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
