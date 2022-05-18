package org.jboss.perf.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import java.util.HashMap;

@Entity
public class QDupDAO extends PanacheEntity {

    public String  targetHost;
    public String  username;
    public String  scriptUrl;

    @SuppressWarnings("JpaAttributeTypeInspection")
    public HashMap<String, String> params;
}
