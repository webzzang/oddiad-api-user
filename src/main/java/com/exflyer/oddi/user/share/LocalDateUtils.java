package com.exflyer.oddi.user.share;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalDateUtils {

    private static String timeZone = "Asia/Seoul";

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${timezone}")
    private void setTimeZone(String timeZone){
    this.timeZone = timeZone;
    }

    public static LocalDateTime krNow(){
    return LocalDateTime.now(ZoneId.of(timeZone));
    }

    public static String krNowByFormatter(String formatter){
    return LocalDateTime.now(ZoneId.of(timeZone)).format(DateTimeFormatter.ofPattern(formatter));
    }

    public static LocalDate krNowLocalDate() {
    return LocalDate.now(ZoneId.of(timeZone));
    }

    public static String addkrNow(Integer day){
    return LocalDateUtils.krNow().plusDays(day).format(formatter);
    }

    public static String dateConvertFormatter(){
        return LocalDateUtils.krNow().format(formatter);
    }

}
