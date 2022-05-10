package org.jboss.perf.data.entity;


import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.Entity;

@Entity
public class Tunable extends PanacheEntity {

    public Long experimentID;
}
