package com.mcal.kotlin.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.mcal.kotlin.R;
import com.mcal.kotlin.adapters.BookmarksAdapter;
import com.mcal.kotlin.data.Bookmarks;

import java.util.ArrayList;

import ru.svolf.melissa.sheet.SweetViewDialog;

public class BookmarksFragment extends BottomSheetDialogFragment {
    ArrayList<String> items = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Cursor cursor = Bookmarks.getAllBookmarks();
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                items.add(cursor.getString(1));
            }
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.bookmarks_fragment, null);

        if (items.size() > 0) {
            RecyclerView rcview = view.findViewById(R.id.bookmarksList);
            rcview.setLayoutManager(new LinearLayoutManager(getActivity()));
            rcview.setAdapter(new BookmarksAdapter(items, this, (MainView) getActivity()));
            rcview.setVisibility(View.VISIBLE);
        } else view.findViewById(R.id.no_bookmarks).setVisibility(View.VISIBLE);

        final SweetViewDialog dialog = new SweetViewDialog(getContext());
        dialog.setTitle(getString(R.string.bookmarks));
        dialog.setView(view);
        dialog.setPositive(android.R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

}
