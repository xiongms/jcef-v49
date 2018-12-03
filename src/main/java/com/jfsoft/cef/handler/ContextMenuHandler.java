// Copyright (c) 2014 The Chromium Embedded Framework Authors. All rights
// reserved. Use of this source code is governed by a BSD-style license that
// can be found in the LICENSE file.

package com.jfsoft.cef.handler;

import java.awt.Frame;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import com.jfsoft.cef.dialog.SearchDialog;
import com.jfsoft.cef.dialog.ShowTextDialog;
import org.cef.browser.CefBrowser;
import org.cef.callback.CefContextMenuParams;
import org.cef.callback.CefMenuModel;
import org.cef.callback.CefMenuModel.MenuId;
import org.cef.handler.CefContextMenuHandler;



public class ContextMenuHandler implements CefContextMenuHandler {
    private final Frame owner_;
    private Map<Integer, String> suggestions_ = new HashMap<Integer, String>();

    public ContextMenuHandler(Frame owner) {
        owner_ = owner;
    }

    @Override
    public void onBeforeContextMenu(CefBrowser browser,
                                    CefContextMenuParams params,
                                    CefMenuModel model) {

        model.clear();

        // Navigation menu
        model.addItem(MenuId.MENU_ID_BACK, "后退");
        model.setEnabled(MenuId.MENU_ID_BACK, browser.canGoBack());

        model.addItem(MenuId.MENU_ID_FORWARD, "前进");
        model.setEnabled(MenuId.MENU_ID_FORWARD, browser.canGoForward());
        model.addItem(MenuId.MENU_ID_RELOAD_NOCACHE, "强制刷新");

        model.addSeparator();
        model.addItem(MenuId.MENU_ID_FIND, "查找...");
        if (params.hasImageContents() && params.getSourceUrl() != null)
            model.addItem(MenuId.MENU_ID_USER_FIRST, "下载图像...");
        model.addItem(MenuId.MENU_ID_VIEW_SOURCE, "查看网页源代码...");


        Vector<String> suggestions = new Vector<String>();
        params.getDictionarySuggestions(suggestions);

        // Spell checking menu
        model.addSeparator();
        if (suggestions.size() == 0) {
            model.addItem(MenuId.MENU_ID_NO_SPELLING_SUGGESTIONS, "No suggestions");
            model.setEnabled(MenuId.MENU_ID_NO_SPELLING_SUGGESTIONS, false);
            return;
        }

        int id = MenuId.MENU_ID_SPELLCHECK_SUGGESTION_0;
        for (String suggestedWord : suggestions) {
            model.addItem(id, suggestedWord);
            suggestions_.put(id, suggestedWord);
            if (++id > MenuId.MENU_ID_SPELLCHECK_SUGGESTION_LAST)
                break;
        }
    }

    @Override
    public boolean onContextMenuCommand(CefBrowser browser,
                                        CefContextMenuParams params,
                                        int commandId,
                                        int eventFlags) {
        switch (commandId) {
            case MenuId.MENU_ID_VIEW_SOURCE:
                ShowTextDialog visitor = new ShowTextDialog(owner_, "Source of \"" +
                        browser.getURL() + "\"");
                browser.getSource(visitor);
                return true;
            case MenuId.MENU_ID_FIND:
                SearchDialog search = new SearchDialog(owner_, browser);
                search.setVisible(true);
                return true;
            case MenuId.MENU_ID_USER_FIRST:
                browser.startDownload(params.getSourceUrl());
                return true;
            default:
                if (commandId >= MenuId.MENU_ID_SPELLCHECK_SUGGESTION_0) {
                    String newWord = suggestions_.get(commandId);
                    if (newWord != null) {
                        System.err.println("replacing " + params.getMisspelledWord() +
                                " with " + newWord);
                        browser.replaceMisspelling(newWord);
                        return true;
                    }
                }
                return false;
        }
    }

    @Override
    public void onContextMenuDismissed(CefBrowser browser) {
        suggestions_.clear();
    }
}
