package com.rebuild.backend.utils.database_utils;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RLSStatementInspector implements StatementInspector {

    @Override
    public String inspect(String s) {
        UUID id = UserContext.get();

        //Returning s or returning null here has the exact same effect.
        if (id == null) {
            return s;
        }
        // SET current_user_id TO 'id'; s;
        return "SET current_user_id TO '" + id + "'; " + s;
    }
}
