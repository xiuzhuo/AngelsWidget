package angels.zhuoxiu.widget;

import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Adapter;
import android.widget.ListView;

public class AnimationListView extends ListView implements AnimationType {

	public interface SlideAnimationListener {
		public void onAnimationStart(View view, int type);

		public void onAnimationEnd(View view, int type);
	}

	static final String tag = AnimationListView.class.getSimpleName();
	static int ANIM_TIME_MIN = 300;
	static int ANIM_TIME_SHORT = 500;
	static int ANIM_TIME_MIDDLE = 1000;
	static int ANIM_TIME_LONG = 2000;
	static final int MAX_Y_OVERSCROLL_DISTANCE = 100;
	static final int SNAP_VELOCITY = 400;
	int mMaxYOverscrollDistance;
	View headerView;

	SlideAnimationListener listViewAnimationListener;
	ReentrantLock lock = new ReentrantLock();
	VelocityTracker velocityTracker;
	int mTouchSlop;
	int actionMoveCount;
	int downX, downY;

	boolean isAnimating = false, isSliding = false;
	boolean enableSlideLeft = true, enableSlideRight = true;

	public AnimationListView(Context context) {
		this(context, null);
	}

	public AnimationListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public AnimationListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		ANIM_TIME_SHORT = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
		ANIM_TIME_MIDDLE = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
		initBounceListView();
	}

	private void initBounceListView() {
		final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		final float density = metrics.density;
		mMaxYOverscrollDistance = (int) (density * MAX_Y_OVERSCROLL_DISTANCE);
	}

	@Override
	public void addHeaderView(View v) {
		if (v != null) {
			super.addHeaderView(v);
			headerView = v;
			headerView.setVisibility(View.GONE);
		}
	}

	int downPosition;
	View itemView;

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		addVelocityTracker(ev);
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downX = (int) ev.getX();
			downY = (int) ev.getY();
			downPosition = pointToPosition(downX, downY);
			if (downPosition == INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			} else {
				itemView = getChildAt(downPosition - getFirstVisiblePosition());
			}

			break;
		case MotionEvent.ACTION_MOVE:
			if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY || (Math.abs(ev.getX() - downX) > mTouchSlop && Math.abs(ev.getY() - downY) < mTouchSlop)) {
				isSliding = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			// recycleVelocityTracker();
			break;
		}

		return super.dispatchTouchEvent(ev);
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (isSliding) {
			requestDisallowInterceptTouchEvent(true);
			addVelocityTracker(ev);
			int x = (int) ev.getX(), y = (int) ev.getY();
			switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN:
				actionMoveCount = 0;
				break;
			case MotionEvent.ACTION_MOVE:
				actionMoveCount++;
				int deltaX = x - downX;
				if (deltaX > 0 && enableSlideLeft || deltaX < 0 && enableSlideRight) {
					itemView.setX(itemView.getX() + deltaX);
					// itemView.scrollBy(deltaX, 0);
				}
				Log.i(tag, "getX()=" + itemView.getX());
				downX = x;
				downY = y;
				break;
			case MotionEvent.ACTION_UP:
				if (actionMoveCount <= 1) {
					performClick();
				}
//				int velocityX = getScrollVelocity();
//				if (velocityX > SNAP_VELOCITY) {
//					// scrollRight(mListener);
//					doAnimation(itemView, TYPE_RIGHT);
//				} else if (velocityX < -SNAP_VELOCITY) {
//					// scrollLeft(mListener);
//					doAnimation(itemView, TYPE_MIDDLE);
//				} else {
//					// scrollByDistanceX();
//				}
				recycleVelocityTracker();

				break;
			case MotionEvent.ACTION_CANCEL:
				// scrollMiddle(mListener);
				recycleVelocityTracker();
				isSliding = false;
				break;
			}
			return true;
		}
		return super.onTouchEvent(ev);
	}
	
	

	private int getScrollVelocity() {
		velocityTracker.computeCurrentVelocity(1000);
		int velocity = (int) velocityTracker.getXVelocity();
		return velocity;
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
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (headerView != null) {
			int h = headerView.getMeasuredHeight();
			if (h > 0) {
				final DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
				final float density = metrics.density;
				mMaxYOverscrollDistance = (int) (density * h);
			}
		}
	}

	public void setAnimationListener(SlideAnimationListener al) {
		this.listViewAnimationListener = al;
	}

//	public void doAnimation(final View view, int... types) {
//		final int measuredWidth = view.getMeasuredWidth();
//		final int measuredHeight = view.getMeasuredHeight();
//		final float startAlpha = view.getAlpha(), startX = view.getX(), startY = view.getY();
//		AnimationSet animSet = new AnimationSet(true);
//		for (final int type : types) {
//			Animation anim = new Animation() {
//				@Override
//				protected void applyTransformation(float interpolatedTime, Transformation t) {
//					if (interpolatedTime == 0) {
//
//					} else if (interpolatedTime == 1) {
//
//					} else {
//						view.setAlpha((type & TYPE_FADE_OUT) > 0 ^ (type & TYPE_FADE_IN) > 0 ? ((type & TYPE_FADE_OUT) > 0 ? interpolatedTime : 1 - interpolatedTime)
//								: startAlpha);
//						view.setX((type & TYPE_LEFT) > 0 ^ (type & TYPE_RIGHT) > 0 ? interpolatedTime * (((type & TYPE_LEFT) > 0 ? -1 : 1) * measuredWidth)
//								+ (1 - interpolatedTime) * startX : startX);
//						view.setY((type & TYPE_UP) > 0 ^ (type & TYPE_DOWN) > 0 ? interpolatedTime * (((type & TYPE_UP) > 0 ? -1 : 1) * measuredHeight)
//								+ (1 - interpolatedTime) * startY : startY);
//						if ((type & TYPE_MIDDLE) > 0) {
//							view.setX(startX * (1 - interpolatedTime));
//						}
//						if ((type & TYPE_STRETCH_X) > 0 ^ (type & TYPE_SHRINK_X) > 0 || (type & TYPE_STRETCH_Y) > 0 ^ (type & TYPE_SHRINK_Y) > 0) {
//							view.getLayoutParams().width = ((type & TYPE_STRETCH_X) > 0 ? (int) (measuredWidth * interpolatedTime) : measuredWidth
//									- (int) (interpolatedTime * measuredWidth));
//							view.getLayoutParams().height = ((type & TYPE_STRETCH_Y) > 0 ? (int) (measuredHeight * interpolatedTime) : measuredHeight
//									- (int) (measuredHeight * interpolatedTime));
//							view.requestLayout();
//						}
//					}
//				}
//
//				@Override
//				public boolean willChangeBounds() {
//					return true;
//				}
//			};
//			anim.setDuration(ANIM_TIME_SHORT);
//			anim.setAnimationListener(new AnimationListener() {
//				@Override
//				public void onAnimationStart(Animation animation) {
//					if (listViewAnimationListener != null) {
//						listViewAnimationListener.onAnimationStart(view, type);
//					}
//				}
//
//				@Override
//				public void onAnimationRepeat(Animation animation) {
//
//				}
//
//				@Override
//				public void onAnimationEnd(Animation animation) {
//					if (listViewAnimationListener != null) {
//						listViewAnimationListener.onAnimationEnd(view, type);
//					}
//				}
//			});
//			animSet.addAnimation(anim);
//		}
//		view.startAnimation(animSet);
//	}

	@SuppressLint("NewApi")
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX,
			int maxOverScrollY, boolean isTouchEvent) {
		if (headerView != null) {
			if (-scrollY == mMaxYOverscrollDistance && headerView.getVisibility() != VISIBLE) {
				headerView.setVisibility(VISIBLE);
				headerView.startAnimation(AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in));
			} else if (scrollY == 0 && headerView.getVisibility() == VISIBLE) {
				Animation animation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						headerView.setVisibility(GONE);
					}
				});
				headerView.startAnimation(animation);

			}

		}
		if (Build.VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD) {
			return super.overScrollBy(deltaX, deltaY, scrollX, scrollY, scrollRangeX, scrollRangeY, maxOverScrollX, mMaxYOverscrollDistance, isTouchEvent);
		} else {
			return false;
		}
	}

}
