package com.jfsoft.cef;

import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author ChenXc
 * @version V1.0
 * @Date 2018/11/30  15:14
 * @Description TODO(一句话描述类作用)
 */
public class BrowserFrame extends JFrame {

    private boolean isClosed_ = false;
    private CefBrowser browser_ = null;
    private CefClient client_ = null;
    private static int browserCount_ = 0;

    public BrowserFrame() {
        this(null);
    }

    public BrowserFrame(String title) {
        super(title);

        // Browser window closing works as follows:
        //   1. Clicking the window X button calls WindowAdapter.windowClosing.
        //   2. WindowAdapter.windowClosing calls CefBrowser.close(false).
        //   3. CEF calls CefLifeSpanHandler.doClose() which calls CefBrowser.doClose()
        //      which returns true (canceling the close).
        //   4. CefBrowser.doClose() triggers another call to WindowAdapter.windowClosing.
        //   5. WindowAdapter.windowClosing calls CefBrowser.close(true).
        //   6. For windowed browsers CEF destroys the native window handle. For OSR
        //      browsers CEF calls CefLifeSpanHandler.doClose() which calls
        //      CefBrowser.doClose() again which returns false (allowing the close).
        //   7. CEF calls CefLifeSpanHandler.onBeforeClose and the browser is destroyed.
        //
        // On macOS pressing Cmd+Q results in a call to CefApp.handleBeforeTerminate
        // which calls CefBrowser.close(true) for each existing browser. CEF then calls
        // CefLifeSpanHandler.onBeforeClose and the browser is destroyed.
        //
        // Application shutdown works as follows:
        //   1. CefLifeSpanHandler.onBeforeClose calls CefApp.getInstance().dispose()
        //      when the last browser window is destroyed.
        //   2. CefAppHandler.stateHasChanged terminates the application by calling
        //      System.exit(0) when the state changes to CefAppState.TERMINATED.
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (browser_ == null) {
                    // If there's no browser we can dispose immediately.
                    isClosed_ = true;
                    System.out.println("BrowserFrame.windowClosing Frame.dispose");
                    dispose();
                    return;
                }

                boolean isClosed = isClosed_;

                if (isClosed) {
                    // Cause browser.doClose() to return false so that OSR browsers
                    // can close.
                    browser_.close();
                }

                // Results in another call to this method.
                System.out.println("BrowserFrame.windowClosing CefBrowser.close(" + isClosed + ")");
                browser_.close();
                if (!isClosed_) {
                    isClosed_ = true;
                }
//                if (isClosed) {
//                    // Dispose after the 2nd call to this method.
//                    System.out.println("BrowserFrame.windowClosing Frame.dispose");
//                    dispose();
//                }
                dispose();
            }
        });
    }

    public void setBrowser(CefClient client, CefBrowser browser) {
        if (client_ == null) client_ = client;
        if (browser_ == null) browser_ = browser;

        client_.removeLifeSpanHandler();
        client_.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public void onAfterCreated(CefBrowser browser) {
                System.out.println("BrowserFrame.onAfterCreated id=" + browser.getIdentifier());
                browserCount_++;
            }

            @Override
            public boolean doClose(CefBrowser browser) {
                browser.close();
                System.out.println("BrowserFrame.doClose id=" + browser.getIdentifier()
                        + " CefBrowser.doClose=" + true);
                return true;
            }

            @Override
            public void onBeforeClose(CefBrowser browser) {
                System.out.println("BrowserFrame.onBeforeClose id=" + browser.getIdentifier());
                if (--browserCount_ == 0) {
                    System.out.println("BrowserFrame.onBeforeClose CefApp.dispose");
                    CefApp.getInstance().dispose();
                }
            }
        });
    }

    public void removeBrowser() {
        browser_ = null;
    }

    public CefBrowser getBrowser() {
        return browser_;
    }
}
