package com.rahul.figcanvas;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.DriveScopes;
import com.google.gson.annotations.SerializedName;

import java.io.File;
import java.util.Collections;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * Created by rspl-rahul on 18/3/20.
 */
public class UploadWorkManager extends Worker {
    private DriveServiceHelper mDriveServiceHelper;

    public UploadWorkManager(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        String imagePath = getInputData().getString("imagePath");
        showNotification("FIGCANVAS", imagePath + " Image uploaded successfully");
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());
        if (account == null) {
            signIn();
        }//has to write logic here for upload image
        File file = new File(imagePath);
        mDriveServiceHelper.uploadFile(file, "image/jpeg", "Test");
        return Result.success();
        //Defining retrofit api service/* Retrofit retrofit = new Retrofit.Builder()
        //                .baseUrl("Base Url")
        //                .addConverterFactory(GsonConverterFactory.create())
        //                .build();
        //
        //        File file = new File(imagePath);
        //        RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), file);
        //        MultipartBody.Part fileupload = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        //        RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());
        //        ApiService service = retrofit.create(ApiService.class);
        //        Call<PostResponse> call = service.postData(fileupload, filename);
        //        //calling the api
        //        call.enqueue(new Callback<PostResponse>() {
        //            @Override
        //            public void onResponse(Call<PostResponse> call, Response<PostResponse> response) {
        //                if(response.isSuccessful()){
        //                   // showNotification("FIGCANVAS", "Image uploaded successfully");
        //                }
        //            }
        //
        //            @Override
        //            public void onFailure(Call<PostResponse> call, Throwable t) {
        //                Result.failure();
        //            }
        //        });
        //        return Result.success();*/

    }

    private void showNotification(String title, String task) {
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel("FIGCANVAS", "FIGCANVAS", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), "FIGCANVAS")
                .setContentTitle(title)
                .setContentText(task)
                .setSmallIcon(R.mipmap.ic_launcher);
        notificationManager.notify(1, notification.build());
    }

    private void signIn() {

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getApplicationContext());

        GoogleAccountCredential credential =
                GoogleAccountCredential.usingOAuth2(
                        getApplicationContext(), Collections.singleton(DriveScopes.DRIVE_FILE));
        credential.setSelectedAccount(account.getAccount());
        com.google.api.services.drive.Drive googleDriveService =
                new com.google.api.services.drive.Drive.Builder(
                        AndroidHttp.newCompatibleTransport(),
                        new GsonFactory(),
                        credential)
                        .setApplicationName("com.rahul.figcanvas")
                        .build();
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);

    }
   /* @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }
    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
         //   updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("TAG", "signInResult:failed code=" + e.getStatusCode());
           // updateUI(null);
        }
    }*/


    private interface ApiService {
        @Multipart
        @POST("index.php")
        Call<PostResponse> postData(
                @Part MultipartBody.Part file,
                @Part("name") RequestBody name);
    }

    private class PostResponse {
        @SerializedName("success")
        private String success;

        public String getSuccess() {
            return success;
        }

        public void setSuccess(String success) {
            this.success = success;
        }
    }

}

