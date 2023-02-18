package musicplayer;

import android.app.Activity;
import android.content.Context;
import android.widget.FrameLayout;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.NativeAdLayout;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.moutamid.meusom.R;

public class AdManager {

    private InterstitialAd adMobInterstitialAd;
    private com.facebook.ads.InterstitialAd fBInterstitial;
    private UnifiedNativeAd nativeAd;
    private boolean isAdLoad = true;
    private NativeAdLayout nativeAdLayout;

    public AdManager(Context context) {
        loadInterstitialAds(context);
    }

    private static volatile AdManager instance;

    public static AdManager getInstance(Context context) {
        if (instance == null) {//Check for the first time
            synchronized (AdManager.class) {   //Check for the second time.
                if (instance == null) instance = new AdManager(context);
            }
        }
        return instance;
    }

//    public void showBannerAd(Activity activity) {
//
//        FrameLayout adContainer = activity.findViewById(R.id.ad_container);
//        loadFbBannerAd(adContainer, activity);
//    }

    private void loadFbBannerAd(FrameLayout adContainer, Activity activity) {

        com.facebook.ads.AdView fBAdView = new com.facebook.ads.AdView(activity,
                activity.getString(R.string.fb_banner_id), com.facebook.ads.AdSize.BANNER_HEIGHT_50);
        adContainer.addView(fBAdView);
        com.facebook.ads.AdListener adListener = new com.facebook.ads.AdListener() {
            @Override
            public void onError(Ad ad, AdError adError) {
                AdView mAdView = new AdView(activity);
                mAdView.setAdSize(AdSize.SMART_BANNER);
                mAdView.setAdUnitId(activity.getString(R.string.admob_banner_id));

                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
                adContainer.addView(mAdView);
                // Ad error callback
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Ad loaded callback
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Ad clicked callback
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Ad impression logged callback
            }
        };
        // Request an ad
        fBAdView.loadAd(fBAdView.buildLoadAdConfig().withAdListener(adListener).build());

    }

    public void showInterstitialAd(Context context) {

        if(fBInterstitial != null && fBInterstitial.isAdLoaded() && !fBInterstitial.isAdInvalidated()){
            fBInterstitial.show();
            loadInterstitialAds(context);
            return;
        }
        if (adMobInterstitialAd != null && adMobInterstitialAd.isLoaded()) {
            adMobInterstitialAd.show();
            loadInterstitialAds(context);
            return;
        }

        if (adMobInterstitialAd == null || !adMobInterstitialAd.isLoaded()) {
            loadInterstitialAds(context);
            return;
        }

        adMobInterstitialAd.show();
        loadInterstitialAds(context);
    }

    private void loadInterstitialAds(Context context) {

        adMobInterstitialAd = new InterstitialAd(context);
        adMobInterstitialAd.setAdUnitId(context.getString(R.string.admob_interstitial_id));
        adMobInterstitialAd.loadAd(new AdRequest.Builder().build());
        loadFbInterstitial(context);

    }

    private void loadFbInterstitial(Context context) {
        fBInterstitial = new com.facebook.ads.InterstitialAd(context,
                context.getString(R.string.fb_interstitial_id));
        fBInterstitial.loadAd(
                fBInterstitial.buildLoadAdConfig()
                        .build());


    }






//    static boolean isPurchased() {
//        return MainActivity.isPurchased;
//    }

    public InterstitialAd getAdMobInterstitialAd() {

        return adMobInterstitialAd;

    }

    public com.facebook.ads.InterstitialAd getFBInterstitial() {
        return fBInterstitial;
    }


}
