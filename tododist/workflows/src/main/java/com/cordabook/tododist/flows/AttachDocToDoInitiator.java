package com.cordabook.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.cordabook.tododist.contracts.Command;
import com.cordabook.tododist.states.todo.ToDoState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.crypto.SecureHash;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;
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
public class AttachDocToDoInitiator extends FlowLogic<Void> {
    private String linearId;
    private String fileURL;
    private String fileName;

    public AttachDocToDoInitiator(String linearId, String fileURL, String fileName) {
        this.linearId = linearId;
        this.fileURL = fileURL;
        this.fileName = fileName;
    }

    // How to get the log in here?

    @Suspendable
    @Override
    public Void call() throws FlowException {

        // http://www.java2s.com/Code/Jar/s/Downloadsamplejar.htm

        String url= "https://www.dropbox.com/s/fyt0f3znxh2gz6u/Gemini%20Pitch%20v3.pdf?dl=0";
        BufferedInputStream in = null;
        SecureHash secureHash = null;
        try {
            in = new BufferedInputStream(new URL(fileURL).openStream());
            InputStream inputStream = new URL(fileURL).openStream();
            secureHash = getServiceHub().getAttachments().importAttachment(inputStream, "", fileName);
            System.out.println(secureHash.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


        final ServiceHub sb = getServiceHub();
        final QueryCriteria q = new QueryCriteria.LinearStateQueryCriteria(null, ImmutableList.of(UUID.fromString(linearId)));
        final Vault.Page<ToDoState> taskStatePage = sb.getVaultService().queryBy(ToDoState.class, q);
        final List<StateAndRef<ToDoState>> states = taskStatePage.getStates();
        final StateAndRef<ToDoState> sar = states.get(0);
        final ToDoState toDoState = sar.getState().getData();

        Party notary = sb.getNetworkMapCache().getNotaryIdentities().get(0);

        System.out.println("Building tb");
        PublicKey myKey = getOurIdentity().getOwningKey();
        PublicKey counterPartyKey = toDoState.getAssignedTo().getOwningKey();
        TransactionBuilder tb = new TransactionBuilder(notary)
                .addInputState(sar)
                .addOutputState(toDoState)
                .addCommand(new Command.AttachToDoCommand(),myKey,counterPartyKey)
                .addAttachment(secureHash);

        SignedTransaction ptx = getServiceHub().signInitialTransaction(tb);
        FlowSession assignedToSession = initiateFlow(toDoState.getAssignedTo());

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
