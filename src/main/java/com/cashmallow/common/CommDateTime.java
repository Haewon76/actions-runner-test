package com.cashmallow.common;

import com.cashmallow.api.interfaces.Convert;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CommDateTime {

    private static final Logger logger = LoggerFactory.getLogger(CommDateTime.class);

    public static final SimpleDateFormat fmtYYYYMMDD = new SimpleDateFormat("yyyyMMdd");
    public static final SimpleDateFormat fmtYYYYMMDD2 = new SimpleDateFormat("yyyy-MM-dd");
    public static final SimpleDateFormat fmtYYYYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final SimpleDateFormat fmtYYYYMMDDHHMMSSF1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    public static final SimpleDateFormat fmtYYYYMMDDHHMMSSF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SS");
    public static final SimpleDateFormat fmtYYYYMMDDHHMMSSF3 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static final long LENGTH_OF_DAY = 24 * 60 * 60 * 1000;

    // 기능: 현재 날짜/시간을 응답한다.
    // SimpleDateFormat: http://everlikemorning.tistory.com/entry/SimpleDateFormat-%ED%8C%A8%ED%84%B4%EB%B0%8F-%EC%82%AC%EC%9A%A9%EC%98%88%EC%A0%9C
    public static Timestamp getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        String today = fmtYYYYMMDDHHMMSSF3.format(calendar.getTime());
        Timestamp ts = Timestamp.valueOf(today);
        return ts;
    }

    // 기능: 현재 날짜를 응답한다.
    public static Timestamp getCurrentDate() {
        Calendar calendar = Calendar.getInstance();
        String today = fmtYYYYMMDD2.format(calendar.getTime());
        Timestamp ts = Timestamp.valueOf(today + " 00:00:00");
        return ts;
    }

    // 기능: 입력 Parameter에 대한 날짜/시간을 문자열 형태로 응답한다.
    public static String toString(Timestamp ts) {
        return fmtYYYYMMDDHHMMSS.format(ts);
    }

    // 기능: 입력 Parameter에 대한 날짜/시간을 문자열 형태로 응답한다.
    public static String currentDateTimeToString() {
        Calendar calendar = Calendar.getInstance();
        return fmtYYYYMMDDHHMMSS.format(calendar.getTime());
    }

    public static Timestamp objToTimestamp(Object obj) {
        return strToTimestamp(obj != null ? obj.toString() : null);
    }

    // 기능: 문자열("YYYY-MM-DD" 등)형태를 Timestamp로 변환한다. 
    // 결과: 오류가 없는 경우 변환된 timestamp 값을 응답하며, 그렇지 않은 경우 null을 응답한다. 
    public static Timestamp strToTimestamp(String dateStr) {

        Timestamp result = null;

        if (dateStr != null) {
            SimpleDateFormat sd = null;

            try {
                switch (dateStr.length()) {
                    case 8:
                        sd = fmtYYYYMMDD;
                        break;
                    case 10:
                        sd = fmtYYYYMMDD2;
                        break;
                    case 19:
                        sd = fmtYYYYMMDDHHMMSS;
                        break;
                    case 21:
                        sd = fmtYYYYMMDDHHMMSSF1;
                        break;
                    case 22:
                        sd = fmtYYYYMMDDHHMMSSF2;
                        break;
                    case 23:
                        sd = fmtYYYYMMDDHHMMSSF3;
                        break;
                    // default:   // 1491145200000
                }

                if (sd != null) {
                    sd.parse(dateStr);
                    Calendar calendar = sd.getCalendar();
                    result = new Timestamp(calendar.getTime().getTime());
                } else {
                    Long lTime = Convert.strToLongDef(dateStr, null);

                    if (lTime != null) {
                        result = new Timestamp(lTime);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return result;
    }

    // java 정규 표현식: http://breath91.tistory.com/entry/Java-%EC%A0%95%EA%B7%9C%ED%91%9C%ED%98%84%EC%8B%9D
    // 기능: 입력 parameter(YYYYMMDD)에 대한 만 나이를 응답한다.
    public static int getAge(String birthDate) {
        int result = 0;

        if (StringUtils.isBlank(birthDate)) {
            return result;
        }

        birthDate = birthDate.replaceAll("-", "");

        if (birthDate.length() >= 8) {
            try {
                int birthYear = Convert.strToIntDef(birthDate.substring(0, 4), 0);
                int birthMonth = Convert.strToIntDef(birthDate.substring(4, 6), 0);
                int birthDay = Convert.strToIntDef(birthDate.substring(6, 8), 0);

                if (1900 <= birthYear && birthYear <= 2100 && 1 <= birthMonth && birthMonth <= 12 && 1 <= birthDay && birthDay <= 31) {
                    Calendar current = Calendar.getInstance();
                    int currentYear = current.get(Calendar.YEAR);
                    int currentMonth = current.get(Calendar.MONTH) + 1;
                    int currentDay = current.get(Calendar.DAY_OF_MONTH);

                    result = currentYear - birthYear
                            - ((birthMonth * 100 + birthDay > currentMonth * 100 + currentDay) ? 1 : 0);
                }
            } catch (Exception e) {
                // ex.toString();
            }
        }

        return result;
    }

    // 기능 : timestamp의 날짜 부분을 응답한다. 단, timestamp는 GMT+0 기준이다.
    public static long getDays(Timestamp timestamp, String timeZone) {
        Calendar calendar = Calendar.getInstance();

        long millis = timestamp.getTime();
        calendar.setTimeInMillis(millis);

        //        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (timeZone != null && !timeZone.isEmpty()) {
            calendar.setTimeZone(TimeZone.getTimeZone(timeZone));   // calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
            //            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }

        //        calendar.set(Calendar.HOUR_OF_DAY, 0);
        //        calendar.set(Calendar.MINUTE, 0);
        //        calendar.set(Calendar.SECOND, 0);
        //        calendar.set(Calendar.MILLISECOND, 0);

        millis = calendar.getTimeInMillis();

        // long offset = calendar.getTimeZone().getOffset(millis);
        // millis = millis - ((millis + offset) % LENGTH_OF_DAY);
        long days = millis / LENGTH_OF_DAY;

        return days;
    }

    // 기능 : 오늘 날짜가 지정일 기준 대비 짝수일인지 여부를 음답한다.
    public static String isEvenDay(long days) {
        return days % 2 == 0 ? "Y" : "N";
    }

    public static LocalDateTime localDateTimeToUTCDateTime(String input) {
        try {
            return ZonedDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return LocalDateTime.now();
    }

    public static ZonedDateTime localDateTimeToLocalDateTime(ZonedDateTime zonedDateTime, ZoneId zoneId) {
        return zonedDateTime.withZoneSameInstant(zoneId);
    }

    public static String zonedDateTimeToString(ZonedDateTime zonedDateTime, ZoneId zoneId) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssz");
        return zonedDateTime.withZoneSameInstant(zoneId).format(formatter);
    }

    public static String dateToKSTString() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
        return simpleDateFormat.format(new Date());
    }

    public static Timestamp addTimestamp(Timestamp timestamp, int Days) {

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp.getTime());
        cal.add(Calendar.DATE, Days);
        Timestamp later = new Timestamp(cal.getTime().getTime());

        return later;
    }

    public static LocalDateTime TimestampToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return timestamp.toLocalDateTime();
    }

    public static Date getStartDate(String date) {
        String str = date + " 00:00:00";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        Date result = Date.from(dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
        return result;
    }

    public static Date getEndDate(String date) {
        String str = date + " 23:59:59";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(str, formatter);
        Date result = Date.from(dateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant());
        return result;
    }

    public static String dateToString(Date date) {
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN);
            simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+9:00"));
            return simpleDateFormat.format(date);
        }

        return "";
    }

    public static String dateToStringUTC(Date date) {
        if (date != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREAN);
            return simpleDateFormat.format(date);
        }

        return "";
    }

    public static String localDateToString(LocalDate localDate) {
        if (localDate != null) {
            return localDate.toString();
        }
        return null;
    }

    public static String localDateTimeToString(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            return localDateTime.toString().replace("T", " ");
        }
        return null;
    }

    public static String getTimeFormat(ZoneId zoneid, Locale locale) {

        ZonedDateTime utcTime = ZonedDateTime.now(ZoneId.of("UTC")).withHour(0).withMinute(0).withSecond(0);
        ZonedDateTime countryTime = utcTime.withZoneSameInstant(zoneid);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a h", locale);

        return countryTime.format(formatter);
    }
}
