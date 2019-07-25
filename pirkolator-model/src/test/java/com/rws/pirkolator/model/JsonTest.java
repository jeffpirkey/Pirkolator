package com.rws.pirkolator.model;

import static com.rws.utility.common.Preconditions.notNull;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rws.utility.json.JsonUtils;

public class JsonTest {

    private final static Logger LOG = notNull (LoggerFactory.getLogger (JsonTest.class));

    @Test
    public void testMetadata () throws IOException {

        final Metadata metadata = new Metadata ();
        metadata.getMap ().put ("metadata key 1", "metadata value 1");
        metadata.getMap ().put ("metadata key 2", "metadata value 2");

        final String json = JsonUtils.generateJson (metadata);
        LOG.info ("Metadata {}", json);
        final Metadata resultMetadata = JsonUtils.generateObjectFromJson (json, Metadata.class);

        Assert.assertNotNull (resultMetadata);
        Assert.assertNotNull (resultMetadata.getMap());
        final String resultMetadataValue1 = resultMetadata.getMap ().get ("metadata key 1");
        Assert.assertEquals ("metadata value 1", resultMetadataValue1);
        final String resultMetadataValue2 = resultMetadata.getMap ().get ("metadata key 2");
        Assert.assertEquals ("metadata value 2", resultMetadataValue2);

    }
}
