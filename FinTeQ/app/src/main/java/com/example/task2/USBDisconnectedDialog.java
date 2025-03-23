package com.example.task2;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class USBDisconnectedDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        builder.setTitle("TrusToken Disconnected")
                .setMessage("Please connect the USB token to proceed.")
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    requireActivity().finishAffinity();
                    requireActivity().finish();
                     // Close all activities
                });

        AlertDialog dialog = builder.create();

        // Set Window Type to make it appear above Razorpay
        if (dialog.getWindow() != null) {
            dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
        }

        return dialog;
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        requireActivity().finishAffinity();
    }
}

