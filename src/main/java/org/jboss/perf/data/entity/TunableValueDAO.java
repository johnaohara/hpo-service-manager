package org.jboss.perf.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class TunableValueDAO extends PanacheEntity {
    public String tunable;
    public Float value;

    public TunableValueDAO() {
    }

    public TunableValueDAO(String tunable, Float value) {
        this.tunable = tunable;
        this.value = value;
    }
}
