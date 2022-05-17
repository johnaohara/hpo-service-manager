package org.jboss.perf.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "trial_result")
public class TrialResultDAO extends PanacheEntity {

    public Float value;
    @OneToMany(cascade = CascadeType.ALL)
    public List<TunableValueDAO> tunables = new ArrayList<>();

}
