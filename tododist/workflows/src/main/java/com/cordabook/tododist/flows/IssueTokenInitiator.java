package com.cordabook.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import com.cordabook.tododist.contracts.Command;
import com.cordabook.tododist.states.TokenState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IssueTokenInitiator extends FlowLogic<Void> {

    private String owner;

    public IssueTokenInitiator(String owner) {
        this.owner = owner;
    }


    // How to get the log in here?

    @Suspendable
    @Override
    public Void call() throws FlowException {

        System.out.println("Principal: " + getStateMachine().getContext().getOrigin().principal().getName());
        System.out.println("StateMachine ID: " + getStateMachine().getId());
        System.out.println("Strand: " + Strand.currentStrand().getId());
        System.out.println("Strand: " + Strand.currentStrand().getName());
        System.out.println("Strand: " + Strand.currentStrand().getState().name());
        System.out.println("Strand: " + Strand.currentStrand().getBlocker());
        System.out.println("Strand: " + Strand.currentStrand().isFiber());
        System.out.println("Strand: " + Strand.currentStrand().getPriority());

        int n = 1;
        step(n++);
        final ServiceHub sb = getServiceHub();
        step(n++);
        System.out.println("Looking up party " + owner);
        step(n++);
        final Set<Party> parties = sb.getIdentityService().partiesFromName(owner, true);
        step(n++);
        Party assignedToParty = parties.iterator().next();
        step(n++);
        System.out.println("Found party");
        TokenState ts = new TokenState(assignedToParty,getOurIdentity());
        step(n++);
        List<PublicKey> signers = ImmutableList.of(getOurIdentity().getOwningKey(),assignedToParty.getOwningKey());
        step(n++);
        Party notary = sb.getNetworkMapCache().getNotaryIdentities().get(0);
        step(n++);
        TransactionBuilder tb = new TransactionBuilder(notary)
            .addOutputState(ts) //, TemplateContract.ID)
            .addCommand(new Command.CreateToDoCommand(),getOurIdentity().getOwningKey(),assignedToParty.getOwningKey());
        //tb.verify(sb);
        step(n++);
        SignedTransaction ptx = getServiceHub().signInitialTransaction(tb);
        step(n++);
        FlowSession assignedToSession = initiateFlow(assignedToParty);
        System.out.println("Flo session counterparty: " + assignedToSession.getCounterparty().getName().getCommonName());
        Destination destination = assignedToSession.getDestination();
        System.out.println("Destination: " + destination);
//        System.out.println("Counterparty flow app name: " + assignedToSession.getCounterpartyFlowInfo().getAppName());
//        System.out.println("Flow version: " + assignedToSession.getCounterpartyFlowInfo().getFlowVersion());

        step(n++);
        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, ImmutableSet.of(assignedToSession)));
        step(n++);
        SignedTransaction signedTransaction = subFlow(new FinalityFlow(stx, Arrays.asList(assignedToSession)));
        step(n++);
//        System.out.println("Token issued: " + signedTransaction.getMissingSigners().size());
//        System.out.println("bytes: " + signedTransaction.getTxBits());


        return null;
    }

    private void step(int n) {
        System.out.println("** " + n);
    }
}
