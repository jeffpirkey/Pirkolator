package com.rws.utility.test;

import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UUIDGenerator {

    private static Logger LOG = LoggerFactory.getLogger (UUIDGenerator.class);

    @Test
    public void generateUUID () {

        String id = UUID.randomUUID ().toString ();
        LOG.info (id);

        StringBuilder bean =
                new StringBuilder (
                        "<bean id=\"Id\" class=\"java.util.UUID\" factory-method=\"fromString\"><constructor-arg value=\"");
        bean.append (id);
        bean.append ("\" /></bean>");
        LOG.info (bean.toString ());
    }
}
