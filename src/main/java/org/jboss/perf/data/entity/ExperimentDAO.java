package org.jboss.perf.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.smallrye.common.constraint.NotNull;

import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "experiment")
public class ExperimentDAO extends PanacheEntity {

    @NotNull
    public String name;
    @NotNull
    public Integer test_id;
    public State state;
    public Integer parrallelism = 1;
    public Integer total_trials = 100;
    public String direction;
    public String hpo_algo_impl;
    public String objective_function;
    @OneToMany(mappedBy = "experimentID")
    public Set<Tunable> tunables;
    public String value_type;


    public enum State {
        NEW,
        READY,
        RUNNING,
        FAILURE,
        FINISHED
    }

    public static ExperimentDAO findByTestId(Integer testId) {
        return find("name", testId).firstResult();
    }

}
