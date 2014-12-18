package angels.zhuoxiu.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * A Layout which allow child view float to a direction
 * @author Zhuo Xiu
 *
 */
public class FloatLayout extends FrameLayout {
	String tag = this.getClass().getSimpleName();
	int[][] params;

	public FloatLayout(Context context) {
		this(context, null);
	}

	public FloatLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public FloatLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		Log.d(tag, "onMeasure");
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		Log.d(tag, "onLayout getChildCount()=" + getChildCount() + " l=" + l + " t=" + t + " r=" + r + " b=" + b);
		int width = r - l - getPaddingLeft() - getPaddingRight();
		int height = b - t - getPaddingTop() - getPaddingBottom();
		Log.d(tag, "onLayout width =" + width + " height=" + height);
		int minX = getPaddingLeft();
		int maxX = width + getPaddingLeft();
		int minY = getPaddingTop();
		int offSetX = minX; 
		int offSetY = minY;
		int lineMaxHeight = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			int w = child.getMeasuredWidth();
			int h = child.getMeasuredHeight();
			ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
			lineMaxHeight = Math.max(lineMaxHeight, h + params.topMargin + params.bottomMargin);
			if (offSetX > minX && offSetX + w + params.leftMargin + params.rightMargin > maxX) {
				offSetX = minX;
				offSetY += lineMaxHeight;
				lineMaxHeight = h;
			}
			Log.i(tag, "child " + i + " w=" + w + " h=" + h + " offSetX=" + offSetX + " offSetY=" + offSetY);
			child.layout(offSetX + params.leftMargin, offSetY + params.topMargin, offSetX + params.leftMargin + w, offSetY + params.topMargin + h);
			offSetX += w + params.leftMargin + params.rightMargin;
			Log.i(tag, "offSetX=" + offSetX);
		}

	}
}
