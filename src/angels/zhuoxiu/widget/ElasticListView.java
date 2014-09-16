package angels.zhuoxiu.widget;

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
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Scroller;

public class ElasticListView extends ListView {
	static final String tag = ElasticListView.class.getSimpleName();
	private static final int MAX_Y_OVERSCROLL_DISTANCE = 100;
	private Context mContext;
	private int mMaxYOverscrollDistance;
	View headerView;

	/** 
	 * 当前滑动的ListView　position 
	 */
	private int position;
	/** 
	 * 手指按下X的坐标 
	 */
	private int downY;
	/** 
	 * 手指按下Y的坐标 
	 */
	private int downX;
	/** 
	 * 屏幕宽度 
	 */
	private int screenWidth;
	/** 
	 * ListView的item 
	 */
	private View itemView;
	/** 
	 * 滑动类 
	 */
	private Scroller scroller;
	private static final int SNAP_VELOCITY = 600;
	/** 
	 * 速度追踪对象 
	 */
	private VelocityTracker velocityTracker;
	/** 
	 * 是否响应滑动，默认为不响应 
	 */
	private boolean isSlide = false;

	/**
	 * If it is scroll event to the listview
	 */
	private boolean isScroll = false;
	/** 
	 * 认为是用户滑动的最小距离 
	 */
	private int mTouchSlop;
	/** 
	 *  移除item后的回调接口 
	 */
	private RemoveListener mRemoveListener;
	/** 
	 * 用来指示item滑出屏幕的方向,向左或者向右,用一个枚举值来标记 
	 */
	private RemoveDirection removeDirection;

	// 滑动删除方向的枚举值
	public enum RemoveDirection {
		RIGHT, LEFT, CLICK, LONGCLICK;
	}

	public ElasticListView(Context context) {
		this(context, null);
	}

	public ElasticListView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ElasticListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
		scroller = new Scroller(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		mContext = context;
		initBounceListView();
	}

	/** 
	 * 设置滑动删除的回调接口 
	 * @param removeListener 
	 */
	public void setRemoveListener(RemoveListener removeListener) {
		this.mRemoveListener = removeListener;
	}

	int actionMoveCount = 0;
	long actionDownTime = 0;

	/** 
	 * 分发事件，主要做的是判断点击的是那个item, 以及通过postDelayed来设置响应左右滑动事件 
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		Log.i(tag, "dispatchTouchEvent ev=" + ev.getAction());
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN: {
			actionMoveCount = 0;
			addVelocityTracker(ev);

			// 假如scroller滚动还没有结束，我们直接返回
			if (!scroller.isFinished()) {
				return super.dispatchTouchEvent(ev);
			}
			downX = (int) ev.getX();
			downY = (int) ev.getY();

			position = pointToPosition(downX, downY);

			// 无效的position, 不做任何处理
			if (position == AdapterView.INVALID_POSITION) {
				return super.dispatchTouchEvent(ev);
			}

			// 获取我们点击的item view
			itemView = getChildAt(position - getFirstVisiblePosition());
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			actionMoveCount++;
			if (Math.abs(getScrollVelocity()) > SNAP_VELOCITY || (Math.abs(ev.getX() - downX) > mTouchSlop && Math.abs(ev.getY() - downY) < mTouchSlop)) {
				isSlide = true;
			} else if ((Math.abs(ev.getX() - downX) < mTouchSlop && Math.abs(ev.getY() - downY) > mTouchSlop)) {
				isScroll = true;
			} else {
				isScroll = false;
			}
			break;
		}
		case MotionEvent.ACTION_UP:
			recycleVelocityTracker();
			break;
		}

		return super.dispatchTouchEvent(ev);
	}

	/** 
	 * 往右滑动，getScrollX()返回的是左边缘的距离，就是以View左边缘为原点到开始滑动的距离，所以向右边滑动为负值 
	 */
	private void scrollRight() {
		removeDirection = RemoveDirection.RIGHT;
		final int delta = (screenWidth + itemView.getScrollX());
		// 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
		scroller.startScroll(itemView.getScrollX(), 0, -delta, 0, Math.abs(delta));
		postInvalidate(); // 刷新itemView
	}

	/** 
	 * 向左滑动，根据上面我们知道向左滑动为正值 
	 */
	private void scrollLeft() {
		removeDirection = RemoveDirection.LEFT;
		final int delta = (screenWidth - itemView.getScrollX());
		// 调用startScroll方法来设置一些滚动的参数，我们在computeScroll()方法中调用scrollTo来滚动item
		scroller.startScroll(itemView.getScrollX(), 0, delta, 0, Math.abs(delta));
		postInvalidate(); // 刷新itemView
	}

	/** 
	 * 根据手指滚动itemView的距离来判断是滚动到开始位置还是向左或者向右滚动 
	 */
	private void scrollByDistanceX() {
		// 如果向左滚动的距离大于屏幕的二分之一，就让其删除
		if (itemView.getScrollX() >= screenWidth * 0.5) {
			scrollLeft();
		} else if (itemView.getScrollX() <= -screenWidth * 0.5) {
			scrollRight();
		} else {
			// 滚回到原始位置,为了偷下懒这里是直接调用scrollTo滚动
			itemView.scrollTo(0, 0);
		}

	}

	/** 
	 * 处理我们拖动ListView item的逻辑 
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		Log.d(tag, "onTouchEvent\n ev=" + ev.getAction());
		final int action = ev.getAction();
		if (isSlide && position != AdapterView.INVALID_POSITION) {
			requestDisallowInterceptTouchEvent(true);
			addVelocityTracker(ev);
			int x = (int) ev.getX();
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEvent.ACTION_MOVE:

				MotionEvent cancelEvent = MotionEvent.obtain(ev);
				cancelEvent.setAction(MotionEvent.ACTION_CANCEL | (ev.getActionIndex() << MotionEvent.ACTION_POINTER_INDEX_SHIFT));
				onTouchEvent(cancelEvent);

				int deltaX = downX - x;
				downX = x;

				// 手指拖动itemView滚动, deltaX大于0向左滚动，小于0向右滚
				itemView.scrollBy(deltaX, 0);

				return true; // 拖动的时候ListView不滚动
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
				// 手指离开的时候就不响应左右滚动
				isSlide = false;
				break;
			}
		} else {
			Log.i(tag, "other isScroll=" + isScroll);
			switch (action) {
			case MotionEvent.ACTION_UP:
				if (!isScroll && ev.getEventTime() - ev.getDownTime() < 100 && mRemoveListener != null) {
					itemView.performClick();
					return true;
				}
				break;
			}
		}

		// 否则直接交给ListView来处理onTouchEvent事件
		return super.onTouchEvent(ev);
	}

	@Override
	public void computeScroll() {
		// 调用startScroll的时候scroller.computeScrollOffset()返回true，
		if (scroller.computeScrollOffset()) {
			// 让ListView item根据当前的滚动偏移量进行滚动
			itemView.scrollTo(scroller.getCurrX(), scroller.getCurrY());

			postInvalidate();

			// 滚动动画结束的时候调用回调接口
			if (scroller.isFinished()) {
				if (mRemoveListener == null) {
					throw new NullPointerException("RemoveListener is null, we should called setRemoveListener()");
				}

				itemView.scrollTo(0, 0);
				mRemoveListener.removeItem(removeDirection, position);
			}
		}
	}

	/** 
	 * 添加用户的速度跟踪器 
	 *  
	 * @param event 
	 */
	private void addVelocityTracker(MotionEvent event) {
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}

		velocityTracker.addMovement(event);
	}

	/** 
	 * 移除用户速度跟踪器 
	 */
	private void recycleVelocityTracker() {
		if (velocityTracker != null) {
			velocityTracker.recycle();
			velocityTracker = null;
		}
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

	/** 
	 *  
	 * 当ListView item滑出屏幕，回调这个接口 
	 * 我们需要在回调方法removeItem()中移除该Item,然后刷新ListView 
	 *  
	 * @author xiaanming 
	 * 
	 */
	public interface RemoveListener {
		public void removeItem(RemoveDirection direction, int position);
	}

	private void initBounceListView() {
		// get the density of the screen and do some maths with it on the max
		// overscroll distance
		// variable so that you get similar behaviors no matter what the screen
		// size

		final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
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

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
		if (headerView != null) {
			int h = headerView.getMeasuredHeight();
			if (h > 0) {
				final DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
				final float density = metrics.density;
				mMaxYOverscrollDistance = (int) (density * h);
			}
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@SuppressLint("NewApi")
	@Override
	protected boolean overScrollBy(int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX,
			int maxOverScrollY, boolean isTouchEvent) {
		// This is where the magic happens, we have replaced the incoming
		// maxOverScrollY with our own custom variable mMaxYOverscrollDistance;
		// Log.i(tag, "scrollY=" + scrollY + " mMaxYOverscrollDistance=" +
		// mMaxYOverscrollDistance+" isTouchEvent "+isTouchEvent);
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
