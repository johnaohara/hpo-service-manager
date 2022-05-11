package org.jboss.perf.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;
import java.util.Map;

@Entity
public class JenkinsDAO extends PanacheEntity {

    public String job;
    public String job_url;

    @SuppressWarnings("JpaAttributeTypeInspection")
    public Map<String, String> params;
}
