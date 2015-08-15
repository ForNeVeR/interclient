/*
 * The contents of this file are subject to the Interbase Public
 * License Version 1.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy
 * of the License at http://www.Inprise.com/IPL.html
 *
 * Software distributed under the License is distributed on an
 * "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * The Original Code was created by Inprise Corporation
 * and its predecessors. Portions created by Inprise Corporation are
 * Copyright (C) Inprise Corporation.
 * All Rights Reserved.
 * Contributor(s): ______________________________________.
 */
package interbase.interclient;

/** 
 * InterBase does not support XOPEN/SQL Date and Time.
 * An InterBase Date is an XOPEN/SQL Timestamp.
 * <p>
 * The granularity of sub-second timestamp precision will vary
 * between databases, and stored values will get rounded to the databases
 * internal precision.
 *
 * @author Paul Ostler
 **/
final class IBTimestamp
{
  // Seen by friendly PreparedStatement.send_Data() method.
  int encodedYearMonthDay_ = 0;
  int encodedHourMinuteSecond_ = 0;

  private int year_;  // year-1900
  private int month_; // 0-11
  private int date_;   // 1-31

  private int hour_;
  private int minute_;
  private int second_;

  // See defines in gds.c for PRECISION and TIMEZONE
  static final private int PRECISION__ = 10000;
  static final private int TIME_ZONE__ = 0;

  // Should ib timestamp be considered a date or a time.
  static final int DATE = 0;
  static final int TIME = 1;
  static final int DATETIME = 2;

  //Torsten-start 08-11-2000
  class FooCalendar extends java.util.GregorianCalendar {
    public long getTheTimeInMillis() {
      return super.getTimeInMillis();
    }
  }
  private FooCalendar cal = new FooCalendar();

  public long getTimeInMillis() {
    cal.set(java.util.Calendar.YEAR, year_ + 1900);
    cal.set(java.util.Calendar.MONTH, month_);
    cal.set(java.util.Calendar.DATE, date_);
    cal.set(java.util.Calendar.HOUR_OF_DAY, hour_);
    cal.set(java.util.Calendar.MINUTE, minute_);
    cal.set(java.util.Calendar.SECOND, second_);
    cal.set(java.util.Calendar.MILLISECOND, 0);
    return cal.getTheTimeInMillis();
  }

  IBTimestamp(java.util.Date aDate) {
    cal.setTime(aDate);
    year_ = cal.get(java.util.Calendar.YEAR) - 1900;
    month_ = cal.get(java.util.Calendar.MONTH);
    date_ = cal.get(java.util.Calendar.DATE);
    hour_ = cal.get(java.util.Calendar.HOUR_OF_DAY);
    minute_ = cal.get(java.util.Calendar.MINUTE);
    second_ = cal.get(java.util.Calendar.SECOND);

    encodeYearMonthDay ();
    encodeHourMinuteSecond ();
  }
  //Torsten-end 08-11-2000

  // Construct an interbase date from encoded data recv'd from server
  IBTimestamp (int datetimeType, int[] timestampId) throws BugCheckException
  {
    setTimestampId (datetimeType, timestampId);
  }

// CJL-IB6 added to support new types
// This constructor works with a partial timestamp (date or time);
  IBTimestamp (int datetimeType, int dateTimeInt) throws BugCheckException
  {
    setDateTime (datetimeType, dateTimeInt);
  }

  void setDateTime (int datetimeType, int dateTimeInt) throws BugCheckException
  {
    switch (datetimeType) {
    case DATE:
      encodedYearMonthDay_ = dateTimeInt;
      decodeYearMonthDay ();
      break;
    case TIME:
      encodedHourMinuteSecond_ = dateTimeInt;
      decodeHourMinuteSecond ();
      break;
    default:
       throw new BugCheckException (ErrorKey.bugCheck__0__,
				    116);
    }
  }

// CJL-IB6 end change

  void setTimestampId (int datetimeType, int[] timestampId) throws BugCheckException
  {
    switch (datetimeType) {
    case DATETIME:
      encodedYearMonthDay_ = timestampId[0];
      encodedHourMinuteSecond_ = timestampId[1];
      decodeYearMonthDay ();
      decodeHourMinuteSecond ();
      break;
    case DATE:
      encodedYearMonthDay_ = timestampId[0];
      decodeYearMonthDay ();
      break;
    case TIME:
      encodedHourMinuteSecond_ = timestampId[1];
      decodeHourMinuteSecond ();
      break;
    default:
       throw new BugCheckException (ErrorKey.bugCheck__0__,
				    116);
    }
  }

  // Construct an interbase timestamp to send to server
  // year year-1900
  // month 0 to 11 
  // date 1 to 31
  IBTimestamp (int year, 
	              int month,
	              int date)
  {
    year_ = year;
    month_ = month;
    date_ = date;

    encodeYearMonthDay ();
  }

  // Construct an interbase timestamp to send to server
  // !! is this used? remove if not.
  IBTimestamp (int year, 
		      int month, 
		      int date, 
		      int hour, 
		      int minute)
  {
    year_ = year;
    month_ = month;
    date_ = date;
    hour_ = hour;
    minute_ = minute;
    second_ = 0;
    
    encodeYearMonthDay ();
    encodeHourMinuteSecond ();
  }


  // Construct an interbase timestamp to send to server
  IBTimestamp (int year, 
		      int month, 
		      int date, 
		      int hour, 
		      int minute,
		      int second)
  {
    year_ = year;
    month_ = month;
    date_ = date;
    hour_ = hour;
    minute_ = minute;
    second_ = second;

    encodeYearMonthDay ();
    encodeHourMinuteSecond ();
  }

  int getYear ()
  {
    return year_;
  }

  int getMonth ()
  {
    return month_;
  }

  int getDate ()
  {
    return date_;
  }

  int getHours ()
  {
    return hour_;
  }

  int getMinutes ()
  {
    return minute_;
  }

  int getSeconds ()
  {
    return second_;
  }

  int getNanos ()
  {
    return 0;
  }

  // ****** Encode routines *******

  // See gds__encode_date() and nday() in gds.c
  private void encodeYearMonthDay ()
  {
    int c, ya;
    int tempYear = year_+1900; // scale to range 1900 + year
    int tempMonth = month_+1;  // scale to range 1-12

    if (tempMonth > 2)
      tempMonth -= 3;
    else {
      tempMonth += 9;
      tempYear -= 1;
    }

    c = tempYear / 100;
    ya = tempYear - 100 * c;

    encodedYearMonthDay_ = 
      (146097 * c) / 4 + 
      (1461 * ya) / 4 + 
      (153 * tempMonth + 2) / 5 + 
      date_ + 1721119 - 2400001;
  }

  // see gds__encode_date() in gds.c
  private void encodeHourMinuteSecond ()
  {
    int minutesInDay = hour_ * 60 + minute_ - TIME_ZONE__;

    encodedHourMinuteSecond_ = minutesInDay * 60 * PRECISION__ + second_*PRECISION__;
  }

  // ****** Decode routines *******

  // See gds__decode_date() and nday() in gds.c
  private void decodeYearMonthDay () // 1-31
  {
    int nday = encodedYearMonthDay_;

    nday -= 1721119 - 2400001;
    year_ = (4 * nday - 1) / 146097;
    nday = 4 * nday - 1 - 146097 * year_;
    date_ = nday / 4;

    nday = (4 * date_ + 3) / 1461;
    date_ = 4 * date_ + 3 - 1461 * nday;
    date_ = (date_ + 4) / 4;

    month_ = (5 * date_ - 3) / 153;
    date_ = 5 * date_ - 3 - 153 * month_;
    date_ = (date_ + 5) / 5;

    year_ = 100 * year_ + nday;

    if (month_ < 10)
      month_ += 3;
    else {
      month_ -= 9;
      year_ += 1;
    }

    year_ -= 1900; // scale to make year_-1900
    month_--; // scale from 1-12 range to 0-11 range
  }

  // See gds__decode_date() in gds.c
  private void decodeHourMinuteSecond () 
  {
    int minutesInDay = encodedHourMinuteSecond_ / (PRECISION__ * 60) + TIME_ZONE__;
    hour_ = minutesInDay / 60;
    minute_ = minutesInDay % 60;
    second_ = (encodedHourMinuteSecond_ / PRECISION__) % 60;
  }

};


