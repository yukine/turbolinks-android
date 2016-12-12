package com.basecamp.turbolinks.demo;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

import com.basecamp.turbolinks.TurbolinksSession;
import com.basecamp.turbolinks.TurbolinksAdapter;
import com.basecamp.turbolinks.TurbolinksView;

public class MainActivity extends AppCompatActivity implements TurbolinksAdapter {
    // Change the BASE_URL to an address that your VM or device can hit.
    private static final String BASE_URL = "http://61.80.249.252:50081/m/";
    private static final String INTENT_URL = "intentUrl";
    private static final String CALLER_URL = "CALLER_URL";

    private String location;
    private TurbolinksView turbolinksView;

    // -----------------------------------------------------------------------
    // Activity overrides
    // -----------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find the custom TurbolinksView object in your layout
        turbolinksView = (TurbolinksView) findViewById(R.id.turbolinks_view);

        // For this demo app, we force debug logging on. You will only want to do
        // this for debug builds of your app (it is off by default)
        TurbolinksSession.getDefault(this).setDebugLoggingEnabled(true);

        // For this example we set a default location, unless one is passed in through an intent
        location = getIntent().getStringExtra(INTENT_URL) != null ? getIntent().getStringExtra(INTENT_URL) : BASE_URL;

        // Execute the visit
        TurbolinksSession.getDefault(this)
            .activity(this)
            .adapter(this)
            .view(turbolinksView)
            .visit(location);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (0 != (getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE))
            {
                TurbolinksSession.getDefault(this).getWebView().setWebContentsDebuggingEnabled(true);
            }
        }
    }

    @Override
    protected void onRestart() {
        if (backLocation != null) {
            String callerUrl = getIntent().getStringExtra(CALLER_URL);
            if (callerUrl == null || callerUrl.isEmpty()) {
                location = BASE_URL;
                backLocation = null;
            } else {
                Uri backUri = Uri.parse(backLocation);
                Uri uri = Uri.parse(location);

                if (backUri != null && uri != null) {
                    if (backUri.getPath() != null && backUri.getPath().equals(uri.getPath())) {
                        backLocation = null;
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
            }
        }

        super.onRestart();

        // Since the webView is shared between activities, we need to tell Turbolinks
        // to load the location from the previous activity upon restarting
        TurbolinksSession.getDefault(this)
            .activity(this)
            .adapter(this)
            .restoreWithCachedSnapshot(true)
            .view(turbolinksView)
            .visit(location.replace("_BOS=1", ""));
    }

    @Override
    public void onBackPressed() {
        String callerUrl = getIntent().getStringExtra(CALLER_URL) != null ? getIntent().getStringExtra(CALLER_URL) : "";
        if (callerUrl == null || callerUrl.isEmpty()) {
            return;
        }

        super.onBackPressed();
    }

    // -----------------------------------------------------------------------
    // TurbolinksAdapter interface
    // -----------------------------------------------------------------------

    @Override
    public void onPageFinished() {

    }

    @Override
    public void onReceivedError(int errorCode) {
        handleError(errorCode);
    }

    @Override
    public void pageInvalidated() {

    }

    @Override
    public void requestFailedWithStatusCode(int statusCode) {
        handleError(statusCode);
    }

    @Override
    public void visitCompleted() {

    }

    private static String backLocation = null;

    // The starting point for any href clicked inside a Turbolinks enabled site. In a simple case
    // you can just open another activity, or in more complex cases, this would be a good spot for
    // routing logic to take you to the right place within your app.
    @Override
    public void visitProposedToLocationWithAction(String location, String action) {
        if ("back".equals(action)) {
            backLocation = location;

            String callerUrl = getIntent().getStringExtra(CALLER_URL) != null ? getIntent().getStringExtra(CALLER_URL) : "";
            if (callerUrl == null || callerUrl.isEmpty()) {
                return;
            }

            finish();

            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(INTENT_URL, location);
        intent.putExtra(CALLER_URL, this.location);

        this.startActivity(intent);
    }

    // -----------------------------------------------------------------------
    // Private
    // -----------------------------------------------------------------------

    // Simply forwards to an error page, but you could alternatively show your own native screen
    // or do whatever other kind of error handling you want.
    private void handleError(int code) {
        if (code == 404) {
            TurbolinksSession.getDefault(this)
                .activity(this)
                .adapter(this)
                .restoreWithCachedSnapshot(false)
                .view(turbolinksView)
                .visit(BASE_URL + "/error");
        }
    }
}
