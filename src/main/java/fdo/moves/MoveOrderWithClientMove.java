package fdo.moves;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.heuristic.move.Move;
import fdo.domain.CourierShift;
import fdo.domain.DeliverySolution;
import fdo.domain.Visit;

import java.util.Collection;
import java.util.List;

public class MoveOrderWithClientMove implements Move<DeliverySolution> {

    private final Visit orderVisit;
    private final Visit clientVisit;
    private final CourierShift source;
    private final CourierShift target;

    public MoveOrderWithClientMove(
            Visit orderVisit,
            Visit clientVisit,
            CourierShift source,
            CourierShift target
    ) {
        this.orderVisit = orderVisit;
        this.clientVisit = clientVisit;
        this.source = source;
        this.target = target;
    }

    @Override
    public boolean isMoveDoable(ScoreDirector scoreDirector) {
        return source != target;
    }

    @Override
    public void doMoveOnly(ScoreDirector scoreDirector) {

        scoreDirector.beforeListVariableChanged(source, "visits", 0, source.getVisits().size());
        source.getVisits().remove(orderVisit);
        source.getVisits().remove(clientVisit);
        scoreDirector.afterListVariableChanged(source, "visits", 0, source.getVisits().size());

        scoreDirector.beforeListVariableChanged(target, "visits", 0, target.getVisits().size());
        target.getVisits().add(orderVisit);
        target.getVisits().add(clientVisit);
        scoreDirector.afterListVariableChanged(target, "visits", 0, target.getVisits().size());
    }

    @Override
    public Move rebase(ScoreDirector destinationScoreDirector) {
        return new MoveOrderWithClientMove(
                (Visit) destinationScoreDirector.lookUpWorkingObject(orderVisit),
                (Visit) destinationScoreDirector.lookUpWorkingObject(clientVisit),
                (CourierShift) destinationScoreDirector.lookUpWorkingObject(source),
                (CourierShift) destinationScoreDirector.lookUpWorkingObject(target)
        );
    }


    @Override
    public Collection<?> getPlanningEntities() {
        return List.of(orderVisit, clientVisit);
    }

    @Override
    public Collection<?> getPlanningValues() {
        return List.of(source, target);
    }
}
