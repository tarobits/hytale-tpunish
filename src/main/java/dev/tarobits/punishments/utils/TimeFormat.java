package dev.tarobits.punishments.utils;

import com.hypixel.hytale.logger.HytaleLogger;
import dev.tarobits.punishments.TPunish;
import dev.tarobits.punishments.exceptions.UserException;

import java.time.*;
import java.util.Map;

public class TimeFormat {
	private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
	public Integer years = 0;
	public Integer months = 0;
	public Integer days = 0;
	public Integer hours = 0;
	public Integer minutes = 0;

	public TimeFormat() {
	}

	public TimeFormat(TimeFormat prevVal) {
		this.years = prevVal.years;
		this.months = prevVal.months;
		this.days = prevVal.days;
		this.hours = prevVal.hours;
		this.minutes = prevVal.minutes;
	}

	public static TimeFormat fromNowToInstant(Instant instant) {
		return fromInstantToInstant(Instant.now(), instant);
	}

	public static TimeFormat fromInstantToInstant(
			Instant startInst,
			Instant endInst
	) {
		ZoneId zone = ZoneId.systemDefault();

		ZonedDateTime start = startInst.atZone(zone);
		ZonedDateTime end = endInst.atZone(zone);
		return fromZDTToZDT(start, end);
	}

	public static TimeFormat fromZDTToZDT(
			ZonedDateTime startZDT,
			ZonedDateTime endZDT
	) {
		TimeFormat timeFormat = new TimeFormat();
		Period period = Period.between(startZDT.toLocalDate(), endZDT.toLocalDate());
		timeFormat.days = period.getDays();
		timeFormat.months = period.getMonths();
		timeFormat.years = period.getYears();

		startZDT = startZDT.plus(period);
		Duration duration = Duration.between(startZDT, endZDT);
		timeFormat.hours = duration.toHoursPart();
		timeFormat.minutes = duration.toMinutesPart();

		return timeFormat;
	}

	public static TimeFormat fromDurationString(String durationString) {
		TimeFormat timeFormat = new TimeFormat();
		if (durationString.contains("perm")) {
			return timeFormat;
		}
		durationString = durationString.replaceAll(" ", "")
				.replaceAll("min", "n");
		char[] singular = durationString.toCharArray();

		StringBuilder numString = new StringBuilder();

		for (char ch : singular) {
			if (Character.isDigit(ch)) {
				numString.append(ch);
				continue;
			}

			int num = Integer.parseInt(numString.toString());
			numString = new StringBuilder();
			switch (ch) {
				case 'y' -> timeFormat.add(TimeUnit.YEARS, num);
				case 'm' -> timeFormat.add(TimeUnit.MONTHS, num);
				case 'd' -> timeFormat.add(TimeUnit.DAYS, num);
				case 'h' -> timeFormat.add(TimeUnit.HOURS, num);
				case 'n' -> timeFormat.add(TimeUnit.MINUTES, num);
				default -> TPunish.getLogger("TimeFormat")
						.atWarning()
						.log(new UserException("tarobits.punishments.time.error.unkmod").getTextMessage());
			}
		}
		return timeFormat;
	}

	public Boolean isZero() {
		return this.years == 0 && this.months == 0 && this.days == 0 && this.hours == 0 && this.minutes == 0;
	}

	public Boolean isInvalid() {
		return this.years < 0 || this.months < 0 || this.days < 0 || this.hours < 0 || this.minutes < 0;
	}

	public Boolean add(
			TimeUnit unit,
			int num
	) {
		TimeFormat prevValue = new TimeFormat(this);

		unit.add(this, num);
		this.recalculate();
		if (this.isInvalid()) {
			LOGGER.atWarning()
					.log("Generated TimeFormat is invalid. Resetting to previous value!");
			this.resetToPrevious(prevValue);
			return false;
		}
		return true;
	}

	public Boolean sub(
			TimeUnit unit,
			int num
	) {
		TimeFormat prevValue = new TimeFormat(this);

		unit.sub(this, num);
		this.recalculate();
		if (this.isInvalid()) {
			LOGGER.atWarning()
					.log("Generated TimeFormat is invalid. Resetting to previous value!");
			this.resetToPrevious(prevValue);
			return false;
		}
		return true;
	}

	public void resetToPrevious(TimeFormat prevFormat) {
		this.years = prevFormat.years;
		this.months = prevFormat.months;
		this.days = prevFormat.days;
		this.hours = prevFormat.hours;
		this.minutes = prevFormat.minutes;
	}

	public void recalculate() {
		ZoneId zone = ZoneId.systemDefault();

		ZonedDateTime start = Instant.now()
				.atZone(zone);
		ZonedDateTime end = start.plusYears(this.years)
				.plusMonths(this.months)
				.plusDays(this.days)
				.plusHours(this.hours)
				.plusMinutes(this.minutes);

		TimeFormat newFormat = fromZDTToZDT(start, end);
		this.years = newFormat.years;
		this.months = newFormat.months;
		this.days = newFormat.days;
		this.hours = newFormat.hours;
		this.minutes = newFormat.minutes;
	}

	public String toFullDurationString() {
		return this.toFullDurationString(true);
	}

	public String toFullDurationString(Boolean shortForm) {
		return this.toDurationString(true, true, true, true, true, shortForm);
	}

	public String toDateDurationString() {
		return this.toDateDurationString(true);
	}

	public String toDateDurationString(Boolean shortForm) {
		return this.toDurationString(true, true, true, false, false, shortForm);
	}

	public String toDurationString(
			Boolean useYears,
			Boolean useMonths,
			Boolean useDays,
			Boolean useHours,
			Boolean useMinutes,
			Boolean shortForm
	) {
		if (this.isZero()) {
			return "0d";
		}
		Map<String, Map<Boolean, String>> strings = Map.of(
				"y", Map.of(true, "y", false, " years"), "m", Map.of(true, "m", false, " months"), "d",
				Map.of(true, "d", false, " days"), "h", Map.of(true, "h", false, " hours"), "min",
				Map.of(true, "min", false, " minutes")
		);
		StringBuilder durString = new StringBuilder();
		if (useYears && this.years != 0) {
			durString.append(this.years);
			durString.append(strings.get("y")
					                 .get(shortForm));
			durString.append(" ");
		}
		if (useMonths && this.months != 0) {
			durString.append(this.months);
			durString.append(strings.get("m")
					                 .get(shortForm));
			durString.append(" ");
		}
		if (useDays && this.days != 0) {
			durString.append(this.days);
			durString.append(strings.get("d")
					                 .get(shortForm));
			durString.append(" ");
		}
		if (useHours && this.hours != 0) {
			durString.append(this.hours);
			durString.append(strings.get("h")
					                 .get(shortForm));
			durString.append(" ");
		}
		if (useMinutes && this.minutes != 0) {
			durString.append(this.minutes);
			durString.append(strings.get("min")
					                 .get(shortForm));
			durString.append(" ");
		}
		return durString.toString()
				.trim();
	}

	public Instant toInstantFromNow(Instant inst) {
		return inst.atZone(ZoneId.systemDefault())
				.plusYears(this.years)
				.plusMonths(this.months)
				.plusDays(this.days)
				.plusHours(this.hours)
				.plusMinutes(this.minutes)
				.toInstant();
	}

	public enum TimeUnit {
		YEARS {
			void add(
					TimeFormat t,
					int n
			) {
				t.years += n;
			}

			void sub(
					TimeFormat t,
					int n
			) {
				t.years -= n;
			}
		},
		MONTHS {
			void add(
					TimeFormat t,
					int n
			) {
				t.months += n;
			}

			void sub(
					TimeFormat t,
					int n
			) {
				t.months -= n;
			}
		},
		DAYS {
			void add(
					TimeFormat t,
					int n
			) {
				t.days += n;
			}

			void sub(
					TimeFormat t,
					int n
			) {
				t.days -= n;
			}
		},
		HOURS {
			void add(
					TimeFormat t,
					int n
			) {
				t.hours += n;
			}

			void sub(
					TimeFormat t,
					int n
			) {
				t.hours -= n;
			}
		},
		MINUTES {
			void add(
					TimeFormat t,
					int n
			) {
				t.minutes += n;
			}

			void sub(
					TimeFormat t,
					int n
			) {
				t.minutes -= n;
			}
		};

		abstract void add(
				TimeFormat t,
				int n
		);

		abstract void sub(
				TimeFormat t,
				int n
		);
	}
}
