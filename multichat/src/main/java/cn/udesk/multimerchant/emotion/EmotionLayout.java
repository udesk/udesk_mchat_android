package cn.udesk.multimerchant.emotion;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;

import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.util.List;

import cn.udesk.multimerchant.R;

/**
 * 表情布局
 */
public class EmotionLayout extends LinearLayout implements View.OnClickListener {

    public static final int EMOJI_COLUMNS = 7;
    public static final int EMOJI_ROWS = 3;
    public static final int EMOJI_PER_PAGE = EMOJI_COLUMNS * EMOJI_ROWS - 1;//最后一个是删除键

    public static final int STICKER_COLUMNS = 4;
    public static final int STICKER_ROWS = 2;
    public static final int STICKER_PER_PAGE = STICKER_COLUMNS * STICKER_ROWS;

    private int mMeasuredWidth;
    private int mMeasuredHeight;

    private int mTabPosi = 0;
    private Context mContext;
    private ViewPager mVpEmotioin;
    private LinearLayout mLlPageNumber;
    private LinearLayout mLlTabContainer;
    private RelativeLayout mRlEmotionAdd;

    private int mTabCount;
    private SparseArray<View> mTabViewArray = new SparseArray<>();
    private EmotionTab mSettingTab;
    private IEmotionSelectedListener mEmotionSelectedListener;
    private IEmotionExtClickListener mEmotionExtClickListener;
    private boolean mEmotionAddVisiable = false;
    private boolean mEmotionSettingVisiable = false;

    public EmotionLayout(Context context) {
        this(context, null);
    }

    public EmotionLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmotionLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public void setEmotionSelectedListener(IEmotionSelectedListener emotionSelectedListener) {
        if (emotionSelectedListener != null) {
            this.mEmotionSelectedListener = emotionSelectedListener;
        }
    }


    public void setEmotionExtClickListener(IEmotionExtClickListener emotionExtClickListener) {
        if (emotionExtClickListener != null) {
            this.mEmotionExtClickListener = emotionExtClickListener;
        }
    }


    /**
     * 设置表情添加按钮的显隐
     *
     * @param visiable
     */
    public void setEmotionAddVisiable(boolean visiable) {
        mEmotionAddVisiable = visiable;
        if (mRlEmotionAdd != null) {
//            mRlEmotionAdd.setVisibility(mEmotionAddVisiable ? View.VISIBLE : View.GONE);
            mRlEmotionAdd.setVisibility(View.GONE);
        }
    }

    /**
     * 设置表情设置按钮的显隐
     *
     * @param visiable
     */
    public void setEmotionSettingVisiable(boolean visiable) {
        mEmotionSettingVisiable = visiable;
        if (mSettingTab != null) {
//            mSettingTab.setVisibility(mEmotionSettingVisiable ? View.VISIBLE : View.GONE);
            mSettingTab.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        init();
        initListener();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            mMeasuredWidth = measureWidth(widthMeasureSpec);
            mMeasuredHeight = measureHeight(heightMeasureSpec);
            setMeasuredDimension(mMeasuredWidth, mMeasuredHeight);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private int measureWidth(int measureSpec) {
        int result = 0;
        try {
            int specMode = MeasureSpec.getMode(measureSpec);
            int specSize = MeasureSpec.getSize(measureSpec);

            if (specMode == MeasureSpec.EXACTLY) {
                result = specSize;
            } else {
                result = LQREmotionKit.dip2px(200);
                if (specMode == MeasureSpec.AT_MOST) {
                    result = Math.min(result, specSize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private int measureHeight(int measureSpec) {
        int result = 0;
        try {
            int specMode = MeasureSpec.getMode(measureSpec);
            int specSize = MeasureSpec.getSize(measureSpec);

            if (specMode == MeasureSpec.EXACTLY) {
                result = specSize;
            } else {
                result = LQREmotionKit.dip2px(200);
                if (specMode == MeasureSpec.AT_MOST) {
                    result = Math.min(result, specSize);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private void init() {
        try {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            inflater.inflate(R.layout.udesk_multimerchant_emotion_layout, this);

            mVpEmotioin = (ViewPager) findViewById(R.id.vpEmotioin);
            mLlPageNumber = (LinearLayout) findViewById(R.id.llPageNumber);
            mLlTabContainer = (LinearLayout) findViewById(R.id.llTabContainer);
            mRlEmotionAdd = (RelativeLayout) findViewById(R.id.rlEmotionAdd);
            setEmotionAddVisiable(mEmotionAddVisiable);

            initTabs();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void initTabs() {
        //默认添加一个表情tab
        try {
            mLlTabContainer.removeAllViews();
            mTabViewArray.clear();
            EmotionTab emojiTab = new EmotionTab(mContext, R.drawable.udesk_multimerchant_001);
            mLlTabContainer.addView(emojiTab);
            mTabViewArray.put(0, emojiTab);

            //添加所有的贴图tab
            List<StickerCategory> stickerCategories = StickerManager.getInstance().getStickerCategories();
            if (stickerCategories.size() >0){
                mLlTabContainer.setVisibility(VISIBLE);
                for (int i = 0; i < stickerCategories.size(); i++) {
                    StickerCategory category = stickerCategories.get(i);
                    EmotionTab tab = new EmotionTab(mContext, category.getCoverImgPath());
                    mLlTabContainer.addView(tab);
                    mTabViewArray.put(i + 1, tab);
                }
            }else {
                mLlTabContainer.setVisibility(GONE);
            }

            //最后添加一个表情设置Tab
            mSettingTab = new EmotionTab(mContext, R.drawable.udesk_multimerchant_001);
            StateListDrawable drawable = new StateListDrawable();
            Drawable unSelected = mContext.getResources().getDrawable(R.color.udesk_multimerchant_white);
            drawable.addState(new int[]{-android.R.attr.state_pressed}, unSelected);
            Drawable selected = mContext.getResources().getDrawable(R.color.udesk_multimerchant_gray);
            drawable.addState(new int[]{android.R.attr.state_pressed}, selected);
            mSettingTab.setBackgroundDrawable(drawable);
            mLlTabContainer.addView(mSettingTab);
            mTabViewArray.put(mTabViewArray.size(), mSettingTab);
            setEmotionSettingVisiable(mEmotionSettingVisiable);

            selectTab(0);
        } catch (Resources.NotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initListener() {
        if (mLlTabContainer != null) {
            try {
                mTabCount = mLlTabContainer.getChildCount() - 1;//不包含最后的设置按钮
                for (int position = 0; position < mTabCount; position++) {
                    View tab = mLlTabContainer.getChildAt(position);
                    tab.setTag(position);
                    tab.setOnClickListener(this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mVpEmotioin.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                setCurPageCommon(position);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mRlEmotionAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmotionExtClickListener != null) {
                    mEmotionExtClickListener.onEmotionAddClick(v);
                }
            }
        });

        mSettingTab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEmotionExtClickListener != null) {
                    mEmotionExtClickListener.onEmotionSettingClick(v);
                }
            }
        });
    }

    private void setCurPageCommon(int position) {
        try {
            if (mTabPosi == 0) {
                setCurPage(position, (int) Math.ceil(EmojiManager.getDisplayCount() / (float) EmotionLayout.EMOJI_PER_PAGE));
            } else {
                StickerCategory category = StickerManager.getInstance().getStickerCategories().get(mTabPosi - 1);
                setCurPage(position, (int) Math.ceil(category.getStickers().size() / (float) EmotionLayout.STICKER_PER_PAGE));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        mTabPosi = (int) v.getTag();
        selectTab(mTabPosi);
    }

    public void selectTab(int tabPosi) {
        try {
            if (tabPosi == mTabViewArray.size() - 1) {
                return;
            }

            for (int i = 0; i < mTabCount; i++) {
                View tab = mTabViewArray.get(i);
                if (tab != null){
                    tab.setBackgroundResource(R.drawable.udesk_multimerchant_shape_tab_normal);
                }
            }
            mTabViewArray.get(tabPosi).setBackgroundResource(R.drawable.udesk_multimerchant_shape_tab_press);
            //显示表情内容
            fillVpEmotioin(tabPosi);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fillVpEmotioin(int tabPosi) {

        try {
            EmotionViewPagerAdapter adapter = new EmotionViewPagerAdapter(mMeasuredWidth, mMeasuredHeight, tabPosi, mEmotionSelectedListener);
            mVpEmotioin.setAdapter(adapter);
            mLlPageNumber.removeAllViews();
            setCurPageCommon(0);
            if (tabPosi == 0) {
                adapter.attachEditText(mMessageEditText);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCurPage(int page, int pageCount) {
        try {
            int hasCount = mLlPageNumber.getChildCount();
            int forMax = Math.max(hasCount, pageCount);

            ImageView ivCur = null;
            for (int i = 0; i < forMax; i++) {
                if (pageCount <= hasCount) {
                    if (i >= pageCount) {
                        mLlPageNumber.getChildAt(i).setVisibility(View.GONE);
                        continue;
                    } else {
                        ivCur = (ImageView) mLlPageNumber.getChildAt(i);
                    }
                } else {
                    if (i < hasCount) {
                        ivCur = (ImageView) mLlPageNumber.getChildAt(i);
                    } else {
                        ivCur = new ImageView(mContext);
                        ivCur.setBackgroundResource(R.drawable.udesk_multimerchant_selector_view_pager_indicator);
                        LayoutParams params = new LayoutParams(LQREmotionKit.dip2px(8), LQREmotionKit.dip2px(8));
                        ivCur.setLayoutParams(params);
                        params.leftMargin = LQREmotionKit.dip2px(3);
                        params.rightMargin = LQREmotionKit.dip2px(3);
                        mLlPageNumber.addView(ivCur);
                    }
                }

                ivCur.setId(i);
                ivCur.setSelected(i == page);
                ivCur.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    EditText mMessageEditText;

    public void attachEditText(EditText messageEditText) {
        mMessageEditText = messageEditText;
    }

}
