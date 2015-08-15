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
/** * An enumeration of IANA character encodings supported by InterClient.
 * <p>
 * A complete list of IANA encodings may be found at
 * <a href="ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets">
 * ftp://ftp.isi.edu/in-notes/iana/assignments/character-sets</a>.
 * The InterBase name for the IANA character encoding may
 * be obtained by calling
 * {@link #getInterBaseCharacterSetName getInterBaseCharacterSetName()}.
 * <p>
 * Any of the following character encodings may be specified
 * on connection using the <code>charSet</code> connection property.
 * It is recommended to use one of the static final encoding
 * variables below
 * rather than using hardwired encoding names in your application.
 * The actual encoding names represented by these variables could
 * change from one release to the next.
 * <p>
 * See <a href="../../../help/icConnectionProperties.html">DataSource Properties</a>
 * for a description of the <code>charSet</code> connection property.
 *
 * @since <font color=red>Extension, since InterClient 1.50</font> * @author Paul Ostler **/
public final class CharacterEncodings
{
  // This is a static class, so define a private
  // constructor so that the default constructor  // is not automatically exported.  CharacterEncodings () {}
  // ********************************
  // *** InterBase Character Sets ***
  // ********************************

  // NONE is the default and is used by result set to assign metadata charset.  static final int NONE__ = 0;

  private static final int OCTETS__ = 1;
  private static final int ASCII__ = 2;
  private static final int UNICODE_FSS__ = 3;
  private static final int ISO8859_1__ = 21; // aka LATIN-1
  private static final int BIG_5__ = 56;
  private static final int CYRL__ = 50;
  private static final int DOS437__ = 10;
  private static final int DOS850__ = 11;
  private static final int DOS852__ = 45;
  private static final int DOS857__ = 46;
  private static final int DOS860__ = 13;
  private static final int DOS861__ = 47;
  private static final int DOS863__ = 14;
  private static final int DOS865__ = 12;
  private static final int EUCJ_0208__ = 6;
  private static final int GB_2312__ = 57;
  private static final int KSC_5601__ = 44;
  private static final int NEXT__ = 19;
  private static final int SJIS_0208__ = 5;
  private static final int WIN1250__ = 51;
  private static final int WIN1251__ = 52;
  private static final int WIN1252__ = 53;
  private static final int WIN1253__ = 54;
  private static final int WIN1254__ = 55;
  /**   * The default encoding is no encoding at all.
   * Represents the value "NONE".
   * <p>   * If an encoding is not specified using the   * <code>charSet</code> connection property, an attachment   * will use the InterBase default <code>NONE</code> character set.   * The <code>NONE</code> character set attachment specifies   * that data is processed <i>as is</i> with no conversions.   * The default encoding for your locale is not used.   * Because conversions are not performed and no particular   * encoding is enforced, all SQL and SQL data is   * interpreted byte-wise as the low order byte of a Java Unicode   * 2-byte character.   * Therefore, all SQL and character data should be restricted to   * ASCII when connecting to an InterBase   * server using the default <code>NONE</code> character set.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String NONE = "NONE";
  /**   * ISO Latin-1.   * Represents the IANA name and preferred MIME name <code>ISO-8859-1</code>.   * The InterBase character set name is <code>ISO8859_1</code>.   * The Java encoding name is <code>8859_1</code>.   * <p>   * An 8-bit encoding that supports many latin languages,   * including Afrikaans, Albanian, Basque, Catalan,   * Danish, Dutch, English, Faroese, Finnish, French,   * Galician, German, Icelandic, Irish, Italian,   * Norwegian, Portuguese, Scottish, Spanish, and Swedish.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String _8859_1 = "8859_1";

  /**   * Big 5 Chinese for Taiwan Multi-byte set.   * Represents the IANA name and preferred MIME name <code>Big5</code>.   * The InterBase character set name is <code>BIG_5</code>.   * The Java encoding name is <code>Big5</code>.   * Also known as <code>csBig5</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Big5 = "Big5";  public final static String MS950 = "MS950";//!kna
  /**   * Windows Eastern Europe/Latin-2.   * Represents the IANA name <code>windows-1250</code>.   * The InterBase character set name is <code>WIN1250</code>.   * The Java encoding name is <code>Cp1250</code>;   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp1250 = "Cp1250";
  /**   * Windows Cyrillic.   * Represents the IANA name <code>windows-1251</code>.   * The InterBase character set name is <code>WIN1251</code>.   * The Java encoding name is <code>Cp1251</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp1251 = "Cp1251";
  /**   * Windows Western Europe/Latin-1.   * Represents the IANA name <code>windows-1252</code>.   * The InterBase character set name is <code>WIN1252</code>.   * The Java encoding name is <code>Cp1252</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp1252 = "Cp1252";  /**   * Windows Greek.   * Represents the IANA name <code>windows-1253</code>.   * The InterBase character set name is <code>WIN1253</code>.   * The Java encoding name is <code>Cp1253</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp1253 = "Cp1253";  /**   * Windows Turkish.   * Represents the IANA name <code>windows-1254</code>.   * The InterBase character set name is <code>WIN1254</code>.   * The Java encoding name is <code>Cp1254</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp1254 = "Cp1254";
  /**   * PC Original.   * Represents the IANA name <code>IBM437</code>.   * The InterBase character set name is <code>DOS437</code>.   * The Java encoding name is <code>Cp437</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp437 = "Cp437";  /**   * PC Latin-1.   * Represents the IANA name <code>IBM850</code>.   * The InterBase character set name is <code>DOS850</code>.   * The Java encoding name is <code>Cp850</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp850 = "Cp850";
  /**   * PC Latin-2.   * Represents the IANA name <code>IBM852</code>.   * The InterBase character set name is <code>DOS852</code>.   * The Java encoding name is <code>Cp852</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp852 = "Cp852";
  /**
   * PC Turkish.   * Represents the IANA name <code>IBM857</code>.   * The InterBase character set name is <code>DOS857</code>.   * The Java encoding name is <code>Cp857</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp857 = "Cp857";  /**   * PC Portuguese.   * Represents the IANA name <code>IBM860</code>.   * The InterBase character set name is <code>DOS860</code>.   * The Java encoding name is <code>Cp860</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp860 = "Cp860";
  /**   * PC Icelandic.   * Represents the IANA name <code>IBM861</code>.   * The InterBase character set name is <code>DOS861</code>.   * The Java encoding name is <code>Cp861</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp861 = "Cp861";

  /**   * PC Canadian French.   * Represents the IANA name <code>IBM863</code>.   * The InterBase character set name is <code>DOS863</code>.   * The Java encoding name is <code>Cp863</code>   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp863 = "Cp863";

  /**   * PC Nordic.   * Represents the IANA name <code>IBM865</code>.   * The InterBase character set name is <code>DOS865</code>.   * The Java encoding name is <code>Cp865</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String Cp865 = "Cp865";
  /**   * Japanese Extended Unix Code (EUC).   * Represents the IANA name <code>Extended_UNIX_Code_Packed_Format_for_Japanese</code>,   * and preferred MIME name <code>EUC-JP</code>.   * The InterBase character set name is <code>EUCJ_0208</code>.   * The Java encoding name is <code>EUCJIS</code>.   * <p>   * A double 7-bit byte set, restricted to A0-FF in both bytes   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String EUCJIS = "EUCJIS";  /**   * Simplified Chinese for People's Republic of China.   * Represents the IANA name and preferred MIME name <code>GB2312</code>.
   * The InterBase character set name is <code>GB_2312</code>.   * The Java encoding name is <code>GB2312</code>.   * <p>   * A mixed one byte, two byte set:   * <pre>   * 20-7E = one byte ASCII   * A1-FE = two byte PRC Kanji   * </pre>   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String GB2312 = "GB2312";  public final static String GBK = "GBK";//!kna
  /**   * Korean.
   * Represents the IANA name <code>KS_C_5601-1987</code>.
   * The InterBase character set name is also <code>KSC_5601</code>.   * The Java encoding name is <code>KSC5601</code>.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String KSC5601 = "KSC5601";
  public final static String MS949 = "MS949";//!kna

  /**
   * PC and Windows Japanese.   * Represents the IANA name and preferred MIME name <code>Shift_JIS</code>.   * The InterBase character set name is <code>SJIS_0208</code>.   * The Java encoding name is <code>SJIS</code>.   * Also known as <code>MS_Kanji</code>.   * <p>   * A Microsoft code that extends <code>csHalfWidthKatakana</code> to include   * kanji by adding a second byte when the value of the first   * byte is in the ranges 81-9F or E0-EF.   *  * @since <font color=red>Extension, since InterClient 1.50</font>
  **/  public final static String SJIS = "SJIS";
  public final static String MS932 = "MS932";//!kna

  /**   * Standard UTF-8.   * Represents the IANA name <code>UTF-8</code>.   * The InterBase character set name is <code>UNICODE_FSS</code>.   * The Java encoding name is <code>UTF8</code>.   * <p>   * UTF-8 is a 1 to 3 byte universal encoding of 2-byte   * Unicode in which ASCII has a one byte representation.   * That is, unlike 2-byte Unicode, ASCII is a subset of   * UTF-8.  This makes UTF-8 a File-Safe encoding,   * and is therefore known as File System Safe Unicode,   * or Unicode FSS.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public final static String UTF8 = "UTF8";
  private final static String[] supportedEncodings__ =    new String[] {      NONE,      _8859_1,      Big5,      Cp1250,      Cp1251,      Cp1252,      Cp1253,      Cp1254,      Cp437,      Cp850,      Cp852,      Cp857,      Cp860,      Cp861,      Cp863,      Cp865,      EUCJIS,      GB2312,      GBK,//!kna      KSC5601,      MS932,//!kna      MS949,//!kna      MS950,//!kna      SJIS,      UTF8    };  private static java.util.Hashtable ianaToIBCharNameTable__ = new java.util.Hashtable ();
  static {    ianaToIBCharNameTable__.put (NONE,    "NONE");    ianaToIBCharNameTable__.put (_8859_1,  "ISO8859_1");    ianaToIBCharNameTable__.put (Big5,    "BIG_5");    ianaToIBCharNameTable__.put (MS950,    "BIG_5");//!kna    ianaToIBCharNameTable__.put (Cp1250,  "WIN1250");    ianaToIBCharNameTable__.put (Cp1251,  "WIN1251");    ianaToIBCharNameTable__.put (Cp1252,  "WIN1252");    ianaToIBCharNameTable__.put (Cp1253,  "WIN1253");    ianaToIBCharNameTable__.put (Cp1254,  "WIN1254");    ianaToIBCharNameTable__.put (Cp437,   "DOS437");    ianaToIBCharNameTable__.put (Cp850,   "DOS850");    ianaToIBCharNameTable__.put (Cp852,   "DOS852");    ianaToIBCharNameTable__.put (Cp857,   "DOS857");    ianaToIBCharNameTable__.put (Cp860,   "DOS860");    ianaToIBCharNameTable__.put (Cp861,   "DOS861");    ianaToIBCharNameTable__.put (Cp863,   "DOS863");    ianaToIBCharNameTable__.put (Cp865,   "DOS865");    ianaToIBCharNameTable__.put (EUCJIS,  "EUCJ_0208");    ianaToIBCharNameTable__.put (GB2312,  "GB_2312");    ianaToIBCharNameTable__.put (GBK,  "GB_2312");//!kna    ianaToIBCharNameTable__.put (KSC5601, "KSC_5601");    ianaToIBCharNameTable__.put (MS949, "KSC_5601");//!kna    ianaToIBCharNameTable__.put (SJIS,    "SJIS_0208");    ianaToIBCharNameTable__.put (MS932,    "SJIS_0208");//!kna    ianaToIBCharNameTable__.put (UTF8,    "UNICODE_FSS");  };
  /**   * Return the InterBase character set name for a supported Java encoding name.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   * @throws java.sql.SQLException if the IANA encoding is not supported.   **/  public static String getInterBaseCharacterSetName (String encoding) throws java.sql.SQLException  {    String ibCharName = (String) ianaToIBCharNameTable__.get (encoding);    if (ibCharName == null)      throw new UnsupportedCharacterSetException (ErrorKey.unsupportedCharacterSet__0__, encoding);    else      return ibCharName;  }

  /**   * Return an array of all Java encodings supported by the driver.   * Driver support for an encoding does not necessarily imply database support   * for the encoding.  Database support for particular encodings will depend   * on the version of InterBase being used.   *   * @since <font color=red>Extension, since InterClient 1.50</font>   **/  public static String[] getSupportedEncodings ()  {    return supportedEncodings__;  }}
