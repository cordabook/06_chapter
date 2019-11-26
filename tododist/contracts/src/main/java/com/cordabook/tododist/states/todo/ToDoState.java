package com.cordabook.tododist.states.todo;

import com.cordabook.tododist.contracts.ToDoContract;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.flows.FlowLogicRef;
import net.corda.core.flows.FlowLogicRefFactory;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.serialization.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(ToDoContract.class)
public class ToDoState implements LinearState,  SchedulableState {

    private final Party assignedBy;
    private final Party assignedTo;
    private final String taskDescription;
    private final UniqueIdentifier linearId;

    public Party getAssignedBy() {
        return assignedBy;
    }

    public Party getAssignedTo() {
        return assignedTo;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public ToDoState(Party assignedBy, Party assignedTo, String taskDescription) {
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.linearId = new UniqueIdentifier();
    }

    @ConstructorForDeserialization
    public ToDoState(Party assignedBy, Party assignedTo, String taskDescription, UniqueIdentifier linearId) {
        this.assignedBy = assignedBy;
        this.assignedTo = assignedTo;
        this.taskDescription = taskDescription;
        this.linearId = linearId;
    }

    public ToDoState assign(Party assignedTo) {
        return new ToDoState(assignedBy,assignedTo,taskDescription,linearId);
    }


    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(assignedBy, assignedTo);
    }

    @Override
    public UniqueIdentifier getLinearId() {
        System.out.println(linearId);
        return linearId;
    }

//    @NotNull
//    @Override
//    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
//        if (schema instanceof ToDoSchemaV1) {
//            return new ToDoSchemaV1.ToDoModel(taskDescription, linearId.getId());
//        } else if (schema instanceof ToDoSchemaV2) {
//            return new ToDoSchemaV2.ToDoModel(taskDescription, linearId.getId(),assignedTo);
//        } else
//            throw new IllegalArgumentException("No supported schema found");
//
//    }
//
//    @NotNull
//    @Override
//    public Iterable<MappedSchema> supportedSchemas() {
//        System.out.println("supported schema called");
//        return ImmutableList.of(new ToDoSchemaV1(), new ToDoSchemaV2());
//    }

    @Nullable
    @Override
    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {
        System.out.println("fired");
        FlowLogicRef flowLogicRef = flowLogicRefFactory.create("com.cordabook.tododist.flows.AlarmFlow", thisStateRef);
        return new ScheduledActivity(flowLogicRef,Instant.now().plusSeconds(20));
    }

//    @Nullable
//    @Override
//    public ScheduledActivity nextScheduledActivity(@NotNull StateRef thisStateRef, @NotNull FlowLogicRefFactory flowLogicRefFactory) {
//        System.out.println("next act called");
//        Instant requestTime = Instant.now().plusSeconds(10);
//        System.out.println("Tx hash: " + thisStateRef.getTxhash().toString());
//        // what if alarmflow is moved back to workflows package?s
//        //FlowLogicRef flowLogicRef = flowLogicRefFactory.create(AlarmFlow.class);
//        FlowLogicRef flowLogicRef = flowLogicRefFactory.create("com.cordabook.tododist.flows.AlarmFlow",thisStateRef);
//        ScheduledActivity scheduledActivity = new ScheduledActivity(flowLogicRef,requestTime);
//        return scheduledActivity;
//    }


}


