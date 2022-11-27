package configuration;

import io.quarkus.runtime.annotations.StaticInitSafe;
import io.smallrye.config.ConfigMapping;

import javax.enterprise.context.ApplicationScoped;

@ConfigMapping(prefix = "local")
@ApplicationScoped
@StaticInitSafe
public interface AppConfig {

    MLLP mllp();
    HTTP http();

    interface MLLP {
        String hostname();
        String port();
    }

    interface HTTP {
        String hostname();
        String port();
    }
}
