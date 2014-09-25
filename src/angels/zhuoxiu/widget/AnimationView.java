package angels.zhuoxiu.widget;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import android.widget.FrameLayout;
import android.widget.Scroller;

public class AnimationView extends FrameLayout implements AnimationType {
	protected String tag = this.getClass().getSimpleName();

	public interface AnimationViewListener {
		public void onAnimationStart(View view, int type, int index);

		public void onAnimationEnd(View view, int type, int index);
	}

	public interface SlideTrigger {
		public void onTriggerSlide(AnimationView v, DIRECTION direction);
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
	private static final int SNAP_VELOCITY = 800;
	private VelocityTracker velocityTracker;
	AnimationViewListener itemAnimationListener;
	SlideTrigger slideTrigger;
	DIRECTION direction = DIRECTION.MIDDLE, position = DIRECTION.MIDDLE;
	int actionMoveCount;
	static int ANIM_TIME_SHORT = 1000;
	static int ANIM_TIME_MIDDLE = 2000;

	public AnimationView(Context context) {
		this(context, null);
	}

	public AnimationView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		ANIM_TIME_SHORT = context.getResources().getInteger(android.R.integer.config_shortAnimTime);
		ANIM_TIME_MIDDLE = context.getResources().getInteger(android.R.integer.config_mediumAnimTime);
	}

	public void doAnimation(final AnimationViewListener itemAnimListener, final int... types) {
		final int measuredWidth = getMeasuredWidth();
		final int measuredHeight = getMeasuredHeight();
		final float startAlpha = getAlpha(), startX = getX(), startY = getY();
		AnimationSet animSet = new AnimationSet(true);
		for (int i = 0; i < types.length; i++) {
			final int type = types[i];
			final int index = i;
			Animation anim = new Animation() {
				@Override
				protected void applyTransformation(float time, Transformation t) {
					if (time == 0) {

					} else if (time == 1) {

					} else {
						setAlpha((type & TYPE_FADE_IN) > 0 ^ (type & TYPE_FADE_OUT) > 0 ? ((type & TYPE_FADE_IN) > 0 ? time * startAlpha : (1 - time)
								* startAlpha) : startAlpha);

						if ((type & TYPE_SLIDE_MIDDLE) > 0 ^ (type & TYPE_SLIDE_IN_LEFT) > 0 ^ (type & TYPE_SLIDE_OUT_LEFT) > 0
								^ (type & TYPE_SLIDE_IN_RIGHT) > 0 ^ (type & TYPE_SLIDE_OUT_RIGHT) > 0) {
							if ((type & TYPE_SLIDE_MIDDLE) > 0) {
								setX((1 - time) * startX);
							} else if ((type & TYPE_SLIDE_IN_LEFT) > 0) {
								setX((1 - time) * (-measuredWidth));
							} else if ((type & TYPE_SLIDE_OUT_LEFT) > 0) {
								setX((1 - time) * startX + time * (-measuredWidth));
							} else if ((type & TYPE_SLIDE_IN_RIGHT) > 0) {
								setX((1 - time) * measuredWidth);
							} else if ((type & TYPE_SLIDE_OUT_RIGHT) > 0) {
								setX((1 - time) * startX + time * measuredWidth);
							}

						}
						if ((type & TYPE_STRETCH_X) > 0 ^ (type & TYPE_SHRINK_X) > 0) {
							getLayoutParams().width = ((type & TYPE_STRETCH_X) > 0 ? (int) (measuredWidth * time) : measuredWidth
									- (int) (time * measuredWidth));
							requestLayout();
						}
						if ((type & TYPE_STRETCH_Y) > 0 ^ (type & TYPE_SHRINK_Y) > 0) {
							getLayoutParams().height = ((type & TYPE_STRETCH_Y) > 0 ? (int) (measuredHeight * time) : measuredHeight
									- (int) (measuredHeight * time));
							requestLayout();
						}
					}
				}

				@Override
				public boolean willChangeBounds() {
					return true;
				}
			};
			anim.setDuration(ANIM_TIME_MIDDLE);
			anim.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					if (itemAnimListener != null) {
						itemAnimListener.onAnimationStart(AnimationView.this, type, index);
					}
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					if (itemAnimListener != null) {
						itemAnimListener.onAnimationEnd(AnimationView.this, type, index);
					}
				}
			});
			anim.setFillAfter(true);
			anim.setFillBefore(true);
			anim.setStartOffset(index*ANIM_TIME_MIDDLE);
			animSet.addAnimation(anim);
		}
		startAnimation(animSet);
	}

	public void setAnimationListener(AnimationViewListener al) {
		this.itemAnimationListener = al;
	}

	public void setSlideTrigger(SlideTrigger trigger) {
		this.slideTrigger = trigger;
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
					AnimationView.this.setX(xNew);
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
					AnimationView.this.setVisibility(show ? View.GONE : View.VISIBLE);
				} else if (interpolatedTime == 1) {
					AnimationView.this.setVisibility(show ? View.VISIBLE : View.GONE);
				} else {
					int height;
					if (show) {
						height = (int) (heightMeasure * interpolatedTime);
					} else {
						height = heightMeasure - (int) (heightMeasure * interpolatedTime);
					}
					AnimationView.this.getLayoutParams().height = height;
					AnimationView.this.requestLayout();
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
				int deltaX = x - downX;
				setX(getX() + deltaX);

				// if (deltaX > 0 && enableSlideLeft || deltaX < 0 &&
				// enableSlideRight) {
				// scrollBy(deltaX, 0);
				// }
				downX = x;
				downY = y;
				break;
			case MotionEvent.ACTION_UP:
				if (actionMoveCount <= 1) {
					performClick();
					break;
				}
				int velocityX = getScrollVelocity();
				DIRECTION mDirection;
				if (velocityX > SNAP_VELOCITY) {
					// scrollRight(mListener);
					if (getX() < 0) {
						mDirection = DIRECTION.MIDDLE;
					} else {
						mDirection = DIRECTION.RIGHT;
					}
				} else if (velocityX < -SNAP_VELOCITY) {
					// scrollLeft(mListener);
					if (getX() >= 0) {
						mDirection = DIRECTION.MIDDLE;
					} else {
						mDirection = DIRECTION.LEFT;
					}

				} else {
					mDirection = DIRECTION.MIDDLE;
				}

				slideTrigger.onTriggerSlide(this, mDirection);
				recycleVelocityTracker();
				break;
			case MotionEvent.ACTION_CANCEL:
				scrollMiddle();
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

			}
		}
	}

	/** 
	 * scroll to right (negative value)
	 */
	public void scrollRight() {
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
			scrollMiddle();
		}

	}

	/** 
	 * scroll to left (positive value)
	 */
	@SuppressLint("NewApi")
	public void scrollLeft() {
		if (!scroller.isFinished() || !enableSlideLeft) {
			return;
		}
		if (position == DIRECTION.MIDDLE) {
			direction = DIRECTION.LEFT;
			position = DIRECTION.LEFT;
			int deltaX = getScrollX() - getWidth();
			scroller.startScroll(getScrollX(), 0, -deltaX, 0, ANIM_TIME_MIDDLE);
			postInvalidate(); // refresh
		}

	}

	public void scrollMiddle() {
		if (!scroller.isFinished()) {
			return;
		}
		direction = DIRECTION.MIDDLE;
		position = DIRECTION.MIDDLE;
		scroller.startScroll(getScrollX(), getScrollY(), -getScrollX(), -getScrollY(), ANIM_TIME_MIDDLE);
		postInvalidate();
	}

	/**
	 * If slide more than half of the width, then slide it out
	 */
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
}
