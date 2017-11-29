package org.andrey.ukr.foreign.passport.recognizer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by andrey on 29.11.2017.
 */
public class MRZTextParser {
    public static final String MALE_SEX = "M";
    public static final String MALE_SEX_LABEL = "Male";
    public static final String FEMALE_SEX_LABEL = "Female";

    public static final String PASSPORT_TYPE = "P";
    public static final String PASSPORT_TYPE_LABEL = "Passport";
    public static final String VISA_TYPE = "V";
    public static final String VISA_TYPE_LABEL = "Visa";
    public static final String UNKNOWN_LABEL = "Unknown";

    public static final int SEX_POSITION = 21;
    public static final int DOC_NUMBER_START_POSITION = 0;
    public static final int DOC_NUMBER_LENGTH = 9;
    public static final int NATIONALITY_START_POSITION = 10;
    public static final int NATIONALITY_LENGTH = 3;
    public static final int COUNTRY_CODE_START_POSITION = 2;
    public static final int COUNTRY_CODE_LENGTH = 3;
    public static final int BIRTHDAY_DATE_START_POSITION = 13;
    public static final int EXPIRY_DATE_START_POSITION = 21;
    public static final int DATE_LENGTH = 6;
    public static final int RECORD_NUM_START_POSITION = 28;
    public static final int RECORD_NUM_LENGTH = 10;
    public static final int DOCUMENT_TYPE_POSITION = 0;
    public static final int DOCUMENT_TYPE_LENGTH = 1;
    public static final int NAME_START_POSITION = 5;

    public static final int LINE_LENGTH = 44;
    public static final String DATE_FORMAT = "yyMMdd";

    private String parseSex(String line) {
        return MALE_SEX.equals(line.substring(SEX_POSITION, SEX_POSITION + 1)) ? MALE_SEX_LABEL : FEMALE_SEX_LABEL;
    }

    private String parseDocumentNumber(String line) {
        return line.substring(DOC_NUMBER_START_POSITION, DOC_NUMBER_START_POSITION + DOC_NUMBER_LENGTH);
    }

    private String parseNationality(String line) {
        return line.substring(NATIONALITY_START_POSITION, NATIONALITY_START_POSITION + NATIONALITY_LENGTH);
    }

    private String parseCountryCode(String line) {
        return line.substring(COUNTRY_CODE_START_POSITION, COUNTRY_CODE_START_POSITION + COUNTRY_CODE_LENGTH);
    }

    private String parseRecordNumber(String line) {
        return line.substring(RECORD_NUM_START_POSITION, RECORD_NUM_START_POSITION + RECORD_NUM_LENGTH);
    }

    private Date parseDateOfBirth(String line) throws ParseException {
        return parseDate(line, BIRTHDAY_DATE_START_POSITION, DATE_LENGTH);
    }

    private Date parseDateOfExpiry(String line) throws ParseException {
        return parseDate(line, EXPIRY_DATE_START_POSITION, DATE_LENGTH);
    }

    private String parseDocumentType(String line) {
        String type = line.substring(DOCUMENT_TYPE_POSITION, DOCUMENT_TYPE_LENGTH);

        if (PASSPORT_TYPE.equals(type)) {
            return PASSPORT_TYPE_LABEL;
        } else if (VISA_TYPE.equals(type)) {
            return VISA_TYPE_LABEL;
        }

        return UNKNOWN_LABEL;
    }

    private String parseSecondName(String line) {
        return parseName(line, NAME_START_POSITION);
    }

    private String parseFirstName(String line, String firstName) {
        return parseName(line, NAME_START_POSITION + firstName.length() + 2);
    }

    private String parseName(String line, int startPos) {
        StringBuilder sb = new StringBuilder();

        for (int i = startPos; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (Character.isAlphabetic(ch)) {
                sb.append(ch);
            } else {
                break;
            }
        }

        return sb.toString();
    }

    private Date parseDate(String line, int start, int length) throws ParseException {
        String dateStr = line.substring(start, start + length);

        if (dateStr.matches("\\D")) {
            return null;
        }

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.parse(dateStr);
    }

    private String cleanLineAndVerify(String line) {
        if (line.length() < LINE_LENGTH) {
            throw new IllegalArgumentException("Line is too short: \"" + line +"\"");
        }

        line = line.replaceAll("([^\\w\\d<])+", "");

        if (line.length() != LINE_LENGTH) {
            throw new IllegalArgumentException("Line has no valid length: \"" + line +"\"");
        }

        return line;
    }

    private String cleanFromSeparator(String line) {
        return line.replaceAll("<", "");
    }

    private String normalizeName(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    public MRZData parse(String scan) throws ParseException {
        String[] lines = scan.split("\n");

        if (lines.length < 2) {
            throw new IllegalArgumentException("Expect at least two lines");
        }

        String firstLine = cleanLineAndVerify(lines[lines.length - 2]);
        String secondLine = cleanLineAndVerify(lines[lines.length - 1]);

        MRZData data = new MRZData();
        data.setDocumentType(parseDocumentType(firstLine));
        data.setCountryCode(parseCountryCode(firstLine));
        data.setSecondName(normalizeName(parseSecondName(firstLine)));
        data.setFirstName(normalizeName(parseFirstName(firstLine, data.getSecondName())));
        data.setDocumentNumber(cleanFromSeparator(parseDocumentNumber(secondLine)));
        data.setNationality(parseNationality(secondLine));
        data.setRecordNum(parseRecordNumber(secondLine));
        data.setDateOfBirth(parseDateOfBirth(secondLine));
        data.setDateOfExpiry(parseDateOfExpiry(secondLine));
        data.setSex(parseSex(secondLine));
        return data;
    }
}
