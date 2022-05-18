package org.jboss.perf.services.backend.runtime;

import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Qualifier;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Qualifier
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RuntimeEnvironment {
    String name() ;

    class RuntimeEnvironmentQualifier extends AnnotationLiteral<RuntimeEnvironment> implements RuntimeEnvironment {

        private final String name;

        public RuntimeEnvironmentQualifier(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }
    }

}
