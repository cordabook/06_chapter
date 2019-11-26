package com.cordabook.tododist.contracts;

import net.corda.core.contracts.CommandData;

public interface Command extends CommandData {
    class CreateToDoCommand implements CommandData {}
    class AssignToDoCommand implements CommandData {}
    class AttachToDoCommand implements CommandData {}

}
