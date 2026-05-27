package id.cutiezy.agen;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.print.PrintAttributes;
import android.print.PrintManager;
import android.print.PrintDocumentAdapter;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.webkit.CookieManager;
import android.webkit.DownloadListener;
import android.webkit.JavascriptInterface;
import android.webkit.PermissionRequest;
import android.webkit.SslErrorHandler;
import android.webkit.ValueCallback;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceError;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import org.json.JSONObject;
import org.json.JSONArray;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
    private static final String LOGIN_URL = "https://cutiezy.id/index";
    private static final String FALLBACK_URL = "https://cutiezy.id/index";
    private static final String LOGO_URL = "https://cutiezy.id/library/assets/images/logo2.png";
    private static final int SPLASH_DELAY_MS = 1900;

    private static final int REQ_FILE_CHOOSER = 1001;
    private static final int REQ_CONTACT_PICKER = 1002;
    private static final int REQ_CAMERA_PERMISSION = 2001;
    private static final int REQ_BLUETOOTH_PERMISSION = 2002;
    private static final int REQ_WEBRTC_CAMERA_PERMISSION = 2003;
    private static final int NAV_FORWARD = 1;
    private static final int NAV_BACK = -1;

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private WebView webView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBar;
    private FrameLayout mainRoot;
    private View loadingOverlay;
    private TextView loadingText;
    private boolean redirectingToFallback = false;
    private ValueCallback<Uri[]> filePathCallback;
    private WebChromeClient.FileChooserParams pendingFileChooserParams;
    private Uri cameraImageUri;
    private String contactRequestId;
    private String pendingBluetoothPrintText;
    private PermissionRequest pendingWebPermissionRequest;
    private int currentNavigationDirection = NAV_FORWARD;
    private int mainFrameErrorCount = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupSystemBars();
        showSplash();
        handler.postDelayed(this::showWebView, SPLASH_DELAY_MS);
    }

    private void setupSystemBars() {
        Window window = getWindow();
        window.setStatusBarColor(Color.rgb(22, 63, 130));
        window.setNavigationBarColor(Color.rgb(22, 63, 130));
    }

    private void showSplash() {
        FrameLayout root = new FrameLayout(this);
        GradientDrawable background = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{Color.rgb(45, 97, 180), Color.rgb(22, 63, 130), Color.rgb(7, 30, 74)}
        );
        root.setBackground(background);

        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);
        box.setPadding(dp(28), dp(28), dp(28), dp(28));

        ImageView logo = new ImageView(this);
        GradientDrawable logoBg = new GradientDrawable();
        logoBg.setColor(Color.WHITE);
        logoBg.setShape(GradientDrawable.RECTANGLE);
        logoBg.setCornerRadius(dp(30));
        logo.setBackground(logoBg);
        logo.setPadding(dp(14), dp(14), dp(14), dp(14));
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        logo.setImageResource(getResources().getIdentifier("ic_launcher_foreground", "drawable", getPackageName()));
        LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(132), dp(132));
        logoParams.bottomMargin = dp(22);
        box.addView(logo, logoParams);

        TextView title = new TextView(this);
        title.setText("Cutiezy Agen");
        title.setTextColor(Color.WHITE);
        title.setTextSize(30);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        box.addView(title, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView subtitle = new TextView(this);
        subtitle.setText("Server PPOB dan Marketplace Terkini");
        subtitle.setTextColor(Color.argb(230, 255, 255, 255));
        subtitle.setTextSize(14);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams subParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(8);
        box.addView(subtitle, subParams);

        TextView loading = new TextView(this);
        loading.setText("Membuka aplikasi...");
        loading.setTextColor(Color.argb(210, 255, 255, 255));
        loading.setTextSize(13);
        loading.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams loadParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        loadParams.topMargin = dp(28);
        box.addView(loading, loadParams);

        root.addView(box, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        animateSplash(logo, title, subtitle, loading);
        loadLogoFromUrl(logo);
    }

    private void animateSplash(View... views) {
        int delay = 0;
        for (View view : views) {
            AnimationSet set = new AnimationSet(true);
            AlphaAnimation alpha = new AlphaAnimation(0f, 1f);
            TranslateAnimation translate = new TranslateAnimation(0, 0, dp(20), 0);
            ScaleAnimation scale = new ScaleAnimation(0.94f, 1f, 0.94f, 1f,
                    AnimationSet.RELATIVE_TO_SELF, 0.5f, AnimationSet.RELATIVE_TO_SELF, 0.5f);
            set.addAnimation(alpha);
            set.addAnimation(translate);
            set.addAnimation(scale);
            set.setDuration(620);
            set.setStartOffset(delay);
            view.startAnimation(set);
            delay += 130;
        }
    }

    private void loadLogoFromUrl(final ImageView logo) {
        executor.execute(() -> {
            try {
                InputStream input = new URL(LOGO_URL).openStream();
                final Bitmap bitmap = BitmapFactory.decodeStream(input);
                handler.post(() -> {
                    if (bitmap != null) logo.setImageBitmap(bitmap);
                });
            } catch (Exception ignored) {
                // fallback tetap pakai ikon bawaan aplikasi
            }
        });
    }

    private void showWebView() {
        mainRoot = new FrameLayout(this);
        webView = new WebView(this);
        swipeRefreshLayout = new SwipeRefreshLayout(this);
        swipeRefreshLayout.setColorSchemeColors(Color.rgb(45, 97, 180), Color.rgb(22, 63, 130));
        swipeRefreshLayout.setProgressViewOffset(false, dp(18), dp(72));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            if (webView != null) {
                currentNavigationDirection = NAV_FORWARD;
                showSmoothLoader("Menyegarkan halaman...");
                webView.reload();
            }
        });
        swipeRefreshLayout.setOnChildScrollUpCallback((parent, child) -> webView != null && webView.getScrollY() > 0);
        progressBar = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progressBar.setMax(100);
        progressBar.setVisibility(View.GONE);

        setupWebView(webView);
        swipeRefreshLayout.addView(webView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mainRoot.addView(swipeRefreshLayout, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        FrameLayout.LayoutParams progressParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3));
        progressParams.gravity = Gravity.TOP;
        mainRoot.addView(progressBar, progressParams);

        loadingOverlay = createLoadingOverlay();
        mainRoot.addView(loadingOverlay, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(mainRoot);

        webView.setAlpha(0f);
        webView.animate().alpha(1f).setDuration(340).start();
        showSmoothLoader("Membuka Cutiezy...");
        webView.loadUrl(LOGIN_URL);
    }

    private View createLoadingOverlay() {
        FrameLayout overlay = new FrameLayout(this);
        overlay.setVisibility(View.GONE);
        overlay.setAlpha(0f);
        overlay.setClickable(false);
        overlay.setBackgroundColor(Color.argb(78, 0, 0, 0));

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(22), dp(18), dp(22), dp(18));
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.argb(235, 255, 255, 255));
        cardBg.setCornerRadius(dp(22));
        card.setBackground(cardBg);
        card.setElevation(dp(8));

        ProgressBar spinner = new ProgressBar(this);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(dp(34), dp(34));
        sp.bottomMargin = dp(10);
        card.addView(spinner, sp);

        loadingText = new TextView(this);
        loadingText.setText("Memuat halaman...");
        loadingText.setTextColor(Color.rgb(34, 57, 93));
        loadingText.setTextSize(13);
        loadingText.setTypeface(Typeface.DEFAULT_BOLD);
        loadingText.setGravity(Gravity.CENTER);
        card.addView(loadingText, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.gravity = Gravity.CENTER;
        overlay.addView(card, cardParams);
        return overlay;
    }

    private void showSmoothLoader(String message) {
        if (loadingOverlay == null || webView == null) return;
        handler.post(() -> {
            if (loadingText != null && message != null) loadingText.setText(message);
            loadingOverlay.animate().cancel();
            webView.animate().cancel();
            loadingOverlay.setVisibility(View.VISIBLE);
            loadingOverlay.animate().alpha(1f).setDuration(180).start();
            webView.animate().alpha(0.62f).setDuration(180).start();
        });
    }

    private void hideSmoothLoader() {
        if (loadingOverlay == null || webView == null) return;
        handler.postDelayed(() -> {
            if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
            loadingOverlay.animate().cancel();
            webView.animate().cancel();
            // Efek slide kiri/kanan dimatikan agar perpindahan halaman tidak terasa geser.
            // Loader tetap meredup-terang halus, dan pull-to-refresh tetap aktif.
            webView.setTranslationX(0f);
            webView.animate()
                    .alpha(1f)
                    .setDuration(220)
                    .start();
            loadingOverlay.animate()
                    .alpha(0f)
                    .setDuration(260)
                    .withEndAction(() -> loadingOverlay.setVisibility(View.GONE))
                    .start();
            currentNavigationDirection = NAV_FORWARD;
        }, 160);
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void setupWebView(WebView view) {
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        view.setOverScrollMode(View.OVER_SCROLL_IF_CONTENT_SCROLLS);
        view.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        view.setHapticFeedbackEnabled(true);
        view.setVerticalScrollBarEnabled(true);
        view.setHorizontalScrollBarEnabled(false);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);

        WebSettings settings = view.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setMediaPlaybackRequiresUserGesture(false);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSupportMultipleWindows(true);
        settings.setGeolocationEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
            CookieManager.getInstance().setAcceptThirdPartyCookies(view, true);
        }
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().flush();

        view.addJavascriptInterface(new CutiezyBridge(), "CutiezyNative");
        // Compatibility JS disuntikkan setelah halaman mulai/selesai dimuat.
        // Ini lebih aman untuk build cloud karena tidak bergantung pada API AndroidX WebKit baru.

        view.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return handleUrl(view, request.getUrl().toString());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return handleUrl(view, url);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                showSmoothLoader("Memuat halaman...");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                redirectingToFallback = false;
                mainFrameErrorCount = 0;
                injectNativeCompatibilityLayer();
                if (swipeRefreshLayout != null) swipeRefreshLayout.setRefreshing(false);
                hideSmoothLoader();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                // Jangan panggil super agar WebView tidak sempat menampilkan halaman bawaan
                // "Halaman web tidak tersedia". Semua error main-frame langsung diarahkan.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request != null && request.isForMainFrame()) {
                    handleMainFrameLoadError(view, request.getUrl() == null ? null : request.getUrl().toString(),
                            error == null ? 0 : error.getErrorCode(),
                            error == null ? "" : String.valueOf(error.getDescription()));
                }
            }

            @SuppressWarnings("deprecation")
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                // Fallback untuk Android lama.
                handleMainFrameLoadError(view, failingUrl, errorCode, description);
            }

            @Override
            public void onReceivedHttpError(WebView view, WebResourceRequest request, WebResourceResponse errorResponse) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && request != null && request.isForMainFrame()
                        && errorResponse != null && errorResponse.getStatusCode() >= 400) {
                    handleMainFrameLoadError(view, request.getUrl() == null ? null : request.getUrl().toString(),
                            errorResponse.getStatusCode(), "HTTP " + errorResponse.getStatusCode());
                }
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.cancel();
                redirectToFallback(view);
            }
        });

        view.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                progressBar.setVisibility(newProgress < 100 ? View.VISIBLE : View.GONE);
                progressBar.setProgress(newProgress);
            }

            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> callback,
                                             FileChooserParams fileChooserParams) {
                if (MainActivity.this.filePathCallback != null) {
                    MainActivity.this.filePathCallback.onReceiveValue(null);
                }
                MainActivity.this.filePathCallback = callback;
                pendingFileChooserParams = fileChooserParams;

                if (wantsCamera(fileChooserParams) && !hasPermission(Manifest.permission.CAMERA)) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_CAMERA_PERMISSION);
                        return true;
                    }
                }
                startFileChooser(fileChooserParams);
                return true;
            }

            @Override
            public void onPermissionRequest(PermissionRequest request) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    boolean needsCamera = false;
                    for (String resource : request.getResources()) {
                        if (PermissionRequest.RESOURCE_VIDEO_CAPTURE.equals(resource)) {
                            needsCamera = true;
                            break;
                        }
                    }
                    if (needsCamera && !hasPermission(Manifest.permission.CAMERA)) {
                        pendingWebPermissionRequest = request;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{Manifest.permission.CAMERA}, REQ_WEBRTC_CAMERA_PERMISSION);
                        } else {
                            request.deny();
                        }
                    } else {
                        request.grant(request.getResources());
                    }
                }
            }

            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, android.os.Message resultMsg) {
                WebView.HitTestResult result = view.getHitTestResult();
                if (result != null && result.getExtra() != null) {
                    return handleUrl(view, result.getExtra());
                }
                return false;
            }
        });

        view.setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
            if (url != null && (url.startsWith("http://") || url.startsWith("https://"))) {
                downloadHttpFile(url, userAgent, contentDisposition, mimetype);
            } else {
                toast("File sedang disiapkan. Coba tekan tombol unduh sekali lagi.");
            }
        });
    }

    private boolean handleUrl(WebView view, String url) {
        if (url == null || url.trim().isEmpty()) return false;
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase(Locale.US);
        String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase(Locale.US);

        if ("http".equals(scheme) || "https".equals(scheme)) {
            if (host.contains("wa.me") || host.contains("api.whatsapp.com") || host.contains("web.whatsapp.com")) {
                return openExternal(url);
            }
            currentNavigationDirection = NAV_FORWARD;
            return false;
        }

        if ("intent".equals(scheme)) {
            return openIntentScheme(url);
        }

        if ("my.bluetoothprint.scheme".equals(scheme)) {
            return handleBluetoothPrintScheme(url);
        }

        if ("tel".equals(scheme) || "mailto".equals(scheme) || "sms".equals(scheme)
                || "smsto".equals(scheme) || "geo".equals(scheme) || "market".equals(scheme)
                || "whatsapp".equals(scheme)) {
            return openExternal(url);
        }

        // Semua skema selain http/https dicegah masuk WebView agar tidak muncul halaman
        // "web page not available". Aplikasi akan coba membuka intent luar.
        return openExternal(url);
    }

    private void handleMainFrameLoadError(WebView view, String failingUrl, int errorCode, String description) {
        mainFrameErrorCount++;

        // ERR_TOO_MANY_REDIRECTS / ERROR_REDIRECT_LOOP sering muncul ketika URL login
        // dibuka saat user sebenarnya sudah punya sesi. Mulai dari /index lebih aman,
        // dan jika tetap error jangan tampilkan halaman error bawaan WebView.
        String lowerUrl = failingUrl == null ? "" : failingUrl.toLowerCase(Locale.US);
        String lowerDesc = description == null ? "" : description.toLowerCase(Locale.US);
        boolean redirectLoop = errorCode == -9 || lowerDesc.contains("redirect") || lowerDesc.contains("too_many_redirects");

        try {
            view.stopLoading();
            // Kosongkan tampilan dulu supaya teks "Halaman web tidak tersedia" tidak terlihat.
            view.loadDataWithBaseURL(FALLBACK_URL,
                    "<html><head><meta name='viewport' content='width=device-width, initial-scale=1'></head>" +
                            "<body style='margin:0;background:#f7f9fc;font-family:sans-serif;'></body></html>",
                    "text/html", "UTF-8", null);
        } catch (Exception ignored) {
        }

        // Untuk loop login, jangan ulang ke /auth/login. Arahkan ke index.
        if (redirectLoop || lowerUrl.contains("/auth/login") || mainFrameErrorCount <= 2) {
            handler.postDelayed(() -> redirectToFallback(view), 180);
        } else {
            hideSmoothLoader();
        }
    }

    private void redirectToFallback(WebView view) {
        if (view == null || redirectingToFallback) return;
        String currentUrl = view.getUrl() == null ? "" : view.getUrl();
        if (currentUrl.startsWith(FALLBACK_URL) && mainFrameErrorCount == 0) {
            hideSmoothLoader();
            return;
        }
        redirectingToFallback = true;
        try {
            view.stopLoading();
        } catch (Exception ignored) {
        }
        showSmoothLoader("Mengalihkan ke Cutiezy...");
        view.loadUrl(FALLBACK_URL);
    }

    private boolean handleBluetoothPrintScheme(String url) {
        // Prioritas pertama: buka aplikasi Bluetooth Print seperti Chrome.
        if (launchExternalUrl(url)) return true;

        // Kalau aplikasi Bluetooth Print tidak ada, jangan tampilkan error WebView.
        // Pakai print Bluetooth native dari aplikasi.
        toast("Aplikasi Bluetooth Print tidak ditemukan. Membuka printer bawaan Cutiezy.");
        printVisiblePageTextToBluetooth();
        return true;
    }

    private void printVisiblePageTextToBluetooth() {
        if (webView == null) {
            chooseBluetoothPrinter("Cutiezy Struk\n");
            return;
        }
        webView.evaluateJavascript("(document.body && document.body.innerText) ? document.body.innerText : ''", value -> {
            String text = decodeJsString(value);
            if (text == null || text.trim().isEmpty()) text = "Cutiezy Struk\n";
            chooseBluetoothPrinter(text);
        });
    }

    private String decodeJsString(String value) {
        if (value == null || "null".equals(value)) return "";
        try {
            return new JSONArray("[" + value + "]").getString(0);
        } catch (Exception e) {
            return value;
        }
    }

    private boolean openIntentScheme(String url) {
        try {
            Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            intent.setComponent(null);
            if (intent.getPackage() != null && getPackageManager().getLaunchIntentForPackage(intent.getPackage()) == null) {
                String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                if (fallbackUrl != null) return openExternal(fallbackUrl);
            }
            startActivity(intent);
            return true;
        } catch (Exception e) {
            try {
                Intent fallback = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(fallback);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        }
    }

    private boolean launchExternalUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            intent.addCategory(Intent.CATEGORY_BROWSABLE);
            startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean openExternal(String url) {
        if (launchExternalUrl(url)) return true;
        toast("Aplikasi pendukung belum terpasang.");
        return true;
    }

    private boolean wantsCamera(WebChromeClient.FileChooserParams params) {
        if (params == null) return false;
        boolean capture = params.isCaptureEnabled();
        String[] types = params.getAcceptTypes();
        if (capture) return true;
        if (types == null || types.length == 0) return true;
        for (String type : types) {
            if (type == null || type.isEmpty() || type.equals("*/*") || type.startsWith("image/") || type.startsWith("video/")) {
                return true;
            }
        }
        return false;
    }

    private void startFileChooser(WebChromeClient.FileChooserParams params) {
        try {
            Intent contentIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentIntent.setType(getAcceptMimeType(params));
            if (params != null && params.getMode() == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE) {
                contentIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }

            ArrayList<Intent> extraIntents = new ArrayList<>();
            if (wantsCamera(params)) {
                Intent cameraIntent = createCameraIntent();
                if (cameraIntent != null) extraIntents.add(cameraIntent);
            }

            Intent chooser = Intent.createChooser(contentIntent, "Pilih file / ambil foto");
            if (!extraIntents.isEmpty()) {
                chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents.toArray(new Intent[0]));
            }
            startActivityForResult(chooser, REQ_FILE_CHOOSER);
        } catch (Exception e) {
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(null);
                filePathCallback = null;
            }
            toast("Pemilih file tidak tersedia di perangkat ini.");
        }
    }

    private String getAcceptMimeType(WebChromeClient.FileChooserParams params) {
        if (params == null || params.getAcceptTypes() == null || params.getAcceptTypes().length == 0) return "*/*";
        String first = params.getAcceptTypes()[0];
        if (first == null || first.trim().isEmpty()) return "*/*";
        if (first.contains(",")) return "*/*";
        return first;
    }

    private Intent createCameraIntent() {
        try {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) == null) return null;
            File photo = createImageFile();
            cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", photo);
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return cameraIntent;
        } catch (Exception e) {
            cameraImageUri = null;
            return null;
        }
    }

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
        File storageDir = new File(getCacheDir(), "camera");
        if (!storageDir.exists()) storageDir.mkdirs();
        return File.createTempFile("CUTIEZY_" + timeStamp + "_", ".jpg", storageDir);
    }

    private void openContactPicker(String requestId) {
        contactRequestId = requestId;
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(intent, REQ_CONTACT_PICKER);
        } catch (Exception e) {
            sendContactResult(requestId, null, null);
            toast("Kontak tidak bisa dibuka di perangkat ini.");
        }
    }

    private void sendContactResult(String requestId, String name, String phone) {
        if (webView == null || requestId == null) return;
        try {
            String payload = "null";
            if (name != null || phone != null) {
                JSONObject obj = new JSONObject();
                JSONArray names = new JSONArray();
                JSONArray phones = new JSONArray();
                JSONArray emails = new JSONArray();
                if (name != null && !name.trim().isEmpty()) names.put(name);
                if (phone != null && !phone.trim().isEmpty()) phones.put(phone);
                obj.put("name", names);
                obj.put("tel", phones);
                obj.put("email", emails);
                obj.put("displayName", name == null ? "" : name);
                obj.put("phone", phone == null ? "" : phone);
                payload = obj.toString();
            }
            String js = "window.__cutiezyContactResult && window.__cutiezyContactResult(" +
                    JSONObject.quote(requestId) + "," + payload + ");";
            webView.evaluateJavascript(js, null);
        } catch (Exception ignored) {
        }
    }

    private void shareText(String text) {
        try {
            Intent sendIntent = new Intent(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, text == null ? "" : text);
            startActivity(Intent.createChooser(sendIntent, "Bagikan via"));
        } catch (Exception e) {
            toast("Fitur share tidak tersedia.");
        }
    }

    private void shareBase64File(String dataUrl, String mimeType, String fileName) {
        executor.execute(() -> {
            try {
                if (dataUrl == null || dataUrl.trim().isEmpty()) throw new Exception("empty");
                String base64 = dataUrl;
                String detectedMime = mimeType == null || mimeType.isEmpty() ? "image/png" : mimeType;
                if (base64.startsWith("data:")) {
                    int comma = base64.indexOf(',');
                    String prefix = comma > 0 ? base64.substring(0, comma) : "";
                    if (prefix.contains(";")) {
                        detectedMime = prefix.substring(5, prefix.indexOf(';'));
                    }
                    base64 = comma > 0 ? base64.substring(comma + 1) : base64;
                }
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                File dir = new File(getCacheDir(), "share");
                if (!dir.exists()) dir.mkdirs();
                String safeName = (fileName == null || fileName.trim().isEmpty()) ? "cutiezy-share.png" : fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
                File file = new File(dir, safeName);
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bytes);
                fos.flush();
                fos.close();

                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType(detectedMime);
                sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                handler.post(() -> startActivity(Intent.createChooser(sendIntent, "Bagikan file via")));
            } catch (Exception e) {
                handler.post(() -> toast("File/gambar belum bisa dibagikan."));
            }
        });
    }

    private void saveBase64File(String dataUrl, String mimeType, String fileName) {
        executor.execute(() -> {
            try {
                if (dataUrl == null || dataUrl.trim().isEmpty()) throw new Exception("empty data");
                String base64 = dataUrl;
                String detectedMime = (mimeType == null || mimeType.trim().isEmpty()) ? "image/jpeg" : mimeType;
                if (base64.startsWith("data:")) {
                    int comma = base64.indexOf(',');
                    String prefix = comma > 0 ? base64.substring(0, comma) : "";
                    if (prefix.contains(";")) detectedMime = prefix.substring(5, prefix.indexOf(';'));
                    base64 = comma > 0 ? base64.substring(comma + 1) : base64;
                }

                String safeNameWork = sanitizeFileName(fileName);
                if (!safeNameWork.contains(".")) {
                    safeNameWork += detectedMime.contains("png") ? ".png" : ".jpg";
                }
                final String finalSafeName = safeNameWork;
                final String finalMimeType = detectedMime;
                byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
                Uri savedUri = writeToDownloads(bytes, finalMimeType, finalSafeName);
                handler.post(() -> toast("Struk tersimpan di Download/Cutiezy: " + finalSafeName));
                if (savedUri != null) {
                    handler.post(() -> scanSavedFile(savedUri, finalMimeType));
                }
            } catch (Exception e) {
                handler.post(() -> toast("Gagal mengunduh struk. Coba share gambar lalu simpan dari galeri."));
            }
        });
    }

    private String sanitizeFileName(String fileName) {
        String safeName = (fileName == null || fileName.trim().isEmpty()) ? "struk-cutiezy.jpg" : fileName.trim();
        return safeName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    private Uri writeToDownloads(byte[] bytes, String mimeType, String fileName) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
            values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Cutiezy");
            values.put(MediaStore.MediaColumns.IS_PENDING, 1);
            Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            Uri uri = getContentResolver().insert(collection, values);
            if (uri == null) throw new Exception("cannot create media uri");
            OutputStream output = getContentResolver().openOutputStream(uri);
            if (output == null) throw new Exception("cannot open output stream");
            output.write(bytes);
            output.flush();
            output.close();
            values.clear();
            values.put(MediaStore.MediaColumns.IS_PENDING, 0);
            getContentResolver().update(uri, values, null, null);
            return uri;
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Cutiezy");
            if (!dir.exists()) dir.mkdirs();
            File file = new File(dir, fileName);
            FileOutputStream output = new FileOutputStream(file);
            output.write(bytes);
            output.flush();
            output.close();
            return Uri.fromFile(file);
        }
    }

    private void scanSavedFile(Uri uri, String mimeType) {
        try {
            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(uri);
            sendBroadcast(scanIntent);
        } catch (Exception ignored) {
        }
    }

    private void downloadHttpFile(String url, String userAgent, String contentDisposition, String mimetype) {
        try {
            String fileName = URLUtil.guessFileName(url, contentDisposition, mimetype);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setTitle(fileName);
            request.setDescription("Mengunduh file Cutiezy");
            request.setMimeType(mimetype == null || mimetype.trim().isEmpty() ? "application/octet-stream" : mimetype);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "Cutiezy/" + fileName);
            if (userAgent != null) request.addRequestHeader("User-Agent", userAgent);
            String cookie = CookieManager.getInstance().getCookie(url);
            if (cookie != null) request.addRequestHeader("Cookie", cookie);
            DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            if (manager == null) throw new Exception("download manager null");
            manager.enqueue(request);
            toast("Download dimulai. Cek folder Download/Cutiezy.");
        } catch (Exception e) {
            openExternal(url);
        }
    }

    private void printCurrentWebView(String title) {
        if (webView == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            toast("Print membutuhkan Android 4.4 ke atas.");
            return;
        }
        try {
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            String jobName = (title == null || title.trim().isEmpty()) ? "Cutiezy Struk" : title;
            PrintDocumentAdapter adapter;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                adapter = webView.createPrintDocumentAdapter(jobName);
            } else {
                adapter = webView.createPrintDocumentAdapter();
            }
            printManager.print(jobName, adapter, new PrintAttributes.Builder().build());
        } catch (Exception e) {
            toast("Print sistem tidak tersedia.");
        }
    }

    private void showPrintChoice(String title, String text) {
        runOnUiThread(() -> {
            String cleanText = text == null ? "" : text.trim();
            String[] options = {"Print Sistem / Simpan PDF", "Printer Bluetooth Thermal"};
            new AlertDialog.Builder(this)
                    .setTitle("Cetak Struk")
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            printCurrentWebView(title);
                        } else {
                            chooseBluetoothPrinter(cleanText.isEmpty() ? "Cutiezy\n" : cleanText);
                        }
                    })
                    .setNegativeButton("Batal", null)
                    .show();
        });
    }

    private void chooseBluetoothPrinter(String text) {
        pendingBluetoothPrintText = text;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQ_BLUETOOTH_PERMISSION);
            return;
        }

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            toast("Perangkat ini tidak mendukung Bluetooth.");
            return;
        }
        if (!adapter.isEnabled()) {
            try {
                startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
            } catch (Exception ignored) {
            }
            toast("Aktifkan Bluetooth dulu, lalu coba print lagi.");
            return;
        }

        try {
            Set<BluetoothDevice> bondedDevices = adapter.getBondedDevices();
            if (bondedDevices == null || bondedDevices.isEmpty()) {
                toast("Belum ada printer Bluetooth yang dipasangkan. Pairing dulu di pengaturan Bluetooth.");
                return;
            }

            ArrayList<BluetoothDevice> devices = new ArrayList<>(bondedDevices);
            String[] names = new String[devices.size()];
            for (int i = 0; i < devices.size(); i++) {
                BluetoothDevice d = devices.get(i);
                String name = d.getName() == null ? "Bluetooth Device" : d.getName();
                names[i] = name + "\n" + d.getAddress();
            }

            new AlertDialog.Builder(this)
                    .setTitle("Pilih Printer Bluetooth")
                    .setItems(names, (dialog, which) -> printToBluetooth(devices.get(which), text))
                    .setNegativeButton("Batal", null)
                    .show();
        } catch (SecurityException e) {
            toast("Izin Bluetooth belum diberikan.");
        }
    }

    private void printToBluetooth(BluetoothDevice device, String text) {
        toast("Menghubungkan ke printer...");
        executor.execute(() -> {
            BluetoothSocket socket = null;
            try {
                socket = device.createRfcommSocketToServiceRecord(SPP_UUID);
                socket.connect();
                OutputStream output = socket.getOutputStream();
                output.write(new byte[]{0x1B, 0x40}); // initialize printer
                output.write((text + "\n\n\n").getBytes("UTF-8"));
                output.write(new byte[]{0x1D, 0x56, 0x42, 0x00}); // partial cut, ignored by printers without cutter
                output.flush();
                handler.post(() -> toast("Struk berhasil dikirim ke printer."));
            } catch (Exception e) {
                handler.post(() -> toast("Gagal print. Pastikan printer sudah pairing dan memakai mode Bluetooth SPP."));
            } finally {
                try {
                    if (socket != null) socket.close();
                } catch (Exception ignored) {
                }
            }
        });
    }

    private String buildCompatibilityJs() {
        return "(function(){" +
                "if(window.__cutiezyNativeReady)return;window.__cutiezyNativeReady=true;" +
                "var style=document.createElement('style');" +
                "style.innerHTML='html{scroll-behavior:smooth;}a,button,[onclick],input[type=button],input[type=submit],.btn,.button{ -webkit-tap-highlight-color: rgba(45,97,180,.18); touch-action: manipulation; transition: transform .14s ease, opacity .14s ease, box-shadow .14s ease;} .ctz-native-tap{transform:scale(.975);opacity:.88;}';" +
                "document.addEventListener('DOMContentLoaded',function(){try{document.head&&document.head.appendChild(style);}catch(e){}});" +
                "if(document.head){try{document.head.appendChild(style);}catch(e){}}" +
                "document.addEventListener('touchstart',function(e){var t=e.target.closest&&e.target.closest('a,button,[onclick],input[type=button],input[type=submit],.btn,.button');if(!t)return;t.classList.add('ctz-native-tap');setTimeout(function(){t.classList.remove('ctz-native-tap')},160);},{passive:true});" +
                "document.addEventListener('click',function(e){try{var a=e.target.closest&&e.target.closest('a[href]');if(!a)return;var h=a.getAttribute('href')||'';if(h.indexOf('my.bluetoothprint.scheme:')===0){e.preventDefault();window.CutiezyNative.openExternalOrNativePrint(h);}}catch(x){}},true);" +
                "window.__cutiezyContactCallbacks={};" +
                "window.__cutiezyContactResult=function(id,data){try{if(data){if(data.tel&&!Array.isArray(data.tel))data.tel=[data.tel];if(data.phone&&!data.tel)data.tel=[data.phone];if(data.name&&!Array.isArray(data.name))data.name=[data.name];if(data.displayName&&!data.name)data.name=[data.displayName];if(data.email&&!Array.isArray(data.email))data.email=[data.email];}}catch(e){}var cb=window.__cutiezyContactCallbacks[id];if(cb){delete window.__cutiezyContactCallbacks[id]; if(data){cb.resolve([data]);}else{cb.resolve([]);}}};" +
                "try{if(!window.ContactsManager){Object.defineProperty(window,'ContactsManager',{value:function(){},configurable:true});}}catch(e){}" +
                "try{if(!navigator.contacts){Object.defineProperty(navigator,'contacts',{value:{},configurable:true});}}catch(e){navigator.contacts={};}" +
                "navigator.contacts.getProperties=function(){return Promise.resolve(['name','tel','email']);};" +
                "navigator.contacts.select=function(props,opts){return new Promise(function(resolve,reject){var id='ctz_'+Date.now()+'_'+Math.random().toString(36).slice(2);window.__cutiezyContactCallbacks[id]={resolve:resolve,reject:reject};window.CutiezyNative.openContacts(id);});};" +
                "navigator.canShare=function(){return true;};" +
                "try{Object.defineProperty(navigator,'share',{configurable:true,value:function(data){data=data||{};return new Promise(function(resolve,reject){try{if(data.files&&data.files.length){var f=data.files[0];var r=new FileReader();r.onload=function(){window.CutiezyNative.shareBase64File(String(r.result),f.type||'image/png',f.name||'cutiezy-share.png');resolve();};r.onerror=function(){reject(new Error('file read failed'));};r.readAsDataURL(f);return;}var parts=[];if(data.title)parts.push(data.title);if(data.text)parts.push(data.text);if(data.url)parts.push(data.url);window.CutiezyNative.shareText(parts.join('\\n'));resolve();}catch(e){reject(e);}});}});}catch(e){}" +
                "function ctzSaveDownload(h,n,m){try{if(!h)return false;if(h.indexOf('data:')===0){window.CutiezyNative.saveBase64File(h,m||'image/jpeg',n||'struk-cutiezy.jpg');return true;}if(h.indexOf('blob:')===0){fetch(h).then(function(r){return r.blob();}).then(function(b){var fr=new FileReader();fr.onload=function(){window.CutiezyNative.saveBase64File(String(fr.result),b.type||m||'image/jpeg',n||'struk-cutiezy.jpg');};fr.onerror=function(){window.CutiezyNative.toast('Gagal membaca file unduhan.');};fr.readAsDataURL(b);}).catch(function(){window.CutiezyNative.toast('Gagal membaca file unduhan.');});return true;}return false;}catch(e){return false;}}" +
                "document.addEventListener('click',function(e){try{var a=e.target.closest&&e.target.closest('a[download]');if(!a)return;var h=a.href||a.getAttribute('href')||'';if(ctzSaveDownload(h,a.getAttribute('download')||'struk-cutiezy.jpg','image/jpeg')){e.preventDefault();e.stopPropagation();}}catch(x){}},true);" +
                "window.print=function(){try{window.CutiezyNative.printCurrentPage(document.title||'Cutiezy Struk',document.body?document.body.innerText:'');}catch(e){}};" +
                "window.CutiezyAndroid={shareText:function(t){window.CutiezyNative.shareText(String(t||''));},printBluetooth:function(t){window.CutiezyNative.printBluetooth(String(t||''));},printSystem:function(){window.CutiezyNative.printSystem(document.title||'Cutiezy');}};" +
                "})();";
    }

    private void injectNativeCompatibilityLayer() {
        if (webView == null) return;
        webView.evaluateJavascript(buildCompatibilityJs(), null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_FILE_CHOOSER) {
            Uri[] results = null;
            if (resultCode == RESULT_OK) {
                if (data == null || data.getData() == null) {
                    if (cameraImageUri != null) results = new Uri[]{cameraImageUri};
                } else if (data.getClipData() != null) {
                    ClipData clipData = data.getClipData();
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        results[i] = clipData.getItemAt(i).getUri();
                    }
                } else if (data.getData() != null) {
                    results = new Uri[]{data.getData()};
                }
            }
            if (filePathCallback != null) {
                filePathCallback.onReceiveValue(results);
                filePathCallback = null;
            }
            pendingFileChooserParams = null;
            return;
        }

        if (requestCode == REQ_CONTACT_PICKER) {
            String name = null;
            String phone = null;
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(data.getData(), null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                        int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        if (nameIndex >= 0) name = cursor.getString(nameIndex);
                        if (phoneIndex >= 0) phone = cursor.getString(phoneIndex);
                    }
                } catch (Exception ignored) {
                } finally {
                    if (cursor != null) cursor.close();
                }
            }
            sendContactResult(contactRequestId, name, phone);
            contactRequestId = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean granted = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (granted && pendingFileChooserParams != null) {
                startFileChooser(pendingFileChooserParams);
            } else {
                if (filePathCallback != null) filePathCallback.onReceiveValue(null);
                filePathCallback = null;
                pendingFileChooserParams = null;
                toast("Izin kamera diperlukan untuk ambil foto.");
            }
            return;
        }

        if (requestCode == REQ_WEBRTC_CAMERA_PERMISSION && pendingWebPermissionRequest != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (granted) pendingWebPermissionRequest.grant(pendingWebPermissionRequest.getResources());
                else pendingWebPermissionRequest.deny();
            }
            pendingWebPermissionRequest = null;
            return;
        }

        if (requestCode == REQ_BLUETOOTH_PERMISSION) {
            if (granted && pendingBluetoothPrintText != null) {
                chooseBluetoothPrinter(pendingBluetoothPrintText);
            } else {
                toast("Izin Bluetooth diperlukan untuk print ke printer thermal.");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (webView != null) {
            String currentUrl = webView.getUrl() == null ? "" : webView.getUrl();
            if (isCutiezyIndex(currentUrl)) {
                moveTaskToBack(true);
                return;
            }
            if (webView.canGoBack()) {
                currentNavigationDirection = NAV_BACK;
                showSmoothLoader("Kembali...");
                webView.goBack();
                return;
            }
        }
        moveTaskToBack(true);
    }

    private boolean isCutiezyIndex(String url) {
        if (url == null) return false;
        String lower = url.toLowerCase(Locale.US);
        return lower.startsWith("https://cutiezy.id/index") || lower.startsWith("http://cutiezy.id/index");
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        executor.shutdownNow();
        super.onDestroy();
    }

    private boolean hasPermission(String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void toast(String message) {
        handler.post(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    public class CutiezyBridge {
        @JavascriptInterface
        public void openContacts(String requestId) {
            runOnUiThread(() -> openContactPicker(requestId));
        }

        @JavascriptInterface
        public void shareText(String text) {
            runOnUiThread(() -> MainActivity.this.shareText(text));
        }

        @JavascriptInterface
        public void shareBase64File(String dataUrl, String mimeType, String fileName) {
            MainActivity.this.shareBase64File(dataUrl, mimeType, fileName);
        }

        @JavascriptInterface
        public void saveBase64File(String dataUrl, String mimeType, String fileName) {
            MainActivity.this.saveBase64File(dataUrl, mimeType, fileName);
        }

        @JavascriptInterface
        public void printSystem(String title) {
            runOnUiThread(() -> printCurrentWebView(title));
        }

        @JavascriptInterface
        public void printBluetooth(String text) {
            runOnUiThread(() -> chooseBluetoothPrinter(text));
        }

        @JavascriptInterface
        public void printCurrentPage(String title, String text) {
            showPrintChoice(title, text);
        }

        @JavascriptInterface
        public void openExternalOrNativePrint(String url) {
            runOnUiThread(() -> {
                if (url != null && url.startsWith("my.bluetoothprint.scheme:")) {
                    handleBluetoothPrintScheme(url);
                } else if (url != null) {
                    openExternal(url);
                }
            });
        }

        @JavascriptInterface
        public void toast(String message) {
            MainActivity.this.toast(message);
        }
    }
}
