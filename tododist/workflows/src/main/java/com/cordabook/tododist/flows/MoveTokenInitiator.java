package com.cordabook.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.cordabook.tododist.contracts.Command;
import com.cordabook.tododist.states.todo.ToDoState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;


// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class MoveTokenInitiator extends FlowLogic<Void> {
    private String linearId;
    private String assignedTo;

/*
    1. Create tasks
    2. Assign tasks
    3. Create a reminder
    4. Add notes to a task
    5. Observer node
    6

 */

    public MoveTokenInitiator(String linearId, String assignedTo) {
        this.linearId = linearId;
        this.assignedTo = assignedTo;
    }

    // How to get the log in here?

    @Suspendable
    @Override
    public Void call() throws FlowException {
        final ServiceHub sb = getServiceHub();
        final QueryCriteria q = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(UUID.fromString(linearId)));
        final Vault.Page<ToDoState> taskStatePage = sb.getVaultService().queryBy(ToDoState.class, q);
        final List<StateAndRef<ToDoState>> states = taskStatePage.getStates();
        final StateAndRef<ToDoState> sar = states.get(0);
        final ToDoState toDoState = sar.getState().getData();

        System.out.println(toDoState.getTaskDescription());

        for (StateAndRef<ToDoState> s : states) {
            System.out.println(s.getRef());
            ToDoState ts = s.getState().getData();
            System.out.println(ts.getTaskDescription());
            System.out.println("Assigned to: " + ts.getAssignedTo().getName().getOrganisation());
        }

        System.out.println("Looking up party " + assignedTo);
        final Set<Party> parties = sb.getIdentityService().partiesFromName(assignedTo, true);
        Party assignedToParty = parties.iterator().next();
        System.out.println("Found party");

        // getCommonName returned null.. why?
        System.out.println(assignedToParty.getName().getOrganisation());
        System.out.println("Creating new task");
        ToDoState newToDoState = toDoState.assign(assignedToParty);

        //ToDoState withNewAssignedTo = new ToDoState(toDoState.getAssignedBy(),assignedToParty, toDoState.getTaskDescription());

        // need receivers agreement to the task
        System.out.println("Looking up notary");
        Party notary = sb.getNetworkMapCache().getNotaryIdentities().get(0);

        System.out.println("Building tb");
        PublicKey myKey = getOurIdentity().getOwningKey();
        TransactionBuilder tb = new TransactionBuilder(notary)
                .addInputState(sar)
                .addOutputState(newToDoState)
                .addCommand(new Command.CreateToDoCommand(),myKey);
        System.out.println("1");
        // Command has two parameters - but this compiled when add command had only one param!
//        tb = tb.addCommand(new TaskContract.Commands.Assign(),getOurIdentity().getOwningKey());
//        System.out.println("1");
//        tb = tb.addInputState(sar);
//        System.out.println("1");
//        tb = tb.addOutputState(newToDoState);
//        System.out.println("1");
        SignedTransaction ptx = getServiceHub().signInitialTransaction(tb);

        System.out.println("1");

        // empty collection throws java.lang.IllegalArgumentException: Flow sessions were not provided for the following transaction participants: - if all parties not in finality
        FlowSession assignedToSession = initiateFlow(assignedToParty);

        // See: https://stackoverflow.com/questions/52511609/corda-contract-verification-called-during-collectsignatureflow-and-finalityflow
        /*
        CollectSignaturesFlow, called by the node gathering the signatures, calls verify. SignTransactionFlow, the responder flow called by the nodes adding their signatures, also calls verify before signing.

FinalityFlow calls verify. NotaryServiceFlow, the flow run by the notary in response to FinalityFlow, should call verify if the notary is validating (in fact, this is the definition of a validating notary). And finally, ReceiveTransactionFlow, the flow run by the transaction's participants in response to FinalityFlow, calls verify before storing the transaction.
         */
        SignedTransaction stx = subFlow(new CollectSignaturesFlow(ptx, ImmutableSet.of(assignedToSession)));
        // subFlow(new FinalityFlow(stx, Collections.<FlowSession>emptySet()));

        subFlow(new FinalityFlow(stx, Arrays.asList(assignedToSession)));

        /*

            If there is now responder flow on the other side an error will be thrown

            net.corda.core.flows.UnexpectedFlowEndException: O=PartyB, L=New York, C=US has finished prematurely and we're trying to send them the finalised transaction. Did they forget to call ReceiveFinalityFlow? (com.template.flows.AssignTaskFlow is not registered)
         */

        return null;
    }
}
