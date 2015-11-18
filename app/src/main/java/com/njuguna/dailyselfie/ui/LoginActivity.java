package com.njuguna.dailyselfie.ui;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.digits.sdk.android.AuthCallback;
import com.digits.sdk.android.DigitsAuthButton;
import com.digits.sdk.android.DigitsException;
import com.digits.sdk.android.DigitsSession;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.common.base.Strings;
import com.njuguna.dailyselfie.R;
import com.njuguna.dailyselfie.api.UserServiceApi;
import com.njuguna.dailyselfie.app.Config;
import com.njuguna.dailyselfie.app.SelfieApplication;
import com.njuguna.dailyselfie.common.Constants;
import com.njuguna.dailyselfie.profile.entity.User;
import com.njuguna.dailyselfie.profile.entity.UserBuilder;
import com.njuguna.dailyselfie.util.TimedOutUrlConnectionClient;

import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;

import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {

    private static final String TAG = LoginActivity.class.getSimpleName();

    private SelfieApplication mSelfieApplication;
    private LinearLayout mSignedInLayout;
    private TextView mFullNameView;
    private TextView mUserNameView;
    private Button mCloseButton;
    private Button mExitButton;
    private boolean mSignInAttempted = false;

    // [START google sign in]
    private com.google.android.gms.common.SignInButton mGoogleSignInButton;
    private LinearLayout mGoogleSignOutDisconnectLayout;
    private Button mGoogleSignOutButton;
    private Button mGoogleDisconnectButton;

    private static final int RC_SIGN_IN = 9001;

    private GoogleApiClient mGoogleApiClient;
//    private TextView mStatusTextView;
    private ProgressDialog mProgressDialog;

    private Uri mAuthenticatedUserPhotoUrl;

    // [END google sign in]

    // [START facebook sign in]
    private LoginButton mFacebookLoginButton;

    CallbackManager callbackManager;
    // [END facebook sign in]

    DigitsAuthButton mDigitsAuthButton;

    // store which authtype was selected by user
    private int mRequestedAuthType = 0;

    public static void startActionCreateNewSession(Context context) {
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private class ProfileUserFetchResponse {
        public final static int STATUS_USER_FOUND = 1;
        public final static int STATUS_USER_NOT_FOUND = 2;
        public final static int STATUS_USER_ADDED = 3;
        public final static int STATUS_USER_NOT_ADDED = 4;
        public final static int STATUS_INVALID_AUTH_TYPE = -2;
        public final static int STATUS_RETROFIT_ERROR = -1;

        private int status = 0;
        private Response response;
        private User user;

        public ProfileUserFetchResponse(Response response, User user, int status) {
            this.response = response;
            this.user = user;
            this.status = status;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public Response getResponse() {
            return response;
        }

        public void setResponse(Response response) {
            this.response = response;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mSelfieApplication = (SelfieApplication) getApplication();
        mSignedInLayout = (LinearLayout) findViewById(R.id.signed_in_layout);
        mFullNameView = (TextView) findViewById(R.id.full_name);
        mUserNameView = (TextView) findViewById(R.id.user_name);
        mCloseButton = (Button) findViewById(R.id.close_button);
        mExitButton = (Button) findViewById(R.id.exit_button);
        mCloseButton.setOnClickListener(this);
        mExitButton.setOnClickListener(this);

        // [START google sign in]
        // Views
        mGoogleSignInButton = (SignInButton) findViewById(R.id.sign_in_button);
        mGoogleSignOutButton = (Button) findViewById(R.id.sign_out_button);
        mGoogleDisconnectButton = (Button) findViewById(R.id.disconnect_button);
        mGoogleSignOutDisconnectLayout = (LinearLayout) findViewById(R.id.sign_out_and_disconnect);
//        mStatusTextView = (TextView) findViewById(R.id.status);

        // Button listeners
        mGoogleSignInButton.setOnClickListener(this);
        mGoogleSignOutButton.setOnClickListener(this);
        mGoogleDisconnectButton.setOnClickListener(this);

        // [START configure_signin]
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build();
        // [END configure_signin]

        // [START build_client]
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // [END build_client]

        // [START customize_button]
        // Customize sign-in button. The sign-in button can be displayed in
        // multiple sizes and color schemes. It can also be contextually
        // rendered based on the requested scopes. For example. a red button may
        // be displayed when Google+ scopes are requested, but a white button
        // may be displayed when only basic profile is requested. Try adding the
        // Scopes.PLUS_LOGIN scope to the GoogleSignInOptions to see the
        // difference.
        mGoogleSignInButton.setSize(SignInButton.SIZE_STANDARD);
        mGoogleSignInButton.setScopes(gso.getScopeArray());
        // [END customize_button]
        // [END google sign in]


        // [START facebook sign in]
        callbackManager = CallbackManager.Factory.create();
        mFacebookLoginButton = (LoginButton) findViewById(R.id.login_button);
        mFacebookLoginButton.setReadPermissions("email,public_profile");

        mFacebookLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //TODO: update signed in user
                // get the user who has just been authenticated from the profile server
                // if user does not exist, create one otherwise set the authenticated user as signed in user

                Toast.makeText(LoginActivity.this, "Login successful: " + loginResult.getAccessToken() + " perms: " + loginResult.getRecentlyGrantedPermissions().toString(),
                        Toast.LENGTH_LONG).show();
            }

            @Override
            public void onCancel() {
                Toast.makeText(LoginActivity.this, "Login canceled", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(LoginActivity.this, "Login error", Toast.LENGTH_SHORT).show();
            }
        });
        // [END facebook sign in]

        // [START digits sign in]
        mDigitsAuthButton = (DigitsAuthButton) findViewById(R.id.auth_button);
        mDigitsAuthButton.setCallback(new AuthCallback() {
            @Override
            public void success(DigitsSession session, String phoneNumber) {
                //TODO: update signed in user
                // get the user who has just been authenticated from the profile server
                // if user does not exist, create one otherwise set the authenticated user as signed in user

                // TODO: associate the session userID with your user model
                Toast.makeText(getApplicationContext(), "Authentication successful for "
                        + phoneNumber, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(DigitsException exception) {
                Log.d("Digits", "Sign in with Digits failure", exception);
            }
        });
        // [END digits sign in]

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (mRequestedAuthType) {
            case Constants.AUTH_TYPE_GOOGLE_PLUS: {
                if (requestCode == RC_SIGN_IN) {
                    GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                    handleSignInResult(result);
                }
            }
            break;
            default: {
                // [START facebook sign in]
                callbackManager.onActivityResult(requestCode, resultCode, data);
                // [END facebook sign in]
            }
            break;
        }
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
    }

    @Override
    public void onStart() {
        super.onStart();

        // start google sign in sequence if we are not signed in
        if (!((mSelfieApplication.isSignedIn()) && null != mSelfieApplication.getUser())) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                showProgressDialog();
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        hideProgressDialog();
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }


    private void fetchUserFromProfileServer(User authenticatedUser) {
        new FetchUserFromProfileServer(this).execute(authenticatedUser);

    }

    private String generateUserMapKey(User user) {
        String userMap = null;
        switch (user.getAuthType()) {
            case Constants.AUTH_TYPE_GOOGLE_PLUS: {
                userMap = (!Strings.isNullOrEmpty(user.getEmail()) ? Constants.ID_PREFIX_GOOGLE_USER_MAP + user.getEmail() : null );
            }
            break;
            case Constants.AUTH_TYPE_FACEBOOK: {
                userMap = (!Strings.isNullOrEmpty(user.getEmail()) ? Constants.ID_PREFIX_FACEBOOK_USER_MAP + user.getEmail() : null );
            }
            break;
            case Constants.AUTH_TYPE_TWITTER_DIGITS: {
                userMap = (!Strings.isNullOrEmpty(user.getTelephone()) ? Constants.ID_PREFIX_TWITTER_DIGITS_USER_MAP + user.getTelephone() : null );
            }
            break;
            case Constants.AUTH_TYPE_BASIC: {
                userMap = (!Strings.isNullOrEmpty(user.getUsername()) ? Constants.ID_PREFIX_BASIC_USER_MAP + user.getUsername() : null );
            }
            break;
        }
        return userMap;
    }

    private class FetchUserFromProfileServer extends AsyncTask<User,Void,ProfileUserFetchResponse> {

        final WeakReference<Activity> weakActivity;
        private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        private FetchUserFromProfileServer(Activity weakActivity) {
            this.weakActivity = new WeakReference<>(weakActivity);
        }

        @Override
        protected void onPreExecute() {
            Activity activity = weakActivity.get();
            if ((activity != null) && (!activity.isFinishing())) {
                lockScreenOrientation();
                this.dialog.setMessage("Checking user registration...");
                this.dialog.show();
            }
        }

        @Override
        protected ProfileUserFetchResponse doInBackground(User... users) {
            User authenticatedUser = users[0];
            String userMap = generateUserMapKey(authenticatedUser);

            if (Strings.isNullOrEmpty(userMap)) {
                return new ProfileUserFetchResponse(null, null, ProfileUserFetchResponse.STATUS_INVALID_AUTH_TYPE);
            }

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(Config.PROFILE_SERVER)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setClient(new TimedOutUrlConnectionClient())
                    .build();

            UserServiceApi userService = restAdapter.create(UserServiceApi.class);

            // check if user is registered on profile server
            User user;
            try {
                user = userService.getMapUser(userMap);
            }  catch (RetrofitError e) {
                e.printStackTrace();
                return new ProfileUserFetchResponse(e.getResponse(), null, ProfileUserFetchResponse.STATUS_RETROFIT_ERROR);
            }

            if (user == null) {
                return new ProfileUserFetchResponse(null, authenticatedUser, ProfileUserFetchResponse.STATUS_USER_NOT_FOUND);
            } else {
                return new ProfileUserFetchResponse(null, user, ProfileUserFetchResponse.STATUS_USER_FOUND);
            }
        }

        @Override
        protected void onPostExecute(ProfileUserFetchResponse profileUserFetchResponse) {
            Activity activity = weakActivity.get();
            if ((activity != null) && (!activity.isFinishing())) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                unlockScreenOrientation();
                handleProfileUserFetchResponse(profileUserFetchResponse);
            }
        }
    }

    private void handleProfileUserFetchResponse(ProfileUserFetchResponse profileUserFetchResponse) {
        if (profileUserFetchResponse.getStatus() == ProfileUserFetchResponse.STATUS_USER_NOT_ADDED) {
            Toast.makeText(
                    getApplicationContext(),
                    "User registration failed. Please try again.",
                    Toast.LENGTH_LONG
            ).show();
        } else if (profileUserFetchResponse.getStatus() == ProfileUserFetchResponse.STATUS_USER_ADDED) {
            Toast.makeText(
                    getApplicationContext(),
                    "User registered successfully. Completing sign in.",
                    Toast.LENGTH_LONG
            ).show();
            mSelfieApplication.setUser(profileUserFetchResponse.getUser());
            mSelfieApplication.setSignedIn(true);
            updateUI();
        } else if (profileUserFetchResponse.getStatus() == ProfileUserFetchResponse.STATUS_USER_NOT_FOUND) {
            Toast.makeText(
                    getApplicationContext(),
                    "You have not registered on this service. Registering now.",
                    Toast.LENGTH_LONG
            ).show();
            registerUserOnProfileServer(profileUserFetchResponse.getUser());
        } else if ((profileUserFetchResponse.getStatus() == ProfileUserFetchResponse.STATUS_INVALID_AUTH_TYPE)
                && (null != profileUserFetchResponse.getResponse())) {
            Toast.makeText(
                    getApplicationContext(),
                    "User registration failed. Authentication type problem.",
                    Toast.LENGTH_LONG
            ).show();
        } else if ((profileUserFetchResponse.getStatus() == ProfileUserFetchResponse.STATUS_RETROFIT_ERROR)
                && (null != profileUserFetchResponse.getResponse())) {
            Toast.makeText(
                    getApplicationContext(),
                    "User registration failed. More details in Alert.",
                    Toast.LENGTH_LONG
            ).show();
            handleRetrofitErrorResponse(profileUserFetchResponse.getResponse());
        } else if ((profileUserFetchResponse.getStatus() == ProfileUserFetchResponse.STATUS_RETROFIT_ERROR)) {
            Toast.makeText(
                    getApplicationContext(),
                    "User registration failed. More details in Alert.",
                    Toast.LENGTH_LONG
            ).show();
            new MaterialDialog.Builder(this)
                    .content(R.string.http_generic)
                    .positiveText(android.R.string.ok)
                    .show();
        } else if (profileUserFetchResponse.getStatus() == ProfileUserFetchResponse.STATUS_USER_FOUND) {
            Toast.makeText(
                    getApplicationContext(),
                    "User registration found. Completing sign in.",
                    Toast.LENGTH_LONG
            ).show();
            mSelfieApplication.setUser(profileUserFetchResponse.getUser());
            mSelfieApplication.setSignedIn(true);
            updateUI();
        }
    }

    private void registerUserOnProfileServer(User authenticatedUser) {
        new RegisterUserOnProfileServer(this).execute(authenticatedUser);
    }

    private void lockScreenOrientation() {
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
    }

    private void unlockScreenOrientation() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    private class RegisterUserOnProfileServer extends AsyncTask<User,Void,ProfileUserFetchResponse> {

        final WeakReference<Activity> weakActivity;
        private ProgressDialog dialog = new ProgressDialog(LoginActivity.this);

        private RegisterUserOnProfileServer(Activity weakActivity) {
            this.weakActivity = new WeakReference<>(weakActivity);
        }

        @Override
        protected void onPreExecute() {
            Activity activity = weakActivity.get();
            if ((activity != null) && (!activity.isFinishing())) {
                lockScreenOrientation();
                this.dialog.setMessage("Registering user...");
                this.dialog.show();
            }
        }

        @Override
        protected ProfileUserFetchResponse doInBackground(User... users) {
            User authenticatedUser = users[0];
            String userMap = generateUserMapKey(authenticatedUser);

            if (Strings.isNullOrEmpty(userMap)) {
                return new ProfileUserFetchResponse(null, null, ProfileUserFetchResponse.STATUS_INVALID_AUTH_TYPE);
            }

            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint(Config.PROFILE_SERVER)
                    .setLogLevel(RestAdapter.LogLevel.FULL)
                    .setClient(new TimedOutUrlConnectionClient())
                    .build();

            UserServiceApi userService = restAdapter.create(UserServiceApi.class);

            // check if user is registered on profile server
            User addedUser;
            try {
                addedUser = userService.addUser(authenticatedUser);
            }  catch (RetrofitError e) {
                e.printStackTrace();
                return new ProfileUserFetchResponse(e.getResponse(), null, ProfileUserFetchResponse.STATUS_RETROFIT_ERROR);
            }

            if (addedUser == null) {
                return new ProfileUserFetchResponse(null, authenticatedUser, ProfileUserFetchResponse.STATUS_USER_NOT_ADDED);
            } else {
                return new ProfileUserFetchResponse(null, addedUser, ProfileUserFetchResponse.STATUS_USER_ADDED);
            }
        }

        @Override
        protected void onPostExecute(ProfileUserFetchResponse profileUserFetchResponse) {
            Activity activity = weakActivity.get();
            if ((activity != null) && (!activity.isFinishing())) {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
                unlockScreenOrientation();
                handleProfileUserFetchResponse(profileUserFetchResponse);
            }
        }
    }

    private void handleRetrofitErrorResponse(Response response) {
        switch (response.getStatus()) {
            case HttpURLConnection.HTTP_BAD_REQUEST: {
                new MaterialDialog.Builder(this)
                        .content(R.string.http_bad_request)
                        .positiveText(android.R.string.ok)
                        .show();
            }
            break;
            case HttpURLConnection.HTTP_INTERNAL_ERROR: {
                new MaterialDialog.Builder(this)
                        .content(R.string.http_internal_error)
                        .positiveText(android.R.string.ok)
                        .show();
            }
            break;
            case HttpURLConnection.HTTP_NOT_FOUND: {
                new MaterialDialog.Builder(this)
                        .content(R.string.http_not_found)
                        .positiveText(android.R.string.ok)
                        .show();
            }
            break;
            case HttpURLConnection.HTTP_CONFLICT: {
                new MaterialDialog.Builder(this)
                        .content(R.string.http_conflict)
                        .positiveText(android.R.string.ok)
                        .show();
            }
            break;
            default: {
                new MaterialDialog.Builder(this)
                        .content(R.string.http_generic)
                        .positiveText(android.R.string.ok)
                        .show();
            }
            break;
        }
    }

    // [START handleSignInResult]
    private void handleSignInResult(GoogleSignInResult result) {
        Log.d(TAG, "handleSignInResult:" + result.isSuccess());
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
//            mStatusTextView.setText(getString(R.string.signed_in_fmt, acct.getDisplayName()));

            //TODO: update signed in user
            // get the user who has just been authenticated from the profile server
            // if user does not exist, create one otherwise set the authenticated user as signed in user

            mAuthenticatedUserPhotoUrl = acct.getPhotoUrl();

            User authenticatedUser = new UserBuilder()
                    .setFullname(acct.getDisplayName())
                    .setEmail(acct.getEmail())
                    .setUsername(acct.getId())
                    .setAuthType(Constants.AUTH_TYPE_GOOGLE_PLUS)
                    .setAuthToken(acct.getIdToken())
                    .setGoogleUserPhotoUrl((null != mAuthenticatedUserPhotoUrl ? mAuthenticatedUserPhotoUrl.toString() : null))
                    .createUser();

            fetchUserFromProfileServer(authenticatedUser);
        } else {
            //TODO: consider whether to update the application state to signed out. should we keep the old session or not?
            // Signed out, show unauthenticated UI.
            if (mSignInAttempted) {
                new MaterialDialog.Builder(this)
                        .content(R.string.google_sign_failed)
                        .positiveText(android.R.string.ok)
                        .show();
            }
//            updateUI();
        }
    }
    // [END handleSignInResult]

    // [START signIn]
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signIn]

    // [START signOut]
    private void signOut() {
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mSelfieApplication.setSignedIn(false);
                        mSelfieApplication.setUser(null);
                        // [START_EXCLUDE]
                        updateUI();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END signOut]

    // [START revokeAccess]
    private void revokeAccess() {
        Auth.GoogleSignInApi.revokeAccess(mGoogleApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        mSelfieApplication.setSignedIn(false);
                        mSelfieApplication.setUser(null);
                        // [START_EXCLUDE]
                        updateUI();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END revokeAccess]

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.hide();
        }
    }

    private void updateUI() {
        User user = mSelfieApplication.getUser();
        if ((mSelfieApplication.isSignedIn()) && null != user) {
            int authType = user.getAuthType();
            switch (authType) {
                case Constants.AUTH_TYPE_GOOGLE_PLUS: {
                    if (!Strings.isNullOrEmpty(user.getFullname())) {
                        mFullNameView.setText(user.getFullname());
                    }
                    if (!Strings.isNullOrEmpty(user.getEmail())) {
                        mUserNameView.setText(user.getEmail());
                    }
                    // google views update
                    mGoogleSignInButton.setVisibility(View.GONE);
                    mGoogleSignOutDisconnectLayout.setVisibility(View.VISIBLE);

                    // facebook views update
                    mFacebookLoginButton.setVisibility(View.GONE);

                    // digits views update
                    mDigitsAuthButton.setVisibility(View.GONE);
                }
                break;
                case Constants.AUTH_TYPE_FACEBOOK: {
                    if (!Strings.isNullOrEmpty(user.getFullname())) {
                        mFullNameView.setText(user.getFullname());
                    }
                    if (!Strings.isNullOrEmpty(user.getEmail())) {
                        mUserNameView.setText(user.getEmail());
                    }
                    // google views update
                    mGoogleSignInButton.setVisibility(View.GONE);
                    mGoogleSignOutDisconnectLayout.setVisibility(View.GONE);

                    // facebook views update
                    mFacebookLoginButton.setVisibility(View.VISIBLE);

                    // digits views update
                    mDigitsAuthButton.setVisibility(View.GONE);
                }
                break;
                case Constants.AUTH_TYPE_TWITTER_DIGITS: {
                    if (!Strings.isNullOrEmpty(user.getFullname())) {
                        mFullNameView.setText(user.getFullname());
                    }
                    if (!Strings.isNullOrEmpty(user.getTelephone())) {
                        mUserNameView.setText(user.getTelephone());
                    }
                    // google views update
                    mGoogleSignInButton.setVisibility(View.GONE);
                    mGoogleSignOutDisconnectLayout.setVisibility(View.GONE);

                    // facebook views update
                    mFacebookLoginButton.setVisibility(View.GONE);

                    // digits views update
                    mDigitsAuthButton.setVisibility(View.VISIBLE);
                }
                break;
            }
            mSignedInLayout.setVisibility(View.VISIBLE);
        } else {
            mSignedInLayout.setVisibility(View.GONE);

            // google views update
//            mStatusTextView.setText(R.string.signed_out);
            mGoogleSignInButton.setVisibility(View.VISIBLE);
            mGoogleSignOutDisconnectLayout.setVisibility(View.GONE);

            // facebook views update
            mFacebookLoginButton.setVisibility(View.VISIBLE);

            // digits views update
            mDigitsAuthButton.setVisibility(View.VISIBLE);
        }
        mFacebookLoginButton.setVisibility(View.GONE);
        mDigitsAuthButton.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                mRequestedAuthType = Constants.AUTH_TYPE_GOOGLE_PLUS;
                mSignInAttempted = true;
                signIn();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
            case R.id.exit_button:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra(MainActivity.EXTRA_EXIT_APPLICATION, true);
                startActivity(intent);
                finish();
                break;
            case R.id.close_button:
                finish();
                break;
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mSignInAttempted = false;
    }
}
