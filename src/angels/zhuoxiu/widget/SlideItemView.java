package angels.zhuoxiu.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SlideItemView extends FrameLayout {
	protected String tag = this.getClass().getSimpleName();

	public interface OnSildeListener {
		public void onSildeFinish(DIRECTION direction);
	}

	public enum DIRECTION {
		LEFT, RIGHT, MIDDLE
	};

	private Scroller scroller;
	int mTouchSlop;
	int downX, downY;
	float startAlpha = 1.0f, endAlpha = 0.0f;
	boolean enableSlideLeft = true, enableSlideRight = true;
	private static final int SNAP_VELOCITY = 600;
	private VelocityTracker velocityTracker;
	OnSildeListener listener;
	DIRECTION direction = DIRECTION.MIDDLE, position = DIRECTION.MIDDLE;
	int actionMoveCount;

	public SlideItemView(Context context) {
		this(context, null);
	} 

	public SlideItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		addVelocityTracker(ev);
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = (int) ev.getX();
			downY = (int) ev.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			break;
		case MotionEvent.ACTION_UP:
			// recycleVelocityTracker();
			break;
		}
		return super.dispatchTouchEvent(ev);
	}

	/** 
	 * 获取X方向的滑动速度,大于0向右滑动，反之向左 
	 *  
	 * @return 
	 */
	private int getScrollVelocity() {
		velocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) velocityTracker.getXVelocity();
		return velocity;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.i(tag, "onTouchEvent" + " action = " + ev.getAction());
		// requestDisallowInterceptTouchEvent(true);
		addVelocityTracker(ev);
		int x = (int) ev.getX(), y = (int) ev.getY();
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			actionMoveCount = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			actionMoveCount++;
			int deltaX = downX - x;
			if (deltaX > 0 && enableSlideLeft || deltaX < 0 && enableSlideRight) {
				scrollBy(deltaX, 0);
			}
			downX = x;
			downY = y;
			break;
		case MotionEvent.ACTION_UP:
			int velocityX = getScrollVelocity();
			if (velocityX > SNAP_VELOCITY) {
				scrollRight();
			} else if (velocityX < -SNAP_VELOCITY) {
				scrollLeft();
			} else {
				scrollByDistanceX();
			}
			recycleVelocityTracker();
			if (actionMoveCount <= 1) {
				performClick();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			scrollMiddle();
			recycleVelocityTracker();
			break;
		}

		return true;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void computeScroll() {
		// 调用startScroll的时候scroller.computeScrollOffset()返回true，
		if (scroller.computeScrollOffset()) {
			// 让ListView item根据当前的滚动偏移量进行滚动
			scrollTo(scroller.getCurrX(), scroller.getCurrY());
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
				float progress = (float) Math.abs(scroller.getCurrX()) / (float) getWidth();
				setAlpha(startAlpha * (1 - progress) + endAlpha * progress);
			}
			postInvalidate();
			// 滚动动画结束的时候调用回调接口
			if (scroller.isFinished()) {
				if (listener != null) {
					listener.onSildeFinish(direction);
				}
			}
		}
	}

	/** 
	 * scroll to right (negative value)
	 */
	public void scrollRight() {
		if (position == DIRECTION.MIDDLE) {
			direction = DIRECTION.RIGHT;
			position = DIRECTION.RIGHT;
			int deltaX = getWidth() + getScrollX();
			scroller.startScroll(getScrollX(), 0, -deltaX, 0, Math.abs(deltaX));
			postInvalidate(); // 刷新itemView
		} else if (position == DIRECTION.LEFT) {
			scrollMiddle();
		}
	}

	/** 
	 * scroll to left (positive value)
	 */
	@SuppressLint("NewApi")
	public void scrollLeft() {
		if (position == DIRECTION.MIDDLE) {
			direction = DIRECTION.LEFT;
			position = DIRECTION.LEFT;
			int deltaX = getScrollX() - getWidth();
			scroller.startScroll(getScrollX(), 0, -deltaX, 0, Math.abs(deltaX));
			postInvalidate(); // refresh
		} else if (position == DIRECTION.RIGHT) {
			scrollMiddle();
		}

	}

	public void scrollMiddle() {
		direction = DIRECTION.MIDDLE;
		position = DIRECTION.MIDDLE;
		scroller.startScroll(getScrollX(), getScrollY(), -getScrollX(), -getScrollY(), Math.abs(getScrollX()));
		postInvalidate();
	}

	private void scrollByDistanceX() {
		// 如果向左滚动的距离大于屏幕的二分之一，就让其删除
		if (getScrollX() >= getWidth() * 0.5) {
			scrollLeft();
		} else if (getScrollX() <= -getWidth() * 0.5) {
			scrollRight();
		} else {
			// 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动
			scrollMiddle();
		}

	}

	void addVelocityTracker(MotionEvent event) {
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}

		velocityTracker.addMovement(event);
	}

	void recycleVelocityTracker() {
		if (velocityTracker != null) {
			velocityTracker.recycle();
			velocityTracker = null;
		}
	}

	@Override
	public boolean performClick() {
		return super.performClick();
	}

	public void setEnableSlideLeft(boolean enable) {
		enableSlideLeft = enable;
	}

	public void setEnableSlideRight(boolean enable) {
		enableSlideRight = enable;
	}

	public void setOnSlideListener(OnSildeListener listener) {
		this.listener = listener;
	}
}
