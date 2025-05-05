package kr.ac.dankook.parkingApplication.util;

import kr.ac.dankook.parkingApplication.entity.ParkingLot;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class DateUtil {

    private static final Set<String> HOLIDAYS = Set.of(
            "01-01", "03-01", "05-05", "06-06", "08-15", "10-03", "10-09", "12-25"
    );

    public static boolean isPublicHoliday(LocalDate date) {
        String formattedDate = date.format(DateTimeFormatter.ofPattern("MM-dd"));
        return HOLIDAYS.contains(formattedDate);
    }

    public static boolean getOperatingStatus(ParkingLot parkingLot) {
        LocalDate nowDate = LocalDate.now();
        LocalTime nowTime = LocalTime.now();

        int dayOfWeek = nowDate.getDayOfWeek().getValue();

        String startTimeStr;
        String endTimeStr;

        if (dayOfWeek == 7 || isPublicHoliday(nowDate)) {
            startTimeStr = parkingLot.getHolidayStartTime();
            endTimeStr = parkingLot.getHolidayEndTime();
        } else if (dayOfWeek >= 1 && dayOfWeek <= 5) {
            startTimeStr = parkingLot.getWeekdayStartTime();
            endTimeStr = parkingLot.getWeekdayEndTime();
        } else {
            startTimeStr = parkingLot.getWeekendStartTime();
            endTimeStr = parkingLot.getWeekendEndTime();
        }

        if (startTimeStr.equals("00:00") && endTimeStr.equals("00:00")) {
            return true;
        }

        LocalTime startTime = LocalTime.parse(startTimeStr);
        LocalTime endTime = LocalTime.parse(endTimeStr);

        return !nowTime.isBefore(startTime) && !nowTime.isAfter(endTime);
    }
}
