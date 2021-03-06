package com.android.imeng.ui.decorate.photo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.imeng.R;
import com.android.imeng.framework.logic.InfoResult;
import com.android.imeng.framework.ui.BasicActivity;
import com.android.imeng.framework.ui.base.annotations.ViewInject;
import com.android.imeng.framework.ui.base.annotations.event.OnClick;
import com.android.imeng.logic.BitmapHelper;
import com.android.imeng.logic.model.HairInfo;
import com.android.imeng.logic.NetLogic;
import com.android.imeng.logic.model.PictureInfo;
import com.android.imeng.ui.base.adapter.HairAdpater;
import com.android.imeng.ui.base.adapter.PictureAdpater;
import com.android.imeng.ui.base.adapter.ViewPagerAdapter;
import com.android.imeng.ui.decorate.cartoon.adapter.DecorationAdpater;
import com.android.imeng.util.APKUtil;
import com.android.imeng.util.Constants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 组装个人形象界面
 * @author hiphonezhu@gmail.com
 * @version [iMeng, 2015/06/04 17:09]
 */
public class PhotoDecorateActivity extends BasicActivity implements ViewPager.OnPageChangeListener, AdapterView.OnItemClickListener{

    /**
     * 跳转
     * @param faceUrl 脸地址
     * @param sex 性别 0：女 1：男
     * @param faceShape 脸型
     * @param context 上下文
     * @param glassValue 眼镜 // None/Dark/Normal
     */
    public static void actionStart(String faceUrl, int sex, int faceShape, Context context, String glassValue)
    {
        Intent intent = new Intent(context, PhotoDecorateActivity.class);
        intent.putExtra("faceUrl", faceUrl);
        intent.putExtra("sex", sex);
        intent.putExtra("faceShape", faceShape);
        intent.putExtra("glassValue", glassValue);
        context.startActivity(intent);
    }

    @ViewInject(R.id.image_wall)
    private View imageWall; // 背景墙
    @ViewInject(R.id.image_view)
    private ImageView imageView; // 形象展示View
    @ViewInject(R.id.face_btn)
    private Button faceBtn; // 表情Tab
    @ViewInject(R.id.face_indicator)
    private Button faceIndicator;
    @ViewInject(R.id.clothes_btn)
    private Button clothesBtn; // 衣服Tab
    @ViewInject(R.id.clothes_indicator)
    private Button clothesIndicator;
    @ViewInject(R.id.decoration_btn)
    private Button decorationBtn; // 表情Tab
    @ViewInject(R.id.decoration_indicator)
    private Button decorationIndicator;
    @ViewInject(R.id.view_pager)
    private ViewPager viewPager;
    private String faceUrl; // 脸地址
    private int sex; // 性别
    private int faceShape; // 脸型
    private String glassValue; // None/Dark/Normal
    private NetLogic netLogic;
    // key 0：后面的头发  1：衣服   2：脸   3：前面的头发   4：装饰
    private Map<Integer, Drawable> drawableMap = new HashMap<Integer, Drawable>();
    private final int TOTAL_LAYER_COUNT = 5; // 总计图层数量

    private GridView hairGrid;
    private HairAdpater hairAdpater; // 头发
    private int hairIndex;
    private GridView clothesGrid;
    private PictureAdpater clothesAdapter; // 衣服
    private int clothesIndex;
    private GridView decorationGrid;
    private DecorationAdpater decorationAdapter; // 装饰
    private int decorationIndex;

    private float scale = Constants.PIC_THUMBNAIL_WIDTH / (Constants.PIC_THUMBNAIL_HEIGHT * 1.0f); // GridView item宽高比
    /**
     * 记录请求状态
     * key=0,1,2
     * value:0正在请求  1请求成功   2请求失败
     */
    private Map<Integer, REQUEST_STATUS> requestStatus = new HashMap<Integer, REQUEST_STATUS>();
    private enum REQUEST_STATUS
    {
        REQUESTING,
        REQUEST_SUCCESSED,
        REQUEST_FAILURED
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_assemble_image);
    }

    @Override
    protected void init() {
        super.init();
        setTitleBar(true, "捏表情", true);
        leftBtn.setText("返回");
        rightBtn.setVisibility(View.VISIBLE);

        faceUrl = getIntent().getStringExtra("faceUrl");
        sex = getIntent().getIntExtra("sex", 0);
        faceShape = getIntent().getIntExtra("faceShape", 0);
        glassValue = getIntent().getStringExtra("glassValue");
        netLogic = new NetLogic(this);

        // 调整宽高
        adjustView();
        // 背景墙
        adjustWall();
        // 加载表情
        loadFace();
        // 初始化GridView
        loadGridView();

        // 查询头发、衣服、装饰列表
        requestStatus.put(0, REQUEST_STATUS.REQUESTING);
        requestStatus.put(1, REQUEST_STATUS.REQUESTING);
        requestStatus.put(2, REQUEST_STATUS.REQUESTING);
        netLogic.hairs(sex, hairIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
        netLogic.clothes(sex, clothesIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
        netLogic.decorations(sex, decorationIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterAll(netLogic);
    }

    /**
     * 初始化GridView
     */
    private void loadGridView()
    {
        for(int i = 0; i < 3; i++)
        {
            GridView grid = new GridView(this);
            grid.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            grid.setHorizontalSpacing(APKUtil.dip2px(this, 2));
            grid.setVerticalSpacing(APKUtil.dip2px(this, 2));
            grid.setNumColumns(3);
            grid.setSelector(new ColorDrawable(Color.TRANSPARENT));
            grid.setOnItemClickListener(this);
            if (i == 0)
            {
                hairGrid = grid;
            }
            else if (i == 1)
            {
                clothesGrid = grid;
            }
            else
            {
                decorationGrid = grid;
            }
        }
        viewPager.setAdapter(new ViewPagerAdapter(hairGrid, clothesGrid, decorationGrid));
        viewPager.setOnPageChangeListener(this);
    }

    /**
     * 调整宽高
     */
    private void adjustView()
    {
        imageView.post(new Runnable() {
            @Override
            public void run() {
                // 1、调整背景墙, 如果宽度>500且高度<500，则高度调节为500
                int wallWidth = imageWall.getMeasuredWidth();
                int wallHeight = imageWall.getMeasuredHeight();
                if (wallWidth > Constants.IMAGE_WIDTH_HEIGHT && wallHeight < Constants.IMAGE_WIDTH_HEIGHT)
                {
                    LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams)imageWall.getLayoutParams();
                    layoutParams.height = Constants.IMAGE_WIDTH_HEIGHT;
                    layoutParams.weight= 0;
                    imageWall.setLayoutParams(layoutParams);
                    imageWall.requestLayout();
                }

                // 2、调整形象，取宽与高的较小值，如果较小值仍大于500，则设置宽高都为500，否则设置宽和高为较小值
                ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
                int viewWidth = imageView.getMeasuredWidth();
                int viewHeight = imageView.getMeasuredHeight();

                int minSize = Math.min(viewWidth, viewHeight);
//                if (minSize > Constants.IMAGE_WIDTH_HEIGHT)
//                {
//                    minSize = Constants.IMAGE_WIDTH_HEIGHT;
//                }
                layoutParams.width = minSize;
                layoutParams.height = minSize;
                imageView.setLayoutParams(layoutParams);

                // x轴往左平移
                imageView.setTranslationX(-minSize * Constants.TRANSLATE_X_PERCENT);
            }
        });
    }

    /**
     * 性别调节背景墙
     */
    private void adjustWall()
    {
        if (sex == 0)
        {
            imageWall.setBackgroundColor(Color.parseColor("#CAE4F3"));
        }
        else
        {
            imageWall.setBackgroundColor(Color.parseColor("#f3bec4"));
        }
    }

    /**
     * 加载脸
     */
    private void loadFace()
    {
        String facePath = BitmapHelper.getLocalPath(faceUrl);
        if (!TextUtils.isEmpty(facePath))
        {
            Drawable faceDrawable = new BitmapDrawable(getResources(), facePath);
            drawableMap.put(2, faceDrawable);
            imageView.setImageDrawable(BitmapHelper.overlay(drawableMap, TOTAL_LAYER_COUNT));
        }
        else
        {
            netLogic.download(faceUrl);
        }
    }

    @OnClick({R.id.hair_lay, R.id.face_btn, R.id.clothes_lay, R.id.clothes_btn, R.id.decoration_lay, R.id.decoration_btn,
              R.id.title_right_btn, R.id.title_left_btn})
    public void onViewClick(View view)
    {
        switch (view.getId())
        {
            case R.id.hair_lay: // 头发
            case R.id.face_btn:
                viewPager.setCurrentItem(0);
                break;
            case R.id.clothes_lay: // 衣服
            case R.id.clothes_btn:
                viewPager.setCurrentItem(1);
                break;
            case R.id.decoration_lay: // 装饰
            case R.id.decoration_btn:
                viewPager.setCurrentItem(2);
                break;
            case R.id.title_left_btn:
                finish();
                break;
            case R.id.title_right_btn:
                if (!drawableMap.containsKey(3))
                {
                    showToast("请选择头发");
                    return;
                }
                else if (!drawableMap.containsKey(1))
                {
                    showToast("请选择衣服");
                    return;
                }
                // 制作界面
                MakeAllImageActivity.actionStart(sex, choosedClothesCategroyId, faceShape,
                    choosedHairBackground, choosedHairFont, choosedDecoration, faceUrl, this);
                finish();
                break;
        }
    }

    private int choosedClothesCategroyId; // 选择的衣服类别Id
    private String choosedHairBackground; // 选择的背后头发
    private String choosedHairFont; // 选择的前面头发
    private String choosedDecoration; // 选择的装饰
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         BaseAdapter adapter = (BaseAdapter)parent.getAdapter();
         if (adapter == hairAdpater) // 头发
         {
             if (hairAdpater.isMore(position) && requestStatus.get(0) != REQUEST_STATUS.REQUESTING) // More
             {
                 requestStatus.put(0, REQUEST_STATUS.REQUESTING);
                 netLogic.hairs(sex, hairIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
             }
             else
             {
                 HairInfo hairInfo = hairAdpater.getItem(position);
                 List<PictureInfo> hairInfos = hairInfo.getOriginalInfos();
                 if (!hairAdpater.hasDownload(position)) // 未下载
                 {
                     for(PictureInfo pictureInfo : hairInfos)
                     {
                         if (TextUtils.isEmpty(pictureInfo.getOriginalLocalPath()))
                         {
                             netLogic.download(R.id.downloadHair, pictureInfo, hairInfo);
                         }
                         hairAdpater.notifyDataSetChanged();
                     }
                 }
                 else
                 {
                     if (hairInfos != null && hairInfos.size() > 0)
                     {
                         decorateHair(hairInfos);
                     }
                 }
             }
         }
         else if (adapter == clothesAdapter) // 衣服
         {
             if (clothesAdapter.isMore(position) && requestStatus.get(1) != REQUEST_STATUS.REQUESTING) // More
             {
                 requestStatus.put(1, REQUEST_STATUS.REQUESTING);
                 netLogic.clothes(sex, clothesIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
             }
             else
             {
                 PictureInfo pictureInfo = clothesAdapter.getItem(position);
                 if (!clothesAdapter.hasDownload(position)) // 未下载
                 {
                     netLogic.download(R.id.downloadClothes, pictureInfo);
                     clothesAdapter.notifyDataSetChanged();
                 }
                 else
                 {
                     decorateClothes(pictureInfo);
                 }
             }
         }
         else if (adapter == decorationAdapter) // 装饰
         {
             if (decorationAdapter.isMore(position) && requestStatus.get(2) != REQUEST_STATUS.REQUESTING) // More
             {
                 requestStatus.put(2, REQUEST_STATUS.REQUESTING);
                 netLogic.decorations(sex, decorationIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
             }
             else
             {
                 if (position == 0) // 删除装饰
                 {
                     decorateDecoration(null);
                 }
                 else
                 {
                     PictureInfo pictureInfo = decorationAdapter.getItem(position - 1);
                     if (!decorationAdapter.hasDownload(position)) // 未下载
                     {
                         netLogic.download(R.id.downloadDecoration, pictureInfo);
                         decorationAdapter.notifyDataSetChanged();
                     }
                     else
                     {
                         decorateDecoration(pictureInfo);
                     }
                 }
             }
         }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position)
        {
            case 0:
                faceBtn.setEnabled(false);
                faceIndicator.setEnabled(false);
                clothesBtn.setEnabled(true);
                clothesIndicator.setEnabled(true);
                decorationBtn.setEnabled(true);
                decorationIndicator.setEnabled(true);
                // 请求失败, 重试
                if (requestStatus.get(0) == REQUEST_STATUS.REQUEST_FAILURED)
                {
                    requestStatus.put(0, REQUEST_STATUS.REQUESTING);
                    netLogic.hairs(sex, hairIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
                }
                break;
            case 1:
                faceBtn.setEnabled(true);
                faceIndicator.setEnabled(true);
                clothesBtn.setEnabled(false);
                clothesIndicator.setEnabled(false);
                decorationBtn.setEnabled(true);
                decorationIndicator.setEnabled(true);
                // 请求失败, 重试
                if (requestStatus.get(1) == REQUEST_STATUS.REQUEST_FAILURED)
                {
                    requestStatus.put(1, REQUEST_STATUS.REQUESTING);
                    netLogic.clothes(sex, clothesIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
                }
                break;
            case 2:
                faceBtn.setEnabled(true);
                faceIndicator.setEnabled(true);
                clothesBtn.setEnabled(true);
                clothesIndicator.setEnabled(true);
                decorationBtn.setEnabled(false);
                decorationIndicator.setEnabled(false);
                // 请求失败, 重试
                if (requestStatus.get(2) == REQUEST_STATUS.REQUEST_FAILURED)
                {
                    requestStatus.put(2, REQUEST_STATUS.REQUESTING);
                    netLogic.decorations(sex, decorationIndex * Constants.DEFAULT_PAGE_SIZE, Constants.DEFAULT_PAGE_SIZE);
                }
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /**
     * 头发
     * @param hairInfos
     */
    private void decorateHair(List<PictureInfo> hairInfos)
    {
        drawableMap.remove(0);
        drawableMap.remove(3);
        for(int i = 0; i < hairInfos.size(); i++)
        {
            PictureInfo pictureInfo =  hairInfos.get(i);
            int index = 0;
            if (pictureInfo.getNo() == 1) // 前面的头发
            {
                index = 3;
                choosedHairFont = pictureInfo.getOriginalLocalPath();
            }
            else if (pictureInfo.getNo() == 2) // 后面的头发
            {
                index = 0;
                choosedHairBackground = pictureInfo.getOriginalLocalPath();
            }
            drawableMap.put(index, new BitmapDrawable(getResources(), pictureInfo.getOriginalLocalPath()));
        }
        imageView.setImageDrawable(BitmapHelper.overlay(drawableMap, TOTAL_LAYER_COUNT));
    }

    /**
     * 衣服
     * @param pictureInfo
     */
    private void decorateClothes(PictureInfo pictureInfo)
    {
        choosedClothesCategroyId = pictureInfo.getCategoryId();
        drawableMap.put(1, new BitmapDrawable(getResources(), pictureInfo.getOriginalLocalPath()));
        imageView.setImageDrawable(BitmapHelper.overlay(drawableMap, TOTAL_LAYER_COUNT));
    }

    /**
     * 装饰
     */
    private void decorateDecoration(PictureInfo pictureInfo)
    {
        if (pictureInfo == null)
        {
            choosedDecoration = null;
            drawableMap.put(4, null);
        }
        else
        {
            choosedDecoration = pictureInfo.getOriginalLocalPath();
            drawableMap.put(4, new BitmapDrawable(getResources(), pictureInfo.getOriginalLocalPath()));
        }
        imageView.setImageDrawable(BitmapHelper.overlay(drawableMap, TOTAL_LAYER_COUNT));
    }

    @Override
    public void onResponse(Message msg) {
        super.onResponse(msg);
        switch (msg.what)
        {
            case R.id.downloadFace:
                if (checkResponse(msg))
                {
                    PictureInfo pictureInfo = (PictureInfo)(((InfoResult)msg.obj).getExtraObj());
                    Drawable faceDrawable = new BitmapDrawable(getResources(), pictureInfo.getOriginalLocalPath());
                    drawableMap.put(2, faceDrawable);
                    imageView.setImageDrawable(BitmapHelper.overlay(drawableMap, TOTAL_LAYER_COUNT));
                }
                break;
            case R.id.downloadHair:
                if (hairAdpater != null)
                {
                    hairAdpater.notifyDataSetChanged();
                    if (checkResponse(msg, false))
                    {
                        InfoResult result = (InfoResult)msg.obj;
                        if (result.getOtherObj() instanceof HairInfo)
                        {
                            HairInfo hairInfo = (HairInfo)result.getOtherObj();
                            // 前后头发全部下载成功
                            boolean allSuccess = true;
                            List<PictureInfo> hairInfos = hairInfo.getOriginalInfos();
                            for(PictureInfo pictureInfo : hairInfos)
                            {
                                if (TextUtils.isEmpty(pictureInfo.getOriginalLocalPath()))
                                {
                                    allSuccess = false;
                                }
                            }
                            if (allSuccess)
                            {
                                if (hairInfos != null && hairInfos.size() > 0)
                                {
                                    decorateHair(hairInfos);
                                }
                            }
                        }
                    }
                }
                break;
            case R.id.downloadClothes:
                if (clothesAdapter != null)
                {
                    clothesAdapter.notifyDataSetChanged();
                    if (checkResponse(msg, false))
                    {
                        InfoResult result = (InfoResult) msg.obj;
                        if (result.getExtraObj() instanceof PictureInfo)
                        {
                            PictureInfo pictureInfo = (PictureInfo)result.getExtraObj();
                            decorateClothes(pictureInfo);
                        }
                    }
                }
                break;
            case R.id.downloadDecoration:
                if (decorationAdapter != null)
                {
                    decorationAdapter.notifyDataSetChanged();
                    if (checkResponse(msg, false))
                    {
                        InfoResult result = (InfoResult) msg.obj;
                        if (result.getExtraObj() instanceof PictureInfo)
                        {
                            PictureInfo pictureInfo = (PictureInfo)result.getExtraObj();
                            decorateDecoration(pictureInfo);
                        }
                    }
                }
                break;
            case R.id.hairs: // 头发
                if (checkResponse(msg))
                {
                    requestStatus.put(0, REQUEST_STATUS.REQUEST_SUCCESSED);
                    InfoResult infoResult = (InfoResult)msg.obj;
                    List<HairInfo> hairInfos = (List<HairInfo>)infoResult.getExtraObj();
                    hairIndex++;

                    if (hairAdpater == null)
                    {
                        int hairCount = Integer.parseInt(infoResult.getOtherObj().toString());
                        hairAdpater = new HairAdpater(this, hairInfos, R.layout.layout_item_picture, hairCount);

                        final int viewWidth = viewPager.getWidth();
                        int numColumns = 3;
                        int hoizontalSpacing = APKUtil.dip2px(this, 2);
                        int columnWidth = (int)((viewWidth - (numColumns - 1) * hoizontalSpacing) / (1.0f * numColumns));
                        int columnHeight = (int)(columnWidth * scale);
                        hairAdpater.setSize(columnWidth, columnHeight);
                        hairGrid.setAdapter(hairAdpater);

                        if (hairInfos != null && hairInfos.size() > 0) // 默认加载第一个
                        {
                            HairInfo hairInfo = hairInfos.get(0);
                            List<PictureInfo> onehairInfos = hairInfo.getOriginalInfos();
                            if (!hairAdpater.hasDownload(0)) // 未下载
                            {
                                for(PictureInfo pictureInfo : onehairInfos)
                                {
                                    if (TextUtils.isEmpty(pictureInfo.getOriginalLocalPath()))
                                    {
                                        netLogic.download(R.id.downloadHair, pictureInfo, hairInfo);
                                    }
                                    hairAdpater.notifyDataSetChanged();
                                }
                            }
                            else
                            {
                                if (onehairInfos != null && onehairInfos.size() > 0)
                                {
                                    decorateHair(onehairInfos);
                                }
                            }
                        }
                    }
                    else
                    {
                        hairAdpater.getDataSource().addAll(hairInfos);
                    }
                    hairAdpater.notifyDataSetChanged();
                }
                else
                {
                    requestStatus.put(0, REQUEST_STATUS.REQUEST_FAILURED);
                }
                break;
            case R.id.clothes: // 衣服
                if (checkResponse(msg))
                {
                    requestStatus.put(1, REQUEST_STATUS.REQUEST_SUCCESSED);
                    InfoResult infoResult = (InfoResult)msg.obj;
                    List<PictureInfo> pictureInfos = (List<PictureInfo>)infoResult.getExtraObj();
                    clothesIndex++;

                    if (clothesAdapter == null)
                    {
                        int clothesCount = Integer.parseInt(infoResult.getOtherObj().toString());
                        clothesAdapter = new PictureAdpater(this, pictureInfos, R.layout.layout_item_picture, clothesCount);

                        final int viewWidth = viewPager.getWidth();
                        int numColumns = 3;
                        int hoizontalSpacing = APKUtil.dip2px(this, 2);
                        int columnWidth = (int)((viewWidth - (numColumns - 1) * hoizontalSpacing) / (1.0f * numColumns));
                        int columnHeight = (int)(columnWidth * scale);
                        clothesAdapter.setSize(columnWidth, columnHeight);
                        clothesGrid.setAdapter(clothesAdapter);

                        if (pictureInfos != null && pictureInfos.size() > 0) // 默认加载第一个
                        {
                            PictureInfo pictureInfo = pictureInfos.get(0);
                            if (!clothesAdapter.hasDownload(0)) // 未下载
                            {
                                netLogic.download(R.id.downloadClothes, pictureInfo);
                                clothesAdapter.notifyDataSetChanged();
                            }
                            else
                            {
                                decorateClothes(pictureInfo);
                            }
                        }
                    }
                    else
                    {
                        clothesAdapter.getDataSource().addAll(pictureInfos);
                    }
                    clothesAdapter.notifyDataSetChanged();
                }
                else
                {
                    requestStatus.put(1, REQUEST_STATUS.REQUEST_FAILURED);
                }
                break;
            case R.id.decorations: // 装饰
                if (checkResponse(msg))
                {
                    requestStatus.put(2, REQUEST_STATUS.REQUEST_SUCCESSED);
                    InfoResult infoResult = (InfoResult)msg.obj;
                    List<PictureInfo> pictureInfos = (List<PictureInfo>)infoResult.getExtraObj();
                    decorationIndex++;

                    if (decorationAdapter == null)
                    {
                        int decorationCount = Integer.parseInt(infoResult.getOtherObj().toString());
                        decorationAdapter = new DecorationAdpater(this, pictureInfos, R.layout.layout_item_picture, decorationCount);

                        final int viewWidth = viewPager.getWidth();
                        int numColumns = 3;
                        int hoizontalSpacing = APKUtil.dip2px(this, 2);
                        int columnWidth = (int)((viewWidth - (numColumns - 1) * hoizontalSpacing) / (1.0f * numColumns));
                        int columnHeight = (int)(columnWidth * scale);
                        decorationAdapter.setSize(columnWidth, columnHeight);
                        decorationGrid.setAdapter(decorationAdapter);

                        if (pictureInfos != null && pictureInfos.size() > 0)
                        {
                            int glassIndex = -1;
                            if (!"None".equals(glassValue))
                            {
                                if ("Dark".equals(glassValue))
                                {
                                    glassIndex = 6;
                                }
                                else if ("Normal".equals(glassValue))
                                {
                                    glassIndex = 5;
                                }
                                if (glassIndex != -1)
                                {
                                    PictureInfo pictureInfo = pictureInfos.get(glassIndex);
                                    glassIndex += 1; // 排除第一张是默认的
                                    if (!decorationAdapter.hasDownload(glassIndex)) // 未下载
                                    {
                                        netLogic.download(R.id.downloadDecoration, pictureInfo);
                                        decorationAdapter.notifyDataSetChanged();
                                    }
                                    else
                                    {
                                        decorateDecoration(pictureInfo);
                                    }
                                }
                            }
                        }
                    }
                    else
                    {
                        decorationAdapter.getDataSource().addAll(pictureInfos);
                    }
                    decorationAdapter.notifyDataSetChanged();
                }
                else
                {
                    requestStatus.put(2, REQUEST_STATUS.REQUEST_FAILURED);
                }
                break;
        }
    }
}
