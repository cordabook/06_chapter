package com.cordabook.tododist.flows;

import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.StartableByRPC;

@StartableByRPC
public class VerifyAttachment extends FlowLogic<Boolean> {
    private String hash;

    public VerifyAttachment(String hash) {
        this.hash = hash;
    }
    @Override
    public Boolean call() throws FlowException {
        SecureHash secureHash = SecureHash.parse(hash);
        Boolean doesExist = getServiceHub().getAttachments().hasAttachment(secureHash);
        System.out.println("Does exist: " + doesExist);
        return doesExist;
    }
}
