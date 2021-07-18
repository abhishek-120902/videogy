/*
 * Copyright (c) 2021.  Alpha Coders
 */

package com.abhishek.Videogy.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.abhishek.Videogy.R;
import com.abhishek.Videogy.VDApp;
import com.abhishek.Videogy.activity.MainActivity;
import com.abhishek.Videogy.history.HistorySQLite;
import com.abhishek.Videogy.history.VisitedPage;
import com.abhishek.Videogy.model.VDFragment;
import com.abhishek.Videogy.utils.Utils;
import com.abhishek.Videogy.view.CustomMediaController;
import com.abhishek.Videogy.view.CustomVideoView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Arrays;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

public class BrowserWindow extends VDFragment implements View.OnClickListener, MainActivity.OnBackPressedListener {

    private final Activity activity;
    private String url;
    private View view;
    private TouchableWebView page;
    private SSLSocketFactory defaultSSLSF;
    private FrameLayout videoFoundTV;
    private CustomVideoView videoFoundView;
    private FloatingActionButton videosFoundHUD;
    private View foundVideosWindow;
    private VideoList videoList;
    private ImageView foundVideosClose;
    private ProgressBar loadingPageProgress;
    private int orientation;
    private boolean loadedFirsTime;
    private List<String> blockedWebsites;
    private BottomSheetDialog dialog;
    private InterstitialAd mInterstitialAd;

    public BrowserWindow(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onClick(View v) {
        if (v == videosFoundHUD) {
            if (videoList.getSize() > 0) {
                dialog.show();
            } else {
                showGuide();
            }
        } else if (v == foundVideosClose) {
            dialog.dismiss();
        }
    }

    private void showGuide() {
        final Dialog guide = new Dialog(getContext());
        guide.setContentView(R.layout.dialog_guide);

        Button button = guide.findViewById(R.id.btn_ok);
        button.setOnClickListener(view -> guide.dismiss());

        guide.show();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle data = getArguments();
        url = data.getString("url");
        defaultSSLSF = HttpsURLConnection.getDefaultSSLSocketFactory();
        blockedWebsites = Arrays.asList(getResources().getStringArray(R.array.blocked_sites));
        setRetainInstance(true);
        AdRequest adRequest = new AdRequest.Builder().build();


        InterstitialAd.load(getContext(), getResources().getString(R.string.intersititial), adRequest, new InterstitialAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd;
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mInterstitialAd = null;
            }
        });
    }

    private void createVideosFoundTV() {
        videoFoundTV = view.findViewById(R.id.videoFoundTV);
        videoFoundView = view.findViewById(R.id.videoFoundView);
        CustomMediaController mediaFoundController = view.findViewById(R.id.mediaFoundController);
        mediaFoundController.setFullscreenEnabled();
        videoFoundView.setMediaController(mediaFoundController);
        videoFoundTV.setVisibility(View.GONE);
    }

    private void createVideosFoundHUD() {
        videosFoundHUD = view.findViewById(R.id.videosFoundHUD);
        videosFoundHUD.setOnClickListener(this);
    }

    private void createFoundVideosWindow() {

        dialog = new BottomSheetDialog(activity);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.video_qualities_dialog);

        RecyclerView qualities = dialog.findViewById(R.id.qualities_rv);
        ImageView dismiss = dialog.findViewById(R.id.dismiss);

        assert dismiss != null;
        dismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        foundVideosWindow = view.findViewById(R.id.foundVideosWindow);
        if (videoList != null) {
            videoList.recreateVideoList(qualities);
        } else {
            videoList = new VideoList(activity, qualities) {
                @Override
                void onItemDeleted() {
                    dialog.dismiss();
                    if (mInterstitialAd != null) {
                        mInterstitialAd.show(getVDActivity());
                    }
                    updateFoundVideosBar();
                }

                @Override
                void onVideoPlayed(String url) {
                    dialog.dismiss();
                    updateVideoPlayer(url);
                }
            };
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (view == null || getResources().getConfiguration().orientation != orientation) {
            int visibility = View.VISIBLE;
            if (view != null) {
                visibility = view.getVisibility();
            }
            view = inflater.inflate(R.layout.browser, container, false);
            view.setVisibility(visibility);
            if (page == null) {
                page = view.findViewById(R.id.page);
            } else {
                View page1 = view.findViewById(R.id.page);
                ((ViewGroup) view).removeView(page1);
                ((ViewGroup) page.getParent()).removeView(page);
                ((ViewGroup) view).addView(page);
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.videosFoundHUD));
                ((ViewGroup) view).bringChildToFront(view.findViewById(R.id.foundVideosWindow));
            }
            loadingPageProgress = view.findViewById(R.id.loadingPageProgress);
            loadingPageProgress.setVisibility(View.GONE);

            createVideosFoundHUD();
            createVideosFoundTV();
            createFoundVideosWindow();
            updateFoundVideosBar();
        }

        return view;
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        if (!loadedFirsTime) {
            page.getSettings().setJavaScriptEnabled(true);
            page.getSettings().setDomStorageEnabled(true);
            page.getSettings().setAllowUniversalAccessFromFileURLs(true);
            page.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            page.setWebViewClient(new WebViewClient() {//it seems not setting webclient, launches
                //default browser instead of opening the page in webview
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {

//                    if (blockedWebsites.contains(Utils.getBaseDomain(request.getUrl().toString()))) {
//                        Log.d("vdd", "URL : " + request.getUrl().toString());
//                        new AlertDialog.Builder(activity)
//                                .setMessage("Youtube is not supported according to google policy.")
//                                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
//                                    @Override
//                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
//                                    }
//                                })
//                                .create()
//                                .show();
//                        return true;
//                    }

                    return super.shouldOverrideUrlLoading(view, request);
                }

                @Override
                public void onPageStarted(final WebView webview, final String url, Bitmap favicon) {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            EditText urlBox = getVDActivity().findViewById(R.id.et_search_bar);
                            urlBox.setText(url);
                            urlBox.setSelection(urlBox.getText().length());
                            BrowserWindow.this.url = url;
                        }
                    });
                    view.findViewById(R.id.loadingProgress).setVisibility(View.GONE);
                    loadingPageProgress.setVisibility(View.VISIBLE);
                    super.onPageStarted(webview, url, favicon);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    loadingPageProgress.setVisibility(View.GONE);
                }

                @Override
                public void onLoadResource(final WebView view, final String url) {
                    Log.d("fb :", "URL: " + url);
                    final String viewUrl = view.getUrl();
                    final String title = view.getTitle();

                    new VideoContentSearch(activity, url, viewUrl, title) {
                        @Override
                        public void onStartInspectingURL() {
                            Utils.disableSSLCertificateChecking();
                        }

                        @Override
                        public void onFinishedInspectingURL(boolean finishedAll) {
                            HttpsURLConnection.setDefaultSSLSocketFactory(defaultSSLSF);
                        }

                        @Override
                        public void onVideoFound(String size, String type, String link, String name, String page, boolean chunked, String website, boolean audio) {
                            videoList.addItem(size, type, link, name, page, chunked, website, audio);
                            updateFoundVideosBar();
                        }
                    }.start();
                }

                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                    if (activity != null) {
                        Log.d("VDDebug", "Url: " + url);
                        if (activity.getSharedPreferences("settings", 0).getBoolean(getString(R
                                .string.adBlockON), true)
                                && (url.contains("ad") || url.contains("banner") || url.contains("pop"))
                                && getVDActivity().getBrowserManager().checkUrlIfAds(url)) {
                            Log.d("VDDebug", "Ads detected: " + url);
                            return new WebResourceResponse(null, null, null);
                        }
                    }
                    return super.shouldInterceptRequest(view, url);
                }

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && getVDActivity() !=
                            null) {
                        if (VDApp.getInstance().getSharedPreferences("settings", 0).getBoolean(getString
                                (R.string.adBlockON), true)
                                && (request.getUrl().toString().contains("ad") ||
                                request.getUrl().toString().contains("banner") ||
                                request.getUrl().toString().contains("pop"))
                                && getVDActivity().getBrowserManager().checkUrlIfAds(request.getUrl()
                                .toString())) {
                            Log.i("VDInfo", "Ads detected: " + request.getUrl().toString());
                            return new WebResourceResponse(null, null, null);
                        } else return null;
                    } else {
                        return shouldInterceptRequest(view, request.getUrl().toString());
                    }
                }


            });
            page.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onProgressChanged(WebView view, int newProgress) {
                    loadingPageProgress.setProgress(newProgress);
                }

                @Override
                public void onReceivedTitle(WebView view, String title) {
                    super.onReceivedTitle(view, title);
                    videoList.deleteAllItems();
                    updateFoundVideosBar();
                    VisitedPage vp = new VisitedPage();
                    vp.title = title;
                    vp.link = view.getUrl();
                    HistorySQLite db = new HistorySQLite(activity);
                    db.addPageToHistory(vp);
                    db.close();
                }

                @Override
                public Bitmap getDefaultVideoPoster() {
                    return Bitmap.createBitmap(50, 50, Bitmap.Config.ARGB_8888);
                }


            });

            page.loadUrl(url);
            loadedFirsTime = true;
        } else {
            EditText urlBox = getVDActivity().findViewById(R.id.et_search_bar);
            urlBox.setText(url);
            urlBox.setSelection(urlBox.getText().length());
        }
    }

    @Override
    public void onDestroy() {
        page.stopLoading();
        page.destroy();
        super.onDestroy();
    }

    private void updateFoundVideosBar() {
        if (videoList.getSize() > 0) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videosFoundHUD.setBackgroundTintList(getResources().getColorStateList(R.color.colorAccent));
                    Animation expandIn = AnimationUtils.loadAnimation(activity.getApplicationContext(), R.anim.expand_in);
                    videosFoundHUD.startAnimation(expandIn);
                }
            });

        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    videosFoundHUD.setBackgroundTintList(getResources().getColorStateList(R.color.dark_gray));
                    if (foundVideosWindow.getVisibility() == View.VISIBLE)
                        foundVideosWindow.setVisibility(View.GONE);
                }
            });
        }
    }

    private void updateVideoPlayer(String url) {
        videoFoundTV.setVisibility(View.VISIBLE);
        Uri uri = Uri.parse(url);
        videoFoundView.setVideoURI(uri);
        videoFoundView.start();
    }

    @Override
    public void onBackpressed() {
        if (foundVideosWindow.getVisibility() == View.VISIBLE && !videoFoundView.isPlaying() && videoFoundTV.getVisibility() == View.GONE) {
            foundVideosWindow.setVisibility(View.GONE);
        } else if (videoFoundView.isPlaying() || videoFoundTV.getVisibility() == View.VISIBLE) {
            videoFoundView.closePlayer();
            videoFoundTV.setVisibility(View.GONE);
        } else if (page.canGoBack()) {
            page.goBack();
        } else {
            getVDActivity().getBrowserManager().closeWindow(BrowserWindow.this);
        }
    }

    public String getUrl() {
        return url;
    }

    @Override
    public void onPause() {
        super.onPause();
        page.onPause();
        Log.d("debug", "onPause: ");
    }

    @Override
    public void onResume() {
        super.onResume();
        page.onResume();
        Log.d("debug", "onResume: ");
    }

}
