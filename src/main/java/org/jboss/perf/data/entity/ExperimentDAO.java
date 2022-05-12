package org.jboss.perf.data.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import io.smallrye.common.constraint.NotNull;

import javax.persistence.*;
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
    public Integer currentTrial = 0;

    @OneToOne(cascade=CascadeType.ALL)
    public HorreumDAO horreum;

    @OneToOne(cascade=CascadeType.ALL)
    public JenkinsDAO jenkins;


    public enum State {
        NEW {
            @Override
            public State nextState() {
                return READY;
            }
        },
        READY {
            @Override
            public State nextState() {
                return RUNNING;
            }
        },
        RUNNING {
            @Override
            public State nextState() {
                return FINISHED;
            }
        },
        FAILURE {
            @Override
            public State nextState() {
                return null;
            }
        },
        FINISHED {
            @Override
            public State nextState() {
                return null;
            }
        };

        public abstract State nextState();
        }

    public static ExperimentDAO findByTestId(Integer testId) {
        return find("test_id", testId).firstResult();
    }

}
