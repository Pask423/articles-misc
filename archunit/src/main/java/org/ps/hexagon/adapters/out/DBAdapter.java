package org.ps.hexagon.adapters.out;

import org.ps.hexagon.application.ports.out.DBPort;
import org.ps.hexagon.infrastructure.DBSetup;

public class DBAdapter implements DBPort {

    private final DBSetup dbSetup;

    public DBAdapter(DBSetup dbSetup) {
        this.dbSetup = dbSetup;
    }
}
