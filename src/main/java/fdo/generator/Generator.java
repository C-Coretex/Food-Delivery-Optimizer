package fdo.generator;

import fdo.domain.DeliverySolution;
import fdo.domain.Order;
import fdo.domain.Restaurant;
import fdo.domain.Visit;
import fdo.domain.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Generator {
    public final class VisitGenerator {

        private VisitGenerator() {}

        public static List<Visit> generateAll(DeliverySolution solution) {

            List<Visit> visits = new ArrayList<>();

            for (Order order : solution.getOrders()) {


                Visit delivery = new Visit();
                //delivery.setId("DELIVERY-" + order.getId());
                delivery.setVisitType(Visit.VisitType.CUSTOMER);
                delivery.setOrder(order);
                delivery.setLocation(order.getDeliveryLocation());
                visits.add(delivery);


                for (Restaurant restaurant : solution.getRestaurantList().stream().filter(r -> Objects.equals(order.getChainId(), r.getChainId())).toList()) {
                    Visit pickup = new Visit();
                    //pickup.setId("PICKUP-" + order.getId() + "-" + restaurant.getId());
                    pickup.setVisitType(Visit.VisitType.RESTAURANT);
                    pickup.setOrder(order);
                    pickup.setRestaurant(restaurant);
                    pickup.setLocation(restaurant.getLocation());

                    visits.add(pickup);
                }
            }

            return visits;
        }
    }
}