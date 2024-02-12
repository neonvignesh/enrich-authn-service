package com.enrich.authn.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

/**
 * CommonDateUtil - Date manipulation and Date converter from Date to String
 * and String to Date
 * 
 * @author Sivaraj
 * @since 09-05-2022
 * @category Utility - Date
 */
public final class CommonDateUtil {

	public static final String DEFAULT_DATE_FORMAT = "dd-MM-yyyy";

	public static final String IPO_DATE_FORMAT = "dd/MM/yyyy";

	public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

	public static final String DEFAULT_DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";

	public static final DateTimeFormatter DEFAULT_TIME_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);

	public static final DateTimeFormatter DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT);

	public static final DateTimeFormatter IPO_DATE_FORMATTER = DateTimeFormatter.ofPattern(IPO_DATE_FORMAT);

	public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER = DateTimeFormatter
			.ofPattern(DEFAULT_DATE_TIME_FORMAT);

	public static LocalDate stringToDate(String dateAsString) {
		if (StringUtils.isEmpty(dateAsString))
			return null;
		return LocalDate.parse(dateAsString, DEFAULT_DATE_FORMATTER);
	}

	public static LocalDate stringToIpoDate(String dateAsString) {
		if (StringUtils.isEmpty(dateAsString))
			return null;
		return LocalDate.parse(dateAsString, IPO_DATE_FORMATTER);
	}

	public static LocalDateTime stringToDateTime(String dateTimeAsString) {
		if (StringUtils.isEmpty(dateTimeAsString))
			return null;
		return LocalDateTime.parse(dateTimeAsString, DEFAULT_DATE_TIME_FORMATTER);
	}

	public static LocalDate stringToDate(String dateTimeAsString, final DateTimeFormatter dateFormatter) {
		if (StringUtils.isEmpty(dateTimeAsString))
			return null;
		return LocalDate.parse(dateTimeAsString, dateFormatter);
	}

	public static String dateToString(LocalDate localDate) {
		if (Objects.isNull(localDate))
			return null;
		return DEFAULT_DATE_FORMATTER.format(localDate);
	}

	public static String dateTimeToString(LocalDateTime localDateTime) {
		if (Objects.isNull(localDateTime))
			return null;
		return DEFAULT_DATE_TIME_FORMATTER.format(localDateTime);
	}

	public static long daysBetween(LocalDate startDate, LocalDate endDate) {
		return ChronoUnit.DAYS.between(endDate, startDate);
	}

	public static boolean isTodayOrAfter(LocalDate now, LocalDate ipoDate) {
		return (now.isAfter(ipoDate) || now.isEqual(ipoDate));
	}

	public static boolean isTodayOrBefore(LocalDate now, LocalDate ipoDate) {
		return (now.isBefore(ipoDate) || now.isEqual(ipoDate));
	}

	public static String getAge(LocalDate localDate) {
		if (Objects.isNull(localDate))
			return null;
		LocalDate today = LocalDate.now();
		Period period = Period.between(localDate, today);
		return period.getYears() + " Years " + period.getMonths() + " Months " + period.getDays() + " Days";
	}
}
