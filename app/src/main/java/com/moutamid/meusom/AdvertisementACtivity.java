package com.moutamid.meusom;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.moutamid.meusom.utilis.Constants;
import com.moutamid.meusom.utilis.Utils;

import java.util.ArrayList;

import musicplayer.AdManager;

public class AdvertisementACtivity extends AppCompatActivity {
    private Utils utils = new Utils();
    private static final String TAG = "AdvertisementACtivity";
    private Context context = AdvertisementACtivity.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.ENGLISH)) {
            utils.changeLanguage(context, "en");
        } else if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {
            utils.changeLanguage(context, "pr");
        }
        setContentView(R.layout.activity_advertisement);
        AdManager.getInstance(context).showInterstitialAd(context);
        findViewById(R.id.backBtnAdvertising).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Button button = findViewById(R.id.adBtn);

        if (utils.isTodaySunday()) {
            button.setText(
                    getString(R.string.watch_advertising)
                            + " ("
                            + utils.getAdsInteger(context, utils.getDate())
                            + ")"
            );
        } else {
            button.setText(
                    getString(R.string.watch_advertising)
                            + " ("
                            + utils.getAdsInteger(context, utils.getLastSunday())
                            + ")"
            );
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!checkIfAdsAreWatched()) {
                    return;
                }

                Toast.makeText(context, "Loading...", Toast.LENGTH_SHORT).show();
                AdManager.getInstance(context).showInterstitialAd(context);

                if (utils.isTodaySunday()) {
                    utils.storeInteger(context, utils.getDate(),
                            utils.getAdsInteger(context, utils.getDate()) - 1);
                    button.setText(
                            getString(R.string.watch_advertising)
                                    + " ("
                                    + utils.getAdsInteger(context, utils.getDate())
                                    + ")"
                    );
                } else {
                    utils.storeInteger(context, utils.getLastSunday(),
                            utils.getAdsInteger(context, utils.getLastSunday()) - 1);
                    button.setText(
                            getString(R.string.watch_advertising)
                                    + " ("
                                    + utils.getAdsInteger(context, utils.getLastSunday())
                                    + ")"
                    );
                }
            }
        });

        ArrayList<SlideModel> imageList = new ArrayList<>();

        if (utils.getStoredString(context, Constants.LANGUAGE).equals(Constants.PORTUGUESE)) {

            imageList.add(new SlideModel(R.drawable.ads_part_1_pr, "", ScaleTypes.CENTER_INSIDE));
            imageList.add(new SlideModel(R.drawable.ads_part_2_pr, "", ScaleTypes.CENTER_INSIDE));
            imageList.add(new SlideModel(R.drawable.ads_part_3_pr, "", ScaleTypes.CENTER_INSIDE));

        } else {

            imageList.add(new SlideModel(R.drawable.ads_part1, "", ScaleTypes.CENTER_INSIDE));
            imageList.add(new SlideModel(R.drawable.ads_part2, "", ScaleTypes.CENTER_INSIDE));
            imageList.add(new SlideModel(R.drawable.ads_part3, "", ScaleTypes.CENTER_INSIDE));
        }

        ImageSlider imageSlider = findViewById(R.id.image_slider);
        imageSlider.setImageList(imageList);
    }

    private boolean checkIfAdsAreWatched() {
        if (utils.getAdsInteger(context, utils.getLastSunday()) > 0) {
            return true;
        }
        if (utils.isTodaySunday()) {
            if (utils.getAdsInteger(context, utils.getDate()) > 0) {
                return true;
            }
        }
        return false;
    }
}