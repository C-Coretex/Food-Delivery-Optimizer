package domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.variable.InverseRelationShadowVariable;
import ai.timefold.solver.core.api.domain.variable.PreviousElementShadowVariable;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import lombok.Getter;

@PlanningEntity
@Getter
public class Visit {
    @JsonIdentityReference(alwaysAsId = true)
    private Location location; //determines the same physical place, e.g., the same CS
    private Long minuteTime;
    private String name;

    //@InverseRelationShadowVariable(sourceVariableName = "visits")
    private CourierShift courier;

    private Order order;
}
