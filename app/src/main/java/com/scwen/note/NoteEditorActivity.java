package com.scwen.note;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.speech.asr.SpeechConstant;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.permissions.RxPermissions;
import com.luck.picture.lib.tools.StringUtils;
import com.scwen.editor.RichEditer;
import com.scwen.editor.util.CaptureUtil;
import com.scwen.editor.util.DensityUtil;
import com.scwen.editor.weight.ImageActionListener;
import com.scwen.editor.weight.ImageWeight;
import com.scwen.note.util.NetWorkUtils;
import com.scwen.note.voicerecog.AutoCheck;
import com.scwen.note.voicerecog.IRecogListener;
import com.scwen.note.voicerecog.MessageStatusRecogListener;
import com.scwen.note.voicerecog.MyRecognizer;
import com.scwen.note.voicerecog.RecoError;
import com.scwen.note.weight.LabelView;
import com.scwen.note.weight.VoiceRectView;

import org.litepal.LitePal;
import org.litepal.crud.LitePalSupport;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.scwen.note.voicerecog.IStatus.*;

public class NoteEditorActivity extends AppCompatActivity {

    public static final String SAVE_PATH = "/Note";

    public static final String NOTE_ID = "note_id";
    /**
     * 软键盘管理
     */
    private InputMethodManager imm = null;

    @BindView(R.id.edt_title)
    EditText mEdtTitle;
    @BindView(R.id.editor_content)
    RichEditer mEditor;
    @BindView(R.id.ll_action_page1)
    LinearLayout mLlActionPage1;
    @BindView(R.id.ll_action_page2)
    LinearLayout mLlActionPage2;
    @BindView(R.id.bottom_tools)
    FrameLayout mBottomTools;

    @BindView(R.id.label_area)
    HorizontalScrollView mLabelArea;
    @BindView(R.id.ll_labels)
    LinearLayout mLlLanels;

    @BindView(R.id.fl_voice)
    FrameLayout mFlVoice;

    @BindView(R.id.voice_view)
    VoiceRectView mVoiceView;

    @BindView(R.id.iv_voice)
    ImageView mIvVoice;

    private View.OnFocusChangeListener mFocusChangeListener;
    private TextView.OnEditorActionListener mActionListener;

    private ImageWeight currentActionWeight;


    private int noteId;
    private Unbinder mUnbinder;
    private NoteBean mDetailBean;


    public static void startIntent(Context context) {
        Intent intent = new Intent(context, NoteEditorActivity.class);
        context.startActivity(intent);
    }

    /**
     * 编辑笔记  启动
     *
     * @param context
     * @param id      笔记的id
     */
    public static void startIntent(Context context, int id) {
        Intent intent = new Intent(context, NoteEditorActivity.class);
        intent.putExtra(NOTE_ID, id);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mToolbar.setTitle(R.string.edit);

        getIntentData();
        mUnbinder = ButterKnife.bind(this);
        initListener();
        createLableView(null);
        initRecog();

        mDetailBean = LitePal.find(NoteBean.class, noteId);

    }

    private void getIntentData() {
        noteId = getIntent().getIntExtra(NOTE_ID, -1);
    }


    //--------------语音识别部分   控制---------------
    /**
     * 控制UI按钮的状态
     */
    protected int status;
    /**
     * 识别控制器，使用MyRecognizer控制识别的流程
     */
    protected MyRecognizer myRecognizer;

    private RxPermissions rxPermissions;

    private RecogHandler handler;

    private void initRecog() {
        handler = new RecogHandler(this);
        status = STATUS_NONE;
        // 基于DEMO集成第1.1, 1.2, 1.3 步骤 初始化EventManager类并注册自定义输出事件
        // DEMO集成步骤 1.2 新建一个回调类，识别引擎会回调这个类告知重要状态和识别结果
        IRecogListener listener = new MessageStatusRecogListener(handler);

        // DEMO集成步骤 1.1 1.3 初始化：new一个IRecogListener示例 & new 一个 MyRecognizer 示例,并注册输出事件
        myRecognizer = new MyRecognizer(this, listener);

        mIvVoice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (status) {
                    case STATUS_NONE: // 初始状态
                        voiceStart();
                        status = STATUS_WAITING_READY;
                        break;
                    case STATUS_WAITING_READY: // 调用本类的start方法后，即输入START事件后，等待引擎准备完毕。
                    case STATUS_READY: // 引擎准备完毕。
                    case STATUS_SPEAKING: // 用户开始讲话
                    case STATUS_FINISHED: // 一句话识别语音结束
                    case STATUS_RECOGNITION: // 识别中
                        voiceStop();
                        status = STATUS_STOPPED; // 引擎识别中
                        break;
                    case STATUS_LONG_SPEECH_FINISHED: // 长语音识别结束
                    case STATUS_STOPPED: // 引擎识别中
                        voiceCancel();
                        status = STATUS_NONE; // 识别结束，回到初始状态
                        break;
                    case STATUS_ERROR:
                        voiceStart();
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 取消语音识别
     */
    private void voiceCancel() {
        myRecognizer.cancel();
    }

    /**
     * 结束语音识别
     * * 开始录音后，手动点击“停止”按钮。
     * * SDK会识别不会再识别停止后的录音。
     * * 基于DEMO集成4.1 发送停止事件 停止录音
     */
    private void voiceStop() {
        mFlVoice.setVisibility(View.GONE);
        myRecognizer.stop();
    }


    private void excuteVoiceInput() {
        boolean netConnected = NetWorkUtils.isNetConnected(getApplicationContext());
        if (netConnected) {
            requestPermission();
        } else {
            Toast.makeText(NoteEditorActivity.this, "语音输入需要网络", Toast.LENGTH_SHORT).show();
        }
    }

    private void startReco() {
        mFlVoice.setVisibility(View.VISIBLE);
        voiceStart();
        status = STATUS_WAITING_READY;

    }

    /**
     * 开始 录音
     */
    private void voiceStart() {
        hideSoftKeyBoard();
        // DEMO集成步骤2.1 拼接识别参数： 此处params可以打印出来，直接写到你的代码里去，最终的json一致即可。
        Map<String, Object> params = new HashMap<String, Object>();
        //  集成时不需要上面的代码，只需要params参数。
        params.put(SpeechConstant.ACCEPT_AUDIO_VOLUME, true);
        params.put(SpeechConstant.VAD_ENDPOINT_TIMEOUT, 0);
        // params 也可以根据文档此处手动修改，参数会以json的格式在界面和logcat日志中打印
        Log.e("TAG", "设置的start输入参数：" + params);
        // 复制此段可以自动检测常规错误
        (new AutoCheck(getApplicationContext(), new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainErrorMessage(); // autoCheck.obtainAllMessage();
                        Log.e("AutoCheckMessage", message);
                    }
                }
            }
        }, false)).checkAsr(params);

        // 这里打印出params， 填写至您自己的app中，直接调用下面这行代码即可。
        // DEMO集成步骤2.2 开始识别
        myRecognizer.start(params);
    }

    /**
     * 如果软键盘是打开的话，就隐藏
     */
    protected void hideSoftKeyBoard() {
        if (getCurrentFocus() != null) {
            View rootView = getCurrentFocus();
            if (this.imm == null) {
                this.imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            }
            if (this.imm != null) {
                this.imm.hideSoftInputFromWindow(rootView.getWindowToken(), 2);
            }
        }
    }

    public void requestPermission() {
        rxPermissions = new RxPermissions(this);
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        rxPermissions.request(permissions)
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        if (aBoolean) {
                            startReco();
                        } else {
                            Toast.makeText(NoteEditorActivity.this, "语音输入相关权限被拒绝", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    //识别各状态回调
    public static class RecogHandler extends Handler {

        private WeakReference<NoteEditorActivity> mReference;


        public RecogHandler(NoteEditorActivity activity) {
            mReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            mReference.get().handleMessage(msg);

        }
    }

    private void handleMessage(Message msg) {
        switch (msg.what) { // 处理MessageStatusRecogListener中的状态回调
            case STATUS_FINISHED:
                if (msg.arg2 == 1) {
                    mEditor.getLastFocusEdit().getText().append(msg.obj.toString());
                }
                status = msg.what;
                break;
            case STATUS_NONE:
            case STATUS_READY:
            case STATUS_SPEAKING:
            case STATUS_RECOGNITION:
                status = msg.what;
                break;
            case STATUS_ERROR:
                status = msg.what;
                mFlVoice.setVisibility(View.GONE);
                RecoError recoError = (RecoError) msg.obj;
                int errorCode = recoError.getErrorCode();
                if (errorCode == 1 || errorCode == 2) {
                    Toast.makeText(NoteEditorActivity.this, "网络不可用", Toast.LENGTH_SHORT).show();
                } else if (errorCode == 3) {
                    Toast.makeText(NoteEditorActivity.this, "录音服务启动失败", Toast.LENGTH_SHORT).show();
                }
                break;
            case VOLUME_CHANGE:
                //音量改变
                float volume = (Float) msg.obj;
                mVoiceView.setVloume(volume);
                break;
            default:
                break;

        }
    }


    private boolean editorHasFocus;

    private void initListener() {

        mEdtTitle.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    //title 控件抢夺了焦点
                    editorHasFocus = false;
                    changeBottomTools(false);
                }
            }
        });
        mEditor.setFocusChangeListener(new com.sogu.kindlelaw.note.editor.FocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    if (!editorHasFocus) {
                        editorHasFocus = true;
                        changeBottomTools(true);
                    }
                }
            }
        });
        //标签控件 抢夺了焦点
        mFocusChangeListener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                //标签控件 抢夺了焦点
                if (hasFocus) {
                    editorHasFocus = false;
                    changeBottomTools(false);
                }
            }
        };


        //创建下一个 label
        mActionListener = new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    LabelView labelView = (LabelView) mLlLanels.getChildAt(mLlLanels.getChildCount() - 1);
                    if (TextUtils.isEmpty(labelView.getContent())) {
                        Toast.makeText(NoteEditorActivity.this, "请输入文字", Toast.LENGTH_SHORT).show();
                        return true;
                    }
                    labelView.showClose();
                    if (mLlLanels.getChildCount() != 20) {
                        //创建下一个 label
                        createLableView(null);
                    }
                    return true;

                }
                return false;
            }
        };

        mEditor.setImageActionListener(new ImageActionListener() {
            @Override
            public void onAction(int action, ImageWeight imageWeight) {
                switch (action) {
                    case ImageActionListener.ACT_REPLACE:
                        //选择单张图片
                        currentActionWeight = imageWeight;
                        pickSingleImage();
                        break;
                    case ImageActionListener.ACT_PREVIEW:
                        Pair<Integer, List<String>> paths = mEditor.getIndexAndPaths(imageWeight);
                        preImage(paths);
                        break;
                }
            }
        });

    }

    /**
     * 预览 编辑器中的图片
     *
     * @param paths
     */
    private void preImage(Pair<Integer, List<String>> paths) {
        List<String> path = paths.second;
        List<LocalMedia> mediaList = new ArrayList<>();
        for (String imagePath : path) {
            LocalMedia media = new LocalMedia();
            media.setPath(imagePath);
            mediaList.add(media);
        }

        PictureSelector.create(this).themeStyle(R.style.picture_default_style).openExternalPreview(paths.first, SAVE_PATH, mediaList);

    }

    //----------------------- 选择单个 image  用于编辑器的替换 图片操作
    public static final int PICK_REPLACE_IMAGE = 10001;

    private void pickSingleImage() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .selectionMode(PictureConfig.SINGLE)
                .isCamera(true)
                .compress(true)
                .cropCompressQuality(30)
                .isGif(false)
                .forResult(PICK_REPLACE_IMAGE);
    }

    public void createLableView(String labelContent) {

        LabelView labelView = new LabelView(this, labelContent, mLlLanels.getChildCount() != 0);

        labelView.setActionListener(mActionListener);

        labelView.setFocusListener(mFocusChangeListener);

        labelView.setOnCloseListener(new LabelView.OnCloseListener() {
            @Override
            public void onClose(View view) {
                mLlLanels.removeView(view);
            }
        });

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, DensityUtil.dp2px(20));

        params.leftMargin = DensityUtil.dp2px(11);

        mLlLanels.addView(labelView, params);
    }

    private void changeBottomTools(boolean isShow) {
        mBottomTools.setVisibility(isShow ? View.VISIBLE : View.GONE);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mLabelArea.getLayoutParams();
        layoutParams.bottomMargin = isShow ? DensityUtil.dp2px(44) : 0;
    }


    public void savePicture() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_hhmmss");
            String fileName = sdf.format(new Date()) + ".jpg";
            String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + SAVE_PATH;
            File file = CaptureUtil.saveBitmap(mEditor, dir, fileName);
            if (file == null) {
                Log.e("TAG", "生成失败：");
            } else {
                Toast.makeText(NoteEditorActivity.this, "图片已保存到SD卡kindlawNote目录下", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("TAG", "生成失败：");
            e.printStackTrace();
        }
    }


    @OnClick({R.id.btn_pick_image, R.id.btn_font_size, R.id.btn_alignment, R.id.btn_bullet, R.id.btn_todo, R.id.btn_more, R.id.btn_blod, R.id.btn_italic, R.id.btn_underline, R.id.btn_strikethrough, R.id.btn_quote, R.id.btn_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_pick_image:
                startPickImage();
                break;
            case R.id.btn_font_size:
                mEditor.fontSize();
                break;
            case R.id.btn_alignment:
                mEditor.alignment();
                break;
            case R.id.btn_bullet:
                mEditor.bullet();
                break;
            case R.id.btn_todo:
                mEditor.todo();
                break;
            case R.id.btn_more:
                mLlActionPage1.setVisibility(View.GONE);
                mLlActionPage2.setVisibility(View.VISIBLE);
                break;
            case R.id.btn_blod:
                mEditor.bold();
                break;
            case R.id.btn_italic:
                mEditor.italic();
                break;
            case R.id.btn_underline:
                mEditor.underline();
                break;
            case R.id.btn_strikethrough:
                mEditor.strikethrough();
                break;
            case R.id.btn_quote:
                mEditor.quote();
                break;
            case R.id.btn_back:
                mLlActionPage1.setVisibility(View.VISIBLE);
                mLlActionPage2.setVisibility(View.GONE);
                break;
        }
    }


    private void startPickImage() {
        PictureSelector.create(this)
                .openGallery(PictureMimeType.ofImage())
                .maxSelectNum(9)
                .isCamera(true)
                .isGif(false)
                .compress(true)
                .cropCompressQuality(30)
                .forResult(PictureConfig.CHOOSE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case PictureConfig.CHOOSE_REQUEST:
                    // 图片、视频、音频选择结果回调
                    List<LocalMedia> selectList = PictureSelector.obtainMultipleResult(data);
                    // 例如 LocalMedia 里面返回三种path
                    // 1.media.getPath(); 为原图path
                    // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true  注意：音视频除外
                    // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true  注意：音视频除外
                    // 如果裁剪并压缩了，以取压缩路径为准，因为是先裁剪后压缩的
                    for (LocalMedia media : selectList) {
                        String compressPath = media.getPath();
                        mEditor.insertImage(compressPath);
                    }
                    break;
                case PICK_REPLACE_IMAGE:
                    List<LocalMedia> pick = PictureSelector.obtainMultipleResult(data);
                    if (pick != null && pick.size() > 0) {
                        LocalMedia localMedia = pick.get(0);
                        currentActionWeight.replacePath(localMedia.getCompressPath());
                        currentActionWeight.setShortPath(null);
                    }

                    break;
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                excuteEdit();
                break;
            case R.id.editor_voice_input:
                excuteVoiceInput();
                break;
            case R.id.editor_todo:
                skipTodos();
                break;
            case R.id.editor_export:
                savePicture();
                break;
        }
        return true;
    }

    private void skipTodos() {

    }

    private boolean hasHandle = true;

    @Override
    public void onBackPressed() {
        if (!hasHandle) {
            return;
        }
        hasHandle = false;
        Log.e("TAG", "执行保存");
        excuteEdit();
    }

    /**
     * 执行新增或编辑操作
     */
    private void excuteEdit() {

        showLoadingDialog();
        //标题
        String title = mEdtTitle.getText().toString().trim();
        //正文
        String content = mEditor.getContent();
        //进行 笔记为 空的 判断
        if (TextUtils.isEmpty(title) && TextUtils.isEmpty(content)) {
            //笔记内容为空   直接返回
            hasHandle = true;
            finish();
            return;
        }

        if (TextUtils.isEmpty(title)) {
            title = getString(R.string.title_note);
        }
        //标签
        List<String> labelList = new ArrayList<>();
        int childCount = mLlLanels.getChildCount();
        for (int i = 0; i < childCount; i++) {
            LabelView labelView = (LabelView) mLlLanels.getChildAt(i);
            String labelContent = labelView.getContent();
            if (!TextUtils.isEmpty(labelContent)) {
                labelList.add(labelContent);
            }
        }
        //获取 label 的字符串  就不传
        String labels = labelList.size() == 0 ? "" : JSON.toJSONString(labelList);


        //正文 格式 html
        String contentHtml = mEditor.toHtml();


        //获取  编辑器 的 图片链接
        List<String> paths = mEditor.getImagePaths();
        String imgs = listToString(paths);

        if (mDetailBean == null) {
            //新增笔记
            newNote(title, content, contentHtml, labels, imgs);

        } else {
            boolean eqTitle = mDetailBean.getTitle().equalsIgnoreCase(title);
            boolean eqContent = mDetailBean.getContent_html().equalsIgnoreCase(contentHtml);
            boolean eqLabel = labels.equals(mDetailBean.getNoteLabels());
            Log.e("TAG", "eqTitle: " + eqTitle + "  eqContent: " + eqContent + " eqLabel： " + eqLabel);
            if (eqTitle && eqContent && eqLabel) {
                hasHandle = true;
                finish();
                return;
            }

            //编辑笔记
            StringBuilder delImgs = new StringBuilder();
//            if (mDetailBean != null) {
//                List<NoteBean.NoteImgBean> note_imgs = mDetailBean.getNote_img();
//                if (note_imgs != null && note_imgs.size() > 0) {
//                    Iterator<NoteBean.NoteImgBean> iterator = note_imgs.iterator();
//                    while (iterator.hasNext()) {
//                        NoteBean.NoteImgBean imageBean = iterator.next();
//                        if (paths.contains(imageBean.getShorturl())) {
//                            iterator.remove();
//                        }
//                    }
//                    for (int i = 0; i < note_imgs.size(); i++) {
//                        NoteBean.NoteImgBean imgBean = note_imgs.get(i);
//                        delImgs.append(imgBean.getShorturl());
//                        if (i != note_imgs.size() - 1) {
//                            delImgs.append(",");
//                        }
//                    }
//                }
//            }
            editNote(title, content, contentHtml, labels, imgs, mDetailBean);

        }
    }

    private void editNote(String title, String content, String contentHtml, String labels, String imgs, NoteBean detailBean) {
        detailBean.setTitle(title);
        detailBean.setContent(content);
        detailBean.setContent_html(contentHtml);
        detailBean.setNoteLabels(labels);
        detailBean.setNoteImg(imgs);
        detailBean.setCreatetime(new Date().getTime());
        detailBean.update(detailBean.getId());
        hiddenLoadingDialog();
        finish();
    }

    private void newNote(String title, String content, String contentHtml, String labels, String imgs) {
        NoteBean noteBean = new NoteBean();
        noteBean.setTitle(title);
        noteBean.setContent(content);
        noteBean.setContent_html(contentHtml);
        noteBean.setNoteLabels(labels);
        noteBean.setCreatetime(new Date().getTime());
        noteBean.setNoteImg(imgs);
        boolean save = noteBean.save();
        hiddenLoadingDialog();
        finish();
    }


    private ProgressDialog mLoadingDialog;

    public void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new ProgressDialog(this);
            //设置 弹窗不能消失
            mLoadingDialog.setCancelable(false);
            mLoadingDialog.setCanceledOnTouchOutside(false);
        }
        if (!mLoadingDialog.isShowing())
            mLoadingDialog.show();
    }

    public void hiddenLoadingDialog() {
        if (null != mLoadingDialog && mLoadingDialog.isShowing())
            mLoadingDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        if (myRecognizer != null) {
            // 如果之前调用过myRecognizer.loadOfflineEngine()， release()里会自动调用释放离线资源
            // 基于DEMO5.1 卸载离线资源(离线时使用) release()方法中封装了卸载离线资源的过程
            // 基于DEMO的5.2 退出事件管理器
            myRecognizer.release();
        }

        hiddenLoadingDialog();
        if (mUnbinder != null) {
            mUnbinder.unbind();
        }
        super.onDestroy();
    }

    //如果有Menu,创建完后,系统会自动添加到ToolBar上
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        setIconVisible(menu, true);
        return true;
    }

    public void setIconVisible(Menu menu, boolean visable) {
        Field field;
        try {
            field = menu.getClass().getDeclaredField("mOptionalIconsVisible");

            field.setAccessible(true);
            field.set(menu, visable);
        } catch (NoSuchFieldException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static String listToString(List list) {
        if (list == null) {
            return "";
        }
        Iterator it = list.iterator();
        if (!it.hasNext())
            return "";

        StringBuilder sb = new StringBuilder();
        for (; ; ) {
            Object e = it.next();
            sb.append(e.toString());
            if (!it.hasNext())
                return sb.toString();
            sb.append(',');
        }
    }


}
