package com.shubham.swipenest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ModalBottomSheetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modal_bottom_sheet);

        ImageView btnOptions = findViewById(R.id.ivOptions);
        btnOptions.setOnClickListener( view -> {
//            BottomSheetDialog dialog = new BottomSheetDialog(this);
//            View bottomSheetView = getLayoutInflater().inflate(R.layout.fragment_modal_bottom_sheet, null);
//            dialog.setContentView(R.layout.fragment_modal_bottom_sheet);
//            dialog.show();
//            bottomSheetView.findViewById(R.id.llDelete).setOnClickListener( v -> {
//                Toast.makeText(this, "Delete option pressed", Toast.LENGTH_SHORT).show();
//            });
//            bottomSheetView.findViewById(R.id.llEdit).setOnClickListener( v -> {
//                Toast.makeText(this, "Edit option pressed", Toast.LENGTH_SHORT).show();
//            });
//            bottomSheetView.findViewById(R.id.btnCancel).setOnClickListener( v -> {
//                dialog.dismiss();
//            });
            ModalBottomSheetFragment bottomSheetFragment = new ModalBottomSheetFragment();
            bottomSheetFragment.show(getSupportFragmentManager(), "ModalBottomSheetFragment");
        });
    }
}