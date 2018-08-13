package pl.raaadziu.coinsservice;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
class Configuration {

    private String sqlString;

    private static Logger log = LoggerFactory.getLogger("Configuration");

    String getSqlString()
    {
        return sqlString;
    }

    Configuration()
    {

        sqlString = System.getenv("SQL3");
        if (sqlString == null)
        {
            log.error("Unable to load SQL variable");
            throw new NullPointerException();
        }
        log.info("Environment variables loaded ");
    }
}