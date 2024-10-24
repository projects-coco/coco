package com.coco.presentation.schedule

import org.quartz.CronScheduleBuilder
import org.quartz.ScheduleBuilder

fun intervalNSecondsSchedule(n: Int): ScheduleBuilder<*> = CronScheduleBuilder.cronSchedule("*/$n * * * * ?")

fun interval15SecondsSchedule(): ScheduleBuilder<*> = intervalNSecondsSchedule(15)

fun interval30SecondsSchedule(): ScheduleBuilder<*> = intervalNSecondsSchedule(30)

fun intervalNMinutesSchedule(n: Int): ScheduleBuilder<*> = CronScheduleBuilder.cronSchedule("0 */$n * * * ?")

fun everyMinuteSchedule(): ScheduleBuilder<*> = intervalNMinutesSchedule(1)

fun interval15MinutesSchedule(): ScheduleBuilder<*> = intervalNMinutesSchedule(15)

fun interval30MinutesSchedule(): ScheduleBuilder<*> = intervalNMinutesSchedule(30)

fun intervalNHoursSchedule(n: Int): ScheduleBuilder<*> = CronScheduleBuilder.cronSchedule("0 0 */$n * * ?")

fun everyHourSchedule(): ScheduleBuilder<*> = intervalNHoursSchedule(1)

fun interval2HoursSchedule(): ScheduleBuilder<*> = intervalNHoursSchedule(2)

fun interval3HoursSchedule(): ScheduleBuilder<*> = intervalNHoursSchedule(3)

fun everyMorningSchedule(): ScheduleBuilder<*> = CronScheduleBuilder.cronSchedule("0 0 9 * * ?")

fun everyEveningSchedule(): ScheduleBuilder<*> = CronScheduleBuilder.cronSchedule("0 0 18 * * ?")

fun everyDayScheduleWithHour(hour: Int): ScheduleBuilder<*> = CronScheduleBuilder.cronSchedule("0 0 $hour * * ?")

fun everyDayScheduleWithHour(
    hour: Int,
    dayInterval: Int,
): ScheduleBuilder<*> = CronScheduleBuilder.cronSchedule("0 0 $hour */$dayInterval * ?")
