package ch.zhaw.engineering.aji.services.audio.webradio;

import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Originally from [but modified]: https://stackoverflow.com/q/9237562
 */
/* package */ class IcyStreamMeta {

    private URL streamUrl;
    private Map<String, String> metadata;
    private boolean isError;

    IcyStreamMeta(URL streamUrl) {
        setStreamUrl(streamUrl);

        isError = false;
    }

    /**
     * Get artist using stream's title
     *
     * @return String
     * @throws IOException
     */
    public String getArtist() throws IOException {
        Map<String, String> data = getMetadata();
        if (data == null){
            throw new IOException("Cannot load metadata");
        }
        if (!data.containsKey("StreamTitle"))
            return "";

        String streamTitle = data.get("StreamTitle");
        if (streamTitle != null) {
            int index = streamTitle.indexOf("-");
            if (index == -1) {
                return streamTitle.trim();
            }
            String title = streamTitle.substring(0, index);
            return title.trim();
        }
        return "";
    }

    /**
     * Get title using stream's title
     *
     * @return String
     * @throws IOException
     */
    public String getTitle() throws IOException {
        Map<String, String> data = getMetadata();
        if (data == null){
            throw new IOException("Cannot load metadata");
        }

        if (!data.containsKey("StreamTitle"))
            return "";

        String streamTitle = data.get("StreamTitle");
        if (streamTitle != null) {
            String artist = streamTitle.substring(streamTitle.indexOf("-") + 1);
            return artist.trim();
        }
        return "";
    }

    private Map<String, String> getMetadata() throws IOException {
        if (metadata == null) {
            refreshMeta();
        }

        return metadata;
    }

    void refreshMeta() throws IOException {
        retrieveMetadata();
    }

    private void retrieveMetadata() throws IOException {
        URLConnection con = streamUrl.openConnection();
        con.setRequestProperty("Icy-MetaData", "1");
        con.setRequestProperty("Connection", "close");
        con.setRequestProperty("Accept", null);
        con.connect();

        int metaDataOffset = 0;
        Map<String, List<String>> headers = con.getHeaderFields();
        InputStream stream = con.getInputStream();

        if (headers.containsKey("icy-metaint")) {
            // Headers are sent via HTTP
            List<String> metaHeaders = headers.get("icy-metaint");
            if (metaHeaders != null && metaHeaders.size() > 0) {
                metaDataOffset = Integer.parseInt(metaHeaders.get(0));
            }
        } else {
            // Headers are sent within a stream
            StringBuilder strHeaders = new StringBuilder();
            int c;
            while ((c = stream.read()) != -1) {
                strHeaders.append((char) c);
                if (strHeaders.length() > 5 && (strHeaders.substring((strHeaders.length() - 4), strHeaders.length()).equals("\r\n\r\n"))) {
                    // end of headers
                    break;
                }
            }

            // Match headers to get metadata offset within a stream
            Pattern p = Pattern.compile("\\r\\n(icy-metaint):\\s*(.*)\\r\\n");
            Matcher m = p.matcher(strHeaders.toString());
            if (m.find()) {
                String offset = m.group(2);
                if (offset != null) {
                    metaDataOffset = Integer.parseInt(offset);
                }
            }
        }

        // In case no data was sent
        if (metaDataOffset == 0) {
            isError = true;
            return;
        }

        // Read metadata
        int b;
        int count = 0;
        int metaDataLength = 4080; // 4080 is the max length
        boolean inData;
        StringBuilder metaData = new StringBuilder();
        // Stream position should be either at the beginning or right after headers
        while ((b = stream.read()) != -1) {
            count++;

            // Length of the metadata
            if (count == metaDataOffset + 1) {
                metaDataLength = b * 16;
            }

            inData = count > metaDataOffset + 1 && count < (metaDataOffset + metaDataLength);
            if (inData) {
                if (b != 0) {
                    metaData.append((char) b);
                }
            }
            if (count > (metaDataOffset + metaDataLength)) {
                break;
            }

        }

        // Set the data
        metadata = IcyStreamMeta.parseMetadata(metaData.toString());

        // Close
        stream.close();
    }

    private void setStreamUrl(URL streamUrl) {
        this.metadata = null;
        this.streamUrl = streamUrl;
        this.isError = false;
    }

    static Map<String, String> parseMetadata(String metaString) {
        Map<String, String> metadata = new HashMap<>();
        String[] metaParts = metaString.split(";");
        Pattern p = Pattern.compile("^(\\w+)='(.*)'$");
        Matcher m;
        for (String metaPart : metaParts) {
            m = p.matcher(metaPart);
            if (m.find()) {
                metadata.put(m.group(1), m.group(2));
            }
        }

        if (metadata.isEmpty()) {
            Log.e("METADATA", "ICY METADATA EMPTY");
        }
        return metadata;
    }
}