package com.inprise.sql;

public interface SQLAdapter
{
  public final static int RIGHT_TRIM_STRINGS = 1;
  public final static int SINGLE_INSTANCE_TIME = 2;
  public final int  RESETABLE_STREAM      = 3;
  public boolean adapt(int modifier, Object extraInfo) throws java.sql.SQLException;
  public void revert(int modifier) throws java.sql.SQLException;
}



