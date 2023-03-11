package com.moutamid.meusom;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.audiofx.AudioEffect;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.fxn.stash.Stash;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;

import java.io.File;

public class SettingsActivity extends AppCompatActivity {
    private static final String TAG = "SettingsACtivity";
    private Context context = SettingsActivity.this;
    private Utils utils = new Utils();
    private LinearLayout optionsLayout;
    private boolean isOpen = false;
    private RadioGroup languageRadioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context, "en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context, "pr");
        }
        setContentView(R.layout.activity_settings);
        TextView textView = findViewById(R.id.userDetailsTv);
        languageRadioGroup = findViewById(R.id.languageRadioButtonsGroup);
        optionsLayout = findViewById(R.id.settingsOptionsButtonLinearLayout);
        textView.setText(
                utils.getStoredString(context, Constants.USER_EMAIL)
                        +"\n"+
                        utils.getStoredString(context, Constants.USER_PASSWORD)
        );
        findViewById(R.id.signOutBtn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                utils.showDialog(context,
                        "Are you sure?",
                        "Do you really want to sign out?",
                        "Yes",
                        "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Constants.auth().signOut();
                                Intent intent = new Intent(SettingsActivity.this, SplashActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                dialogInterface.dismiss();
                                finish();
                                startActivity(intent);
                               // Stash.clear(Constants.OFF_DATA);
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }, true);
            }
        });

        findViewById(R.id.backBtnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        findViewById(R.id.equalizerBtnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openEqualizerSettings();
//                startActivity(new Intent(SettingsActivity.this, MainActivity2.class));
            }
        });
        RadioButton enbutton = findViewById(R.id.englishRadioButton);
        RadioButton prbutton = findViewById(R.id.portegueseRadioButton);

        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            enbutton.setChecked(true);
            prbutton.setChecked(false);
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            prbutton.setChecked(true);
            enbutton.setChecked(false);
        }

        languageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.englishRadioButton:
                        utils.changeLanguage(context, "en");
                        utils.storeString(context, Constants.LANGUAGE, Constants.ENGLISH);
                        utils.storeBoolean(context, "isChanged", true);
//                        Toast.makeText(SettingsActivity.this, "English", Toast.LENGTH_SHORT).show();
                        break;

                    case R.id.portegueseRadioButton:
                        utils.changeLanguage(context, "pr");
                        utils.storeString(context, Constants.LANGUAGE, Constants.PORTUGUESE);
                        utils.storeBoolean(context, "isChanged", true);
//                        Toast.makeText(SettingsActivity.this, "Portugues", Toast.LENGTH_SHORT).show();
                        break;
                }

                recreate();
            }
        });

        findViewById(R.id.languageBtnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOpen = true;
                optionsLayout.setVisibility(View.GONE);
                languageRadioGroup.setVisibility(View.VISIBLE);
            }
        });

        findViewById(R.id.storageBtnSettings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isOpen = true;
                optionsLayout.setVisibility(View.GONE);
                findViewById(R.id.storage_layout_settings).setVisibility(View.VISIBLE);
            }
        });

        getUsedSpace();
    }

    private void getUsedSpace() {
        Constants.databaseReference().child(Constants.SONGS)
                .child(Constants.auth().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            return;
                        }

                        long count = (long) (snapshot.getChildrenCount() * 2.6);

                        TextView textView = findViewById(R.id.usedSpaceTextviewSettings);
                        textView.setText("1GB\n" + count + "MB");
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        TextView textView = findViewById(R.id.usedSpaceTextviewSettings);
                        textView.setText("27MB");
                        Toast.makeText(context, error.toException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        EditText editText = findViewById(R.id.increaseSpaceEtSettings);
        String value = utils.getStoredString(context, Constants.CURRENT_SPACE_AMOUNT);
        if (value.equals("Error"))
            editText.setText("256MB");
        else editText.setText(value);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                utils.storeString(context, Constants.CURRENT_SPACE_AMOUNT, charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        findViewById(R.id.terms_and_conditions_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.terms_and_conditions_url)));
                startActivity(browserIntent);
            }
        });

    }

    private void openEqualizerSettings() {
        Intent intent = new Intent(AudioEffect
                .ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL);
        if ((intent.resolveActivity(getPackageManager()) != null)) {
            startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your device does not support an equalizer!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (isOpen) {
            //TODO:HIDE ALL THE OTHER LAYOUTS
            languageRadioGroup.setVisibility(View.GONE);
            findViewById(R.id.storage_layout_settings).setVisibility(View.GONE);

            optionsLayout.setVisibility(View.VISIBLE);
            isOpen = false;
            return;
        }

        if (utils.getStoredBoolean(context, "isChanged")) {
            utils.storeBoolean(context, "isChanged", false);

            Intent intent = new Intent(context, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            finish();
            startActivity(intent);
            return;
        }

        super.onBackPressed();
    }

    private void khk() {
        File file = new File(utils.getSongPath(""));

// Get length of file in bytes
        long fileSizeInBytes = file.length();
// Convert the bytes to Kilobytes (1 KB = 1024 Bytes)
        long fileSizeInKB = fileSizeInBytes / 1024;
// Convert the KB to MegaBytes (1 MB = 1024 KBytes)
        long fileSizeInMB = fileSizeInKB / 1024;

        if (fileSizeInMB > 27) {
//  ...
        }
    }
}