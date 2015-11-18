package com.njuguna.dailyselfie.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.common.net.MediaType;
import com.njuguna.dailyselfie.R;
import com.njuguna.dailyselfie.api.ImageProcessingServiceApi;
import com.njuguna.dailyselfie.app.Config;
import com.njuguna.dailyselfie.app.SelfieApplication;
import com.njuguna.dailyselfie.common.Constants;
import com.njuguna.dailyselfie.common.ImageFilters;
import com.njuguna.dailyselfie.common.util.IDUtils;
import com.njuguna.dailyselfie.data.SelfieContract;
import com.njuguna.dailyselfie.model.Selfie;
import com.njuguna.dailyselfie.util.DataUtils;
import com.njuguna.dailyselfie.util.TimedOutUrlConnectionClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class SelfieDetailEditFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = SelfieDetailEditFragment.class.getSimpleName();

    private static final String ARG_INTENT_ACTION = "intent_action";

    private Selfie mSelfie;
    private SelfieApplication mSelfieApplication;

    private MaterialEditText mCaptionEditText;
    private MaterialEditText mDateTakenEditText;
    private ImageView mSelfieImageView;
    private ImageButton mCropButton;
    private ImageButton mCameraButton;

    private long mSelectedSelfieId;
    private String mIntentAction;

    private static final int SELFIE_LOADER = 100;

    private static final String[] SELFIE_COLUMNS = {
            SelfieContract.Selfies._ID,
            SelfieContract.Selfies.COLUMN_GUID,
            SelfieContract.Selfies.COLUMN_CREATE_DATE,
            SelfieContract.Selfies.COLUMN_CREATE_BY,
            SelfieContract.Selfies.COLUMN_CREATE_LAT,
            SelfieContract.Selfies.COLUMN_CREATE_LONG,
            SelfieContract.Selfies.COLUMN_UPDATE_DATE,
            SelfieContract.Selfies.COLUMN_UPDATE_BY,
            SelfieContract.Selfies.COLUMN_UPDATE_LAT,
            SelfieContract.Selfies.COLUMN_UPDATE_LONG,
            SelfieContract.Selfies.COLUMN_CAPTION,
            SelfieContract.Selfies.COLUMN_SAVED_IMAGE,
            SelfieContract.Selfies.COLUMN_SAVED_PATH,
            SelfieContract.Selfies.COLUMN_THUMBNAIL_PATH,
            SelfieContract.Selfies.COLUMN_ORIGINAL_PATH,
    };

    public static SelfieDetailEditFragment newInstance(String intentAction, Long selectedSelfieId) {
        SelfieDetailEditFragment fragment = new SelfieDetailEditFragment();
        Bundle args = new Bundle();
        args.putString(ARG_INTENT_ACTION, intentAction);
        if (null != selectedSelfieId) args.putLong(SelfieDetailActivity.ARG_SELECTED_SELFIE_ID, selectedSelfieId.longValue());
        fragment.setArguments(args);
        return fragment;
    }

    public SelfieDetailEditFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_selfie_detail, container, false);

        mCaptionEditText = (MaterialEditText) view.findViewById(R.id.caption);
        mDateTakenEditText = (MaterialEditText) view.findViewById(R.id.date_taken);
        mSelfieImageView = (ImageView) view.findViewById(R.id.selfie);

        mCameraButton = (ImageButton) view.findViewById(R.id.camera_button);
        mCropButton = (ImageButton) view.findViewById(R.id.crop_button);

        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        if (mIntentAction.equals(Intent.ACTION_VIEW)) {
            mCaptionEditText.setEnabled(false);
            mCameraButton.setEnabled(false);
        }

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            mSelectedSelfieId = getArguments().getLong(SelfieDetailActivity.ARG_SELECTED_SELFIE_ID, 0);
            mIntentAction = getArguments().getString(ARG_INTENT_ACTION);
        }
    }

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mSelfieApplication = (SelfieApplication) getActivity().getApplication();
        mSelfie = new Selfie(IDUtils.generateBase64GUIDwNum(mSelfieApplication.getUser().getuNum()));
        if ((!mIntentAction.equals(Intent.ACTION_INSERT)) || (mSelectedSelfieId > 0)) {
            getLoaderManager().initLoader(SELFIE_LOADER, null, this);
        }

        if (mIntentAction.equals(Intent.ACTION_INSERT)) {
            dispatchTakePictureIntent();
        }

        ActionBar actionBar = ((AppCompatActivity) getActivity())
                .getSupportActionBar();
        switch (mIntentAction) {
            case Intent.ACTION_EDIT:
                actionBar.setTitle(R.string.fragment_selfie_edit_title);
                break;
            case Intent.ACTION_VIEW:
                actionBar.setTitle(R.string.fragment_selfie_view_title);
                break;
            case Intent.ACTION_INSERT:
                actionBar.setTitle(R.string.fragment_selfie_insert_title);
                break;
        }
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = DataUtils.createOriginalImageFile(mSelfie);
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            ImageLoader imageLoader = ImageLoader.getInstance();
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .build();
            imageLoader.displayImage("file://" + mSelfie.getOriginalPath(), mSelfieImageView, options, new ImageLoadingListener() {
                @Override
                public void onLoadingStarted(String imageUri, View view) {

                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Toast.makeText(getActivity(), failReason.toString(), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                }

                @Override
                public void onLoadingCancelled(String imageUri, View view) {

                }
            });

            imageLoader.loadImage("file://" + mSelfie.getOriginalPath(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    mSelfie.setOriginalBitmap(loadedImage);
                    mSelfie.setSavedBitmap(DataUtils.generateSavedBitmap(loadedImage));
                    mSelfie.setCreateDate(System.currentTimeMillis());
                    mDateTakenEditText.setText(Constants.DATE_TIME_FORMATTER.format(mSelfie.getCreateDate()));
                    DataUtils.saveImage(mSelfie.getSavedBitmap(), SelfieDetailEditFragment.this.mSelfie);
                }

                @Override
                public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                    Toast.makeText(getActivity(), failReason.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mSelfie.getOriginalPath());
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);
    }

    private void setPic() {
        // Get the dimensions of the View
        int targetW = Constants.MAX_WIDTH_PIXELS;
        int targetH = Constants.MAX_HEIGHT_PIXELS;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mSelfie.getOriginalPath(), bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mSelfie.getOriginalPath(), bmOptions);
        mSelfie.setOriginalBitmap(bitmap);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        switch (mIntentAction) {
            case Intent.ACTION_EDIT:
                inflater.inflate(R.menu.default_edit_actions, menu);
                break;
            case Intent.ACTION_VIEW:
                inflater.inflate(R.menu.default_view_actions, menu);
                break;
            case Intent.ACTION_INSERT:
                inflater.inflate(R.menu.default_edit_actions, menu);
                break;
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (mIntentAction) {
            case Intent.ACTION_EDIT:
            case Intent.ACTION_INSERT:
                switch (item.getItemId()) {
                    case R.id.menu_process:
                        new MaterialDialog.Builder(getActivity())
                                .title(R.string.effect_choose_title)
                                .items(R.array.effects)
                                .itemsCallbackSingleChoice(0, new MaterialDialog.ListCallbackSingleChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                                        switch (which) {
                                            case 0:
                                                processImage(ImageFilters.GRAYSCALE_FILTER);
                                                break;
                                            case 1:
                                                processImage(ImageFilters.INVERT_FILTER);
                                                break;
                                            case 2:
                                                processImage(ImageFilters.WEAVE_FILTER);
                                                break;
                                            case 3:
                                                processImage(ImageFilters.SMEAR_FILTER);
                                                break;

                                        }
                                        return true; // allow selection
                                    }
                                })
                                .positiveText(R.string.effect_choose_label)
                                .show();

                        return true;
                    case R.id.menu_save:
                        item.setEnabled(false);
                        if (!mCaptionEditText.getText().toString().trim().isEmpty()) mSelfie.setCaption(mCaptionEditText.getText().toString().trim());
                        mSelfie.setCreateBy(mSelfieApplication.getUser().getId());
                        mSelfie.setThumbnailBitmap(DataUtils.generateThumbnailBitmap(mSelfie.getSavedBitmap()));
                        DataUtils.saveThumbnailImage(mSelfie.getThumbnailBitmap(), mSelfie);
                        mSelectedSelfieId = mSelfieApplication.getSelfiesDataHelper().upsert(mSelfie);
                        getActivity().finish();
                        return true;
                }
            case Intent.ACTION_VIEW:
                switch (item.getItemId()) {
                    case R.id.menu_delete:
                        deleteSelfie();
                        return true;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteSelfie() {
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.selfie_dialog_delete_message)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getActivity().getContentResolver().delete(ContentUris.withAppendedId(SelfieContract.Selfies.CONTENT_URI, mSelectedSelfieId),
                                null, null);
                        getActivity().finish();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        alertDialog.show();
    }
    private void processImage(int filter) {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(Config.PROFILE_SERVER)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setClient(new TimedOutUrlConnectionClient())
                .build();

        ImageProcessingServiceApi imageProcessingService = restAdapter.create(ImageProcessingServiceApi.class);

        imageProcessingService.processImage("ignored", filter,
                new TypedFile(MediaType.JPEG.toString(), new File(mSelfie.getSavedPath())),
                new Callback<Response>() {
                    @Override
                    public void success(Response bytes, Response response) {
                        Log.e(LOG_TAG, "Successfully processed image.");
                        try {
                            BufferedInputStream isr = new BufferedInputStream(response.getBody().in());
                            Bitmap processedImage = BitmapFactory.decodeStream(isr);
                            mSelfieImageView.setImageBitmap(processedImage);
                            mSelfie.setSavedBitmap(processedImage);
                            mSelfie.setUpdateDate(System.currentTimeMillis());
                            DataUtils.saveImage(mSelfie.getSavedBitmap(), SelfieDetailEditFragment.this.mSelfie);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.e(LOG_TAG, "Error processing photo!");
                    }
                }
        );

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                SelfieContract.Selfies.buildSelfieUri(mSelectedSelfieId),
                SELFIE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            mSelfie = mSelfieApplication.getSelfiesDataHelper().buildInstance(data);
            mCaptionEditText.setText(mSelfie.getCaption());
            mDateTakenEditText.setText(Constants.DATE_TIME_FORMATTER.format(mSelfie.getCreateDate()));

            final DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                    .build();
            ImageLoader.getInstance().displayImage("file://" + mSelfie.getSavedPath(), mSelfieImageView, options);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
