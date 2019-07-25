import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.utility.json.JsonUtils;

public class JsonTest {

    private final static Logger LOG = LoggerFactory.getLogger (JsonTest.class);

    @Test
    public void testMap () throws IOException {

        final Map<String, String> map = new ConcurrentHashMap<> ();
        map.put ("TestA", "Test A");
        map.put ("TestB", "Test B");

        final String json = JsonUtils.generateJson (map);

        LOG.info ("Generated JSON string={}", json);
    }
}
