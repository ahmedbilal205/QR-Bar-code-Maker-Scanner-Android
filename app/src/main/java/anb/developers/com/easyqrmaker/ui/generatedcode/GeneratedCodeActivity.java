package anb.developers.com.easyqrmaker.ui.generatedcode;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;
import androidx.databinding.DataBindingUtil;
import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableCompletableObserver;
import io.reactivex.schedulers.Schedulers;
import anb.developers.com.easyqrmaker.R;
import anb.developers.com.easyqrmaker.databinding.ActivityGeneratedCodeBinding;
import anb.developers.com.easyqrmaker.helpers.constant.AppConstants;
import anb.developers.com.easyqrmaker.helpers.constant.IntentKey;
import anb.developers.com.easyqrmaker.helpers.model.Code;
import anb.developers.com.easyqrmaker.helpers.util.FileUtil;
import anb.developers.com.easyqrmaker.helpers.util.PermissionUtil;
import anb.developers.com.easyqrmaker.helpers.util.ProgressDialogUtil;
import anb.developers.com.easyqrmaker.ui.settings.SettingsActivity;

public class GeneratedCodeActivity extends AppCompatActivity implements View.OnClickListener {

    private final int REQUEST_CODE_TO_SHARE = 1;
    private final int REQUEST_CODE_TO_SAVE = 2;
    private final int REQUEST_CODE_TO_PRINT = 3;

    private ActivityGeneratedCodeBinding mBinding;
    private Menu mToolbarMenu;
    private Code mCurrentCode;
    private Bitmap mCurrentGeneratedCodeBitmap;
    private File mCurrentCodeFile, mCurrentPrintedFile;
    private CompositeDisposable mCompositeDisposable;

    public CompositeDisposable getCompositeDisposable() {
        return mCompositeDisposable;
    }

    public void setCompositeDisposable(CompositeDisposable compositeDisposable) {
        mCompositeDisposable = compositeDisposable;
    }

    public File getCurrentPrintedFile() {
        return mCurrentPrintedFile;
    }

    public void setCurrentPrintedFile(File currentPrintedFile) {
        mCurrentPrintedFile = currentPrintedFile;
    }

    public File getCurrentCodeFile() {
        return mCurrentCodeFile;
    }

    public void setCurrentCodeFile(File currentCodeFile) {
        mCurrentCodeFile = currentCodeFile;
    }

    public Code getCurrentCode() {
        return mCurrentCode;
    }

    public void setCurrentCode(Code currentCode) {
        mCurrentCode = currentCode;
    }

    public Menu getToolbarMenu() {
        return mToolbarMenu;
    }

    public void setToolbarMenu(Menu toolbarMenu) {
        mToolbarMenu = toolbarMenu;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_generated_code);
        setCompositeDisposable(new CompositeDisposable());

        getWindow().setBackgroundDrawable(null);

        initializeToolbar();
        loadQRCode();
        setListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getCompositeDisposable().dispose();
    }

    private void setListeners() {
        mBinding.imageViewSave.setOnClickListener(this);
        mBinding.imageViewShare.setOnClickListener(this);
        mBinding.imageViewPrint.setOnClickListener(this);
    }

    private void loadQRCode() {
        Intent intent = getIntent();

        if (intent != null) {
            Bundle bundle = intent.getExtras();

            if (bundle != null && bundle.containsKey(IntentKey.MODEL)) {
                setCurrentCode(bundle.getParcelable(IntentKey.MODEL));
            }
        }

        if (getCurrentCode() != null) {
            ProgressDialogUtil.on().showProgressDialog(this);

            mBinding.textViewContent.setText(String.format(Locale.ENGLISH,
                    getString(R.string.content), getCurrentCode().getContent()));

            mBinding.textViewType.setText(String.format(Locale.ENGLISH, getString(R.string.code_type),
                    getResources().getStringArray(R.array.code_types)[getCurrentCode().getType()]));

            BarcodeFormat barcodeFormat;
            switch (getCurrentCode().getType()) {
                case Code.BAR_CODE:
                    barcodeFormat = BarcodeFormat.CODE_128;
                    break;

                case Code.QR_CODE:
                    barcodeFormat = BarcodeFormat.QR_CODE;
                    break;

                default:
                    barcodeFormat = null;
                    break;
            }

            if (barcodeFormat != null) {
                try {
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.encodeBitmap(getCurrentCode().getContent(),
                            barcodeFormat, 1000, 1000);
                    mBinding.imageViewGeneratedCode.setImageBitmap(bitmap);
                    mCurrentGeneratedCodeBitmap = bitmap;
                } catch (Exception e) {
                    if (!TextUtils.isEmpty(e.getMessage())) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }
                }
            }

            ProgressDialogUtil.on().hideProgressDialog();
        }
    }

    private void initializeToolbar() {
        setSupportActionBar(mBinding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;

            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_toolbar_menu, menu);
        setToolbarMenu(menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.image_view_print:
                if (PermissionUtil.on().requestPermission(this,
                        REQUEST_CODE_TO_PRINT, WRITE_EXTERNAL_STORAGE)) {
                    if (getCurrentPrintedFile() == null) {
                        storeCodeDocument();
                    } else {
                        Toast.makeText(this,
                                getString(R.string.generated_qr_code_already_exists),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.image_view_save:
                if (PermissionUtil.on().requestPermission(this,
                        REQUEST_CODE_TO_SAVE, WRITE_EXTERNAL_STORAGE)) {
                    if (getCurrentCodeFile() == null) {
                        storeCodeImage(true);
                    } else {
                        Toast.makeText(this,
                                getString(R.string.generated_qr_code_already_exists),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case R.id.image_view_share:
                shareCode(getCurrentCodeFile());
                break;

            default:
                break;
        }
    }

    private void storeCodeImage(boolean justSave) {
        ProgressDialogUtil.on().showProgressDialog(this);
        if (PermissionUtil.on().isAllowed(WRITE_EXTERNAL_STORAGE))
        {
            saveToGallery();
        }else {
            PermissionUtil.on().requestPermission(this,WRITE_EXTERNAL_STORAGE);
        }
    }

    private void storeCodeDocument() {
        ProgressDialogUtil.on().showProgressDialog(this);
        getCompositeDisposable().add(
                Completable.create(emitter -> {
                    String type = getResources().getStringArray(R.array.code_types)[getCurrentCode().getType()];
                    File codeDocumentFile = FileUtil.getEmptyFile(this, AppConstants.PREFIX_CODE,
                            String.format(Locale.ENGLISH, getString(R.string.file_name_body),
                                    type.substring(0, type.indexOf(" Code")),
                                    String.valueOf(System.currentTimeMillis())),
                            AppConstants.SUFFIX_CODE,
                            Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT ?
                                    Environment.DIRECTORY_PICTURES : Environment.DIRECTORY_DOCUMENTS);

                    if (codeDocumentFile != null && mCurrentGeneratedCodeBitmap != null && getCurrentCode() != null) {
                        try {
                            Document document = new Document();

                            PdfWriter.getInstance(document, new FileOutputStream(codeDocumentFile));

                            document.open();
                            document.setPageSize(PageSize.A4);
                            document.addCreationDate();
                            document.addAuthor(getString(R.string.app_name));
                            document.addCreator(getString(R.string.app_name));

                            BaseColor colorAccent = new BaseColor(0, 153, 204, 255);
                            float headingFontSize = 20.0f;
                            float valueFontSize = 26.0f;

                            BaseFont baseFont = BaseFont.createFont("res/font/opensans_regular.ttf", "UTF-8", BaseFont.EMBEDDED);

                            LineSeparator lineSeparator = new LineSeparator();
                            lineSeparator.setLineColor(new BaseColor(0, 0, 0, 68));

                            // Adding Title....
                            Font mOrderDetailsTitleFont = new Font(baseFont, 36.0f, Font.NORMAL, BaseColor.BLACK);
                            Chunk mOrderDetailsTitleChunk = new Chunk("Code Details", mOrderDetailsTitleFont);
                            Paragraph mOrderDetailsTitleParagraph = new Paragraph(mOrderDetailsTitleChunk);
                            mOrderDetailsTitleParagraph.setAlignment(Element.ALIGN_CENTER);
                            document.add(mOrderDetailsTitleParagraph);

                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            mCurrentGeneratedCodeBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                            Image codeImage = Image.getInstance(stream.toByteArray());
                            codeImage.setAlignment(Image.ALIGN_CENTER);
                            codeImage.scalePercent(40);
                            Paragraph imageParagraph = new Paragraph();
                            imageParagraph.add(codeImage);
                            document.add(imageParagraph);

                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));

                            // Adding Chunks for Title and value
                            Font mOrderIdFont = new Font(baseFont, headingFontSize, Font.NORMAL, colorAccent);
                            Chunk mOrderIdChunk = new Chunk("Content:", mOrderIdFont);
                            Paragraph mOrderIdParagraph = new Paragraph(mOrderIdChunk);
                            document.add(mOrderIdParagraph);

                            Font mOrderIdValueFont = new Font(baseFont, valueFontSize, Font.NORMAL, BaseColor.BLACK);
                            Chunk mOrderIdValueChunk = new Chunk(getCurrentCode().getContent(), mOrderIdValueFont);
                            Paragraph mOrderIdValueParagraph = new Paragraph(mOrderIdValueChunk);
                            document.add(mOrderIdValueParagraph);

                            document.add(new Paragraph(AppConstants.EMPTY_STRING));
                            document.add(Chunk.NEWLINE);
                            document.add(new Paragraph(AppConstants.EMPTY_STRING));

                            // Fields of Order Details...
                            Font mOrderDateFont = new Font(baseFont, headingFontSize, Font.NORMAL, colorAccent);
                            Chunk mOrderDateChunk = new Chunk("Type:", mOrderDateFont);
                            Paragraph mOrderDateParagraph = new Paragraph(mOrderDateChunk);
                            document.add(mOrderDateParagraph);

                            Font mOrderDateValueFont = new Font(baseFont, valueFontSize, Font.NORMAL, BaseColor.BLACK);
                            Chunk mOrderDateValueChunk = new Chunk(type, mOrderDateValueFont);
                            Paragraph mOrderDateValueParagraph = new Paragraph(mOrderDateValueChunk);
                            document.add(mOrderDateValueParagraph);

                            document.close();

                            setCurrentPrintedFile(codeDocumentFile);
                            if (!emitter.isDisposed()) {
                                emitter.onComplete();
                            }
                        } catch (IOException | DocumentException ie) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(ie);
                            }
                        } catch (ActivityNotFoundException ae) {
                            if (!emitter.isDisposed()) {
                                emitter.onError(ae);
                            }
                        }
                    } else {
                        if (!emitter.isDisposed()) {
                            emitter.onError(new NullPointerException());
                        }
                    }
                }).observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .subscribeWith(new DisposableCompletableObserver() {
                            @Override
                            public void onComplete() {
                                ProgressDialogUtil.on().hideProgressDialog();
                                Toast.makeText(GeneratedCodeActivity.this,
                                        "PDF saved at Internal Storage/Documents/"+getString(R.string.app_name)+"/.",
                                        Toast.LENGTH_LONG).show();
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (e != null && !TextUtils.isEmpty(e.getMessage())) {
                                    Log.e(getClass().getSimpleName(), e.getMessage());
                                }

                                ProgressDialogUtil.on().hideProgressDialog();
                                Toast.makeText(GeneratedCodeActivity.this,
                                        getString(R.string.failed_to_save_the_code),
                                        Toast.LENGTH_SHORT).show();
                            }
                        })
        );
    }

    private void saveToGallery(){
        CardView cardView = findViewById(R.id.card_view_parent);
        Bitmap bitmap = getBitmapFromView(cardView);
        OutputStream fos;

        try{
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                ContentResolver resolver = getContentResolver();
                ContentValues contentValues =  new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Image_" + ".jpg");
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + "QR Generator");
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                fos = resolver.openOutputStream(Objects.requireNonNull(imageUri));
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                Objects.requireNonNull(fos);
                ProgressDialogUtil.on().hideProgressDialog();
                Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();
            }else {
                File directory = new File(Environment.getExternalStorageDirectory().toString() + '/' + getString(R.string.app_name));
                if (!directory.exists()) {
                    directory.mkdirs();
                }
                String fileName = System.currentTimeMillis() + ".png";
                File file = new File(directory, fileName);
                try {
                    saveImageToStream(bitmap, new FileOutputStream(file));
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
                    this.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                    Toast.makeText(this, "Image Saved", Toast.LENGTH_SHORT).show();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save Check Permissions\n"+e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                }
            }

        }catch(Exception e){
            ProgressDialogUtil.on().hideProgressDialog();
            Toast.makeText(this, "Image not saved \n" + e.toString(), Toast.LENGTH_SHORT).show();
        }
        ProgressDialogUtil.on().hideProgressDialog();

    }
    private void shareCode(File codeImageFile) {
        CardView cardView = findViewById(R.id.card_view_parent);
        Bitmap bitmap = getBitmapFromView(cardView);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "title");
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values);
        OutputStream outstream;
        try {
            outstream = getContentResolver().openOutputStream(uri);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outstream);
            outstream.close();
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        startActivity(Intent.createChooser(shareIntent, "Share Image"));
    }

    private void saveImageToStream(Bitmap bitmap, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean isValid = true;

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                isValid = false;
            }
        }

        switch (requestCode) {
            case REQUEST_CODE_TO_SAVE:
                if (isValid) {
                    if (getCurrentCodeFile() == null) {
                        storeCodeImage(true);
                    } else {
                        Toast.makeText(this,
                                getString(R.string.generated_qr_code_already_exists),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case REQUEST_CODE_TO_PRINT:
                if (isValid) {
                    if (getCurrentPrintedFile() == null) {
                        storeCodeDocument();
                    } else {
                        Toast.makeText(this,
                                getString(R.string.generated_qr_code_already_exists),
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            case REQUEST_CODE_TO_SHARE:
                if (isValid) {
                    if (getCurrentCodeFile() == null) {
                        storeCodeImage(false);

                        if (getCurrentCodeFile() != null) {
                            shareCode(getCurrentCodeFile());
                        } else {
                            Toast.makeText(this,
                                    getString(R.string.failed_to_share_the_code), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        shareCode(getCurrentCodeFile());
                    }
                }
                break;

            default:
                break;
        }
    }
    public static Bitmap getBitmapFromView(View view) {

        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return returnedBitmap;
    }
}