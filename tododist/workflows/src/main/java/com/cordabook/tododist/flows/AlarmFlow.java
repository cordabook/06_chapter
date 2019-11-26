package com.cordabook.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.contracts.StateRef;
import net.corda.core.flows.*;
import net.corda.core.node.ServiceHub;

@StartableByRPC
@InitiatingFlow
@SchedulableFlow
public class AlarmFlow extends FlowLogic<Void> {

    private StateRef thisRef;
    static {
        System.out.println("AlarmFlow loaded");
    }

    public AlarmFlow(StateRef stateRef) {
        this.thisRef = stateRef;
        System.out.println("Constructor fired");
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        flowStackSnapshot();
        System.out.println("Alarm fired");
        ServiceHub sb = getServiceHub();
        sb.getVaultService().addNoteToTransaction(thisRef.getTxhash(),"Remember to do it!s");

        return null;
    }
}

