package com.cordabook.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import co.paralleluniverse.strands.Strand;
import net.corda.core.flows.*;
import net.corda.core.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;


@InitiatedBy(IssueTokenInitiator.class)
public class IssueTokenResponder extends FlowLogic<SignedTransaction> {
    private FlowSession counterpartySession;

    public IssueTokenResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {

        System.out.println("responder called");

        System.out.println("Principal: " + getStateMachine().getContext().getOrigin().principal().getName());
        System.out.println("StateMachine ID: " + getStateMachine().getId()); // stored in node_transaction
        System.out.println("Strand: " + Strand.currentStrand().getId());
        System.out.println("Strand: " + Strand.currentStrand().getName());
        System.out.println("Strand: " + Strand.currentStrand().getState().name());
        System.out.println("Strand: " + Strand.currentStrand().getBlocker());
        System.out.println("Strand: " + Strand.currentStrand().isFiber());
        System.out.println("Strand: " + Strand.currentStrand().getPriority());


        System.out.println("Flo session counterparty: " + counterpartySession.getCounterparty().getName().getCommonName());
        Destination destination = counterpartySession.getDestination();
//        System.out.println("Destination: " + destination);
//        System.out.println("Counterparty flow app name: " + counterpartySession.getCounterpartyFlowInfo().getAppName());
//        System.out.println("Flow version: " + counterpartySession.getCounterpartyFlowInfo().getFlowVersion());

        final SignTransactionFlow signTransactionFlow = new SignTransactionFlow(counterpartySession) {
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) throws FlowException { // when is this called? doesnt seem to be called
                System.out.println("check!!");
            }
        };
        SignedTransaction stx = subFlow(signTransactionFlow);
        return subFlow(new ReceiveFinalityFlow(counterpartySession, stx.getId()));
    }

}