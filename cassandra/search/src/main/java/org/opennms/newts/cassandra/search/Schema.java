package org.opennms.newts.cassandra.search;

import java.io.InputStream;

public class Schema implements  org.opennms.newts.cassandra.Schema{

    @Override
    public InputStream getInputStream() {
        return getClass().getResourceAsStream("/search_schema.cql");
    }

}
