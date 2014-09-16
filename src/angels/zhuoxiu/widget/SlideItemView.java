package angels.zhuoxiu.widget;

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

	public interface OnSildeListener {
		public void onSildeFinish(DIRECTION direction);
	}

	public enum DIRECTION {
		LEFT, RIGHT, MIDDLE
	};

	String tag = this.getClass().getSimpleName();
	private Scroller scroller;
	int mTouchSlop;
	int downX, downY;
	float startAlpha = 1.0f, endAlpha = 0.2f;
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
		Log.i(tag, "onInterceptTouchEvent" + " action = " + ev.getAction());
		return true;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.i(tag, "dispatchTouchEvent" + " action = " + ev.getAction());
		int x = (int) ev.getX(), y = (int) ev.getY();
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
			Log.i(tag, "deltaX = " + deltaX);
			if (deltaX > 0 && enableSlideLeft || deltaX < 0 && enableSlideRight) {
				scrollBy(deltaX, 0);
			}
			downX = x;
			break;
		case MotionEvent.ACTION_UP:
			int velocityX = getScrollVelocity();
			Log.d(tag, "velocityX = " + velocityX);
			if (velocityX > SNAP_VELOCITY) {
				scrollRight();
			} else if (velocityX < -SNAP_VELOCITY) {
				scrollLeft();
			} else {
				scrollByDistanceX();
			}
			recycleVelocityTracker();
			if (actionMoveCount == 0) {
				performClick();
			}
			break;
		case MotionEvent.ACTION_CANCEL:
			scrollBack();
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

	private void scrollRight() {
		int deltaX = 0;
		if (position == DIRECTION.MIDDLE) {
			direction = DIRECTION.RIGHT;
			position = DIRECTION.RIGHT;
			deltaX = getWidth() + getScrollX();
		} else if (position == DIRECTION.LEFT) {
			direction = DIRECTION.MIDDLE;
			position = DIRECTION.MIDDLE;
			deltaX = getScrollX();
		}
		Log.wtf(tag, "getScrollX()=" + getScrollX() + " position=" + position + " direction=" + direction);
		// 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
		scroller.startScroll(getScrollX(), 0, -deltaX, 0, Math.abs(deltaX));
		postInvalidate(); // 刷新itemView
	}

	/** 
	 * 向左滑动，根据上面我们知道向左滑动为正值 
	 */
	private void scrollLeft() {
		direction = DIRECTION.LEFT;
		int deltaX = 0;
		if (position == DIRECTION.MIDDLE) {
			direction = DIRECTION.LEFT;
			position = DIRECTION.LEFT;
			deltaX = getScrollX() - getWidth();
		} else if (position == DIRECTION.RIGHT) {
			direction = DIRECTION.MIDDLE;
			position = DIRECTION.MIDDLE;
			deltaX = getScrollX();
		}

		// final int delta = (getWidth() - getScrollX());
		// 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
		scroller.startScroll(getScrollX(), 0, -deltaX, 0, Math.abs(deltaX));
		postInvalidate(); // 刷新itemView
	}

	void scrollBack() {
		direction = DIRECTION.MIDDLE;
		position = DIRECTION.MIDDLE;
		scroller.startScroll(getScrollX(), getScrollY(), -getScrollX(), -getScrollY());
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
			scrollBack();
		}

	}

	void addVelocityTracker(MotionEvent event) {
		Log.i(tag, "velocityTracker add");
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}

		velocityTracker.addMovement(event);
	}

	void recycleVelocityTracker() {
		Log.i(tag, "velocityTracker recycle");
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
