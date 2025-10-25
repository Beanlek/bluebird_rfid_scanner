package com.amastsales.bluebird_rfid_scanner.control;

import android.content.Context;

public class ListNoView {
    private final TagListAdapter adapter;
    private int selectedIndex = -1;

    public ListNoView(Context ctx) {
        this.adapter = new TagListAdapter(ctx);
    }

    public TagListAdapter getAdapter() {
        return adapter;
    }

    public void setSelection(int position) {
        // We don’t scroll in plugin mode, so just record the last index.
        if (position >= 0 && position < adapter.getCount()) {
            selectedIndex = position;
        }
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }
}
