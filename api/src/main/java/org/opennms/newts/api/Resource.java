package org.opennms.newts.api;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;

import com.google.common.base.Objects;
import com.google.common.base.Optional;


/**
 * A unique resource to associate a group of metrics to. Newts utilizes this structure to index the
 * resources of the samples it has seen, providing a means of search and discovery.
 * 
 * @author eevans
 */
public class Resource {

    private final String m_id;
    private final Optional<Map<String, String>> m_attributes;

    /**
     * Creates a new {@link Resource} instance with the supplied resource ID, default application
     * ID, and an empty set of attributes.
     *
     * @param id
     *            the resource identifier.
     */
    public Resource(String id) {
        this(id, Optional.<Map<String, String>>absent());
    }

    /**
     * Creates a new {@link Resource} with the supplied ID.
     *
     * @param id
     *            resource identifier.
     * @param attributes
     *            attributes to associate with this resource.
     */
    public Resource(String id, Optional<Map<String, String>> attributes) {
        m_id = checkNotNull(id, "id argument");
        m_attributes = checkNotNull(attributes, "attributes argument");
    }

    /**
     * @return the ID of this resource.
     */
    public String getId() {
        return m_id;
    }

    /**
     * @return the set of attributes for this resource.
     */
    public Optional<Map<String, String>> getAttributes() {
        return m_attributes;
    }

    @Override
    public String toString() {
        return String.format("%s[%s]", getClass().getSimpleName(), getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId(), getAttributes());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Resource)) return false;
        return getId().equals(((Resource) o).getId());
    }

}
