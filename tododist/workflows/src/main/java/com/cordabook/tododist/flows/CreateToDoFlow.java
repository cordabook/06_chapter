package com.cordabook.tododist.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.cordabook.tododist.contracts.Command;
import com.cordabook.tododist.states.todo.ToDoState;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.TimeWindow;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.ServiceHub;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.time.Instant;
import java.util.Collections;


// flow start CreateToDoFlow task: "Get some cheese"
// flow start AssignToDoInitiator linearId: 3d3d3a7b-35bf-4b1d-83d7-9f10a9c98657 , assignedTo: PartyA

// ******************
// * Initiator flow *
// ******************
@StartableByRPC
public class CreateToDoFlow extends FlowLogic<Void> {
    private Step STEP_1 = new Step("One");
    private Step STEP_2 = new Step("Two");
    private final ProgressTracker progressTracker = new ProgressTracker(STEP_1,STEP_2);
    private final String taskDescription;

    /*
        1. Create task
        2. Assign task
        3. Reward points
        4. Reminder alerts
        5. Task completion timewindows
        6. Add attachment


     */

    public CreateToDoFlow(String task) {
        this.taskDescription = task;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }



    @Suspendable
    @Override
    public Void call() throws FlowException {
        ServiceHub serviceHub = getServiceHub();
        Party me = getOurIdentity();
        ToDoState ts = new ToDoState(me,me,taskDescription);
        System.out.println("Linear ID of state is " + ts.getLinearId());
        Party notary = serviceHub.getNetworkMapCache().getNotaryIdentities().get(0);
        progressTracker.setCurrentStep(STEP_1);
        // Transaction builder is not immutable
        TransactionBuilder tb = new TransactionBuilder(notary);
        progressTracker.setCurrentStep(STEP_2);
        System.out.println("Step label " + progressTracker.getCurrentStep().getLabel());
        // Command has two parameters - but this compiled when add command had only one param!
        tb = tb.addCommand(new Command.CreateToDoCommand(), me.getOwningKey()); //, me.getOwningKey()); // at least one is required
        System.out.println("1");
        tb = tb.addOutputState(ts);
        //CommandWithParties<Command.CreateToDoCommand> cmd =
        TimeWindow tw = TimeWindow.between(Instant.now(),Instant.now().plusSeconds(300));
        tb = tb.setTimeWindow(tw);
        tb = tb.withItems(notary,ts,tw);
        System.out.println("1");
        SignedTransaction stx = getServiceHub()
                .signInitialTransaction(
                        new TransactionBuilder().withItems(
                                notary,
                                new ToDoState(me,me,taskDescription),
                                TimeWindow.between(Instant.now(),Instant.now().plusSeconds(300)),
                                new CommandWithParties<>(ImmutableList.of(me.getOwningKey()),ImmutableList.of(me),new Command.CreateToDoCommand())));
        subFlow(new FinalityFlow(
                getServiceHub()
                        .signInitialTransaction(
                                new TransactionBuilder().withItems(
                                        notary,
                                        new ToDoState(me,me,taskDescription),
                                        TimeWindow.between(Instant.now(),Instant.now().plusSeconds(300)),
                                        new CommandWithParties<>(ImmutableList.of(
                                                getOurIdentity().getOwningKey()),
                                                ImmutableList.of(getOurIdentity()),
                                                new Command.CreateToDoCommand()))),
                Collections.<FlowSession>emptySet())
        );
        System.out.println("1");
        //subFlow(new FinalityFlow(stx));
        subFlow(new FinalityFlow(stx, Collections.<FlowSession>emptySet()));
        System.out.println("1");

//        try {
//            ResultSet rs = serviceHub.jdbcSession().prepareStatement("SELECT v.transaction_id, v.output_index FROM vault_states v WHERE v.state_status = 0").executeQuery();
//            while(rs.next()) {
//                System.out.println(rs.getString(1)); // cant be 0
//            }
//
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        return null;
    }
}

