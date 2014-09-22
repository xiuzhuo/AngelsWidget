package angels.zhuoxiu.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class SlideItemView extends FrameLayout {
	protected String tag = this.getClass().getSimpleName();

	public interface OnSildeListener {
		public void onSlideBegin(SlideItemView v, DIRECTION direction);

		public void onSildeFinish(SlideItemView v, DIRECTION direction);
	}

	public enum DIRECTION {
		LEFT, RIGHT, MIDDLE
	};

	private Scroller scroller;
	boolean isSlide = false;
	int mTouchSlop;
	int downX, downY;
	float startAlpha = 1.0f, endAlpha = 1.0f;
	boolean enableSlideLeft = true, enableSlideRight = true;
	private static final int SNAP_VELOCITY = 400;
	private VelocityTracker velocityTracker;
	OnSildeListener mListener;
	DIRECTION direction = DIRECTION.MIDDLE, position = DIRECTION.MIDDLE;
	int actionMoveCount;
	static int ANIM_TIME_SHORT = 1000;
	static int ANIM_TIME_MIDDLE = 2000;

	public SlideItemView(Context context) {
		this(context, null);
	}

	public SlideItemView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		ANIM_TIME_SHORT = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
		ANIM_TIME_MIDDLE = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
	}

	@SuppressLint("NewApi")
	public void animSlideLeftRight(AnimationListener al, final DIRECTION direction) {
		final int widthMeasure = getMeasuredWidth();
		final float xStart = getX();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 0) {

				} else if (interpolatedTime == 1) {
				} else {
					float xNew = 0;
					switch (direction) {
					case LEFT:
						xNew = -interpolatedTime * widthMeasure + (1 - interpolatedTime) * xStart;
						break;
					case RIGHT:
						xNew = interpolatedTime * widthMeasure + (1 - interpolatedTime) * xStart;
						break;
					}
					SlideItemView.this.setX(xNew);
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		if (al != null) {
			anim.setAnimationListener(al);
		}
		anim.setDuration(ANIM_TIME_MIDDLE);
		this.startAnimation(anim);
	}

	public void animHideShowView(AnimationListener al, final boolean show) {
		final int heightMeasure = getMeasuredHeight();
		Animation anim = new Animation() {
			@Override
			protected void applyTransformation(float interpolatedTime, Transformation t) {
				if (interpolatedTime == 0) {
					SlideItemView.this.setVisibility(show ? View.GONE : View.VISIBLE);
				} else if (interpolatedTime == 1) {
					SlideItemView.this.setVisibility(show ? View.VISIBLE : View.GONE);
				} else {
					int height;
					if (show) {
						height = (int) (heightMeasure * interpolatedTime);
					} else {
						height = heightMeasure - (int) (heightMeasure * interpolatedTime);
					}
					SlideItemView.this.getLayoutParams().height = height;
					SlideItemView.this.requestLayout();
				}
			}

			@Override
			public boolean willChangeBounds() {
				return true;
			}
		};

		if (al != null) {
			anim.setAnimationListener(al);
		}
		anim.setDuration(ANIM_TIME_MIDDLE);
		this.startAnimation(anim);
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
			if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY || (Math.abs(ev.getX() - downX) > mTouchSlop && Math.abs(ev.getY() - downY) < mTouchSlop)) {
				isSlide = true;
			}
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
		if (isSlide) {
			requestDisallowInterceptTouchEvent(true);
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
					scrollRight(mListener);
				} else if (velocityX < -SNAP_VELOCITY) {
					scrollLeft(mListener);
				} else {
					scrollByDistanceX();
				}
				recycleVelocityTracker();
				if (actionMoveCount <= 1) {
					performClick();
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				scrollMiddle(mListener);
				recycleVelocityTracker();
				isSlide = false;
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
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
				if (mListener != null) {
					mListener.onSildeFinish(this, direction);
				}
			}
		}
	}

	/** 
	 * scroll to right (negative value)
	 */
	public void scrollRight(OnSildeListener istener) {
		if (!scroller.isFinished() || !enableSlideRight) {
			return;
		}
		if (position == DIRECTION.MIDDLE) {
			direction = DIRECTION.RIGHT;
			position = DIRECTION.RIGHT;
			int deltaX = getWidth() + getScrollX();
			scroller.startScroll(getScrollX(), 0, -deltaX, 0, ANIM_TIME_MIDDLE);
			postInvalidate(); // 刷新itemView
		} else if (position == DIRECTION.LEFT) {
			scrollMiddle(mListener);
		}

		if (istener == null) {
			istener = mListener;
		}
		if (istener != null) {
			istener.onSlideBegin(this, direction);
		}

	}

	/** 
	 * scroll to left (positive value)
	 */
	@SuppressLint("NewApi")
	public void scrollLeft(OnSildeListener istener) {
		if (!scroller.isFinished() || !enableSlideLeft) {
			return;
		}
		if (position == DIRECTION.MIDDLE) {
			direction = DIRECTION.LEFT;
			position = DIRECTION.LEFT;
			int deltaX = getScrollX() - getWidth();
			scroller.startScroll(getScrollX(), 0, -deltaX, 0, ANIM_TIME_MIDDLE);
			postInvalidate(); // refresh
		} else if (position == DIRECTION.RIGHT) {
			scrollMiddle(mListener);
		}

		if (istener == null) {
			istener = mListener;
		}

		if (istener != null) {
			istener.onSlideBegin(this, direction);
		}

	}

	public void scrollMiddle(OnSildeListener istener) {
		if (!scroller.isFinished()) {
			return;
		}
		direction = DIRECTION.MIDDLE;
		position = DIRECTION.MIDDLE;
		scroller.startScroll(getScrollX(), getScrollY(), -getScrollX(), -getScrollY(), ANIM_TIME_MIDDLE);
		postInvalidate();

		if (istener == null) {
			istener = mListener;
		}
		if (istener != null) {
			istener.onSlideBegin(this, direction);
		}
	}

	/**
	 * If slide more than half of the width, then slide it out
	 */
	private void scrollByDistanceX() {
		// 如果向左滚动的距离大于屏幕的二分之一，就让其删除
		if (getScrollX() >= getWidth() * 0.5) {
			scrollLeft(mListener);
		} else if (getScrollX() <= -getWidth() * 0.5) {
			scrollRight(mListener);
		} else {
			// 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动
			scrollMiddle(mListener);
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
		this.mListener = listener;
	}
}
