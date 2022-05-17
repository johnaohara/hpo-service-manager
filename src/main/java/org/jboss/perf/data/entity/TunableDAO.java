package org.jboss.perf.data.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class TunableDAO extends PanacheEntity {

    public String name;
    public Float value;
    public String value_type;
    public Float lower_bound;
    public Float upper_bound;
    public Float step;
}
