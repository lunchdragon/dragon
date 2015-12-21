package dragon.utils;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class StringHelper {
	
	public static final String COMMA = ",";
	public static final String SEMI_COLON = ";";
	public static final String COLON = ":";
	
	public static final String STRING_QUOTE ="\"";
	
    private StringHelper() {}

    /**
     * compress continuous multiple spaces into one in given string
     * @param s
     * @return
     */
    public static String compressSpaces(String s) {
        return s.replaceAll("\\s+", " ");
    }

    /**
     * trim the sub-string between given character.
     * @param src
     * @param escapeChar
     * @return
     */
    public static String trimBetween(String src, char escapeChar) {
        char[] newSeq = new char[src.length()];
        boolean start = false;
        int index = 0;
        for (char ch : src.toCharArray()) {
            if (ch == escapeChar) {
                if (! start) start = true;
                else start = false;
            } else {
                if (!start) newSeq[index ++] = ch;
            }
        }
        
        return new String(newSeq, 0, index);
    }
    
    public static int occurrs(String str, char ch) {
        char[] chArray = str.toCharArray();
        int count = 0;
        for (int i = 0; i < chArray.length; i ++) {
            if (chArray[i] == ch) count ++;
        }
        
        return count;
    }

    public static String getSearchPattern(String searchString) {
        return searchString == null ? "%" : '%' + searchString.toLowerCase().replace('*', '%') + '%';
    }
    
    public static boolean isIP(String str) {
        boolean ret = false;
        if (str != null) {
            String[] uu = StringUtils.split(str, '.');
            if (uu.length == 4) {
                ret = true;
                for (int i = 0; i < uu.length; i ++) {
                    try {
                        int num = Integer.parseInt(uu[i]);
                        if (num < 0 || num > 255) {
                            ret = false;
                            break;
                        }
                    } catch (NumberFormatException ex) {
                        ret = false;
                        break;
                    }
                }
            }
        }
        return ret;
    }
    
    public static boolean isMAC(String str) {
        boolean ret = false;
        if (str != null && str.length() == 17) {
            String[] uu = StringUtils.split(str, ':');
            if (uu.length == 6) {
                ret = true;
                for (int i = 0; i < uu.length; i ++) {
                    if (uu[i].length() != 2) {
                        ret = false;
                        break;
                    }
                    if (!isHexChar(uu[i].charAt(0)) || !isHexChar(uu[i].charAt(1))) {
                        ret = false;
                        break;
                    } 
                }
            }
        }
        return ret;
    }
    
    public static boolean isHexChar(char ch) {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }

    public static String trunc(String s, int limit) {
        if (s == null) return null;
        s = s.trim();
        if (s.length() > limit) return s.substring(0, limit);
        else return s;
    }

    public static List<String> toList(String str, char delim) {
        String[] tokens = StringUtils.split(str, delim);
        List<String> list = new ArrayList<String>(tokens.length);
        for (String s : tokens) {
            s = s.trim();
            if (s.length() > 0) {
                list.add(s);
            }
        }

        return list;
    }

    public static List<Long> toLongIdList(String str, char delim) {
        String[] tokens = StringUtils.split(str, delim);
        List<Long> list = new ArrayList<Long>(tokens.length);
        for (String s : tokens) {
            s = s.trim();
            if (s.length() > 0) {
                list.add(Long.parseLong(s));
            }
        }

        return list;
    }

    public static String setLongToCommaSeperatedString(Set<Long> longs) {
        if (longs == null || longs.size() == 0) {
            return "";
        }
        StringBuffer idStrBuf = new StringBuffer();
        Iterator<Long> it = longs.iterator();
        while (it.hasNext()) {
            Long l = it.next();
            if (idStrBuf.length() > 0) {
                idStrBuf.append(',');
            }
            idStrBuf.append(l);
        }

        return idStrBuf.toString();
    }

    public static String encodePathname(String pathname) {

        if (pathname != null && pathname.length() > 0) {
            StringBuilder sb = new StringBuilder();
            char[] name = pathname.toCharArray();
            for (int i = 0; i < name.length; i++) {
               Character c = new Character(name[i]);
               if (Character.isLetterOrDigit(c) || c == '.' || c == '-' || c == '_') {
                   sb.append(c);
               } else {
                   sb.append("%");
                   sb.append(Integer.toHexString(0XFF & name[i]));
               }
            }
            return sb.toString();
        }

        return pathname;
    }
    
    public static String[] splitByWholeSeparatorPreserveAllTokens(String str, String separator) {
    	if (str == null) {
    		return null;
    	}
    	
    	List<String> tokens = new ArrayList<String>();
    	
    	int index, lastIndex = 0;
    	while ((index = str.indexOf(separator, lastIndex)) >= 0) {
    		tokens.add(str.substring(lastIndex, index));
    		lastIndex = index + separator.length();
    	}
    	if (lastIndex < str.length()) {
    		tokens.add(str.substring(lastIndex));
    	}
    	
    	return tokens.toArray(new String[tokens.size()]);
    }

    public static String unCapitalize(String str){
        if(StringUtils.isEmpty(str))return str;
        char first = Character.toLowerCase(str.charAt(0));
        str = first + str.substring(1);

        return str;
    }
    
    public static Boolean isBlank(String str){
        return StringUtils.isBlank(str);
    }
    
    /**
     * Check string is double or float
     * 
     * @param str
     * @return
     */
    public static Boolean isDouble(String str){
        final String Digits     = "(\\p{Digit}+)";
        final String HexDigits  = "(\\p{XDigit}+)";
        // an exponent is 'e' or 'E' followed by an optionally
        // signed decimal integer.
        final String Exp        = "[eE][+-]?"+Digits;
        final String fpRegex    =
            ("[\\x00-\\x20]*"+  // Optional leading "whitespace"
             "[+-]?(" + // Optional sign character
             "NaN|" +           // "NaN" string
             "Infinity|" +      // "Infinity" string

             // A decimal floating-point string representing a finite positive
             // number without a leading sign has at most five basic pieces:
             // Digits . Digits ExponentPart FloatTypeSuffix
             //
             // Since this method allows integer-only strings as input
             // in addition to strings of floating-point literals, the
             // two sub-patterns below are simplifications of the grammar
             // productions from section 3.10.2 of
             // The Java Language Specification.

             // Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
             "((("+Digits+"(\\.)?("+Digits+"?)("+Exp+")?)|"+

             // . Digits ExponentPart_opt FloatTypeSuffix_opt
             "(\\.("+Digits+")("+Exp+")?)|"+

             // Hexadecimal strings
             "((" +
              // 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
              "(0[xX]" + HexDigits + "(\\.)?)|" +

              // 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
              "(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

              ")[pP][+-]?" + Digits + "))" +
             "[fFdD]?))" +
             "[\\x00-\\x20]*");// Optional trailing "whitespace"
        
        return Pattern.matches(fpRegex, str);
    }
    
}
