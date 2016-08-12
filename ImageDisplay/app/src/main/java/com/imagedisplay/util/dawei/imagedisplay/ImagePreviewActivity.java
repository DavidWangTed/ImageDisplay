/*
 * Copyright (c) 2015, wordall1101@126.com All Rights Reserved.
 */
package com.imagedisplay.util.dawei.imagedisplay;

import android.content.Context;
import android.content.Intent;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * @author vftour.com
 * @version 1.0
 * @date: 15/10/20 下午4:35
 */
public class ImagePreviewActivity extends BaseActivity implements ViewPager.OnPageChangeListener {

    public static final String BUNDLE_KEY_IMAGES = "bundle_key_images";
    private static final String BUNDLE_KEY_INDEX = "bundle_key_index";
    private ViewPager mViewPager;
    private SamplePagerAdapter mAdapter;
    private TextView mTvImgIndex;
    private ImageView mIvMore;
    private int mCurrentPostion = 0;
    private String[] mImageUrls;
    private int index;

    public static void showImagePreview(Context context, int index, String[] images) {
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(BUNDLE_KEY_IMAGES, images);
        intent.putExtra(BUNDLE_KEY_INDEX, index);
        context.startActivity(intent);
    }

    @Override
    public void setRootView() {
        setContentView(R.layout.aty_image_preview);
    }

    @Override
    public void initWidget() {
        super.initWidget();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mViewPager.setOnPageChangeListener(this);
        mTvImgIndex = (TextView) findViewById(R.id.tv_img_index);
        mIvMore = (ImageView) findViewById(R.id.iv_more);
        mIvMore.setOnClickListener(this);
    }

    @Override
    protected boolean isEnableToolBar() {
        return false;
    }

    @Override
    public void initData() {
        super.initData();
        mImageUrls = getIntent().getStringArrayExtra(BUNDLE_KEY_IMAGES);
        index = getIntent().getIntExtra(BUNDLE_KEY_INDEX, 0);
        mAdapter = new SamplePagerAdapter(mImageUrls);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setCurrentItem(index);
        onPageSelected(index);
    }

    @Override
    public void widgetClick(View v) {
        super.widgetClick(v);
        switch (v.getId()) {
            case R.id.iv_more:
                saveImg();
                break;
            default:
                break;
        }
    }


    /**
     * 保存图片
     */
    private void saveImg() {
        if (mAdapter != null && mAdapter.getCount() > 0) {
            String imgUrl = mAdapter.getItem(mCurrentPostion);
            final DownLoad download = new DownLoad(imgUrl, getPhotoFileName());
            download.setDownLoadListener(new DownLoad.DownLoadListener() {
                @Override
                public void publishProgress(int _progress) {
                    if (_progress >= DownLoad.PROGRESS_MAX) {
                        // 下载完成
                        showMyToast("图片已保存至" + download.filePath);
                    }
                }
            });
            download.start();
        } else {
            showMyToast("图片保存失败");
        }


    }

    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
        return dateFormat.format(date) + ".jpg";
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int idx) {
        mCurrentPostion = idx;
        if (mImageUrls != null && mImageUrls.length > 1) {
            if (mTvImgIndex != null) {
                mTvImgIndex.setText((mCurrentPostion + 1) + "/"
                        + mImageUrls.length);
            }
        }
    }

    class SamplePagerAdapter extends PagerAdapter {
        static final int IGNORE_ITEM_VIEW_TYPE = AdapterView.ITEM_VIEW_TYPE_IGNORE;
        private String[] images = new String[]{};

        SamplePagerAdapter(String[] images) {
            this.images = images;
        }

        public String getItem(int position) {
            return images == null ? null : images[position];
        }

        @Override
        public int getCount() {
            return images == null ? 0 : images.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public final Object instantiateItem(ViewGroup container, int position) {
            int viewType = getItemViewType(position);
            View view = null;
//            if (viewType != IGNORE_ITEM_VIEW_TYPE) {
//                view = recycleBin.getScrapView(position, viewType);
//            }
            view = getView(position, view, container);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        public View getView(int position, View convertView, ViewGroup container) {
            ViewHolder vh = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(container.getContext()).inflate(R.layout.image_preview_item, null);
                vh = new ViewHolder(convertView);
                convertView.setTag(vh);
            } else {
                vh = (ViewHolder) convertView.getTag();
            }
            final ProgressBar bar = vh.progress;
            if(StringUtils.isHttp(images[position])){
                Picasso.with(mContext).load(images[position]).error(R.drawable.default_img).placeholder(R.drawable.default_img).into(vh.image);
            } else {
                Picasso.with(mContext).load(new File(images[position])).error(R.drawable.default_img).placeholder(R.drawable.default_img).into(vh.image);
            }

//            ImageLoader.getInstance().displayImage(images[position], vh.image, ImageLoaderOptions.initOptions(R.drawable.default_img, R.drawable.default_img, false), null);
            convertView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
//                    ImagePreview.this.finish();
                    return false;
                }
            });
            return convertView;
        }
    }

    static class ViewHolder {
        ImageView image;
        ProgressBar progress;

        ViewHolder(View view) {
            image = (ImageView) view.findViewById(R.id.photoview);
            progress = (ProgressBar) view.findViewById(R.id.progress);
        }
    }
}
