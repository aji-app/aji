package ch.zhaw.engineering.aji.ui.preferences.licenses.data;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Collections;
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
                .url(R.string.library_androix_appcompat_url).build());
        addItem(LicenseInformation.builder()
                .libraryName(R.string.library_androidx_annotation)
                .license(R.string.license_apache_2_0)
                .url(R.string.library_androix_annotation_url).build());
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
