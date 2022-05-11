package org.jboss.perf.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class HorreumDAO extends PanacheEntity {

    public String url;
    public String job;
    public Integer jobID;
    public String auth;
    public String metric;


}
