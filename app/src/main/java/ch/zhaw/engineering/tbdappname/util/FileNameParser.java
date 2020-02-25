package ch.zhaw.engineering.tbdappname.util;

import android.content.Context;
import android.text.TextUtils;

import ch.zhaw.engineering.tbdappname.R;
import lombok.AllArgsConstructor;
import lombok.Value;

@AllArgsConstructor
public class FileNameParser {
    private final Context mContext;

    public ParsedFileName parseFileName(String filename) {
        final String unknown = mContext.getString(R.string.unknown);
        if (filename == null) {
            return new ParsedFileName(unknown, unknown, unknown);
        }

        String[] parts = filename.split("-");
        if (parts.length == 1) {
            return new ParsedFileName(unknown, unknown, parts[0]);
        }
        if (parts.length == 2) {
            return new ParsedFileName(parts[0],unknown, parts[1]);
        }
        if (parts.length == 3) {
            return new ParsedFileName(parts[0],parts[1],parts[2]);
        }

        String[] titleParts = new String[parts.length - 2];
        System.arraycopy(parts, 2, titleParts, 0, parts.length - 2);

        return new ParsedFileName(parts[0],parts[1], TextUtils.join(" - ", titleParts));

    }

    @Value
    public static class ParsedFileName {
        String artist;
        String album;
        String title;
    }
}
