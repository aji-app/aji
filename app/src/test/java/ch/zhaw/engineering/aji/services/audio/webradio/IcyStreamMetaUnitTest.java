package ch.zhaw.engineering.aji.services.audio.webradio;

import org.junit.Test;

import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

public class IcyStreamMetaUnitTest {

    @Test
    public void testParseMetadataStringDelimiter() {
        String data = "StreamTitle='Smith - Baby It's You'";

        Map<String, String> parsedMetadata = IcyStreamMeta.parseMetadata(data);

        assertEquals(1, parsedMetadata.size());
        assertTrue(parsedMetadata.containsKey("StreamTitle"));
        assertEquals("Smith - Baby It's You", parsedMetadata.get("StreamTitle"));
    }

    @Test
    public void testParseMetadata() {
        String data = "StreamTitle='Peter Frampton - Show Me The Way [Live]';\n";

        Map<String, String> parsedMetadata = IcyStreamMeta.parseMetadata(data);

        assertEquals(1, parsedMetadata.size());
        assertTrue(parsedMetadata.containsKey("StreamTitle"));
        assertEquals("Peter Frampton - Show Me The Way [Live]", parsedMetadata.get("StreamTitle"));
    }
}
