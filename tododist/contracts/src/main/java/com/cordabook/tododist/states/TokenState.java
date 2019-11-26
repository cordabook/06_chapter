package com.cordabook.tododist.states;

import com.cordabook.tododist.contracts.TemplateContract;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.flows.FlowLogicRef;
import net.corda.core.flows.FlowLogicRefFactory;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.List;

// *********
// * State *
// *********
/*

Belongstocontract is required. if its missing, flows seem to stop after transactionbuilder, probably around addstate

 */
@BelongsToContract(TemplateContract.class)
public class TokenState implements SchedulableState {

    private final Party owner;
    private final Party issuer;
    private final int amount = 5;

    public int getAmount() {
        return amount;
    }

    public TokenState(Party owner, Party issuer) {
        this.owner = owner;
        this.issuer = issuer;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(owner,issuer);
    }

    public Party getOwner() {
        return owner;
    }

    public Party getIssuer() {
        return issuer;
    }

    @Nullable
    @Override
    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {
        System.out.println("fired");
        FlowLogicRef flowLogicRef = flowLogicRefFactory.create("com.cordabook.tododist.flows.AlarmFlow", thisStateRef);
        return new ScheduledActivity(flowLogicRef, Instant.now().plusSeconds(20));
    }
}


