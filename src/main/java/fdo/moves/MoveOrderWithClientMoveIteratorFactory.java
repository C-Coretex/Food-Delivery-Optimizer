package fdo.moves;

import ai.timefold.solver.core.api.score.director.ScoreDirector;
import ai.timefold.solver.core.impl.heuristic.selector.move.factory.MoveIteratorFactory;
import fdo.domain.*;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class MoveOrderWithClientMoveIteratorFactory
        implements MoveIteratorFactory<DeliverySolution, MoveOrderWithClientMove> {

    @Override
    public long getSize(ScoreDirector<DeliverySolution> scoreDirector) {
        return -1;
    }

    @Override
    public Iterator<MoveOrderWithClientMove> createOriginalMoveIterator(
            ScoreDirector<DeliverySolution> scoreDirector) {

        DeliverySolution solution = scoreDirector.getWorkingSolution();
        List<CourierShift> couriers = solution.getCourierShifts();

        Stream<MoveOrderWithClientMove> moves =
                couriers.stream()
                        .flatMap(source ->
                                couriers.stream()
                                        .filter(target -> target != source)
                                        .flatMap(target ->
                                                source.getVisits().stream()
                                                        .filter(v -> v.getType() == Visit.VisitType.RESTAURANT)
                                                        .map(pickup -> {
                                                            Visit delivery =
                                                                    pickup.getOrder().getCustomerVisit();

                                                            if (delivery == null) {
                                                                return null;
                                                            }

                                                            return new MoveOrderWithClientMove(
                                                                    pickup,
                                                                    delivery,
                                                                    source,
                                                                    target
                                                            );
                                                        })
                                        )
                        )
                        .filter(m -> m != null);

        return moves.iterator();
    }

    @Override
    public Iterator<MoveOrderWithClientMove> createRandomMoveIterator(
            ScoreDirector<DeliverySolution> scoreDirector,
            Random random) {

        return createOriginalMoveIterator(scoreDirector);
    }
}
