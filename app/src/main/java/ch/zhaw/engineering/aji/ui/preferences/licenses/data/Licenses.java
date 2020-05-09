package ch.zhaw.engineering.aji.ui.preferences.licenses.data;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.zhaw.engineering.aji.R;
import lombok.Builder;
import lombok.Getter;

public class Licenses {

    public static final List<LicenseInformation> ITEMS = new ArrayList<>();

    public static final Map<Integer, LicenseInformation> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_appcompat)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_appcompat_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_constraintlayout)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_constraintlayout_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_databinding_viewbinding)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_databinding_viewbinding_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_legacy)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_legacy_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_lifecycle)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_lifecycle_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_navigation)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_navigation_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_preference)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_preference_url).build());


        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_recyclerview)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_recyclerview_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_room)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_room_url).build());


        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_androidx_viewpager2)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androidx_androidx_viewpager2_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_exoplayer)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_exoplayer_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_google_material)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_google_material_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_picasso)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_picasso_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_tedpermission)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_tedpermission_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_fastcsv)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_fastcsv_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_lombok)
                .license(R.string.license_lombok)
                .url(R.string.library_lombok_url).build());

        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_pcm_resampler)
                .license(R.string.license_lgpl)
                .url(R.string.library_pcm_resampler_url).build());
    }


    private static void addItem(LicenseInformation item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getLibraryName(), item);
    }

    @Builder
    public static class LicenseInformation {
        public LicenseInformation(@StringRes int libraryName, @StringRes int license, @StringRes Integer url) {
            this.mLibraryName = libraryName;
            this.mLicense = license;
            this.mUrl = url;
        }

        @StringRes
        @Getter
        private final int mLibraryName;

        @StringRes
        @Getter
        private final int mLicense;

        @StringRes
        @Getter
        private final Integer mUrl;
    }

}
