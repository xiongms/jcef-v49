// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package com.jfsoft.cef.handler;

import com.jfsoft.cef.dialog.DevToolsDialog;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefKeyboardHandlerAdapter;

import java.awt.*;

public class KeyboardHandler extends CefKeyboardHandlerAdapter {

    private final Frame owner_;

    public KeyboardHandler(Frame owner) {
        owner_ = owner;
    }

    @Override
    public boolean onKeyEvent(CefBrowser browser, CefKeyEvent event) {
//        if (!event.focus_on_editable_field && event.windows_key_code == 0x20) {
//            // Special handling for the space character when an input element does not
//            // have focus. Handling the event in OnPreKeyEvent() keeps the event from
//            // being processed in the renderer. If we instead handled the event in the
//            // OnKeyEvent() method the space key would cause the window to scroll in
//            // addition to showing the alert box.
//            if (event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN) {
//                browser.executeJavaScript("alert('You pressed the space bar!');", "", 0);
//            }
//            return true;
//        }
        // 开发者工具 F12
        if (event.windows_key_code == 123) {
            if (event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN) {
                DevToolsDialog devToolsDlg =
                        new DevToolsDialog(owner_, "DevTools", browser);
                devToolsDlg.setVisible(true);
            }
        }
        // 拍照 F10
        if (event.windows_key_code == 121) {
            if (event.type == CefKeyEvent.EventType.KEYEVENT_RAWKEYDOWN) {
                browser.executeJavaScript("$('#btn-photo');", "", 0);
            }
        }
        return false;
    }
}
