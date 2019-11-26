package com.cordabook.tododist.states;

import com.cordabook.tododist.contracts.TemplateContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(TemplateContract.class)
public class TemplateState implements ContractState {

    public TemplateState() {

    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList();
    }
}