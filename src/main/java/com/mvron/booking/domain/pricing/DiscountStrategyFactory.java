package com.mvron.booking.domain.pricing;

import java.util.List;

public class DiscountStrategyFactory {

    private DiscountStrategyFactory() {};

    public static DiscountStrategy create(boolean isMember, int hours) {
        boolean isLongBooking = hours >= LongBookingDiscount.MIN_HOURS;

        if(isMember && isLongBooking) {
            return new CompositeDiscount(List.of(
                new LongBookingDiscount(),
                new MemberDiscount()
            ));
        }
        if(isMember) {
            return new MemberDiscount();
        }
        if(isLongBooking) {
            return new LongBookingDiscount();
        }

        return new NoDiscount();
    }
    
}
